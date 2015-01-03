package jp.webpay.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.LinearLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import jp.webpay.android.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// This test is to check that Activities can directly use CardDialogFragment.
// Test logic and other behaviors in WebPayTokenFragmentTest.
@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
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
        CardDialogActivity activity = Robolectric.buildActivity(CardDialogActivity.class).create().visible().start().get();
        CardDialogFragment dialogFragment = (CardDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(CardDialogActivity.FRAGMENT_TAG);
        AlertDialog dialog = (AlertDialog) dialogFragment.getDialog();

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.cardTypeIconList);
        assertEquals(1, layout.getChildCount());
    }

    @Test
    public void testFragmentCallbackOnCancel() throws Exception {
        assertFalse(activity.isCancelled());
        assertTrue(dialogFragment.isAdded());
        assertTrue(dialog.isShowing());
        dialog.getButton(Dialog.BUTTON_NEGATIVE).performClick();
        assertTrue(activity.isCancelled());
        assertFalse(dialog.isShowing());
    }
}
