package jp.webpay.android;

import android.net.Uri;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Communicate with WebPay server using Apache HTTP client
  */
class WebPayPublicClient {
    private final String scheme;
    private final String authority;
    private final String apiKey;

    WebPayPublicClient(String scheme, String authority, String apiKey) {
        this.scheme = scheme;
        this.authority = authority;
        this.apiKey = apiKey;
    }

    /**
     * Send request to WebPay host
     * The caller should take care of exceptions
     * @param method        "GET" or "POST"
     * @param path          request path starts from /v*
     * @param queryParams   query parameters
     * @param jsonBody      json-format body string used only in "POST".
     * @return              pair of response code and body if request completed
     * @throws IOException
     */
    Result request(String method, String path, Map<String, String> queryParams, String jsonBody) throws IOException {
        Uri.Builder builder = new Uri.Builder()
                .scheme(scheme)
                .encodedAuthority(authority)
                .path(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        HttpRequestBase request;
        if (method.equals("GET")) {
            request = new HttpGet(builder.build().toString());
        } else if (method.equals("POST")) {
            HttpPost postRequest = new HttpPost(builder.build().toString());
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setEntity(new StringEntity(jsonBody, "UTF-8"));
            request = postRequest;
        } else {
            throw new IllegalArgumentException("method must be GET or POST");
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            return httpClient.execute(request, new ResponseHandler<Result>() {
                @Override
                public Result handleResponse(HttpResponse response) throws IOException {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                    return new Result(statusCode, body);
                }
            });
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    static class Result {
        final int statusCode;
        final String responseBody;

        Result(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }
}
