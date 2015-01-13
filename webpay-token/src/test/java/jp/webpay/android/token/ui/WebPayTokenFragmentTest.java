package jp.webpay.android.token.ui;

import android.app.AlertDialog;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.maven.artifact.ant.shaded.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.webpay.android.token.ApiSample;
import jp.webpay.android.token.ErrorResponseException;
import jp.webpay.android.R;
import jp.webpay.android.token.model.ErrorResponse;
import jp.webpay.android.token.ui.field.CvcField;
import jp.webpay.android.token.ui.field.NumberField;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@Config(manifest = "./src/main/AndroidManifestTest.xml", emulateSdk = 18)
@RunWith(RobolectricTestRunnerWithDummyResources.class)
public class WebPayTokenFragmentTest {
    private FragmentContainerActivity activity;
    private CountDownLatch latch;
    private WebPayTokenFragment fragment;
    private Button openDialogButton;
    private CardDialogFragment dialogFragment;
    private AlertDialog dialog;
    private int currentYear;

    @Before
    public void setUp() throws Exception {
        Robolectric.addPendingHttpResponse(ApiSample.availabilityVMResponse);

        activity = Robolectric.buildActivity(FragmentContainerActivity.class).create().visible().start().get();
        latch = new CountDownLatch(1);
        activity.setLatch(latch);
        fragment = (WebPayTokenFragment) activity.getSupportFragmentManager().findFragmentByTag(FragmentContainerActivity.FRAGMENT_TAG);
        openDialogButton = (Button) fragment.getView().findViewById(R.id.open_dialog_button);
        openDialogButton.performClick();
        dialogFragment = (CardDialogFragment)fragment.getChildFragmentManager().findFragmentByTag("card_dialog");
        dialog = (AlertDialog)dialogFragment.getDialog();
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    @Test
    public void testFragmentPlacesPayButtonOnActivity() throws Exception {
        assertNotNull(fragment);
        assertEquals(getString(R.string.token_fragment_open_dialog), openDialogButton.getText());

        // adding stub resource from test is difficult, substitute with indifferent string
        fragment.setOpenButtonTitle(R.string.field_name_hint);
        assertEquals(getString(R.string.field_name_hint), openDialogButton.getText());
    }

    @Test
    public void testFragmentShowsAvailableCardTypesFromApi() throws Exception {
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.cardTypeIconList);
        assertEquals(2, layout.getChildCount());

        HttpRequest request = Robolectric.getSentHttpRequest(0);
        assertEquals("GET", request.getRequestLine().getMethod());
        assertEquals("https://api.webpay.jp/v1/account/availability", request.getRequestLine().getUri());
        assertEquals("Bearer test_public_dummykey", request.getFirstHeader("Authorization").getValue());
    }

    @Test
    public void testFragmentSetCardDialogSendButtonTextAfterReopen() throws Exception {
        fragment.setCardDialogSendButtonTitle(R.string.field_name_hint);
        assertEquals(getString(R.string.card_send), ((Button)dialog.findViewById(R.id.button_submit)).getText());

        dialog.findViewById(R.id.button_cancel).performClick();
        assertFalse(dialog.isShowing());

        openDialogButton.performClick();
        Robolectric.runUiThreadTasks();
        CardDialogFragment cardDialog = (CardDialogFragment) fragment.getChildFragmentManager().findFragmentByTag("card_dialog");
        assertEquals(getString(R.string.field_name_hint),
                ((Button) cardDialog.getDialog().findViewById(R.id.button_submit)).getText());
    }

    @Test
    public void testFragmentApplyDetectedCardBrandToIconAndCvcHelp() throws Exception {
        NumberField numberField = (NumberField) dialog.findViewById(R.id.cardNumberField);
        CvcField cvcField = (CvcField) dialog.findViewById(R.id.cardCvcField);

        assertNull(numberField.getCompoundDrawables()[2]);

        numberField.requestFocus();
        numberField.setText("4242"); // starting of Visa
        numberField.clearFocus();

        BitmapDrawable drawable = (BitmapDrawable)numberField.getCompoundDrawables()[2];
        assertNotNull(drawable);
        assertTrue(shadowOf(drawable.getBitmap()).toString().
                contains("resource:jp.webpay.android:drawable/card_visa"));

        numberField.requestFocus();
        numberField.setText("3782"); // amex
        numberField.clearFocus();
        assertTrue(shadowOf(((BitmapDrawable)numberField.getCompoundDrawables()[2]).getBitmap()).toString().
                contains("resource:jp.webpay.android:drawable/card_amex"));

        // press CVC buttonp
        int [] location = {0,0};
        cvcField.getLocationOnScreen(location);
        cvcField.onTouchEvent(MotionEvent.obtain(1, 1, MotionEvent.ACTION_UP,
                location[0] + cvcField.getWidth() - 10,
                location[1] + cvcField.getHeight() / 2,
                0));

        ShadowAlertDialog helpDialog = shadowOf(ShadowAlertDialog.getLatestAlertDialog());
        drawable = (BitmapDrawable)((ImageView)helpDialog.getView().findViewById(R.id.cvc_help)).getDrawable();
        assertTrue(shadowOf(drawable.getBitmap()).toString().
                contains("resource:jp.webpay.android:drawable/cvc_amex"));
    }

