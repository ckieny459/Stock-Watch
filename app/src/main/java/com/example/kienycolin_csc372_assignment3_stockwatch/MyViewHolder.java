package com.example.kienycolin_csc372_assignment3_stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView symbol;
    TextView companyName;
    TextView price;
    TextView priceChange;
    TextView priceChangePercentage;

    MyViewHolder(View view){ //our layout after inflation
        super(view);
        symbol = view.findViewById(R.id.symbol);
        companyName = view.findViewById(R.id.companyName);
        price = view.findViewById(R.id.price);
        priceChange = view.findViewById(R.id.priceChange);
        priceChangePercentage = view.findViewById(R.id.priceChangePercentage);
    }

}
