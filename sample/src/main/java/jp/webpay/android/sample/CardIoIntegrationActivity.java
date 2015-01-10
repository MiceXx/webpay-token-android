package jp.webpay.android.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import jp.webpay.android.WebPay;
import jp.webpay.android.WebPayListener;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.Token;
import jp.webpay.android.ui.WebPayTokenCompleteListener;


public class CardIoIntegrationActivity extends BaseFragmentActivity implements WebPayTokenCompleteListener {

    private static int CARD_IO_SCAN_REQUEST_CODE = 100; // arbitrary int

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_io_integration);
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

    public void onStartCardIo(View view) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false); // default: false

        startActivityForResult(scanIntent, CARD_IO_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CARD_IO_SCAN_REQUEST_CODE)
            return;

        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            askNameForCardIo(scanResult);
        } else {
            setStatusMessage(getResources().getString(R.string.card_io_cancelled));
        }
    }

    private void askNameForCardIo(CreditCard scanResult) {
        final RawCard rawCard = new RawCard()
                .number(scanResult.cardNumber)
                .expMonth(scanResult.expiryMonth)
                .expYear(scanResult.expiryYear)
                .cvc(scanResult.cvv);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Card holder name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rawCard.name(input.getText().toString());
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
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
