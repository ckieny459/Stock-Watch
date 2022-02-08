package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {
    /* <uses-permission android:name="android.permission.INTERNET"> */

    private final ArrayList<Stock> stocksList = new ArrayList<>(); // ArrayList of Stocks on screen
    private final ArrayList<String> symbol_name = new ArrayList<>(); // arrayList of "symbol-name" strings
    private static final String marketWatchURL = "http://www.marketwatch.com/investing/stock"; // used to go to web browswer
    private RecyclerView recyclerView;
    private StocksAdapter stocksAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Stock Watch");

        // Swipe refresh layout here.
        swipeRefreshLayout = findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        recyclerView = findViewById(R.id.recycler);
        // Data to recycler view adapter
        stocksAdapter = new StocksAdapter(stocksList, this);
        recyclerView.setAdapter(stocksAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load JSON contents into notes
        stocksList.clear();
        stocksList.addAll(loadFile());


        // check if you're connected to the internet
        if (!internetConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Display Information Without A Network Connection");

            AlertDialog dialog = builder.create();
            dialog.show();
        }



        // get symbol_name arrayList filled.
        SymbolNameRunnable symbolNameRunnable = new SymbolNameRunnable(this);
        new Thread(symbolNameRunnable).start();

        // update all stocks upon opening the app
        for (int i = 0; i < stocksList.size(); i++){
            FinancialDataRunnable financialDataRunnable= new FinancialDataRunnable(this, stocksList.get(i).getSymbol());
            new Thread(financialDataRunnable).start();
        }
    }

    private void refresh(){
        // this is called on a swipe down refresh

        if (internetConnected()) {
            for (int i = 0; i < stocksList.size(); i++) {
                String symbol = stocksList.get(i).getSymbol();
                FinancialDataRunnable financialDataRunnable = new FinancialDataRunnable(this, symbol);
                new Thread(financialDataRunnable).start();
            }
            Toast.makeText(this, "Stocks Refreshed", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        // stop swipe refresh layout refreshing
        swipeRefreshLayout.setRefreshing(false); // stops refreshing
    }

    public void downloadFinancialData(Stock stock){
        if (stock == null){
            Toast.makeText(this, "no data available", Toast.LENGTH_SHORT).show();
            return;
        }
        String symbol = stock.getSymbol();

        // this for loop is specifically to refresh items in stocksList
        for (int i = 0; i < stocksList.size(); i++){
            if (symbol.equals(stocksList.get(i).getSymbol())){
                stocksList.remove(i);
                stocksAdapter.notifyItemRemoved(i);
                stocksList.add(i, stock);
                stocksAdapter.notifyItemInserted(i);
                saveNotes();
                return;
            }
        }

        // if a new stock is being added...
        stocksList.add(stock);
        Collections.sort(stocksList);
        stocksAdapter.notifyItemRangeChanged(0,stocksList.size());
        saveNotes();
    }

    public void downloadFailed() {

        // stop swipe refresh layout refreshing
        swipeRefreshLayout.setRefreshing(false);
    }

    public void updateSymbolNameList(ArrayList<String> SNList){
        symbol_name.clear();
        symbol_name.addAll(SNList);
    }

    @Override
    public void onClick(View v) {
        // Open MarketWatch in web browser


        // get the symbol of the stock that was clicked.
        int pos = recyclerView.getChildLayoutPosition(v);
        String sym = stocksList.get(pos).getSymbol();

        // build the URL based on the symbol.
        Uri.Builder buildURL = Uri.parse(marketWatchURL).buildUpon();
        buildURL.appendPath(sym);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(buildURL.build());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stocksList.get(pos);

        //making confirmation dialog to ask user if they want to delete the note
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("YES", (dialog, id) -> {
            stocksList.remove(pos);
            stocksAdapter.notifyItemRemoved(pos);
            saveNotes();
        });
        builder.setNegativeButton("NO", (dialog, id) -> dialog.dismiss());

        builder.setTitle("Delete stock '" + s.getSymbol() + "'?");

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    // Menu creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Menu items and actions when clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Checking if a menu button was clicked.
        if (item.getItemId() == R.id.addMenuItem) {
            // check if connected to internet
            if (internetConnected()) {
                symbolEntryDialog();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("No Network Connection");
                builder.setMessage("Stocks Cannot Be Added Without A Network Connection");

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean stockExists(String symbol){
        for (int i = 0; i < stocksList.size(); i++){
            if (symbol.equals(stocksList.get(i).getSymbol())){
                return true;
            }
        }
        return false;
    }

    public void symbolEntryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // create an edit text for this builder's view
        final EditText etSymbol = new EditText(this);
        etSymbol.setInputType(InputType.TYPE_CLASS_TEXT);
        etSymbol.setFilters(new InputFilter[]{new InputFilter.AllCaps()}); // force ALL CAPS
        etSymbol.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(etSymbol);

        builder.setPositiveButton("Ok", (dialog, id) -> {
            if (etSymbol.getText().toString().isEmpty()) {
                dialog.dismiss();
            } else {
                chooseStock(etSymbol.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stockAlreadyDisplayedDialog(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Stock");
        builder.setIcon(R.drawable.alert_icon);
        builder.setMessage(String.format("Stock symbol %s is already displayed", symbol));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stockDoesntExistDialog(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format("Symbol not found: %s", symbol));
        builder.setMessage("");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean internetConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null){
            Toast.makeText(this, "Connectivity Manager is null...", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected;
        if (activeNetwork == null){
            isConnected = false;
            for (int i = 0; i < stocksList.size(); i++) {
                String symbol = stocksList.get(i).getSymbol();
                String companyName = stocksList.get(i).getCompanyName();
                stocksList.remove(i);
                stocksList.add(i, new Stock(symbol, companyName, 0.00, 0.00, 0.00));
                stocksAdapter.notifyItemChanged(i);
            }
        } else {
            isConnected = activeNetwork.isConnectedOrConnecting();
        }

        return isConnected;
    }

    public void chooseStock(String symbol){
        // this is called when user searches for a stock.

        // create an arrayList to add stocks that contain the letters given by user
        final ArrayList<String> matchingStocks = new ArrayList<>();
        for (int i = 0; i < symbol_name.size(); i++){
            String s_n = symbol_name.get(i);
            if (s_n.contains(symbol)){
                matchingStocks.add(s_n);
            }
        }

        // create a CharSequence[] and copy the items in the above arrayList because I don't know a better way.
        final CharSequence[] matStoArray = new CharSequence[matchingStocks.size()];
        for (int i = 0; i < matStoArray.length; i++){
            matStoArray[i] = matchingStocks.get(i);
        }

        /* if their is no results, open error dialog */
        if (matStoArray.length == 0){
            stockDoesntExistDialog(symbol);
        }

        /* if their is only one result, download it without user selection, else open dialog box
           for user to choose from a list of options. */
        else if (matStoArray.length == 1) {
            String clickedSymbol = matStoArray[0].toString().split(" - ")[0];
            // check if stock is already displayed
            if (stockExists(clickedSymbol)){
                stockAlreadyDisplayedDialog(clickedSymbol);
            } else {
                FinancialDataRunnable financialDataRunnable = new FinancialDataRunnable(
                        this,
                        matStoArray[0].toString().split(" - ")[0] // the symbol the user clicked
                );
                new Thread(financialDataRunnable).start();
            }
        } else {
            // build the dialog box for user to choose which stock to add.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make a selection");

            // Put the CharSequence in the dialog box.
            // Download the data of the stock the user clicked.
            builder.setItems(matStoArray, (dialog, which) -> {
                String clickedSymbol = matStoArray[which].toString().split(" - ")[0];
                if (stockExists(clickedSymbol)){
                    stockAlreadyDisplayedDialog(clickedSymbol);
                    dialog.dismiss();
                } else {
                    FinancialDataRunnable financialDataRunnable = new FinancialDataRunnable(
                            this,
                            matStoArray[which].toString().split(" - ")[0] // the symbol the user clicked
                    );
                    new Thread(financialDataRunnable).start();
                }
            });

            builder.setNegativeButton("Nevermind", (dialog, id) ->
                    Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show());

            // Show dialog box
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    // For JSON file loading. Creates your stocksList arraylist from the JSON file.
    public ArrayList<Stock> loadFile(){

        ArrayList<Stock> stocksList = new ArrayList<>();
        try {
            InputStream inputStream = getApplicationContext().openFileInput("Stocks.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String symbol = jsonObject.getString("symbol");
                String companyName = jsonObject.getString("companyName");
                double price = Double.parseDouble(jsonObject.getString("price"));
                double priceChange = Double.parseDouble(jsonObject.getString("priceChange"));
                double priceChangePercentage = Double.parseDouble(jsonObject.getString("priceChangePercentage"));
                Stock stock = new Stock(symbol, companyName, price, priceChange, priceChangePercentage);
                stocksList.add(stock);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stocksList;
    }

    private void saveNotes(){
        // Saves the items in stocksList to Stock.json
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("Stocks.json", Context.MODE_PRIVATE);

            PrintWriter printWriter = new PrintWriter(fos);
            printWriter.print(stocksList);
            printWriter.close();
        } catch (Exception e){
            e.getStackTrace();
        }
    }
}