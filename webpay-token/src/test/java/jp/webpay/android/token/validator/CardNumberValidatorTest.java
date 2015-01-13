package jp.webpay.android.token.validator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jp.webpay.android.token.model.CardType;

import static jp.webpay.android.token.model.CardType.AMERICAN_EXPRESS;
import static jp.webpay.android.token.model.CardType.DINERS_CLUB;
import static jp.webpay.android.token.model.CardType.JCB;
import static jp.webpay.android.token.model.CardType.MASTERCARD;
import static jp.webpay.android.token.model.CardType.VISA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CardNumberValidatorTest {

    @Test
    public void testNullIsInvalid() throws Exception {
        assertFalse(CardNumberValidator.isValid(null));
        assertFalse(CardNumberValidator.isValid(null, new ArrayList<CardType>()));
        assertFalse(CardNumberValidator.isValid("4242-4242-4242-4242", null));
    }

    @Test
    public void testIsValidSanitizesNumber() throws Exception {
        assertTrue(CardNumberValidator.isValid("4242424242424242"));
        assertTrue(CardNumberValidator.isValid("4242-4242-4242-4242"));
        assertFalse(CardNumberValidator.isValid("4242 4242 4242 4242"));
        assertFalse(CardNumberValidator.isValid("4242\n424242424242"));
        assertFalse(CardNumberValidator.isValid("４242424242424242"));
    }

    @Test
    public void testIsValidChecksLuhn() throws Exception {
        // Valid visa number, but fail Luhn check
        assertFalse(CardNumberValidator.isValid("4242424242424243"));
    }

    @Test
    public void testIsValidChecksCardType() throws Exception {
        assertTrue("WebPay supports Visa", CardNumberValidator.isValid("4242424242424242"));
        assertTrue("WebPay supports MasterCard", CardNumberValidator.isValid("5105105105105100"));
        assertTrue("WebPay supports JCB", CardNumberValidator.isValid("3530111333300000"));
        assertTrue("WebPay supports American Express", CardNumberValidator.isValid("378282246310005"));
        assertTrue("WebPay supports Diners Club", CardNumberValidator.isValid("38520000023237"));
        assertFalse("WebPay does not support Discover", CardNumberValidator.isValid("6011837077978913"));
    }

    @Test
    public void testIsValidChecksCardTypeSpecified() throws Exception {
        assertValidAsType("4242424242424242", VISA);
        assertValidAsType("5105105105105100", MASTERCARD);
        assertValidAsType("3530111333300000", JCB);
        assertValidAsType("378282246310005", AMERICAN_EXPRESS);
        assertValidAsType("38520000023237", DINERS_CLUB);
    }

    private void assertValidAsType(String number, CardType type) {
        assertTrue(CardNumberValidator.isValid(number, Arrays.asList(type)));
        List<CardType> allOther = new LinkedList<CardType>(Arrays.asList(CardType.values()));
        allOther.remove(type);
        assertFalse(CardNumberValidator.isValid(number, allOther));
    }
}