    @Test
    public void testFragmentHandleNotSupportedCardTypeAsInvalid() throws Exception {
        LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.cardTypeIconList);
        assertEquals(2, layout.getChildCount());

        NumberField numberField = (NumberField) dialog.findViewById(R.id.cardNumberField);
        int errorTextColor = activity.getResources().getColor(R.color.error_text);

        numberField.requestFocus();
        numberField.setText("4242424242424242"); // Visa is ok
        numberField.clearFocus();
        assertEquals("4242 4242 4242 4242", numberField.getText().toString());
        assertNotEquals(numberField.getCurrentTextColor(), errorTextColor);

        numberField.requestFocus();
        numberField.setText("378282246310005"); // amex is unacceptable
        numberField.clearFocus();
        assertEquals("3782 822463 10005", numberField.getText().toString());
        assertEquals(numberField.getCurrentTextColor(), errorTextColor);
    }

    @Test
    public void testFragmentNotifiesTokenOnGenerated() throws Exception {
        Robolectric.addPendingHttpResponse(ApiSample.tokenResponse);

        assertTrue(fragment.isAdded());
        assertTrue(dialogFragment.isAdded());
        generateTokenFromForm();

        assertFalse(dialog.isShowing());
        assertEquals(getString(R.string.token_fragment_token_generated), openDialogButton.getText());
        assertEquals("tok_3ybc93ckR01qeKx", activity.getLastToken().id);
    }

    @Test
    public void testFragmentSendsGivenCardData() throws Exception {
        Robolectric.addPendingHttpResponse(ApiSample.tokenResponse);

        generateTokenFromForm();

        String rawCardBodyString = "{\"card\":{" +
                "\"number\":\"4242424242424242\"," +
                "\"cvc\":\"012\"," +
                "\"name\":\"TEST USER\"," +
                "\"exp_month\":7," +
                "\"exp_year\":" + (currentYear + 1) +
                "}}";
        HttpRequest request = Robolectric.getSentHttpRequest(1);
        assertEquals("POST", request.getRequestLine().getMethod());
        assertEquals("https://api.webpay.jp/v1/tokens", request.getRequestLine().getUri());
        assertEquals("application/json", request.getFirstHeader("Content-Type").getValue());
        assertEquals("Bearer test_public_dummykey", request.getFirstHeader("Authorization").getValue());
        assertEquals("en", request.getFirstHeader("Accept-Language").getValue());
        String requestBody = IOUtil.toString(((HttpPost) request).getEntity().getContent(), "UTF-8");
        assertEquals(rawCardBodyString, requestBody);
    }

    @Test
    @Config(qualifiers = "ja-normal-port-hdpi")
    public void testFragmentUsesUILanguage() throws Exception {
        shadowOf(activity.getResources().getConfiguration()).setLocale(new Locale("ja"));
        assertEquals("カードで支払う", openDialogButton.getText());

        Robolectric.addPendingHttpResponse(ApiSample.tokenResponse);
        generateTokenFromForm();

        HttpRequest request = Robolectric.getSentHttpRequest(1);
        assertEquals("https://api.webpay.jp/v1/tokens", request.getRequestLine().getUri());
        assertEquals("ja", request.getFirstHeader("Accept-Language").getValue());
    }

    @Test
    public void testFragmentShowErrorToUser() throws Exception {
        Robolectric.addPendingHttpResponse(ApiSample.cardErrorResponse);

        generateTokenFromForm();

        assertTrue(dialog.isShowing());

        ShadowAlertDialog errorDialog = shadowOf(ShadowAlertDialog.getLatestAlertDialog());
        assertEquals("The security code provided is invalid. " +
                        "For Visa, MasterCard, JCB, and Diners Club, enter the last 3 digits on the back of your card. " +
                        "For American Express, enter the 4 digits printed above your number.",
                errorDialog.getMessage());
    }

    @Test
    public void testFragmentNotifiesLastErrorOnCancel() throws Exception {
        Robolectric.addPendingHttpResponse(ApiSample.cardErrorResponse);

        generateTokenFromForm();
        dialog.findViewById(R.id.button_cancel).performClick();
        assertFalse(dialog.isShowing());

        Throwable throwable = activity.getLastThrowable();
        assertThat(throwable, instanceOf(ErrorResponseException.class));
        ErrorResponse error = ((ErrorResponseException) throwable).getResponse();
        assertEquals(error.statusCode, 402);
        assertEquals(error.causedBy, "buyer");
        assertEquals(error.param, "cvc");
        assertEquals(error.type, "card_error");
        assertEquals(error.code, "invalid_cvc");
    }

    private String getString(int id) {
        return activity.getString(id);
    }

    private void generateTokenFromForm() throws InterruptedException {
        ((EditText) dialog.findViewById(R.id.cardNumberField)).setText("4242424242424242");
        ((EditText) dialog.findViewById(R.id.cardNameField)).setText("TEST USER");
        ((EditText) dialog.findViewById(R.id.cardCvcField)).setText("012");
        ((EditText) dialog.findViewById(R.id.cardExpiryField)).setText("07 / " + (currentYear + 1));

        dialog.findViewById(R.id.button_submit).performClick();
        latch.await(1, TimeUnit.SECONDS);
    }
}
