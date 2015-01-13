package jp.webpay.android.token.validator;

import java.util.regex.Pattern;

/**
 * Static class validate names on cards
 */
public class NameValidator {
    private final static Pattern VALID_PATTERN = Pattern.compile("\\A[A-Za-z ]+\\z");

    /**
     * Validates name on card
     * @param name    name on card
     * @return true if valid
     */
    public static boolean isValid(String name) {
        return name != null && VALID_PATTERN.matcher(name).matches();
    }
}
