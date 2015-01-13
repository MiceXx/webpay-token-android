package jp.webpay.android.token.validator;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpiryValidatorTest {
    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
    private final int currentYear = calendar.get(Calendar.YEAR);
    private final int currentMonth = calendar.get(Calendar.MONTH) + 1;

    @Test
    public void testIsValidChecksRange() throws Exception {
        assertFalse(ExpiryValidator.isValid(0, currentYear + 1));
        assertFalse(ExpiryValidator.isValid(13, currentYear));
        assertFalse(ExpiryValidator.isValid(12, 20));
    }

    @Test
    public void testIsValidAroundNow() throws Exception {
        assertTrue(ExpiryValidator.isValid(currentMonth, currentYear));
        assertFalse(ExpiryValidator.isValid(currentMonth - 1, currentYear));
        assertTrue(ExpiryValidator.isValid(1, currentYear + 1));
    }
}
