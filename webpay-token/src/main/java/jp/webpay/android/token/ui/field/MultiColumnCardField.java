package jp.webpay.android.token.ui.field;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Base class for fields with multiple columns, such as card number and expiry.
 * This field inserts/removes separator according to users' interaction.
 */
public abstract class MultiColumnCardField extends BaseCardField implements TextWatcher {
    private final String mSeparator;
    private boolean mDeletingSeparator;

    public MultiColumnCardField(Context context, String separator) {
        super(context);
        mSeparator = separator;
        initialize();
    }

    public MultiColumnCardField(Context context, AttributeSet attrs, String separator) {
        super(context, attrs);
        mSeparator = separator;
        initialize();
    }

    public MultiColumnCardField(Context context, AttributeSet attrs, int defStyle, String separator) {
        super(context, attrs, defStyle);
        mSeparator = separator;
        initialize();
    }

    private void initialize() {
        addTextChangedListener(this);
    }

    @Override
    protected boolean validateCurrentValue() {
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // user trying to delete a separator
        char lastChar = mSeparator.charAt(mSeparator.length() - 1);
        mDeletingSeparator =
                start < s.length() && s.charAt(start) == lastChar && count == 1 && after == 0;
    }

    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString();
        if (mDeletingSeparator) {
            // DEL key input removed separator in android's native behavior.
            // Here, remove the rest of separators and previous column's last letter.
            string = string.substring(0, string.length() - mSeparator.length());
        }
        String visibleText = formatVisibleText(string);
        changeText(visibleText);
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    protected abstract String formatVisibleText(String current);

    private void changeText(String s) {
        removeTextChangedListener(this);
        int nextSelection = getSelectionEnd() - getText().length() + s.length();
        setText(s);
        setSelection(nextSelection);
        addTextChangedListener(this);
    }
}
