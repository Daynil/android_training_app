package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;


public class DetailActivity extends ActionBarActivity {

    private ShareActionProvider mShareActionProvider;
    public final String EXTRA_KEY = "com.me.DetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        // Set up ShareActionProvider's default share intent
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        mShareActionProvider.setShareIntent(getDefaultIntent());

        return super.onCreateOptionsMenu(menu);
    }

    /**Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent is known
     * or changes, you must update the share intent again by calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        // Prevent the app opened after intent from being reopened by your app's icon after close
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        String message = getIntent().getExtras().getString(Intent.EXTRA_TEXT) + " #SunshineApp";
        intent.putExtra(Intent.EXTRA_TEXT, message);
        return intent;
    }

    public void doShare(Intent shareIntent) {
        mShareActionProvider.setShareIntent(shareIntent);
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String LOG_TAG = "DetailFragment";
        public static final int DETAIL_LOADER = 1;
        String mDate;
        Cursor mDetailWeatherData = null;

        TextView mDateTextView;
        TextView mShortDescView;
        TextView mMaxTempView;
        TextView mMinTempView;

        // For the forecast view we're showing only a small subset of the stored data.
        // Specify the columns we need.
        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
        };


        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        //public static final int COL_WEATHER_ID = 0;
        public static final int COL_WEATHER_DATE = 0;
        public static final int COL_WEATHER_DESC = 1;
        public static final int COL_WEATHER_MAX_TEMP = 2;
        public static final int COL_WEATHER_MIN_TEMP = 3;
        public static final int COL_LOCATION_SETTING = 4;

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mDate = getActivity().getIntent().getExtras().getString(Intent.EXTRA_TEXT);

            mDateTextView = (TextView) rootView.findViewById(R.id.detail_item_date_textview);
            mShortDescView = (TextView) rootView.findViewById(R.id.detail_item_forecast_textview);
            mMaxTempView = (TextView) rootView.findViewById(R.id.detail_item_high_textview);
            mMinTempView = (TextView) rootView.findViewById(R.id.detail_item_low_textview);

            if (mDetailWeatherData != null) {
                refreshView();
            }

            return rootView;
        }

        private void refreshView() {
            if (mDetailWeatherData.moveToFirst()) {
                mDateTextView.setText(Utility.formatDate(mDetailWeatherData.getString(
                        mDetailWeatherData.getColumnIndex(WeatherEntry.COLUMN_DATETEXT))));
                mShortDescView.setText(mDetailWeatherData.getString(
                        mDetailWeatherData.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)));

                boolean isMetric = Utility.isMetric(getActivity());

                mMaxTempView.setText(Utility.formatTemperature(mDetailWeatherData.getDouble(
                        mDetailWeatherData.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric));
                mMinTempView.setText(Utility.formatTemperature(mDetailWeatherData.getDouble(
                        mDetailWeatherData.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric));
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String locationSetting = Utility.getPreferredLocation(getActivity());

            Uri uri =  WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    locationSetting, mDate);

            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

            return new CursorLoader(
                    getActivity(),
                    uri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mDetailWeatherData = data;
            refreshView();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mDetailWeatherData = null;
        }
    }
}
