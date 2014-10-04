package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.NameValidator;

import java.util.Locale;

public class NameField extends BaseCardField implements TextWatcher {
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
                InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        addTextChangedListener(this);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString();
        changeText(string.replaceAll("[^A-Za-z ]", "").toUpperCase(Locale.ENGLISH));
    }

    private void changeText(String s) {
        removeTextChangedListener(this);
        int nextSelection = getSelectionEnd() - getText().length() + s.length();
        setText(s);
        setSelection(nextSelection);
        addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     * @return input name value if valid, null otherwise
     */
    public String getValidName() {
        return mValidName;
    }
}
