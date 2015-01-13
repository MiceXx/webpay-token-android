package jp.webpay.android.token.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AccountAvailability {
    public final List<String> currenciesSupported;
    public final List<CardType> cardTypesSupported;

    public static AccountAvailability fromJson(JSONObject json) throws JSONException {
        JSONArray jsonCurrencies = json.getJSONArray("currencies_supported");
        ArrayList<String> currenciesSupported = new ArrayList<String>();
        for (int i = 0; i < jsonCurrencies.length(); i++) {
            currenciesSupported.add(jsonCurrencies.getString(i));
        }

        JSONArray jsonCardTypes = json.getJSONArray("card_types_supported");
        ArrayList<CardType> cardTypesSupported = new ArrayList<CardType>();
        for (int i = 0; i < jsonCardTypes.length(); i++) {
            cardTypesSupported.add(CardType.byName(jsonCardTypes.getString(i)));
        }

        return new AccountAvailability(currenciesSupported, cardTypesSupported);
    }

    AccountAvailability(List<String> currenciesSupported, List<CardType> cardTypesSupported) {
        this.currenciesSupported = currenciesSupported;
        this.cardTypesSupported = cardTypesSupported;
    }
}
