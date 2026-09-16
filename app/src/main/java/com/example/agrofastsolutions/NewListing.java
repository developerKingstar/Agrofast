package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A minimal payload for creating a listing in Supabase.
 * Only includes columns that actually exist in the 'listings' table.
 */
public class NewListing {

    @SerializedName("seller_id")
    private String sellerId;

    @SerializedName("crop_type")
    private String cropType;

    @SerializedName("quantity_available")
    private double quantityAvailable;

    @SerializedName("unit")
    private String unit;

    @SerializedName("asking_price")
    private double askingPrice;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    public NewListing(String sellerId,
                      String cropType,
                      double quantityAvailable,
                      String unit,
                      double askingPrice,
                      String location,
                      String status) {
        this.sellerId = sellerId;
        this.cropType = cropType;
        this.quantityAvailable = quantityAvailable;
        this.unit = unit;
        this.askingPrice = askingPrice;
        this.location = location;
        this.status = status;
    }

    // Getters (Gson uses these when serializing)
    public String getSellerId()          { return sellerId; }
    public String getCropType()          { return cropType; }
    public double getQuantityAvailable() { return quantityAvailable; }
    public String getUnit()              { return unit; }
    public double getAskingPrice()       { return askingPrice; }
    public String getLocation()          { return location; }
    public String getStatus()            { return status; }
}