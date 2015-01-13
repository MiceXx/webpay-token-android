package jp.webpay.android.token.sample;

import android.os.Bundle;
import android.widget.TextView;

import jp.webpay.android.token.model.Token;
import jp.webpay.android.token.ui.WebPayTokenCompleteListener;
import jp.webpay.android.token.ui.WebPayTokenFragment;


public class TokenCreateActivity extends BaseSampleActivity implements WebPayTokenCompleteListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_create);

        WebPayTokenFragment tokenFragment = WebPayTokenFragment.newInstance(WEBPAY_PUBLISHABLE_KEY);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.webpay_token_button_fragment, tokenFragment)
                .commit();
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
