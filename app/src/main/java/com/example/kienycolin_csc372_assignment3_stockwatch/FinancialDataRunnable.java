package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class FinancialDataRunnable implements Runnable{
    private final MainActivity mainActivity;
    private final String symbol;
    private static final String FINANCIAL_DATA_URL = "https://cloud.iexapis.com/stable/stock";

    private static final String yourAPIKey = "pk_665308c976324ff2a3946dbde28c3f82";

    private static final String TAG = "FDrun";

    FinancialDataRunnable(MainActivity ma, String symbol) {
        this.mainActivity = ma;
        this.symbol = symbol;
    }

    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(FINANCIAL_DATA_URL).buildUpon();
        buildURL.appendPath(symbol);
        buildURL.appendPath("quote");
        buildURL.appendQueryParameter("token", yourAPIKey);

        String urlToUse = buildURL.build().toString();

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                handleResults(null);
                return;
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            handleResults(null);
            Log.d(TAG, "run: this https is null bro...");
            return;
        }
        handleResults(sb.toString());
    }

    public void handleResults(final String jsonString){
        final Stock stock = parseJSON(jsonString);
        mainActivity.runOnUiThread(() -> mainActivity.downloadFinancialData(stock));
    }

    private Stock parseJSON(String s){
        // setting prices to 0.00, so they won't be null
        String price = "0.00";
        String priceChange = "0.00";
        String priceChangePercentage = "0.00";

        try {
            JSONObject jObjMain = new JSONObject(s);
            String symbol = jObjMain.getString("symbol");
            String companyName = jObjMain.getString("companyName");
            price = jObjMain.getString("latestPrice");
            priceChange = jObjMain.getString("change");
            priceChangePercentage = jObjMain.getString("changePercent");

            return new Stock(
                    symbol,
                    companyName,
                    Double.parseDouble(price),
                    Double.parseDouble(priceChange),
                    Math.abs(Double.parseDouble(priceChangePercentage))
            );

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
