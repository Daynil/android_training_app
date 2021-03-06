/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues testValues = createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                columns, // null for all - as here
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        validateCursor(cursor, testValues);
        /* TODO The preceding replaces all the following code
        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
        // Can call validateCursor(cursor, testValues) instead of all following - don't get it?
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);
            } else {
            fail("No values returned");
        }
        */
            // Fantastic.  Now that we have a location, add some weather!

        ContentValues weatherValues = createWeatherValues(locationRowId);

        /**
         * TODO YOUR CODE BELOW HERE FOR QUIZ
         * QUIZ - 4a - InsertReadDbTest
         * https://www.udacity.com/course/viewer#!/c-ud853/l-1639338560/e-1633698604/m-1633698605
         **/
        long weatherRowId;
        weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME,
                null, null, null, null, null, null);

        validateCursor(weatherCursor, weatherValues);
        /* The preceding replaces all following code
        if (weatherCursor.moveToFirst()) {
            int dateTextIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT);
            String dateText = weatherCursor.getString(dateTextIndex);

            int degreesIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES);
            double degrees = weatherCursor.getDouble(degreesIndex);

            int humidityIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            double humidity = weatherCursor.getDouble(humidityIndex);

            int pressureIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            double pressure = weatherCursor.getDouble(pressureIndex);

            int maxTempIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            int maxTemp = weatherCursor.getInt(maxTempIndex);

            int minTempIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            int minTemp = weatherCursor.getInt(minTempIndex);

            int shortDescIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC);
            String shortDesc = weatherCursor.getString(shortDescIndex);

            int windSpeedIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            double windSpeed = weatherCursor.getDouble(windSpeedIndex);

            int weatherIdIndex = weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
            int weatherId = weatherCursor.getInt(weatherIdIndex);

            assertEquals("20141205", dateText);
            assertEquals(1.1, degrees);
            assertEquals(1.2, humidity);
            assertEquals(1.3, pressure);
            assertEquals(75, maxTemp);
            assertEquals(65, minTemp);
            assertEquals("Asteroids", shortDesc);
            assertEquals(5.5, windSpeed);
            assertEquals(321, weatherId);

        } else {
            fail("No values returned");
        }
        */
        dbHelper.close();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    static final String TEST_LOCATION = "99705";
    static final String TEST_DATE = "20141205";

}