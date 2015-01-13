package jp.webpay.android.token.sample;

import android.os.Bundle;

public abstract class BaseSampleActivity extends BaseFragmentActivity {

    protected static final String WEBPAY_PUBLISHABLE_KEY = "test_public_19DdUs78k2lV8PO8ZCaYX3JT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
