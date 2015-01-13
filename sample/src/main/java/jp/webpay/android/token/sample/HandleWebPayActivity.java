package jp.webpay.android.token.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import jp.webpay.android.token.WebPay;
import jp.webpay.android.token.WebPayListener;
import jp.webpay.android.token.model.RawCard;
import jp.webpay.android.token.model.Token;


public class HandleWebPayActivity extends BaseSampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_webpay);
    }

    private void onTokenCreated(Token token) {
        setStatusMessage(String.format(getResources().getString(R.string.token_generated), token.id));
    }

    private void onCancelled(Throwable lastException) {
        String message = lastException == null ? "(not set)" : lastException.getMessage();
        setStatusMessage(String.format(getResources().getString(R.string.token_cancelled), message));
    }

    private void setStatusMessage(String text) {
        ((TextView)findViewById(R.id.statusTextView)).setText(text);
    }

    public void createToken(View view) {
        EditText cardNumberField = ((EditText) findViewById(R.id.cardNumberField));
        EditText cardExpiryMonthField = ((EditText) findViewById(R.id.cardExpiryMonth));
        EditText cardExpiryYearField = ((EditText) findViewById(R.id.cardExpiryYear));
        EditText cardCvcField = ((EditText) findViewById(R.id.cardCvcField));
        EditText cardNameField = ((EditText) findViewById(R.id.cardNameField));

        if (cardNumberField.getText().length() < 15 ||
                cardExpiryMonthField.getText().length() != 2 ||
                cardExpiryYearField.getText().length() != 4 ||
                cardCvcField.getText().length() < 3 ||
                cardNameField.getText().length() == 0 ) {
            showMessage(R.string.invalid_input);
            return;
        }

        final RawCard rawCard = new RawCard()
                .number(cardNumberField.getText().toString())
                .expMonth(Integer.valueOf(cardExpiryMonthField.getText().toString()))
                .expYear(Integer.valueOf(cardExpiryYearField.getText().toString()))
                .cvc(cardCvcField.getText().toString())
                .name(cardNameField.getText().toString());

        showMessage(R.string.creating_token);
        new WebPay(WEBPAY_PUBLISHABLE_KEY).createToken(rawCard, new WebPayListener<Token>() {
            @Override
            public void onCreate(Token result) {
                onTokenCreated(result);
            }

            @Override
            public void onException(Throwable cause) {
                onCancelled(cause);
            }
        });
    }
}
