package jp.webpay.android;

import jp.webpay.android.model.ErrorResponse;

public class ErrorResponseException extends RuntimeException {
    private final ErrorResponse response;

    public ErrorResponseException(ErrorResponse response) {
        super(response.message == null ? "Undocumented response error" : response.message);
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}
