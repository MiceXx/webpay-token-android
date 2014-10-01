package jp.webpay.android;

import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.StoredCard;
import jp.webpay.android.model.Token;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.tools.ant.filters.StringInputStream;
import org.codehaus.plexus.util.IOUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.org.apache.http.HttpResponseStub;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class WebPayTest {
    private WebPay webpay;

    private RawCard testCard = new RawCard()
            .number("4242-4242-4242-0123")
            .expMonth(8)
            .expYear(2020)
            .name("TEST USER")
            .cvc("012");

    private final HttpResponse tokenResponse =
            new TestHttpResponse(201, "{\n"+
                    "  \"id\": \"tok_3ybc93ckR01qeKx\",\n"+
                    "  \"object\": \"token\",\n"+
                    "  \"livemode\": false,\n"+
                    "  \"created\": 1396007350,\n"+
                    "  \"used\": false,\n"+
                    "  \"card\": {\n"+
                    "    \"object\": \"card\",\n"+
                    "    \"exp_year\": 2020,\n"+
                    "    \"exp_month\": 8,\n"+
                    "    \"fingerprint\": \"0000000000000000000000000000000000000000\",\n"+
                    "    \"name\": \"TEST USER\",\n"+
                    "    \"country\": \"JP\",\n"+
                    "    \"type\": \"Visa\",\n"+
                    "    \"cvc_check\": \"pass\",\n"+
                    "    \"last4\": \"0123\"\n"+
                    "  }\n"+
                    "}",
                    new BasicHeader("Content-Type", "application/json"));

    private final HttpResponse cardErrorResponse =
            new TestHttpResponse(402, "{\n" +
                    "  \"error\": {\n" +
                    "    \"message\": \"The security code provided is invalid. For Visa, MasterCard, JCB, and Diners Club, enter the last 3 digits on the back of your card. For American Express, enter the 4 digits printed above your number.\",\n" +
                    "    \"caused_by\": \"buyer\",\n" +
                    "    \"param\": \"cvc\",\n" +
                    "    \"type\": \"card_error\",\n" +
                    "    \"code\": \"invalid_cvc\"\n" +
                    "  }\n" +
                    "}",
                    new BasicHeader("Content-Type", "application/json"));

    private final HttpResponse serverErrorResponse =
            new TestHttpResponse(500, "{\n" +
                    "  \"error\": {\n" +
                    "    \"type\": \"api_error\",\n" +
                    "    \"caused_by\": \"service\",\n" +
                    "    \"message\": \"API server is currently unavailable\"\n" +
                    "  }\n" +
                    "}",
                    new BasicHeader("Content-Type", "application/json"));

    @Before
    public void prepareWebPay() {
        webpay = new WebPay("test_public_dummykey");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createTokenReturnsTokenObject() throws Exception {
        Robolectric.addPendingHttpResponse(tokenResponse);
        Token token = createToken(testCard);
        assertEquals("tok_3ybc93ckR01qeKx", token.id);
        assertEquals("token", token.object);
        assertEquals(false, token.livemode);
        assertEquals(1396007350L, (long)token.created);
        assertEquals(false, token.used);
        StoredCard card = token.card;
        assertEquals("card", card.object);
        assertEquals(2020, (int)card.expYear);
        assertEquals(8, (int)card.expMonth);
        assertEquals("0000000000000000000000000000000000000000", card.fingerprint);
        assertEquals("TEST USER", card.name);
        assertEquals("JP", card.country);
        assertEquals("Visa", card.type);
        assertEquals("pass", card.cvcCheck);
        assertEquals("0123", card.last4);
    }

    @Test
    public void createTokenSendCorrectRequest() throws Exception {
        Robolectric.addPendingHttpResponse(tokenResponse);
        String rawCardBodyString = "{\"card\":{" +
                "\"number\":\"4242-4242-4242-0123\"," +
                "\"cvc\":\"012\"," +
                "\"name\":\"TEST USER\"," +
                "\"exp_month\":8," +
                "\"exp_year\":2020" +
                "}}";
        createToken(testCard);
        HttpRequest request = Robolectric.getSentHttpRequest(0);
        assertEquals("POST", request.getRequestLine().getMethod());
        assertEquals("https://api.webpay.jp/v1/tokens", request.getRequestLine().getUri());
        assertEquals("application/json", request.getFirstHeader("Content-Type").getValue());
        assertEquals("Bearer test_public_dummykey", request.getFirstHeader("Authorization").getValue());
        String requestBody = IOUtil.toString(((HttpPost) request).getEntity().getContent(), "UTF-8");
        assertEquals(rawCardBodyString, requestBody);
    }

    @Test
    public void createTokenSendsRequestInSpecifiedLanguage() throws Exception {
        Robolectric.addPendingHttpResponse(tokenResponse);
        createToken(testCard);
        HttpRequest request = Robolectric.getSentHttpRequest(0);
        assertEquals("en", request.getFirstHeader("Accept-Language").getValue());

        Robolectric.addPendingHttpResponse(tokenResponse);
        this.webpay.setLanguage("ja");
        createToken(testCard);
        request = Robolectric.getSentHttpRequest(1);
        assertEquals("ja", request.getFirstHeader("Accept-Language").getValue());
    }

    @Test
    public void createTokenReturnsCardErrorResponse() throws Exception {
        Robolectric.addPendingHttpResponse(cardErrorResponse);
        Throwable throwable = createTokenThenError(testCard);
        assertThat(throwable, instanceOf(ErrorResponseException.class));
        ErrorResponse error = ((ErrorResponseException) throwable).getResponse();
        assertEquals(error.statusCode, 402);
        assertEquals(error.message, "The security code provided is invalid. For Visa, MasterCard, JCB, and Diners Club, enter the last 3 digits on the back of your card. For American Express, enter the 4 digits printed above your number.");
        assertEquals(error.causedBy, "buyer");
        assertEquals(error.param, "cvc");
        assertEquals(error.type, "card_error");
        assertEquals(error.code, "invalid_cvc");
    }

    @Test
    public void createTokenReturnsServerErrorResponse() throws Exception {
        Robolectric.addPendingHttpResponse(serverErrorResponse);
        Throwable throwable = createTokenThenError(testCard);
        assertThat(throwable, instanceOf(ErrorResponseException.class));
        ErrorResponse error = ((ErrorResponseException) throwable).getResponse();
        assertEquals(error.statusCode, 500);
        assertEquals(error.message, "API server is currently unavailable");
        assertEquals(error.causedBy, "service");
        assertEquals(error.param, null);
        assertEquals(error.type, "api_error");
        assertEquals(error.code, null);
    }

    @Test
    public void createTokenReturnsJSONException() throws Exception {
        TestHttpResponse response = new TestHttpResponse(201, "{:}",
                new BasicHeader("Content-Type", "application/json"));
        Robolectric.addPendingHttpResponse(response);
        Throwable throwable = createTokenThenError(testCard);
        assertThat(throwable, instanceOf(JSONException.class));
        assertEquals(throwable.getMessage(), "Expected literal value at character 1 of {:}");
    }

    @Test
    public void createTokenReturnsConnectionError() throws Exception {
        HttpResponseStub response = new TestHttpResponse(200, "") {
            @Override
            public HttpEntity getEntity() {
             return new InputStreamEntity(new StringInputStream("foo"), 3) {
                 @Override
                 public InputStream getContent() throws IOException {
                     throw new IOException("Test exception");
                 }
             };
            }
        };
        Robolectric.addPendingHttpResponse(response);
        Throwable throwable = createTokenThenError(testCard);
        assertThat(throwable, instanceOf(IOException.class));
        assertEquals(throwable.getMessage(), "Test exception");
    }

    @Test
    public void createTokenRaiseErrorForNullCard() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        createToken(null);
    }

    @Test
    public void createTokenRaiseErrorForNullListener() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        webpay.createToken(testCard, null);
    }

    protected Token createToken(RawCard card) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Token[] result = new Token[1];
        WebPayListener listener = new WebPayListener() {
            @Override
            public void onCreateToken(Token token) {
                result[0] = token;
                latch.countDown();
            }

            @Override
            public void onErrorCreatingToken(Throwable cause) {
                fail("Error is not acceptable " + cause.getMessage());
            }
        };
        webpay.createToken(card, listener);
        latch.await(1, TimeUnit.SECONDS);
        return result[0];
    }

    protected Throwable createTokenThenError(RawCard card) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] result = new Throwable[1];
        WebPayListener listener = new WebPayListener() {
            @Override
            public void onCreateToken(Token token) {
                fail("Token response is not acceptable");
            }

            @Override
            public void onErrorCreatingToken(Throwable cause) {
                result[0] = cause;
                latch.countDown();
            }
        };
        webpay.createToken(card, listener);
        latch.await(1, TimeUnit.SECONDS);
        return result[0];
    }
}
