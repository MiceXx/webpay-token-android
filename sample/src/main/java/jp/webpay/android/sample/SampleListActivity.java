package jp.webpay.android.sample;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SampleListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<ActivityInfo> items = new ArrayList<>();
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            ActivityInfo[] aInfos = pInfo.activities;

            String selfName = getClass().getName();
            for (ActivityInfo aInfo : aInfos) {
                if (aInfo.name.startsWith(pInfo.packageName) && !aInfo.name.equals(selfName)) {
                    items.add(aInfo);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        setListAdapter(new SampleAdapter(this, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ActivityInfo info = (ActivityInfo) l.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, info.name));
        startActivity(intent);
    }

    private static class SampleAdapter extends BaseAdapter {

        private final ArrayList<ActivityInfo> items;
        private final LayoutInflater inflater;

        public SampleAdapter(Context context, ArrayList<ActivityInfo> activities) {
            this.items = activities;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ActivityInfo getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            ActivityInfo item = getItem(position);
            if (!TextUtils.isEmpty(item.nonLocalizedLabel)) {
                tv.setText(item.nonLocalizedLabel);
            } else {
                tv.setText(item.labelRes);
            }
            return tv;
        }
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
}