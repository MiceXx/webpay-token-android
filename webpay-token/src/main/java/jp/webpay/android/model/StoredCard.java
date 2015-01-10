package jp.webpay.android.model;

import org.json.JSONException;
import org.json.JSONObject;

public class StoredCard {
    public final String object;
    public final String fingerprint;
    public final String name;
    public final String country;
    public final CardType type;
    public final String cvcCheck;
    public final String last4;
    public final Integer expYear, expMonth;

    public static StoredCard fromJson(JSONObject json) throws JSONException {
        return new StoredCard(
                json.getString("object"),
                json.getInt("exp_year"),
                json.getInt("exp_month"),
                json.getString("fingerprint"),
                json.getString("name"),
                json.getString("country"),
                CardType.byName(json.getString("type")),
                json.getString("cvc_check"),
                json.getString("last4"));
    }

    StoredCard(String object, Integer expYear, Integer expMonth, String fingerprint,
                      String name, String country, CardType type, String cvcCheck, String last4) {
        this.object = object;
        this.fingerprint = fingerprint;
        this.name = name;
        this.country = country;
        this.type = type;
        this.cvcCheck = cvcCheck;
        this.last4 = last4;
        this.expYear = expYear;
        this.expMonth = expMonth;
    }
}
