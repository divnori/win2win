package com.norihome.smartstarz;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.norihome.smartstarz.backend.smartStarzApi.model.EventBean;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.ClearEventsAsyncTask;
import com.norihome.smartstarz.com.norihome.smartstarz.tasks.StoreEventAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean createGroup = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    //RIGHT ONE
    public void onAddDataButtonClick(View v) {

        System.out.println("AdminActivity :: onAddDataButtonClick");


        TextView dateText = (TextView) findViewById(R.id.editDate);
        TextView detailsText = (TextView) findViewById(R.id.editDescription);
        TextView nameText = (TextView) findViewById(R.id.editName);
        TextView levelText = (TextView) findViewById(R.id.editLevel);

        if(     nameText.getText().length() == 0 ||
                dateText.getText().length() == 0 ||
                detailsText.getText().length() == 0 ||
                levelText.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "Please fill all four fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an EventBean with the information the user entered
        EventBean bean = new EventBean();
        bean.setDate(dateText.getText().toString());
        bean.setName(nameText.getText().toString()+ "-1");
        bean.setDescription(detailsText.getText().toString());
        Long level = Long.valueOf(levelText.getText().toString());

        bean.setLevel(level);

        // If the level is 0 then the event is NOT a competition
        if (level == 0L)
            bean.setName(nameText.getText().toString()+ "-0");


        // Create the Store Events Async Task and execute it
        // This task stores the event information in the Google cloudstore (database)
        StoreEventAsyncTask task = new StoreEventAsyncTask();
        task.execute(bean);

        //clear out text fields after AddData is clicked
        dateText.setText("");
        nameText.setText("");
        detailsText.setText("");
        levelText.setText("");
        Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
    }






    private void setupViewPager(ViewPager viewPager) {
        AdminActivity.ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());


        adapter.addFragment(new GroupsFragment(), "Groups");
        adapter.addFragment(new AddEventsFragment(), "Add Events");
        adapter.addFragment(new AdminFeedFragment(), "Edit Events");
        adapter.addFragment(new ApproveFragment(), "Approve");


        viewPager.setAdapter(adapter);


    }
    public void onClearDataButtonClick (View v) {

        ClearEventsAsyncTask task = new ClearEventsAsyncTask();
        task.execute();
        Toast.makeText(getApplicationContext(), "Data Cleared", Toast.LENGTH_SHORT).show();
        System.out.println("##############Admin Activity Clear Events called######################");
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);

        }
    }
}

