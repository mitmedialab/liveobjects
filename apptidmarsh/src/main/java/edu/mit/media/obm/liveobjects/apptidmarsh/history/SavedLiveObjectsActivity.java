package edu.mit.media.obm.liveobjects.apptidmarsh.history;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.Locale;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import edu.mit.media.obm.liveobjects.apptidmarsh.slidingtabs.SlidingTabLayout;
import edu.mit.media.obm.liveobjects.apptidmarsh.utils.EmailFormatter;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.BitmapEditor;
import edu.mit.media.obm.liveobjects.apptidmarsh.widget.MenuActions;
import edu.mit.media.obm.shair.liveobjects.R;

public class SavedLiveObjectsActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @Bind(R.id.viewpager) ViewPager mViewPager;
    @Bind(R.id.sliding_tabs) SlidingTabLayout mSlidingTabLayout;
    @Bind(R.id.root_layout) LinearLayout mRootLayout;

    @BindString(R.string.title_history_tab) String mTitleHistoryTab;
    @BindString(R.string.title_favourite_tab) String mTitleFavouriteTab;

    private static final int NUMBER_OF_TABS = 2;
    public static final int HISTORY_TAB_ID = 0;
    public static final int FAVOURITE_TAB_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_live_objects);
        ButterKnife.bind(this);

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Give the SlidingTabLayout the ViewPager
        // Center the tabs in the layout
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        setBackgroundImage();
    }

    private void setBackgroundImage() {
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.main_background);
        BitmapEditor bitmapEditor = new BitmapEditor(this);
        background = bitmapEditor.cropToDisplayAspectRatio(background, getWindowManager());
        bitmapEditor.blurBitmap(background, 2);

        BitmapDrawable drawableBackground = new BitmapDrawable(getResources(), background);
        mRootLayout.setBackgroundDrawable(drawableBackground);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_goto_home) {
            MenuActions.goToHome(this);
            return true;
        } else if (id == R.id.action_email) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            String subject = EmailFormatter.getSubject();
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            String body = EmailFormatter.getBody(this);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(emailIntent, "Email:"));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return SavedLiveObjectsFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case HISTORY_TAB_ID:
                    return mTitleHistoryTab.toUpperCase(l);
                case FAVOURITE_TAB_ID:
                    return mTitleFavouriteTab.toUpperCase(l);
            }
            return null;
        }
    }


}
