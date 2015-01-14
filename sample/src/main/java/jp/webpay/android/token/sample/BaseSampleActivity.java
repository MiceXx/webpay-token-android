package jp.webpay.android.token.sample;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

public abstract class BaseSampleActivity extends BaseFragmentActivity {

    protected static final String WEBPAY_PUBLISHABLE_KEY = "test_public_19DdUs78k2lV8PO8ZCaYX3JT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void showMessage(int stringResourceId) {
        Toast toast = Toast.makeText(this, stringResourceId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
