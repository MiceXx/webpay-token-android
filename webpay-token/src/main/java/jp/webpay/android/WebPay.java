package jp.webpay.android;

import android.net.Uri;
import android.os.AsyncTask;
import jp.webpay.android.model.AccountAvailability;
import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.Token;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WebPay {

    private static final Uri BASE_URI = Uri.parse("https://api.webpay.jp/v1");
    public static final String VERSION_NAME = "1.0";
    private final WebPayPublicClient client;

    public WebPay(String publishableKey) {
        client = new WebPayPublicClient(BASE_URI, publishableKey);
    }

    public void setLanguage(String language) {
        client.setLanguage(language);
    }

    public void createToken(final RawCard rawCard, WebPayListener<Token> listener) {
        if (rawCard == null) {
            throw new IllegalArgumentException("rawCard must not be null");
        }
        new RequestTask<Token>(listener) {
            @Override
            WebPayPublicClient.Result sendRequest() throws IOException {
                return client.request("POST", "tokens", rawCard.toJson().toString());
            }

            @Override
            Token parseResponse(JSONObject json) throws JSONException {
                return Token.fromJson(json);
            }
        }.execute();
    }

    public void retrieveAvailability(WebPayListener<AccountAvailability> listener) {
        new RequestTask<AccountAvailability>(listener) {
            @Override
            WebPayPublicClient.Result sendRequest() throws IOException {
                return client.request("GET", "account/availability", null);
            }

            @Override
            AccountAvailability parseResponse(JSONObject json) throws JSONException {
                return AccountAvailability.fromJson(json);
            }
        }.execute();
    }

    private abstract static class RequestTask<T> extends AsyncTask<Void, Void, TaskResult<T>> {
        private final WebPayListener<T> listener;

        private RequestTask(WebPayListener<T> listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            this.listener = listener;
        }

        abstract WebPayPublicClient.Result sendRequest() throws IOException;

        abstract T parseResponse(JSONObject json) throws JSONException;

        @Override
        protected TaskResult<T> doInBackground(Void... params) {
            try {
                WebPayPublicClient.Result result;
                try {
                    result = sendRequest();
                } catch (IOException e) {
                    return new TaskResult<T>(e);
                }
                if (result.statusCode >= 200 && result.statusCode < 300) {
                    try {
                        return new TaskResult<T>(parseResponse(new JSONObject(result.responseBody)));
                    } catch (JSONException e) {
                        return new TaskResult<T>(e);
                    }
                } else {
                    try {
                        ErrorResponse error = ErrorResponse.fromJson(result.statusCode, new JSONObject(result.responseBody));
                        return new TaskResult<T>(error);
                    } catch (JSONException e) {
                        return new TaskResult<T>(e);
                    }
                }
            } catch (RuntimeException e) {
                return new TaskResult<T>(e);
            }
        }

        @Override
        protected void onCancelled() {
            listener.onException(new RuntimeException("Communication task is not expected to be cancelled"));
        }

        @Override
        protected void onPostExecute(TaskResult<T> result) {
            if (result.model != null) {
                listener.onCreate(result.model);
            } else if (result.error != null) {
                listener.onException(new ErrorResponseException(result.error));
            } else if (result.cause != null) {
                listener.onException(result.cause);
            } else {
                throw new AssertionError("Incomplete result");
            }
        }
    }

    private static class TaskResult<T> {
        private final T model;
        private final ErrorResponse error;
        private final Throwable cause;

        private TaskResult(T model) {
            this.model = model;
            this.error = null;
            this.cause = null;
        }

        private TaskResult(ErrorResponse error) {
            this.model = null;
            this.error = error;
            this.cause = null;
        }

        private TaskResult(Throwable cause) {
            this.model = null;
            this.error = null;
            this.cause = cause;
        }
    }
}
