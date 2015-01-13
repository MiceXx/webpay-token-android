package jp.webpay.android.token.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import jp.webpay.android.token.R;
import jp.webpay.android.token.WebPay;
import jp.webpay.android.token.WebPayListener;
import jp.webpay.android.token.model.AccountAvailability;
import jp.webpay.android.token.model.CardType;
import jp.webpay.android.token.model.Token;

/**
 * A fragment to create WebPay client-side token.
 * Activities that contain this fragment must implement the
 * {@link WebPayTokenCompleteListener} interface
 * to handle tokens created.
 * Use the {@link WebPayTokenFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WebPayTokenFragment extends Fragment implements WebPayTokenCompleteListener {
    private static final String ARG_PUBLISHABLE_KEY = "publishableKey";
    private static final String CARD_DIALOG_FRAGMENT_TAG = "card_dialog";

    private WebPayTokenCompleteListener mListener;
    private String mPublishableKey;
    private List<CardType> mCardTypesSupported;
    private Button mOpenButton;
    private boolean mHaveToken = false;
    private @StringRes int mOpenButtonTitle = R.string.token_fragment_open_dialog;
    private @StringRes int mCardDialogSendButtonTitle = R.string.card_send;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param publishableKey WebPay publishable key to generate token
     * @return A new instance of fragment WebPayTokenFragment.
     */
    public static WebPayTokenFragment newInstance(String publishableKey) {
        WebPayTokenFragment fragment = new WebPayTokenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PUBLISHABLE_KEY, publishableKey);
        fragment.setArguments(args);
        return fragment;
    }

    public WebPayTokenFragment() {
        // Required empty public constructor
    }

    /**
     * Set button title to open card information dialog.
     * Default is {@code jp.webpay.android.R.string.token_fragment_open_dialog}, which is "Pay with card".
     * This method works before and after fragment is created, but has no effect if token is generated.
     *
     * @param openButtonTitle    title res id
     */
    public void setOpenButtonTitle(@StringRes int openButtonTitle) {
        mOpenButtonTitle = openButtonTitle;
        if (mOpenButton != null && !mHaveToken) {
            mOpenButton.setText(openButtonTitle);
        }
    }

    /**
     * Set send button title of card dialog created in this token fragment.
     * Default is {@code jp.webpay.android.R.string.card_send}, which is "Pay with card".
     * This is delegated to {@link CardDialogFragment#setSendButtonTitle(int)}
     * on opening dialog.
     * So call after dialog open does not effect current one.
     *
     * @param cardDialogSendButtonTitle    title res id
     */
    public void setCardDialogSendButtonTitle(@StringRes int cardDialogSendButtonTitle) {
        this.mCardDialogSendButtonTitle = cardDialogSendButtonTitle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            throw new IllegalStateException("WebPayTokenFragment must be initialized using WebPayTokenFragment.newInstance()");
        }
        mPublishableKey = getArguments().getString(ARG_PUBLISHABLE_KEY);
        if (mPublishableKey == null || mPublishableKey.equals("")) {
            throw new IllegalArgumentException("WebPayTokenFragment requires publishableKey to present. " +
                    "You can find the key starts with \"test_public_\" in WebPay settings page.");
        }
        retrieveAvailability();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_pay_token, container, false);
        mOpenButton = (Button)view.findViewById(R.id.open_dialog_button);
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cardTypesSupported is best-effort. Continue even if null.
                CardDialogFragment fragment = CardDialogFragment.newInstance(mPublishableKey, mCardTypesSupported);
                fragment.setSendButtonTitle(mCardDialogSendButtonTitle);
                fragment.show(getChildFragmentManager(), CARD_DIALOG_FRAGMENT_TAG);
            }
        });
        if (!mHaveToken)
            mOpenButton.setText(mOpenButtonTitle);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WebPayTokenCompleteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WebPayTokenCompleteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Delegate OnCompleteListener from CardDialogFragment to mListener,
    // while setting views to reflect current token status.
    @Override
    public void onTokenCreated(Token token) {
        mListener.onTokenCreated(token);
        mHaveToken = true;
        mOpenButton.setText(R.string.token_fragment_token_generated);
    }

    @Override
    public void onCancelled(Throwable throwable) {
        mListener.onCancelled(throwable);
    }

    private void retrieveAvailability() {
        new WebPay(mPublishableKey).retrieveAvailability(new WebPayListener<AccountAvailability>() {
            @Override
            public void onCreate(AccountAvailability result) {
                mCardTypesSupported = result.cardTypesSupported;
            }

            @Override
            public void onException(Throwable cause) {
                // ignore failures
                // card types supported is not necessary for creating a token
            }
        });
    }
}
