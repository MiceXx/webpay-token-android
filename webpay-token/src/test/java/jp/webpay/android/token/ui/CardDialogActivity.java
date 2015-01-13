package jp.webpay.android.token.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import jp.webpay.android.token.model.Token;

public class CardDialogActivity extends FragmentActivity implements WebPayTokenCompleteListener {
    public static final String FRAGMENT_TAG = "test_card_dialog_fragment";
    private boolean mCancelled;
    private CardDialogFragment fragment;

    public CardDialogActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .add(fragment, FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onTokenCreated(Token token) {
    }

    @Override
    public void onCancelled(Throwable throwable) {
        mCancelled = true;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public void setFragment(CardDialogFragment fragment) {
        this.fragment = fragment;
    }
}
