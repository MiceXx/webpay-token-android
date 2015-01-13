package jp.webpay.android.token.ui;

import jp.webpay.android.token.model.Token;

public interface WebPayTokenCompleteListener {
    public void onTokenCreated(Token token);

    public void onCancelled(Throwable throwable);
}
