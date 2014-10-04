package jp.webpay.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import jp.webpay.android.model.CardType;
import jp.webpay.android.model.Token;

import java.util.Arrays;

public class CardDialogActivity extends FragmentActivity implements WebPayTokenCompleteListener {
    public static final String FRAGMENT_TAG = "test_card_dialog_fragment";
    private boolean mCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardDialogFragment fragment =
                CardDialogFragment.newInstance("test_public_dummykey", Arrays.asList(CardType.JCB));
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
}
