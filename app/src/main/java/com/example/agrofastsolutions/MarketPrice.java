package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * One row from the `market_prices` table.
 * Mirrors the schema your friend created.
 */
public class MarketPrice {

    @SerializedName("crop_name")
    private String cropName;

    @SerializedName("region")
    private String region;

    @SerializedName("unit")
    private String unit;

    @SerializedName("price")
    private Double price;

    @SerializedName("price_low")
    private Double priceLow;

    @SerializedName("price_high")
    private Double priceHigh;

    @SerializedName("weekly_change")
    private Double weeklyChange;

    @SerializedName("monthly_change")
    private Double monthlyChange;

    @SerializedName("published_date")
    private String publishedDate;

    public MarketPrice() {}

    // ===== Getters =====
    public String getCropName()       { return cropName; }
    public String getRegion()         { return region; }
    public String getUnit()           { return unit; }
    public Double getPrice()          { return price; }
    public Double getPriceLow()       { return priceLow; }
    public Double getPriceHigh()      { return priceHigh; }
    public Double getWeeklyChange()   { return weeklyChange; }
    public Double getMonthlyChange()  { return monthlyChange; }
    public String getPublishedDate()  { return publishedDate; }

    // ===== Setters (only needed if you ever write back) =====
    public void setCropName(String v)      { cropName = v; }
    public void setRegion(String v)        { region = v; }
    public void setUnit(String v)          { unit = v; }
    public void setPrice(Double v)         { price = v; }
    public void setPriceLow(Double v)      { priceLow = v; }
    public void setPriceHigh(Double v)     { priceHigh = v; }
    public void setWeeklyChange(Double v)  { weeklyChange = v; }
    public void setMonthlyChange(Double v) { monthlyChange = v; }
    public void setPublishedDate(String v) { publishedDate = v; }
}