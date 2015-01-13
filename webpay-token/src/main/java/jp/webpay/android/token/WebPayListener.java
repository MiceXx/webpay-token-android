package jp.webpay.android.token;

public interface WebPayListener<T> {

    public abstract void onCreate(T result);

    public abstract void onException(Throwable cause);

}
