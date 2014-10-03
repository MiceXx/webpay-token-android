package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.NameValidator;

public class NameField extends BaseCardField {
    private String mValidName;

    public NameField(Context context) {
        super(context);
        initialize();
    }

    public NameField(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public NameField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        setHint(R.string.field_name_hint);
        setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
    }

    @Override
    protected boolean validateCurrentValue() {
        String value = getText().toString();
        if (NameValidator.isValid(value)) {
            mValidName = value;
            return true;
        } else {
            mValidName = null;
            return false;
        }
    }

    @Override
    public void updateCard(RawCard card) {
        card.name(mValidName);
    }

    /**
     * @return input name value if valid, null otherwise
     */
    public String getValidName() {
        return mValidName;
    }
}
