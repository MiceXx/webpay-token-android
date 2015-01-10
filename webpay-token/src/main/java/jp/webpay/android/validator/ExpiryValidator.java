package jp.webpay.android.validator;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Static class validates card expiry
 */
public class ExpiryValidator {

    /**
     * Validates expiry that the numbers are correct and date is after now in JST.
     * This rejects year represented in two digits (e.g. 14) according to WebPay's spec.
     * Pass full representation (e.g. 2014).
     * @param month    month from 1 to 12
     * @param year     year in 4 digits (e.g. 2014)
     * @return true if valid
     */
    public static boolean isValid(int month, int year) {
        if (month < 1 || month > 12)
            return false;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        return year > currentYear
                || (year == currentYear && month >= currentMonth);
    }
}
