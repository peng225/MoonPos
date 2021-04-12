package com.example.moonpos2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.Calendar;

public class MainActivity extends FragmentActivity implements DownloadCallback {
    private NetworkFragment networkFragment;
    private boolean downloading = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String dateTimeString = getCurrentTimeString();
        String latLonString = "lat=35.35&lon=139.45";
        //String url = "https://mgpn.org/api/moon/v2position.cgi?time=" + dateTimeString + "&" + latLonString;
        String url = "https://mgpn.org/api/moon/v2position.cgi?" + latLonString;
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), url);
    }

    private String getCurrentTimeString() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return "" + year + "-" + month + "-" + day + "T" + hour + ":" + minute;
    }

    public void startDownload(View view) {
        if(!downloading && networkFragment != null) {
            networkFragment.startDownload();
            downloading = true;
        }
    }

    @Override
    public void updateFromDownload(String result) {
        TextView textView = (TextView) findViewById(R.id.moonPosText);
        String printText = "Failed to get moon position information.";
        try {
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.getInt("status") != 200) {
                printText = "Invalid status: " + jsonObject.getString("status");
            } else {
                String wholeResultString = jsonObject.getString("result");
                JSONArray jsonArray = new JSONArray(wholeResultString);
                jsonObject = jsonArray.getJSONObject(0);
                String time = jsonObject.getString("time");
                String eclipticLongitutude = jsonObject.getString("ecliptic_longitude");
                String eclipticLatitutude = jsonObject.getString("ecliptic_latitude");
                String altitude = jsonObject.getString("altitude");
                String azimuth = jsonObject.getString("azimuth");
                String age = jsonObject.getString("age");
                printText = "日時 (time): " + time;
                printText += "\n黄緯 (ecliptic_latitude): " + eclipticLatitutude;
                printText += "\n黄経 (ecliptic_longitude): " + eclipticLongitutude;
                printText += "\n仰角 (altitude): " + altitude;
                printText += "\n方位角 (azimuth): " + azimuth;
                printText += " (" + getAzimuthText(Double.parseDouble(azimuth)) + ")";
                printText += "\n月齢 (age): " + age;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            printText = "Exception";
        }
        textView.setText(printText);
    }

    private String getAzimuthText(double azimuth) {
        assert(0 <= azimuth && azimuth < 360);
        String azimuthTextArray[] = {"北", "北北東", "北東", "東北東",
                                     "東", "東南東", "南東", "南南東",
                                     "南", "南南西", "南西", "西南西",
                                     "西", "西北西", "北西", "北北西"};

        return azimuthTextArray[(int)((azimuth + 11.25) / 22.5) % 16];

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishDownloading() {
        downloading = false;
        if (networkFragment != null) {
            networkFragment.cancelDownload();
        }
    }
}