package jp.webpay.android;

import android.accounts.Account;
import android.net.Uri;
import android.os.AsyncTask;

import jp.webpay.android.model.AccountAvailability;
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

    public WebPay(String publishableKey) {
        client = new WebPayPublicClient(BASE_URI, publishableKey);
    }

    public void setLanguage(String language) {
        client.setLanguage(language);
    }

    public void createToken(RawCard rawCard, WebPayListener<Token> listener) {
        new CreateTokenTask(rawCard, listener).execute();
    }

    private class CreateTokenTask extends AsyncTask<Void, Void, TaskResult<Token>> {
        private final RawCard rawCard;
        private final WebPayListener<Token> listener;

        private CreateTokenTask(RawCard rawCard, WebPayListener<Token> listener) {
            if (rawCard == null) {
                throw new IllegalArgumentException("card must not be null");
            }
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
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
            listener.onException(new RuntimeException("Communication task is not expected to be cancelled"));
        }

        @Override
        protected void onPostExecute(TaskResult<Token> result) {
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

    public void retrieveAvailability(WebPayListener<AccountAvailability> listener) {
        new RetrieveAvailabilityTask(listener).execute();
    }

    private class RetrieveAvailabilityTask extends AsyncTask<Void, Void, TaskResult<AccountAvailability>> {
        private final WebPayListener<AccountAvailability> listener;

        private RetrieveAvailabilityTask(WebPayListener<AccountAvailability> listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            this.listener = listener;
        }

        @Override
        protected TaskResult<AccountAvailability> doInBackground(Void... params) {
            try {
                WebPayPublicClient.Result result = null;
                try {
                    result = client.request("GET", "account/availability", new HashMap<String, String>(), null);
                } catch (IOException e) {
                    return new TaskResult<AccountAvailability>(e);
                }
                if (result.statusCode >= 200 && result.statusCode < 300) {
                    try {
                        AccountAvailability availability = AccountAvailability.fromJson(new JSONObject(result.responseBody));
                        return new TaskResult<AccountAvailability>(availability);
                    } catch (JSONException e) {
                        return new TaskResult<AccountAvailability>(e);
                    }
                } else {
                    try {
                        ErrorResponse error = ErrorResponse.fromJson(result.statusCode, new JSONObject(result.responseBody));
                        return new TaskResult<AccountAvailability>(error);
                    } catch (JSONException e) {
                        return new TaskResult<AccountAvailability>(e);
                    }
                }
            } catch (RuntimeException e) {
                return new TaskResult<AccountAvailability>(e);
            }
        }

        @Override
        protected void onCancelled() {
            listener.onException(new RuntimeException("Communication task is not expected to be cancelled"));
        }

        @Override
        protected void onPostExecute(TaskResult<AccountAvailability> result) {
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
