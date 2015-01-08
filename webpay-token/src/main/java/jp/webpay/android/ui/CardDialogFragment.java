package jp.webpay.android.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.webpay.android.ErrorResponseException;
import jp.webpay.android.R;
import jp.webpay.android.WebPay;
import jp.webpay.android.WebPayListener;
import jp.webpay.android.model.CardType;
import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.Token;
import jp.webpay.android.ui.field.BaseCardField;
import jp.webpay.android.ui.field.CvcField;
import jp.webpay.android.ui.field.NameField;
import jp.webpay.android.ui.field.NumberField;

/**
 * This class is to create tokens from users input. WebPayTokenFragment is recommended for most users, but
 * you can use this for better UX. This dialog is responsible for accept card information, validate it,
 * creating token and handling errors.
 * <p>
 * Activities that contain this fragment (and not contain {@link jp.webpay.android.ui.WebPayTokenFragment})
 * must implement the {@link jp.webpay.android.ui.WebPayTokenCompleteListener} interface to handle results.
 */
public class CardDialogFragment extends DialogFragment implements NumberField.OnCardTypeChangeListener {
    private static final String ARG_PUBLISHABLE_KEY = "publishableKey";
    private static final String ARG_SUPPORTED_CARD_TYPES = "supportedCardTypes";
    private static final String TAG = "webpay:CardDialogFragment";
    private static final Map<CardType, Integer> CARD_TYPE_TO_DRAWABLE = new HashMap<CardType, Integer>() {{
        put(CardType.VISA, R.drawable.card_visa);
        put(CardType.AMERICAN_EXPRESS, R.drawable.card_amex);
        put(CardType.MASTERCARD, R.drawable.card_master);
        put(CardType.JCB, R.drawable.card_jcb);
        put(CardType.DINERS_CLUB, R.drawable.card_diners);
    }};
    private WebPay mWebPay;
    private WebPayTokenCompleteListener mListener;
    private Throwable mLastException;
    private ArrayList<CardType> mSupportedCardTypes;
    private @StringRes int mSendButtonTitle = R.string.card_send;

    /**
     * Use this factory method to create a new instance of this fragment
     * using the provided parameters.
     * <p>
     * {@link jp.webpay.android.ui.WebPayTokenFragment} will give an idea
     * about how to use this class.
     *
     * @param publishableKey        WebPay publishable key to generate token
     * @param supportedCardTypes    supported card types retrieved from availability API. Use
     *                              {@link WebPay#retrieveAvailability(jp.webpay.android.WebPayListener)}.
     *                              Pass null if you do not need to show and check supported card types.
     * @return A new instance of dialog fragment
     */
    public static CardDialogFragment newInstance(String publishableKey, List<CardType> supportedCardTypes) {
        CardDialogFragment fragment = new CardDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PUBLISHABLE_KEY, publishableKey);

        if (supportedCardTypes != null) {
            String typeNames[] = new String[supportedCardTypes.size()];
            for (int i = 0; i < supportedCardTypes.size(); i++) {
                typeNames[i] = supportedCardTypes.get(i).toString();
            }
            args.putStringArray(ARG_SUPPORTED_CARD_TYPES, typeNames);
        }

