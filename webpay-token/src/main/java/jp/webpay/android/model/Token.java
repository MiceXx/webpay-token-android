package jp.webpay.android.model;

import jp.webpay.android.WebPay;
import org.json.JSONException;
import org.json.JSONObject;

public class Token {
    public final String id, object;
    public final Boolean livemode;
    public final StoredCard card;
    public final Long timestamp;
    public final Boolean used;

    public static Token fromJson(JSONObject json) throws JSONException {
        return new Token(
                json.getString("id"),
                json.getString("object"),
                json.getBoolean("livemode"),
                StoredCard.fromJson(json.getJSONObject("card")),
                json.getLong("created"),
                json.getBoolean("used"));
    }

    Token(String id, String object, Boolean livemode, StoredCard card, Long timestamp, Boolean used) {
        this.id = id;
        this.object = object;
        this.livemode = livemode;
        this.card = card;
        this.timestamp = timestamp;
        this.used = used;
    }
}
