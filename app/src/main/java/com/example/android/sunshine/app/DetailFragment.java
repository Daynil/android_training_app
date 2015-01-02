package com.example.android.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = "DetailFragment";
    public static final int DETAIL_LOADER = 1;
    String mDate;
    Cursor mDetailWeatherData = null;

    TextView mDateTextView;
    TextView mShortDescView;
    TextView mMaxTempView;
    TextView mMinTempView;
    ImageView mIconView;
    TextView mHumidityView;
    TextView mWindView;
    TextView mPressureView;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    //public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 0;
    public static final int COL_WEATHER_DESC = 1;
    public static final int COL_WEATHER_MAX_TEMP = 2;
    public static final int COL_WEATHER_MIN_TEMP = 3;
    public static final int COL_LOCATION_SETTING = 4;

    public DetailFragment() {
    }

    public static DetailFragment newInstance(String date) {
        DetailFragment f = new DetailFragment();

        Bundle args = new Bundle();
        args.putString("clicked_date", date);
        f.setArguments(args);

        return f;
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
        mDate = getArguments().getString("clicked_date");

        mDateTextView = (TextView) rootView.findViewById(R.id.detail_item_date_textview);
        mShortDescView = (TextView) rootView.findViewById(R.id.detail_item_forecast_textview);
        mMaxTempView = (TextView) rootView.findViewById(R.id.detail_item_high_textview);
        mMinTempView = (TextView) rootView.findViewById(R.id.detail_item_low_textview);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_item_icon);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_item_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_item_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_item_pressure_textview);


        if (mDetailWeatherData != null) {
            refreshView();
        }

        return rootView;
    }

    private void refreshView() {
        if (mDetailWeatherData.moveToFirst()) {
            mDateTextView.setText(Utility.formatDate(mDetailWeatherData.getString(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));
            mShortDescView.setText(mDetailWeatherData.getString(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)));

            boolean isMetric = Utility.isMetric(getActivity());

            mMaxTempView.setText(Utility.formatTemperature(getActivity(),
                    mDetailWeatherData.getDouble(
                            mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric));
            mMinTempView.setText(Utility.formatTemperature(getActivity(),
                    mDetailWeatherData.getDouble(
                            mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric));

            int weatherIconRes = Utility.getArtResourceForWeatherCondition(
                    mDetailWeatherData.getInt(mDetailWeatherData.getColumnIndex(
                            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID)));
            mIconView.setImageResource(weatherIconRes);
            int humidity = mDetailWeatherData.getInt(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            mHumidityView.setText("Humidity: " + humidity + "%");

            float windSpeed = mDetailWeatherData.getFloat(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDirection = mDetailWeatherData.getFloat(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));

            double pressure = mDetailWeatherData.getDouble(
                    mDetailWeatherData.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
            mPressureView.setText("Pressure: " + pressure + " hPa");
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