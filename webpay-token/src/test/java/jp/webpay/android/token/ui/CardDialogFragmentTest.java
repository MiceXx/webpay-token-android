package jp.webpay.android.token.ui;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.Arrays;

import jp.webpay.android.token.R;
import jp.webpay.android.token.model.CardType;
import jp.webpay.android.token.ui.field.NumberField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


// This test is to check that Activities can directly use CardDialogFragment.
// Test logic and other behaviors in WebPayTokenFragmentTest.
@Config(manifest = "./src/main/AndroidManifestTest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunnerWithDummyResources.class)
public class CardDialogFragmentTest {
    private static final String dummyKey = "test_public_dummykey";

    private CardDialogActivity activity;
    private CardDialogFragment dialogFragment;
    private AlertDialog dialog;

    @Test
    public void testFragmentShowsOneSupportedCardType() throws Exception {
        prepareActivity(CardDialogFragment.newInstance(dummyKey, Arrays.asList(CardType.JCB)));

        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.cardTypeIconList);
        assertEquals(View.VISIBLE, layout.getVisibility());
        assertEquals(1, layout.getChildCount());
    }

    @Test
    public void testFragmentHidesSupportedCardTypesRegionWhenNotGiven() throws Exception {
        prepareActivity(CardDialogFragment.newInstance(dummyKey, null));

        assertEquals(View.GONE, dialog.findViewById(R.id.cardTypeLabel).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.cardTypeIconList).getVisibility());

        NumberField numberField = (NumberField) dialog.findViewById(R.id.cardNumberField);
        numberField.requestFocus();
        numberField.setText("378282246310005");
        numberField.clearFocus();
        assertTrue(numberField.isValid());
    }

    @Test
    public void testSetSendButtonTextBeforeFragmentIsCreated() throws Exception {
        CardDialogFragment fragment = CardDialogFragment.newInstance(dummyKey, CardType.VM());
        // adding stub resource from test is difficult, substitute with indifferent string
        fragment.setSendButtonTitle(R.string.field_name_hint);
        prepareActivity(fragment);

        assertEquals(activity.getString(R.string.field_name_hint), ((Button) dialog.findViewById(R.id.button_submit)).getText().toString());
    }

    @Test
    public void testSetSendButtonTextAfterFragmentIsCreated() throws Exception {
        CardDialogFragment fragment = CardDialogFragment.newInstance(dummyKey, CardType.VM());
        prepareActivity(fragment);

        Button button = (Button) dialog.findViewById(R.id.button_submit);
        assertEquals(activity.getString(R.string.card_send), button.getText().toString());

        fragment.setSendButtonTitle(R.string.field_name_hint);
        assertEquals(activity.getString(R.string.field_name_hint), button.getText().toString());
    }

    @Test
    public void testFragmentCallbackOnCancel() throws Exception {
        prepareActivity(CardDialogFragment.newInstance(dummyKey, CardType.VM()));
        assertFalse(activity.isCancelled());
        assertTrue(dialogFragment.isAdded());
        assertTrue(dialog.isShowing());
        dialog.findViewById(R.id.button_cancel).performClick();
        assertTrue(activity.isCancelled());
        assertFalse(dialog.isShowing());
    }

    private void prepareActivity(CardDialogFragment fragment) {
        ActivityController<CardDialogActivity> activityController = Robolectric.buildActivity(CardDialogActivity.class);
        activityController.get().setFragment(fragment);
        activity = activityController.create().visible().start().get();
        dialogFragment = (CardDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(CardDialogActivity.FRAGMENT_TAG);
        dialog = (AlertDialog) dialogFragment.getDialog();
    }
}
