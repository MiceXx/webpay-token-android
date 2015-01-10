package jp.webpay.android.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import jp.webpay.android.model.Token;
import jp.webpay.android.ui.WebPayTokenCompleteListener;
import jp.webpay.android.ui.WebPayTokenFragment;


public class TokenCreateActivity extends FragmentActivity implements WebPayTokenCompleteListener {
    public static final String TAG = "TokenCreateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_create);

        WebPayTokenFragment tokenFragment = WebPayTokenFragment.newInstance("test_public_19DdUs78k2lV8PO8ZCaYX3JT");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.webpay_token_button_fragment, tokenFragment)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.token_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTokenCreated(Token token) {
        ((TextView)findViewById(R.id.statusTextView))
                .setText(String.format(getResources().getString(R.string.token_generated), token.id));
    }

    @Override
    public void onCancelled(Throwable lastException) {
        String message = lastException == null ? "(not set)" : lastException.getMessage();
        ((TextView)findViewById(R.id.statusTextView))
                .setText(String.format(getResources().getString(R.string.token_cancelled), message));
    }
}
