package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.CardNumberValidator;

import javax.xml.validation.Validator;
import java.util.regex.Pattern;

public class NumberField extends MultiColumnCardField {
    public static final String SEPARATOR = " ";
    private String mValidNumber;
    private OnCardTypeChangeListener mOnCardTypeChangeListener;
    private String mCurrentCardType;

    public NumberField(Context context) {
        super(context, SEPARATOR);
        initialize();
    }

    public NumberField(Context context, AttributeSet attrs) {
        super(context, attrs, SEPARATOR);
        initialize();
    }

    public NumberField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, SEPARATOR);
        initialize();
    }

    private void initialize() {
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setHint(R.string.field_number_hint);
    }

    @Override
    protected boolean validateCurrentValue() {
        String value = getText().toString().replace(SEPARATOR, "");
        if (CardNumberValidator.isValid(value)) {
            mValidNumber = value;
            return true;
        } else {
            mValidNumber = null;
            return false;
        }
    }

    @Override
    public void updateCard(RawCard card) {
        card.number(mValidNumber);
    }

    @Override
    protected String formatVisibleText(String current) {
        StringBuilder builder = new StringBuilder();
        int validChars = 0;
        for (int i = 0; i < current.length(); i++) {
            char ch = current.charAt(i);
            if (ch >= '0' && ch <= '9') {
                builder.append(ch);
                validChars += 1;
                if (validChars >= 16)
                    break;
                if (validChars % 4 == 0) {
                    builder.append(SEPARATOR);
                }
            }
        }
        String visibleText = builder.toString();
        notifyCardTypeChange(visibleText);
        return visibleText;
    }

    private String expectCardType(String number) {
        if (Pattern.matches("4[0-9].*", number)) {
            return "Visa";
        }
        if (Pattern.matches("3[47].*", number)) {
            return "American Express";
        }
        if (Pattern.matches("5[1-5].*", number)) {
            return "MasterCard";
        }
        if (Pattern.matches("3[0689].*", number)) {
            return "Diners Club";
        }
        if (Pattern.matches("35.*", number)) {
            return "JCB";
        }
        return null;
    }

    private void notifyCardTypeChange(String number) {
        String cardType = expectCardType(number);
        boolean isSame = mCurrentCardType == null ? cardType == null : mCurrentCardType.equals(cardType);
        if (!isSame) {
            mCurrentCardType = cardType;
            if (mOnCardTypeChangeListener != null) {
                mOnCardTypeChangeListener.onCardTypeChange(cardType);
            }
        }
    }

    public String getValidNumber() {
        return mValidNumber;
    }

    public void setOnCardTypeChangeListener(OnCardTypeChangeListener mListener) {
        this.mOnCardTypeChangeListener = mListener;
    }

    public static interface OnCardTypeChangeListener {
        public void onCardTypeChange(String cardType);
    }
}
