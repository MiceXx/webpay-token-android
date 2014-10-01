package jp.webpay.android.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountAvailability {
    public final List<String> currenciesSupported;
    public final List<String> cardTypesSupported;

    public static AccountAvailability fromJson(JSONObject json) throws JSONException {
        JSONArray jsonCurrencies = json.getJSONArray("currencies_supported");
        ArrayList<String> currenciesSupported = new ArrayList<String>();
        for (int i = 0; i < jsonCurrencies.length(); i++) {
            currenciesSupported.add(jsonCurrencies.getString(i));
        }

        JSONArray jsonCardTypes = json.getJSONArray("card_types_supported");
        ArrayList<String> cardTypesSupported = new ArrayList<String>();
        for (int i = 0; i < jsonCardTypes.length(); i++) {
            cardTypesSupported.add(jsonCardTypes.getString(i));
        }

        return new AccountAvailability(currenciesSupported, cardTypesSupported);
    }

    AccountAvailability(List<String> currenciesSupported, List<String> cardTypesSupported) {
        this.currenciesSupported = currenciesSupported;
        this.cardTypesSupported = cardTypesSupported;
    }
}
