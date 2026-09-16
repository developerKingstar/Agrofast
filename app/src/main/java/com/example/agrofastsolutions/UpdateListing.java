package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal payload for updating a listing.
 * Only includes fields that sellers can change after creation.
 */
public class UpdateListing {

    @SerializedName("quantity_available")
    private double quantityAvailable;

    @SerializedName("asking_price")
    private double askingPrice;

    @SerializedName("location")
    private String location;

    public UpdateListing(double quantityAvailable, double askingPrice, String location) {
        this.quantityAvailable = quantityAvailable;
        this.askingPrice = askingPrice;
        this.location = location;
    }

    public double getQuantityAvailable() { return quantityAvailable; }
    public double getAskingPrice()       { return askingPrice; }
    public String getLocation()          { return location; }
}