package com.radiogapp.app.utils;

import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class UrlParser {

    public static String getUrl(String url) {

        URLConnection conn = getConnection(url);
        if (conn != null) {

            Log.v("INFO", "Requesting: " + url + " Headers: " + conn.getHeaderFields());

            String mContentType = conn.getContentType();
            if (mContentType != null) {
                mContentType = mContentType.toUpperCase();
            }
            if (mContentType != null && mContentType.contains("AUDIO/MPEG")) {
                return url;
            }
        }

        return url;
    }

    private static URLConnection getConnection(String url) {
        URLConnection mUrl;
        try {
            mUrl = new URL(url).openConnection();
            return mUrl;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
