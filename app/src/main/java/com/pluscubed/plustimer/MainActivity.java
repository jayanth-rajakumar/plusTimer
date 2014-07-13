package com.pluscubed.plustimer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

/**
 * Main Activity
 */
public class MainActivity extends ActionBarActivity implements SolveDialog.SolveDialogListener, CurrentSBaseFragment.OnSolveItemClickListener, CurrentSTimerFragment.GetRetainedFragmentCallback {
    public static final String DIALOG_FRAGMENT_TAG = "MODIFY_DIALOG";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String CURRENT_S_TAG = "CURRENT_S_FRAGMENT";
    private static final String CURRENT_S_TIMER_RETAINED_TAG = "CURRENT_S_TIMER_RETAINED";
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private String[] mFragmentTitles;
    private String[] mFragmentActionBarTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private CharSequence mCurrentFragmentTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private int mCurrentSelectedPosition = 0;

    @Override
    public void onDialogDismissed(int position, int penalty) {
        Solve solve = PuzzleType.sCurrentPuzzleType.getSession().getSolveByPosition(position);
        switch (penalty) {
            case SolveDialog.DIALOG_PENALTY_NONE:
                solve.setPenalty(Solve.Penalty.NONE);
                break;
            case SolveDialog.DIALOG_PENALTY_PLUSTWO:
                solve.setPenalty(Solve.Penalty.PLUSTWO);
                break;
            case SolveDialog.DIALOG_PENALTY_DNF:
                solve.setPenalty(Solve.Penalty.DNF);
                break;
            case SolveDialog.DIALOG_RESULT_DELETE:
                PuzzleType.sCurrentPuzzleType.getSession().deleteSolve(position);
                break;
        }
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_S_TAG) != null) {
            ((CurrentSFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_S_TAG)).updateSessionsToCurrent();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mCurrentFragmentTitle = title;
        getSupportActionBar().setTitle(mCurrentFragmentTitle);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
        if (menu.findItem(R.id.menu_current_s_puzzletype_spinner) != null)
            menu.findItem(R.id.menu_current_s_puzzletype_spinner).setVisible(!drawerOpen);
        if (menu.findItem(R.id.menu_current_s_toggle_scramble_image_action) != null) {
            menu.findItem(R.id.menu_current_s_toggle_scramble_image_action).setVisible(!drawerOpen);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.USE_CRASHLYTICS)
            Crashlytics.start(this);

        FragmentManager fm = getSupportFragmentManager();
        Fragment currentSRetainedFragment = fm.findFragmentByTag(CURRENT_S_TIMER_RETAINED_TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (currentSRetainedFragment == null) {
            currentSRetainedFragment = new CurrentSTimerRetainedFragment();
            fm.beginTransaction().add(currentSRetainedFragment, CURRENT_S_TIMER_RETAINED_TAG).commit();
        }

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        mUserLearnedDrawer = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_USER_LEARNED_DRAWER, false);

        mFragmentTitles = getResources().getStringArray(R.array.drawer_array);
        mFragmentActionBarTitles = getResources().getStringArray(R.array.drawer_actionbar_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawerlayout);
        mDrawerListView = (ListView) findViewById(R.id.activity_main_drawer_listview);
        mDrawerTitle = getResources().getString(R.string.app_name);

        mDrawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item_drawer, mFragmentTitles));
        mDrawerListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mCurrentFragmentTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        selectItem(mCurrentSelectedPosition);

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mDrawerListView);
            getSupportActionBar().setTitle(mDrawerTitle);
        }

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    void selectItem(int pos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = "";
        Class fragmentClass = null;
        switch (pos) {
            case 0:
                tag = CURRENT_S_TAG;
                fragmentClass = CurrentSFragment.class;
                break;
            default:
                Toast.makeText(getApplicationContext(), "Work in Progress", Toast.LENGTH_SHORT).show();
                return;
        }
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            if (fragmentClass != null)
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            fragmentManager.beginTransaction()
                    .replace(R.id.activity_main_content_framelayout, fragment, tag)
                    .commit();
        }
        mCurrentSelectedPosition = pos;
        mDrawerListView.setItemChecked(pos, true);
        setTitle(mFragmentActionBarTitles[pos]);
        mDrawerLayout.closeDrawer(mDrawerListView);
    }

    @Override
    public void showCurrentSolveDialog(int position) {
        DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (dialog == null) {
            SolveDialog d = SolveDialog.newInstance(position);
            d.show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
        }
    }

    @Override
    public Fragment getCurrentSTimerRetainedFragment() {
        return getSupportFragmentManager().findFragmentByTag(CURRENT_S_TIMER_RETAINED_TAG);
    }
}