package jp.webpay.android;

import android.os.AsyncTask;

import jp.webpay.android.model.Card;
import jp.webpay.api.WebPayClient;
import jp.webpay.model.Token;
import jp.webpay.request.CardRequest;
import jp.webpay.exception.*;
import lombok.Setter;

public class WebPay {

    @Setter private String publishableKey;
    @Setter private Card card;
    @Setter private WebPayListener listener;

    public WebPay() {}

    public WebPay(WebPayListener listener) {
        this.setListener(listener);
    }

    public WebPay(WebPayListener listener, String publishableKey) {
        this.setListener(listener);
        this.setPublishableKey(publishableKey);
    }

    public WebPay(String publishableKey) {
        this.setPublishableKey(publishableKey);
    }

    public void createToken() {
        new CreateTokenTask().execute();
    }

    public void createToken(Card card, WebPayListener listener) {
        this.setCard(card);
        this.setListener(listener);
        createToken();
    }


    public class CreateTokenTask extends AsyncTask<Void, Void, Token> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Token doInBackground(Void... params) {
            WebPayClient client = new WebPayClient(publishableKey);
            CardRequest request = new CardRequest().number(card.getNumber()).expMonth(card.getExpMonth()).expYear(card.getExpYear()).cvc(Integer.valueOf(card.getCvc())).name(card.getName());
            try {
                return client.tokens.create(request);
            }
            catch (CardException e) {
                listener.onErrorCreatingToken();
            }
            catch (AuthenticationException e) {
                listener.onErrorCreatingToken();
            }
            catch (ApiConnectionException e) {
                listener.onErrorCreatingToken();
            }
            catch (InvalidRequestException e) {
                listener.onErrorCreatingToken();
            }
            catch (APIException e) {
                listener.onErrorCreatingToken();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            listener.onErrorCreatingToken();
        }

        @Override
        protected void onPostExecute(Token token) {
            listener.onCreateToken(null);
        }
    }
}
