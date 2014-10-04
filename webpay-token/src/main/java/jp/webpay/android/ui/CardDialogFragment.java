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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import jp.webpay.android.ErrorResponseException;
import jp.webpay.android.R;
import jp.webpay.android.WebPay;
import jp.webpay.android.WebPayListener;
import jp.webpay.android.model.CardType;
import jp.webpay.android.model.ErrorResponse;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.model.Token;
import jp.webpay.android.ui.field.BaseCardField;
import jp.webpay.android.ui.field.NumberField;

import java.util.*;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        String publishableKey = arguments.getString(ARG_PUBLISHABLE_KEY);
        mWebPay = new WebPay(publishableKey);

        String typeNames[] = arguments.getStringArray(ARG_SUPPORTED_CARD_TYPES);
        mSupportedCardTypes = new ArrayList<CardType>();
        if (typeNames == null) {
            // allow all brands if no specification
            mSupportedCardTypes.addAll(Arrays.asList(CardType.values()));
        } else {
            for (String name : typeNames) {
                mSupportedCardTypes.add(CardType.valueOf(name));
            }
        }
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
        onCardTypeChange(null); // initialize

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

    private void showAvailableCardTypes() {
        LinearLayout iconList = (LinearLayout)getDialog().findViewById(R.id.cardTypeIconList);
        for (CardType cardType : mSupportedCardTypes) {
            ImageView view = new ImageView(getActivity());
            view.setImageDrawable(getResources().getDrawable(CARD_TYPE_TO_DRAWABLE.get(cardType)));
            iconList.addView(view);
        }
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
    public void onCardTypeChange(CardType cardType) {
        ImageView icon = (ImageView) getDialog().findViewById(R.id.cardNumberTypeIcon);
        if (cardType == null) {
            icon.setImageDrawable(null);
        } else {
            icon.setImageDrawable(getResources().getDrawable(CARD_TYPE_TO_DRAWABLE.get(cardType)));
        }

        ImageButton helpButton = (ImageButton) getDialog().findViewById(R.id.cardCvcHelpButton);
        if (CardType.AMERICAN_EXPRESS.equals(cardType)) {
            helpButton.setOnClickListener(cvcHelpListener(R.drawable.cvc_amex));
        } else {
            helpButton.setOnClickListener(cvcHelpListener(R.drawable.cvc));
        }
    }

    private View.OnClickListener cvcHelpListener(final int drawableId) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView descriptionImageView = new ImageView(getActivity());
                descriptionImageView.setImageDrawable(getResources().getDrawable(drawableId));
                new AlertDialog.Builder(getActivity())
                        .setView(descriptionImageView)
                        .setPositiveButton(android.R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        };
    }
}
