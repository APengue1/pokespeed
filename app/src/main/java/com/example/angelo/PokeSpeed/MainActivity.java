package com.example.angelo.PokeSpeed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        StatsFragment.OnFragmentInteractionListener {

    private final MainFragment mainFragment = MainFragment.newInstance();
    private final StatsFragment statsFragment = StatsFragment.newInstance();
    private final FragmentManager mFragment = getSupportFragmentManager();
    static PokeSpeedStats stats = null;
    static Boolean permissionGranted;
    static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!prefs.getBoolean("firstInit", false)) {
            prefs.edit()
                .putBoolean("vibrate", true)
                .putBoolean("imperial", false)
                .putBoolean("speedOverlay", true)
                .putBoolean("firstInit", true)
                .commit();
        }
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, true);
        setContentView(R.layout.activity_main);
        mFragment.beginTransaction().replace(
                R.id.layout_main, mainFragment, mainFragment.getTag()).commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        permissionGranted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            mFragment.beginTransaction().replace(
                    R.id.layout_main, mainFragment, mainFragment.getTag()).commit();
        } else if (id == R.id.nav_stats) {
            mFragment.beginTransaction().replace(
                    R.id.layout_main, statsFragment, statsFragment.getTag()).commit();
        }  else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if(id == R.id.nav_share) {
            shareApp();
        }
        else if(id == R.id.nav_rate) {
            giveRating();
        }
        else if (id == R.id.nav_feedback) {
            sendFeedbackEmail();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Speed for Pokemon GO: speed monitoring for Pokemon Go egg hatchers! "+
                        "Hatch eggs efficiently by staying under the egg hatch speed limit! "  +
                        "Get it on Google Play: " +
                        "https://play.google.com/store/apps/details?id=com.apengue.PokeSpeed");
        shareIntent.setType("text/plain");
        if(shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
        else {
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" +
                                    getApplicationContext().getPackageName())));
        }
    }

    private void giveRating() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        playStoreIntent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        if(playStoreIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(playStoreIntent);
        }
        else {
            startActivity(
                new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                            getApplicationContext().getPackageName())));
        }
    }

    private void sendFeedbackEmail() {
        String[] address = {getResources().getString(R.string.feedbackEmail)};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedbackSubject));
        if(emailIntent.resolveActivity(getPackageManager()) != null)
            startActivity(emailIntent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != -1) {
            permissionGranted = true;
            mainFragment.startSpeedService();
        }
    }

}
