package jp.webpay.android.ui.field;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.webpay.android.R;
import jp.webpay.android.model.RawCard;
import jp.webpay.android.validator.ExpiryValidator;

public class ExpiryField extends MultiColumnCardField {
    public static final String SEPARATOR = " / ";
    private Integer mValidMonth;
    private Integer mValidYear;

    public ExpiryField(Context context) {
        super(context, SEPARATOR);
        initialize();
    }

    public ExpiryField(Context context, AttributeSet attrs) {
        super(context, attrs, SEPARATOR);
        initialize();
    }

    public ExpiryField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, SEPARATOR);
        initialize();
    }

    private void initialize() {
        setHint(R.string.field_expiry_hint);
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    protected boolean validateCurrentValue() {
        String pair[] = parseToPair(getText().toString());
        String month = pair[0];
        String year = pair[1];

        if (year.equals("20")) {
            setText(month + "/ 2020");
        }
        try {
            mValidMonth = Integer.valueOf(month);
            mValidYear = Integer.valueOf(year);
        } catch (NumberFormatException e) {
            mValidMonth = null;
            mValidYear = null;
            return false;
        }

        if (ExpiryValidator.isValid(mValidMonth, mValidYear)) {
            return true;
        } else {
            mValidMonth = null;
            mValidYear = null;
            return false;
        }
    }

    @Override
    public boolean validate() {
        String pair[] = parseToPair(getText().toString());
        String month = pair[0];
        String year = pair[1];

        if (year.equals("20")) {
            setText(month + "/ 2020");
        }
        return super.validate();
    }

    @Override
    public void updateCard(RawCard card) {
        card.expMonth(mValidMonth);
        card.expYear(mValidYear);
    }

    @Override
    protected String formatVisibleText(String current) {
        // "0" -> "0" (for 08)
        // "1 -> "1" (for 12)
        // "12" -> "12 / "
        // "209 / " -> "02 / "
        // "8" -> "08 / " (add 0 to 1-digit month)
        // "08 / 2" -> "08 / 2" (as is)
        // "08 / 1" -> "08 / 201" (other than 2)
        // "08 / 21" -> "08 / 2021"
        // "08 / 2014" -> "08 / 2014" (as is)
        // "08 / 12014" -> "08 / 2012"
        String pair[] = parseToPair(current);
        String month = pair[0];
        String year = pair[1];

        if (month == null)
            return "";

        if (month.length() > 0 && month.charAt(0) >= '2') {
            month = "0" + month;
        }

        if (month.length() > 2) {
            month = month.substring(0, 2);
        }

        if (year == null) {
            if (month.length() == 2)
                return month + SEPARATOR;
            else
                return month;
        }

        if (year.length() > 0 && year.charAt(0) != '2') {
            year = "20" + year;
        }

        if (year.length() == 2 && year.charAt(1) != '0') {
            year = "20" + year;
        }

        if (year.length() > 4) {
            year = year.substring(0, 4);
        }

        return month + SEPARATOR + year;
    }

    // Return array [month part, year part].
    // null if not yet entered.
    private String[] parseToPair(String current) {
        Pattern pat = Pattern.compile("(\\d+)([^\\d]+(\\d+)?)?");
        Matcher matcher = pat.matcher(current);
        if (!matcher.matches()) {
            return new String[2];
        }
        if (matcher.group(2) == null) {
            // year part is unavailable
            return new String[]{matcher.group(1), null};
        }
        return new String[]{matcher.group(1), matcher.group(3)};
    }

    /**
     * Validity of month value is paired with year.
     * Get two values at once.
     * @return input expiry month value if valid, null otherwise
     */
    public Integer getValidMonth() {
        return mValidMonth;
    }

    /**
     * Validity of year value is paired with month.
     * Get two values at once.
     * @return input expiry year value if valid, null otherwise
     */
    public Integer getValidYear() {
        return mValidYear;
    }
}
