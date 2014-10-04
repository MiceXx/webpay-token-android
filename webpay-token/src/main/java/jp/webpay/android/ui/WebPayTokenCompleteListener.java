package jp.webpay.android.ui;

import jp.webpay.android.model.Token;

public interface WebPayTokenCompleteListener {
    public void onTokenCreated(Token token);

    public void onCancelled(Throwable throwable);
}
