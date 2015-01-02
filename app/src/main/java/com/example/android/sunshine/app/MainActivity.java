package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    public boolean mTwoPane;

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            DetailFragment detFrag = DetailFragment.newInstance(date);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detFrag)
                    .commit();
        } else {
            Intent  intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, date);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600). If this view is present, then the activity should be
            // in two-pane mode
            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                String date = new Date().toString();
                DetailFragment detFrag = DetailFragment.newInstance(date);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detFrag)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.map_location) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String location = PreferenceManager.getDefaultSharedPreferences(this).getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));
            String baseGeo = "geo:0,0?";
            Uri encodeLoc = Uri.parse(baseGeo).buildUpon().appendQueryParameter("q", location).build();
            //Log.v("Main Activity", "encoded Uri: " + encodeLoc);
            intent.setData(encodeLoc);
            // Always check if there is a capable app to launch to avoid a crash if not
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
