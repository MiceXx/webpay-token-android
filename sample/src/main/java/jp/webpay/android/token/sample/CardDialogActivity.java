package jp.webpay.android.token.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import jp.webpay.android.token.model.CardType;
import jp.webpay.android.token.model.Token;
import jp.webpay.android.token.ui.CardDialogFragment;
import jp.webpay.android.token.ui.WebPayTokenCompleteListener;


public class CardDialogActivity extends BaseSampleActivity implements WebPayTokenCompleteListener {

    private final static String CARD_DIALOG_FRAGMENT_TAG = "card_dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_dialog);
    }

    public void onButtonClicked(View v) {
        // You can specify supporting card types manually
        List<CardType> supportedCardTypes = CardType.VM();
        CardDialogFragment fragment = CardDialogFragment.newInstance(
                WEBPAY_PUBLISHABLE_KEY, supportedCardTypes);
        fragment.setSendButtonTitle(R.string.button_submit);
        fragment.show(getSupportFragmentManager(), CARD_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onTokenCreated(Token token) {
        setStatusMessage(String.format(getResources().getString(R.string.token_generated), token.id));
    }

    @Override
    public void onCancelled(Throwable lastException) {
        String message = lastException == null ? "(not set)" : lastException.getMessage();
        setStatusMessage(String.format(getResources().getString(R.string.token_cancelled), message));
    }

    private void setStatusMessage(String text) {
        ((TextView)findViewById(R.id.statusTextView)).setText(text);
    }
}
