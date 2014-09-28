package jp.webpay.android;

import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.StoredCard;
import jp.webpay.android.model.Token;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    @Before
    public void prepareWebPay() {
        webpay = new WebPay("test_public_dummykey");
    }

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
}
