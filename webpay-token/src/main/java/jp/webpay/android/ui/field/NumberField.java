package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.CardNumberValidator;

public class NumberField extends MultiColumnCardField {
    public static final String SEPARATOR = " ";
    private String mValidNumber;

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
        return builder.toString();
    }

    public String getValidNumber() {
        return mValidNumber;
    }
}
