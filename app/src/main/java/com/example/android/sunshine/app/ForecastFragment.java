package com.example.android.sunshine.app;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.service.SunshineService;

import java.util.Date;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private String mLocation;
    Callback mCallback;
    private int mPosition;
    private ListView mListView;

    private AlarmManager mAlarmManager;
    private PendingIntent alarmIntent;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    public ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement Callback");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("position", 0);
        } else {
            mPosition = 0;
        }

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        Cursor cursor = mForecastAdapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            String date = cursor.getString(COL_WEATHER_DATE);
            mCallback.onItemSelected(date);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                ForecastAdapter adapter = (ForecastAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String date = cursor.getString(COL_WEATHER_DATE);
                    mCallback.onItemSelected(date);
                }
            }
        });

        return rootView;
    }

    private void updateWeather() {

        Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                Utility.getPreferredLocation(getActivity()));
        getActivity().startService(intent);

        /*
        mAlarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        intent1.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
                Utility.getPreferredLocation(getActivity()));
        alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent1, 0);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 10*1000, alarmIntent);
        */

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        mListView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
