package com.nishay.fuud;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Nishay on 7/24/2016.
 * pass in url string
 * used in AsyncTask to hit the API to return a json string
 * string to be parsed in each fragment
 */
public class APIConnection {

    private String LOG_TAG = new Exception().getStackTrace()[1].getClassName(); ;


    public String getResult(String urlString) throws InterruptedIOException {
        String result;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        Uri.Builder uriBuilder;
        uriBuilder = new Uri.Builder();

        try {

            uriBuilder.encodedPath(urlString);
            uriBuilder.build();

            URL url = new URL(uriBuilder.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = null;
            try {
                inputStream = urlConnection.getInputStream();
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
                return "cannot connect";
            }
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.e(LOG_TAG,"Cannot connect to " + url.toString());
                return "cannot connect";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
            }
            catch (InterruptedIOException e) {
                Log.w(LOG_TAG, "thread killed on purpose, fragment dead, IO interrupted");
                return null;
            }


            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            result = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return result;
    }




}
