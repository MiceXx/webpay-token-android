package jp.webpay.android;

import android.net.Uri;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Communicate with WebPay server using Apache HTTP client
  */
class WebPayPublicClient {
    private final Uri baseUri;
    private final String apiKey;
    private String language = "en";

    WebPayPublicClient(Uri baseUri, String apiKey) {
        this.baseUri = baseUri;
        this.apiKey = apiKey;
    }

    public void setLanguage(String language) {
        this.language = language;
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
        Uri.Builder builder = baseUri.buildUpon()
                .appendEncodedPath(path);
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
        request.setHeader("Accept-Language", language);
        request.setHeader("Authorization", "Bearer " + apiKey);

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

    public String getLanguage() {
        return language;
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
