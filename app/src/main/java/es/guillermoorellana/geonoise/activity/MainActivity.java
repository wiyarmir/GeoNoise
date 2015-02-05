package es.guillermoorellana.geonoise.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import es.guillermoorellana.geonoise.fragment.MapsFragment;
import es.guillermoorellana.geonoise.fragment.NavigationDrawerFragment;
import es.guillermoorellana.geonoise.fragment.PastSessionFragment;
import es.guillermoorellana.geonoise.fragment.RecordFragment;
import es.guillermoorellana.geonoise.service.RecordService;
import es.guillermoorellana.geonoise.utils.LocationNoiseUpdatesListener;
import es.guillermoorellana.geonoise.R;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, PastSessionFragment.OnFragmentInteractionListener {

    private static final int REQUEST_SOLVE_ERROR = 1001;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private StuffReceiver lnr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        lnr = new StuffReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(RecordService.LOCATION_NOISE_UPDATE);
        registerReceiver(lnr, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(lnr);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if (position == 3) { // Settings special case
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment toInstantiate = null;
            String tag = null;

            String[] section_titles = getResources().getStringArray(R.array.section_titles);
            mTitle = section_titles[position];

            switch (position) {
                case 0:
                    toInstantiate = new MapsFragment();
                    tag = "maps";
                    break;

                case 1:
                    toInstantiate = new RecordFragment();
                    tag = "record";
                    break;

                case 2:
                    toInstantiate = new PastSessionFragment();
                    tag = "pastsession";
                    break;
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.container, toInstantiate, tag)
                    .commit();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("maps");
            if (fragment != null && fragment.isVisible()) {
                getMenuInflater().inflate(R.menu.maps, menu);
            } else {
                getMenuInflater().inflate(R.menu.main, menu);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SOLVE_ERROR:

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

                break;
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    private class StuffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle results = intent.getExtras();
            LocationNoiseUpdatesListener lnul = (LocationNoiseUpdatesListener) getSupportFragmentManager().findFragmentById(R.id.container);
            if (lnul != null) {
                if (lnul.isInterestedInLocationNoiseUpdates()) {
                    Location l = results.getParcelable("Location");
                    double db = results.getDouble("Noise", -1);

                    lnul.onLocationNoiseUpdate(l, db);
                }
            }
        }
    }
}
