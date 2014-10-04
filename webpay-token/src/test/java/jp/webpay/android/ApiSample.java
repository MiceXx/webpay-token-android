package jp.webpay.android;

import jp.webpay.android.model.RawCard;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

public class ApiSample {
    public static final RawCard testCard = new RawCard()
            .number("4242-4242-4242-0123")
            .expMonth(8)
            .expYear(2020)
            .name("TEST USER")
            .cvc("012");

    public static final HttpResponse tokenResponse =
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

    public static final HttpResponse availabilityResponse =
            new TestHttpResponse(200, "{\n" +
                    "  \"currencies_supported\": [\n" +
                    "    \"jpy\"\n" +
                    "  ],\n" +
                    "  \"card_types_supported\": [\n" +
                    "    \"Visa\",\n" +
                    "    \"MasterCard\",\n" +
                    "    \"JCB\",\n" +
                    "    \"American Express\",\n" +
                    "    \"Diners Club\"\n" +
                    "  ]\n" +
                    "}",
                    new BasicHeader("Content-Type", "application/json"));

    public static final HttpResponse cardErrorResponse =
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

    public static final HttpResponse serverErrorResponse =
            new TestHttpResponse(500, "{\n" +
                    "  \"error\": {\n" +
                    "    \"type\": \"api_error\",\n" +
                    "    \"caused_by\": \"service\",\n" +
                    "    \"message\": \"API server is currently unavailable\"\n" +
                    "  }\n" +
                    "}",
                    new BasicHeader("Content-Type", "application/json"));
}