        fragment.setArguments(args);
        return fragment;
    }

    public CardDialogFragment() {

    }

    /**
     * Set send button title string resource id.
     * Default is {@code jp.webpay.android.R.string.card_send}, which is "Pay with card".
     * This method works before and after dialog is created.
     *
     * @param sendButtonTitle    send button title res id
     */
    public void setSendButtonTitle(@StringRes int sendButtonTitle) {
        this.mSendButtonTitle = sendButtonTitle;
        Dialog dialog = getDialog();
        if (dialog == null)
            return;
        Button sendButton = (Button) dialog.findViewById(R.id.button_submit);
        if (sendButton == null)
            return;
        sendButton.setText(sendButtonTitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        String publishableKey = arguments.getString(ARG_PUBLISHABLE_KEY);
        mWebPay = new WebPay(publishableKey);

        String typeNames[] = arguments.getStringArray(ARG_SUPPORTED_CARD_TYPES);

        if (typeNames == null) {
            mSupportedCardTypes = null;
        } else {
            mSupportedCardTypes = new ArrayList<CardType>();
            for (String name : typeNames) {
                mSupportedCardTypes.add(CardType.valueOf(name));
            }
        }
    }

    // using "null" for inflate is correct according to
    // http://developer.android.com/guide/topics/ui/dialogs.html#CustomLayout
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.WebPayDialogTheme));
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_card, null));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;

        Button sendButton = (Button) dialog.findViewById(R.id.button_submit);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCardInfoToWebPay();
            }
        });
        sendButton.setText(mSendButtonTitle);

        Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mListener.onCancelled(mLastException);
            }
        });

        NumberField numberField = (NumberField) dialog.findViewById(R.id.cardNumberField);
        numberField.setOnCardTypeChangeListener(this);

        // allow all brands if no specification
        if (mSupportedCardTypes == null)
            numberField.setCardTypesSupported(Arrays.asList(CardType.values()));
        else
            numberField.setCardTypesSupported(mSupportedCardTypes);
        if (numberField.getText().toString().equals("")) {
            onCardTypeChange(null); // initialize
        }

        NameField nameField = (NameField) dialog.findViewById(R.id.cardNameField);
        nameField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    hideSoftKeyboard();
                    sendCardInfoToWebPay();
                    return true;
                }
                return false;
            }
        });
        showAvailableCardTypes();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WebPayTokenCompleteListener) getParentFragment();
        } catch (ClassCastException ignored) {
            mListener = null;
        }
        if (mListener == null) {
            try {
                mListener = (WebPayTokenCompleteListener) activity;
            } catch (ClassCastException ignored) {
                mListener = null;
            }
        }
        if (mListener == null) {
            throw new IllegalStateException("Activity or parent fragment must implement WebPayTokenCompleteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void hideSoftKeyboard() {
        View currentFocus = getDialog().getCurrentFocus();
        if(currentFocus !=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private void showAvailableCardTypes() {
        View label = getDialog().findViewById(R.id.cardTypeLabel);
        LinearLayout iconList = (LinearLayout)getDialog().findViewById(R.id.cardTypeIconList);
        iconList.removeAllViews();
        if (mSupportedCardTypes == null) {
            label.setVisibility(View.GONE);
            iconList.setVisibility(View.GONE);
        } else {
            label.setVisibility(View.VISIBLE);
            iconList.setVisibility(View.VISIBLE);
            for (CardType cardType : mSupportedCardTypes) {
                ImageView view = new ImageView(getActivity());
                view.setImageDrawable(getResources().getDrawable(CARD_TYPE_TO_DRAWABLE.get(cardType)));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 5, 0);
                view.setLayoutParams(lp);
                iconList.addView(view);
            }
        }
    }

    private void sendCardInfoToWebPay() {
        RawCard card = createValidCardFromForm();
        if (card == null) {
            return;
        }
        Log.v(TAG, card.toJson().toString());
        switchIndicatorVisibility(true);
        updateRequestLanguage();
        mWebPay.createToken(card, new WebPayListener<Token>() {
            @Override
            public void onCreate(Token result) {
                switchIndicatorVisibility(false);
                mListener.onTokenCreated(result);
                getDialog().dismiss();
            }

            @Override
            public void onException(Throwable cause) {
                switchIndicatorVisibility(false);
                mLastException = cause;
                Log.i(TAG, "exception while creating a token", cause);
                showWebPayErrorAlert(cause);
            }
        });
    }

    private void switchIndicatorVisibility(boolean visible) {
        if (visible) {
            getDialog().findViewById(R.id.progress).setVisibility(View.VISIBLE);
            getDialog().findViewById(R.id.buttons).setVisibility(View.GONE);
        } else {
            getDialog().findViewById(R.id.progress).setVisibility(View.GONE);
            getDialog().findViewById(R.id.buttons).setVisibility(View.VISIBLE);
        }
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
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .show();
    }

    @Override
    public void onCardTypeChange(CardType cardType) {
        NumberField numberFiled = (NumberField) getDialog().findViewById(R.id.cardNumberField);
        int iconDrawableId = (cardType == null) ? 0 : CARD_TYPE_TO_DRAWABLE.get(cardType).intValue();
        numberFiled.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconDrawableId, 0);

        CvcField cvcField = (CvcField) getDialog().findViewById(R.id.cardCvcField);
        int drawableId = CardType.AMERICAN_EXPRESS.equals(cardType) ? R.drawable.cvc_amex : R.drawable.cvc;
        cvcField.setOnHelpIconClickListener(cvcHelpListener(drawableId));
    }

    @SuppressLint("InflateParams") // using "null" for inflate is correct
    private View.OnClickListener cvcHelpListener(final int drawableId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_cvc_help, null);
                ((ImageView) view.findViewById(R.id.cvc_help)).setImageDrawable(getResources().getDrawable(drawableId));
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.WebPayDialogTheme))
                        .setView(view)
                        .show();
            }
        };
    }
}
