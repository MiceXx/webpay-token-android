package jp.webpay.android.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Static class validates card numbers.
 * <p>
 * This class tests
 * <ul>
 *     <li>the card number does not contain invalid characters,</li>
 *     <li>the card number passes Luhn check, and</li>
 *     <li>the card number is of supported card brands.</li>
 * </ul>
 */
public class CardNumberValidator {
    private static final List<String> ALL_CARD_TYPES =
            Arrays.asList("Visa", "MasterCard", "JCB", "American Express", "Diners Club");
    private static final char SEPARATOR = '-';
    private static final Map<String, Pattern> CARD_TYPE_REGEXP = new HashMap<String, Pattern>();
    static {
        CARD_TYPE_REGEXP.put("Visa", Pattern.compile("\\A4[0-9]{12}(?:[0-9]{3})?\\z"));
        CARD_TYPE_REGEXP.put("American Express", Pattern.compile("\\A3[47][0-9]{13}\\z"));
        CARD_TYPE_REGEXP.put("MasterCard", Pattern.compile("\\A5[1-5][0-9]{14}\\z"));
        CARD_TYPE_REGEXP.put("JCB", Pattern.compile("\\A(?:2131|1800|35\\d{3})\\d{11}\\z"));
        CARD_TYPE_REGEXP.put("Diners Club", Pattern.compile("\\A3(?:0[0-5]|[68][0-9])[0-9]{11}\\z"));
    }

    /**
     * Validate that the number is acceptable as an account number of card types supported by WebPay
     * @param number    Card number composed of digits and hyphens.
     * @return true if card number is valid
     */
    public static boolean isValid(String number) {
        return isValid(number, ALL_CARD_TYPES);
    }

    /**
     * Validate that the number is acceptable as an account number of listed card types
     * @param number       Card number composed of digits, space and hyphens.
     * @param cardTypes    List of available card types
     * @return true if card number is valid
     */
    public static boolean isValid(String number, List<String> cardTypes) {
        if (cardTypes == null)
            return false;

        number = sanitize(number);
        return number != null
                && passLuhnTest(number)
                && matchNumberRegexp(number, cardTypes);
    }

    private static String sanitize(String number) {
        if (number == null)
            return null;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (ch == SEPARATOR)
                continue;
            if (ch >= '0' && ch <= '9') {
                builder.append(ch);
                continue;
            }
            return null;
        }

        return builder.toString();
    }

    private static boolean passLuhnTest(String number) {
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0 ;i < reverse.length();i++){
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {
                s1 += digit;
            } else {
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        return (s1 + s2) % 10 == 0;
    }

    private static boolean matchNumberRegexp(String number, List<String> cardTypes) {
        for (String cardType : cardTypes) {
            Pattern pattern = CARD_TYPE_REGEXP.get(cardType);
            if (pattern == null)
                continue;
            if (pattern.matcher(number).matches())
                return true;
        }
        return false;
    }
}
