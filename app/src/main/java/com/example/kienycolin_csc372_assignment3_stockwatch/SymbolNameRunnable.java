package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class SymbolNameRunnable implements Runnable{
    private static final String TAG = "StocksDownloader";
    private final MainActivity mainActivity;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";

    SymbolNameRunnable(MainActivity ma){
        this.mainActivity = ma;
    }

    @Override
    public void run() {
        // making sure that the URL is in standardized coded format.
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                handleResults(null);
                return;
            }

            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(inputStream)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line); // <= .append('\n');  // not needed, but looks prettier for you */
            }
        } catch (Exception e) {
            Log.d(TAG, "run: " + e);
            handleResults(null);
            return;
        }

        handleResults(sb.toString());
    }

    public void handleResults(String s){
        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        final ArrayList<String> symbol_name = parseJSON(s);
        mainActivity.runOnUiThread(() -> mainActivity.updateSymbolNameList(symbol_name) );
    }

    private ArrayList<String> parseJSON(String s) {
        String symbol;
        String companyName;
        ArrayList<String> sybmol_name = new ArrayList<>();
        try{
            JSONArray jArrayMain = new JSONArray(s);

            for (int i = 0; i < jArrayMain.length(); i++) {
                JSONObject jStock = (JSONObject) jArrayMain.get(i);
                symbol = jStock.getString("symbol");
                companyName = jStock.getString("name");

                sybmol_name.add(String.format("%s - %s", symbol, companyName));
            }
            Collections.sort(sybmol_name);
            return sybmol_name;
        } catch (Exception e){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
