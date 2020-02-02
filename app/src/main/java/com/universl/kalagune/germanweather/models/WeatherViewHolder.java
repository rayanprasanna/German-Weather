package com.universl.kalagune.germanweather.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.universl.kalagune.germanweather.R;

public class WeatherViewHolder extends RecyclerView.ViewHolder {
    public TextView itemDate;
    public TextView itemTemperature;
    public TextView itemDescription;
    public TextView itemyWind;
    public TextView itemPressure;
    public TextView itemHumidity;
    public TextView itemIcon;
    public TextView itemPrecipitation;
    public View lineView;

    public WeatherViewHolder(View view) {
        super(view);
        this.itemDate = (TextView) view.findViewById(R.id.itemDate);
        this.itemTemperature = (TextView) view.findViewById(R.id.itemTemperature);
        this.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
        this.itemyWind = (TextView) view.findViewById(R.id.itemWind);
        this.itemPressure = (TextView) view.findViewById(R.id.itemPressure);
        this.itemHumidity = (TextView) view.findViewById(R.id.itemHumidity);
        this.itemPrecipitation=view.findViewById(R.id.itemPrecipitation);
        this.itemIcon = (TextView) view.findViewById(R.id.itemIcon);
        this.lineView = view.findViewById(R.id.lineView);
    }
}