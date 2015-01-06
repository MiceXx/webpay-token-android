package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;

import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.CvcValidator;

public class CvcField extends BaseCardField {
    public static final int MAX_LENGTH = 4;
    private String mValidCvc;

    public CvcField(Context context) {
        super(context);
        initialize();
    }

    public CvcField(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CvcField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        setInputType(InputType.TYPE_CLASS_NUMBER);
        // TYPE_NUMBER_VARIATION_PASSWORD is unavailable in SDK 8 to 10
        setTransformationMethod(PasswordTransformationMethod.getInstance());
        setHint(R.string.field_cvc_hint);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH)});
    }

    @Override
    protected boolean validateCurrentValue() {
        String value = getText().toString();
        if (CvcValidator.isValid(value)) {
            mValidCvc = value;
            return true;
        } else {
            mValidCvc = null;
            return false;
        }
    }

    @Override
    public void updateCard(RawCard card) {
        card.cvc(mValidCvc);
    }

    /**
     * @return input CVC value if valid, null otherwise
     */
    public String getValidCvc() {
        return mValidCvc;
    }
}
