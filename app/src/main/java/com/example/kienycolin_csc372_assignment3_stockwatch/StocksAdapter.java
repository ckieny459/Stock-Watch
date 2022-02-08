package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class StocksAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private final ArrayList<Stock> stocks;
    private final MainActivity mainActivity;

    StocksAdapter(ArrayList<Stock> stocks, MainActivity ma){
        this.stocks = stocks;
        this.mainActivity = ma;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_entry, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stocks.get(position);

        holder.symbol.setText(stock.getSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.price.setText(String.format(Locale.US, "%.2f", stock.getPrice()));
        holder.priceChange.setText(String.format(Locale.US, "%s%.2f", (
                (stock.getPriceChange() == 0) ? "" : (stock.getPriceChange() > 0) ? "▲" : "▼"),
                stock.getPriceChange()));
        holder.priceChangePercentage.setText(String.format(Locale.US, "(%.2f%%)",stock.getPriceChangePercentage()));

        // change fonts to red/green
        if(Double.parseDouble(holder.priceChange.getText().toString().substring(1)) < 0){
            holder.symbol.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.priceChange.setTextColor(Color.RED);
            holder.priceChangePercentage.setTextColor(Color.RED);
        } else {
            holder.symbol.setTextColor(Color.GREEN);
            holder.companyName.setTextColor(Color.GREEN);
            holder.price.setTextColor(Color.GREEN);
            holder.priceChange.setTextColor(Color.GREEN);
            holder.priceChangePercentage.setTextColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }
}
