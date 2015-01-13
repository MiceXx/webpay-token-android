package jp.webpay.android.token.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SampleListActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_list);
    }

    @Override
    protected boolean enableBackOnActionBar() {
        return false;
    }

    public static class SampleListFragment extends ListFragment {

        public SampleListFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setListAdapter(new SampleAdapter(getActivity(), getActivityInfoArray()));
        }

        private ArrayList<ActivityInfo> getActivityInfoArray() {
            ArrayList<ActivityInfo> items = new ArrayList<>();
            FragmentActivity activity = getActivity();

            try {
                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
                        activity.getPackageName(), PackageManager.GET_ACTIVITIES);
                ActivityInfo[] activities = packageInfo.activities;

                String selfName = getActivity().getLocalClassName();
                for (ActivityInfo aInfo : activities) {
                    if (aInfo.name.startsWith(packageInfo.packageName) && !aInfo.name.endsWith(selfName)) {
                        items.add(aInfo);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            ActivityInfo info = (ActivityInfo) l.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(getActivity(), info.name));
            startActivity(intent);
        }
    }

    private static class SampleAdapter extends BaseAdapter {

        private final ArrayList<ActivityInfo> items;
        private final LayoutInflater inflater;

        public SampleAdapter(Context context, ArrayList<ActivityInfo> items) {
            this.items = items;
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
            tv.setText(getItem(position).labelRes);
            return tv;
        }
    }
}
