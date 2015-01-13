package jp.webpay.android.token.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorResponse {
    public final int statusCode;
    /**
     * all values can be null
     */
    public final String type, causedBy, code, message, param;

    public static ErrorResponse fromJson(int statusCode, JSONObject json) throws JSONException {
        JSONObject error = json.getJSONObject("error");
        return new ErrorResponse(statusCode,
                error.optString("type", null),
                error.optString("caused_by", null),
                error.optString("code", null),
                error.optString("message", null),
                error.optString("param", null));
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
