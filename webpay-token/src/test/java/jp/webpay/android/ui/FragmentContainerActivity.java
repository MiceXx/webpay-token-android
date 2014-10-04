package jp.webpay.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import jp.webpay.android.model.Token;

import java.util.concurrent.CountDownLatch;

public class FragmentContainerActivity extends FragmentActivity
        implements WebPayTokenCompleteListener {
    public static final String FRAGMENT_TAG = "test_webpay_token_fragment";
    private CountDownLatch mLatch;
    private Token mLastToken;
    private Throwable mLastThrowable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebPayTokenFragment tokenFragment = WebPayTokenFragment.newInstance("test_public_dummykey");
        getSupportFragmentManager().beginTransaction()
                .add(tokenFragment, FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onTokenCreated(Token token) {
        if (mLatch != null)
            mLatch.countDown();
        mLastToken = token;
    }

    @Override
    public void onCancelled(Throwable throwable) {
        if (mLatch != null)
            mLatch.countDown();
        mLastThrowable = throwable;
    }

    public void setLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public Token getLastToken() {
        return mLastToken;
    }

    public Throwable getLastThrowable() {
        return mLastThrowable;
    }
}
