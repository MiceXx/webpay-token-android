package jp.webpay.android.token.validator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CvcValidatorTest {

    @Test
    public void testDigitsLength() throws Exception {
        assertFalse(CvcValidator.isValid("01"));
        assertTrue(CvcValidator.isValid("012"));
        assertTrue(CvcValidator.isValid("0123"));
        assertFalse(CvcValidator.isValid("01234"));
    }

    @Test
    public void testNullIsInvalid() throws Exception {
        assertFalse(CvcValidator.isValid(null));
    }

    @Test
    public void testNonDigitIsInvalid() throws Exception {
        assertFalse(CvcValidator.isValid("0 1"));
        assertFalse(CvcValidator.isValid("01a"));
    }
}
