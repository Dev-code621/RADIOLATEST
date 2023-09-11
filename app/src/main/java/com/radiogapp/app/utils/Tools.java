package com.radiogapp.app.utils;

import android.util.Log;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


import com.google.android.exoplayer2.metadata.Metadata;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Tools {

    private static boolean DISPLAY_DEBUG = true;
    private static ArrayList<EventListener> listeners;

    public Tools() { }

    //Get response from an URL request (GET)
    public static String getDataFromUrl(String url) {
        // Making HTTP request
        Log.v("INFO", "Requesting: " + url);

        StringBuffer chaine = new StringBuffer();
        try {
            URL urlCon = new URL(url);

            //Open a connection
            HttpURLConnection connection = (HttpURLConnection) urlCon
                    .openConnection();
            connection.setRequestProperty("User-Agent", "Your Single Radio");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            //Handle redirecti
            int status = connection.getResponseCode();
            if ((status != HttpURLConnection.HTTP_OK)
                    && (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)) {

                // get redirect url from "location" header field
                String newUrl = connection.getHeaderField("Location");
                // get the cookie if need, for login
                String cookies = connection.getHeaderField("Set-Cookie");

                // open the new connnection again
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", "Your Single Radio");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                System.out.println("Redirect to URL : " + newUrl);
            }

            //Get the stream from the connection and read it
            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }

        return chaine.toString();
    }

    //Get JSON from an url and parse it to a JSON Object.
    public static JSONObject getJSONObjectFromUrl(String url) {
        String data = getDataFromUrl(url);

        try {
            return new JSONObject(data);
        } catch (Exception e) {
            Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
            e.printStackTrace();
        }

        return null;
    }

    public static void noConnection(final Activity context, String message) {

        AlertDialog.Builder ab = new AlertDialog.Builder(context);

//        if (isOnline(context)) {
//            String messageText = "";
//            if (message != null && DISPLAY_DEBUG) {
//                messageText = "\n\n" + message;
//            }
//
//            ab.setMessage(context.getResources().getString(R.string.dialog_connection_description) + messageText);
//            ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
//            ab.setTitle(context.getResources().getString(R.string.dialog_connection_title));
//        } else {
//            ab.setMessage(context.getResources().getString(R.string.dialog_internet_description));
//            ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
//            ab.setTitle(context.getResources().getString(R.string.dialog_internet_title));
//        }

        if (!context.isFinishing()) {
            ab.show();
        }
    }

    public static void noConnection(final Activity context) {
        noConnection(context, null);
    }

    /**
     * Returns if the user has an internet connection
     *
     * @param context the context
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    public static boolean isOnlineShowDialog(Activity c) {
        if (isOnline(c))
            return true;
        else
            noConnection(c);
        return false;
    }

    public static boolean isNetworkActive(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void registerAsListener(EventListener listener) {
        if (listeners == null) listeners = new ArrayList<>();

        listeners.add(listener);
    }

    public static void unregisterAsListener(EventListener listener) {
        listeners.remove(listener);
    }

    public static void onEvent(String status) {
        if (listeners == null) return;

        for (EventListener listener : listeners) {
            listener.onEvent(status);
        }
    }

    public static void onAudioSessionId(Integer id) {
        if (listeners == null) return;

        for (EventListener listener : listeners) {
            listener.onAudioSessionId(id);
        }
    }

    public static void onMetaDataReceived(Metadata meta, Bitmap image) {
        Log.d("TOOLTAG", "meta: "+listeners);
        Log.d("TOOLTAG", "image: "+image);
        for (EventListener listener : listeners) {
            listener.onMetaDataReceived(meta, image);
        }
    }

    public interface EventListener {
        void onEvent(String status);

        void onAudioSessionId(Integer i);

        void onMetaDataReceived(Metadata meta, Bitmap image);
    }

    public long convertToMilliSeconds(String s) {

        long ms = 0;
        Pattern p;
        if (s.contains(("\\:"))) {
            p = Pattern.compile("(\\d+):(\\d+)");
        } else {
            p = Pattern.compile("(\\d+).(\\d+)");
        }
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            ms = (long) h * 60 * 60 * 1000 + min * 60 * 1000;
        }
        return ms;
    }

}
