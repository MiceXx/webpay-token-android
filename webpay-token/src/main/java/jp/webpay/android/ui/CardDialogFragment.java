package jp.webpay.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import jp.webpay.android.WebPay;

/**
 * This class is only used from WebPayTokenFragment to create tokens. This
 * dialog is responsible for accept card information, validate it, creating
 * token and handling errors.
 */
public class CardDialogFragment extends DialogFragment {
    private static final String ARG_PUBLISHABLE_KEY = "publishableKey";
    private WebPay mWebPay;
    private WebPayTokenCompleteListener mListener;

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
        builder.setMessage("Fire!Fire!")
                .setPositiveButton("FIRE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onTokenCreated(null);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCancelled(null);
                    }
                });

        return builder.create();
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
}
