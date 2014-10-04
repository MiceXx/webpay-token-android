package jp.webpay.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import jp.webpay.android.ErrorResponseException;
import jp.webpay.android.R;
import jp.webpay.android.WebPay;
import jp.webpay.android.WebPayListener;
import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.Token;
import jp.webpay.android.ui.field.BaseCardField;
import jp.webpay.android.ui.field.NumberField;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class is only used from WebPayTokenFragment to create tokens. This
 * dialog is responsible for accept card information, validate it, creating
 * token and handling errors.
 */
public class CardDialogFragment extends DialogFragment implements NumberField.OnCardTypeChangeListener {
    private static final String ARG_PUBLISHABLE_KEY = "publishableKey";
    private static final String TAG = "webpay:CardDialogFragment";
    private static final Map<String, Integer> CARD_TYPE_TO_DRAWABLE = new HashMap<String, Integer>() {{
        put("Visa", R.drawable.card_visa);
        put("American Express", R.drawable.card_amex);
        put("MasterCard", R.drawable.card_master);
        put("JCB", R.drawable.card_jcb);
        put("Diners Club", R.drawable.card_diners);
    }};
    private WebPay mWebPay;
    private WebPayTokenCompleteListener mListener;
    private Throwable mLastException;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * This is private, not intended to be called by other than
     * {@link jp.webpay.android.ui.WebPayTokenFragment}.
     *
     * @param publishableKey WebPay publishable key to generate token
     * @return A new instance of dialog fragment
     */
    public static CardDialogFragment newInstance(String publishableKey) {
        CardDialogFragment fragment = new CardDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PUBLISHABLE_KEY, publishableKey);
        fragment.setArguments(args);
        return fragment;
    }

    public CardDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String publishableKey = getArguments().getString(ARG_PUBLISHABLE_KEY);
        mWebPay = new WebPay(publishableKey);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_card, null))
                .setTitle(R.string.card_payment_info_title)
                .setPositiveButton(R.string.card_send, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // This is placeholder.
                        // Do nothing here and override button's onClick listener
                        // so that the dialog does not dismiss until a token is
                        // successfully generated.
                        // See AlertController.mButtonHandler
                        // http://stackoverflow.com/questions/13746412/prevent-dialogfragment-from-dismissing-when-button-is-clicked
                    }
                })
                .setNegativeButton(R.string.card_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancelled(mLastException);
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;

        // override default on-click listener not to dismiss automatically
        Button sendButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCardInfoToWebPay();
            }
        });

        ((NumberField)dialog.findViewById(R.id.cardNumberField)).setOnCardTypeChangeListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WebPayTokenCompleteListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent fragment must implement WebPayTokenCompleteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void sendCardInfoToWebPay() {
        RawCard card = createValidCardFromForm();
        if (card == null) {
            return;
        }
        Log.v(TAG, card.toJson().toString());

        updateRequestLanguage();
        mWebPay.createToken(card, new WebPayListener<Token>() {
            @Override
            public void onCreate(Token result) {
                mListener.onTokenCreated(result);
                getDialog().dismiss();
            }

            @Override
            public void onException(Throwable cause) {
                mLastException = cause;
                Log.i(TAG, "exception while creating a token", cause);
                showWebPayErrorAlert(cause);
            }
        });
    }

    private void updateRequestLanguage() {
        if (getResources().getConfiguration().locale.getISO3Language().equals("jpn")) {
            mWebPay.setLanguage("ja");
        } else {
            mWebPay.setLanguage("en");
        }
    }

    /**
     * Collect values on the form and return card
     * @return card that contains input information, null if one of fields is invalid
     */
    private RawCard createValidCardFromForm() {
        Dialog dialog = getDialog();
        RawCard card = new RawCard();

        int fieldIds[] = new int[]{R.id.cardCvcField, R.id.cardExpiryField, R.id.cardNameField, R.id.cardNumberField};
        for (int fieldId : fieldIds) {
            BaseCardField field = (BaseCardField) dialog.findViewById(fieldId);
            if (!field.validate())
                return null;
            field.updateCard(card);
        }

        return card;
    }

    private void showWebPayErrorAlert(Throwable cause) {
        String message = null;
        if (cause instanceof ErrorResponseException) {
            ErrorResponse response = ((ErrorResponseException) cause).getResponse();
            if (response.causedBy.equals("buyer")) {
                message = response.message;
            }
        }

        if (message == null) {
            message = getString(R.string.tokenize_error_message);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tokenize_error_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onCardTypeChange(String cardType) {
        ImageView icon = (ImageView) getDialog().findViewById(R.id.cardNumberTypeIcon);
        if (cardType == null) {
            icon.setImageDrawable(null);
        } else {
            icon.setImageDrawable(getResources().getDrawable(CARD_TYPE_TO_DRAWABLE.get(cardType)));
        }
    }
}
