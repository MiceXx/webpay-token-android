package jp.webpay.android.sample;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public abstract class BaseFragmentActivity extends ActionBarActivity {

    protected static final String WEBPAY_PUBLISHABLE_KEY = "test_public_19DdUs78k2lV8PO8ZCaYX3JT";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.token_create, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_info) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
