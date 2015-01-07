package jp.webpay.android.ui;

import android.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import jp.webpay.android.R;
import jp.webpay.android.ui.field.NumberField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


// This test is to check that Activities can directly use CardDialogFragment.
// Test logic and other behaviors in WebPayTokenFragmentTest.
@Config(manifest = "./src/main/AndroidManifestTest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CardDialogFragmentTest {
    private CardDialogActivity activity;
    private CardDialogFragment dialogFragment;
    private AlertDialog dialog;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(CardDialogActivity.class).create().visible().start().get();
        dialogFragment = (CardDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(CardDialogActivity.FRAGMENT_TAG);
        dialog = (AlertDialog) dialogFragment.getDialog();
    }

    @Test
    public void testFragmentShowsOneSupportedCardType() throws Exception {
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.cardTypeIconList);
        assertEquals(View.VISIBLE, layout.getVisibility());
        assertEquals(1, layout.getChildCount());
    }

    @Test
    public void testFragmentHidesSupportedCardTypesRegionWhenNotGiven() throws Exception {
        ActivityController<CardDialogActivity> noCardActivity = Robolectric.buildActivity(CardDialogActivity.class);
        noCardActivity.get().setSupportedCardTypes(null);
        CardDialogActivity activity = noCardActivity.create().visible().start().get();
        CardDialogFragment dialogFragment = (CardDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(CardDialogActivity.FRAGMENT_TAG);
        AlertDialog dialog = (AlertDialog) dialogFragment.getDialog();

        assertEquals(View.GONE, dialog.findViewById(R.id.cardTypeLabel).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.cardTypeIconList).getVisibility());

        NumberField numberField = (NumberField) dialog.findViewById(R.id.cardNumberField);
        numberField.requestFocus();
        numberField.setText("378282246310005");
        numberField.clearFocus();
        assertNotEquals(numberField.getCurrentTextColor(), activity.getResources().getColor(R.color.error_text));
    }

    @Test
    public void testFragmentCallbackOnCancel() throws Exception {
        assertFalse(activity.isCancelled());
        assertTrue(dialogFragment.isAdded());
        assertTrue(dialog.isShowing());
        dialog.findViewById(R.id.button_cancel).performClick();
        assertTrue(activity.isCancelled());
        assertFalse(dialog.isShowing());
    }
}
