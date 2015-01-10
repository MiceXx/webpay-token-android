package jp.webpay.android.validator;

import java.util.regex.Pattern;

/**
 * Static class validates CVC (Card Verification Code)
 */
public class CvcValidator {
    private final static Pattern VALID_PATTERN = Pattern.compile("\\A\\d{3,4}\\z");

    /**
     * Validates cvc format. This method just checks the format, not correctness.
     * @param cvc    cvc code on card
     * @return true if cvc is acceptable
     */
    public static boolean isValid(String cvc) {
        return cvc != null && VALID_PATTERN.matcher(cvc).matches();
    }
}
