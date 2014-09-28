package jp.webpay.android;

import jp.webpay.android.model.Token;

public interface WebPayListener {

    public abstract void onCreateToken(Token token);

    public abstract void onErrorCreatingToken(Throwable cause);

}
