package jp.webpay.android.validator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NameValidatorTest {

    @Test
    public void testCorrectNameIsValid() throws Exception {
        assertTrue(NameValidator.isValid("KEI KUBO"));
    }

    @Test
    public void testNameWithNewlineIsInvalid() throws Exception {
        assertFalse(NameValidator.isValid("KEI\nKUBO"));
    }

    @Test
    public void testJapaneseNameIsInvalid() throws Exception {
        assertFalse(NameValidator.isValid("久保 渓"));
    }

    @Test
    public void testEmptyIsInvalid() throws Exception {
        assertFalse(NameValidator.isValid(""));
    }

    @Test
    public void testNullIsInvalid() throws Exception {
        assertFalse(NameValidator.isValid(null));
    }
}
