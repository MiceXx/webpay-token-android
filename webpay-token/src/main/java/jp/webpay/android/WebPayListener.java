package jp.webpay.android;

public interface WebPayListener<T> {

    public abstract void onCreate(T result);

    public abstract void onException(Throwable cause);

}
