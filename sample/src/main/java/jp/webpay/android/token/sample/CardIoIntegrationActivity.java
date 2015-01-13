package jp.webpay.android.token.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import jp.webpay.android.token.WebPay;
import jp.webpay.android.token.WebPayListener;
import jp.webpay.android.token.model.RawCard;
import jp.webpay.android.token.model.Token;


public class CardIoIntegrationActivity extends BaseSampleActivity {

    private static int CARD_IO_SCAN_REQUEST_CODE = 100; // arbitrary int

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_io_integration);
        onStartCardIo(null);
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

    public void onStartCardIo(View view) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);

        startActivityForResult(scanIntent, CARD_IO_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CARD_IO_SCAN_REQUEST_CODE)
            return;

        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            setScanResult((CreditCard) data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT));
        } else {
            setStatusMessage(getResources().getString(R.string.card_io_cancelled));
        }
    }

    private void setScanResult(CreditCard scanResult) {
        if (!TextUtils.isEmpty(scanResult.cardNumber))
            ((EditText) findViewById(R.id.cardNumberField)).setText(scanResult.cardNumber);
        if (scanResult.expiryMonth > 0)
            ((EditText) findViewById(R.id.cardExpiryMonth)).setText(scanResult.expiryMonth);
        if (scanResult.expiryYear > 0)
            ((EditText) findViewById(R.id.cardExpiryYear)).setText(scanResult.expiryYear);
        if (!TextUtils.isEmpty(scanResult.cvv))
            ((EditText) findViewById(R.id.cardCvcField)).setText(scanResult.cvv);
    }

    public void createToken(View view) {
        EditText cardNumberField = ((EditText) findViewById(R.id.cardNumberField));
        EditText cardExpiryMonthField = ((EditText) findViewById(R.id.cardExpiryMonth));
        EditText cardExpiryYearField = ((EditText) findViewById(R.id.cardExpiryYear));
        EditText cardCvcField = ((EditText) findViewById(R.id.cardCvcField));
        EditText cardNameField = ((EditText) findViewById(R.id.cardNameField));

        final RawCard rawCard = new RawCard()
                .number(cardNumberField.getText().toString())
                .expMonth(Integer.valueOf(cardExpiryMonthField.getText().toString()))
                .expYear(Integer.valueOf(cardExpiryYearField.getText().toString()))
                .cvc(cardCvcField.getText().toString())
                .name(cardNameField.getText().toString());

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
