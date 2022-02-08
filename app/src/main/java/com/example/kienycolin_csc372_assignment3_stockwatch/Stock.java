package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.util.JsonWriter;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

public class Stock implements Comparable<Stock>, Serializable {
    private final String symbol;
    private final String companyName;
    private final double price;
    private final double priceChange;
    private final double priceChangePercentage;

    Stock (String symbol, String companyName, double price, double priceChange, double priceChangePercentage) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.priceChangePercentage = priceChangePercentage;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName(){
        return companyName;
    }

    public double getPrice(){
        return price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getPriceChangePercentage() {
        return priceChangePercentage;
    }

    @NonNull
    public String toString(){
        try {
            StringWriter sw = new StringWriter();
            JsonWriter jw = new JsonWriter(sw);
            jw.setIndent("  ");
            jw.beginObject();
            jw.name("symbol").value(getSymbol());
            jw.name("companyName").value(getCompanyName());
            jw.name("price").value(getPrice());
            jw.name("priceChange").value(getPriceChange());
            jw.name("priceChangePercentage").value(getPriceChangePercentage());
            jw.endObject();
            jw.close();
            return sw.toString();
        } catch (IOException e) {
            e.getStackTrace();
        }
        return "";
    }

    @Override
    public int compareTo(Stock stock) {

        return symbol.compareTo(stock.symbol);
    }
}
