package jp.webpay.android;

import android.net.Uri;
import android.os.AsyncTask;

import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.Token;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class WebPay {

    private static final Uri BASE_URI = Uri.parse("https://api.webpay.jp/v1");
    private final WebPayPublicClient client;
    private WebPayListener listener;

    public WebPay(String publishableKey, WebPayListener listener) {
        this(publishableKey);
        this.setListener(listener);
    }

    public WebPay(String publishableKey) {
        client = new WebPayPublicClient(BASE_URI, publishableKey);
    }

    public void setListener(WebPayListener listener) {
        this.listener = listener;
    }

    public void setLanguage(String language) {
        client.setLanguage(language);
    }

    public void createToken(RawCard rawCard) {
        createToken(rawCard, this.listener);
    }

    public void createToken(RawCard rawCard, WebPayListener listener) {
        new CreateTokenTask(rawCard, listener).execute();
    }

    private class CreateTokenTask extends AsyncTask<Void, Void, TaskResult<Token>> {
        private final RawCard rawCard;
        private final WebPayListener listener;

        private CreateTokenTask(RawCard rawCard, WebPayListener listener) {
            if (rawCard == null) {
                throw new IllegalArgumentException("card must not be nil");
            }
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be nil");
            }
            this.rawCard = rawCard;
            this.listener = listener;
        }

        @Override
        protected TaskResult<Token> doInBackground(Void... params) {
            try {
                JSONObject json = rawCard.toJson();
                WebPayPublicClient.Result result = null;
                try {
                    result = client.request("POST", "tokens", new HashMap<String, String>(), json.toString());
                } catch (IOException e) {
                    return new TaskResult<Token>(e);
                }
                if (result.statusCode >= 200 && result.statusCode < 300) {
                    try {
                        Token token = Token.fromJson(new JSONObject(result.responseBody));
                        return new TaskResult<Token>(token);
                    } catch (JSONException e) {
                        return new TaskResult<Token>(e);
                    }
                } else {
                    try {
                        ErrorResponse error = ErrorResponse.fromJson(result.statusCode, new JSONObject(result.responseBody));
                        return new TaskResult<Token>(error);
                    } catch (JSONException e) {
                        return new TaskResult<Token>(e);
                    }
                }
            } catch (RuntimeException e) {
                return new TaskResult<Token>(e);
            }
        }

        @Override
        protected void onCancelled() {
            listener.onErrorCreatingToken(new RuntimeException("Communication task is not expected to be cancelled"));
        }

        @Override
        protected void onPostExecute(TaskResult<Token> result) {
            if (result.model != null) {
                listener.onCreateToken(result.model);
            } else if (result.error != null) {
                listener.onErrorCreatingToken(new ErrorResponseException(result.error));
            } else if (result.cause != null) {
                listener.onErrorCreatingToken(result.cause);
            } else {
                throw new AssertionError("Incomplete result");
            }
        }
    }

    private class TaskResult<T> {
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
