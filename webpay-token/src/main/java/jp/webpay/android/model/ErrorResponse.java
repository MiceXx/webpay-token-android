package jp.webpay.android.model;

import org.json.JSONObject;

public class ErrorResponse {
    public final int statusCode;
    /**
     * all values can be null
     */
    public final String type, causedBy, code, message, param;

    public static ErrorResponse fromJson(int statusCode, JSONObject json) {
        return new ErrorResponse(statusCode,
                json.optString("type", null),
                json.optString("caused_by", null),
                json.optString("code", null),
                json.optString("message", null),
                json.optString("param", null));
    }

    public ErrorResponse(int statusCode, String type, String causedBy, String code, String message, String param) {
        this.statusCode = statusCode;
        this.type = type;
        this.causedBy = causedBy;
        this.code = code;
        this.message = message;
        this.param = param;
    }
}
