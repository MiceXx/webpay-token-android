package jp.webpay.android.ui.field;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;

/**
 * Base class for credit card information fields.
 * This class provides validity of current value.
 */
public abstract class BaseCardField extends EditText
        implements View.OnFocusChangeListener {
    private boolean mValid = false;

    public BaseCardField(Context context) {
        super(context);
        initialize();
    }

    public BaseCardField(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public BaseCardField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        this.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // update validity on blur
        if (!hasFocus) {
            validate();
        }
    }

    @Override
    public void setError(CharSequence error) {
        if (error == null) {
            setTextColor(getResources().getColor(android.R.color.black));
        } else {
            setTextColor(getResources().getColor(R.color.error_text));
        }
    }

    /**
     * Validate the current value and update {@code mValidFoo} in subclass.
     * Show error message on the field if invalid.
     * Field users should call this before get value from fields.
     * @return true if the field is valid
     */
    public boolean validate() {
        mValid = validateCurrentValue();
        if (mValid || getText().toString().equals("")) {
            setError(null);
        } else {
            setError(getResources().getString(R.string.field_error_default));
        }
        return mValid;
    }

    protected abstract boolean validateCurrentValue();

    public boolean isValid() {
        return mValid;
    }

    /**
     * Assign the field's current value to the given card's corresponding field.
     * @param card    card to be assigned the value
     */
    public abstract void updateCard(RawCard card);
}
