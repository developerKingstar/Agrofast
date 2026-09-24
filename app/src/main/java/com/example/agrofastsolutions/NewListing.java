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

    /**
     * Listings table uses `photos text[]` — a Postgres text array.
     * We store just one photo per listing for now, wrapped in a 1-element array.
     * Can be null when no photo was uploaded.
     */
    @SerializedName("photos")
    private String[] photos;

    // ===== Original constructor (no photo) — kept for backward compatibility =====
    public NewListing(String sellerId,
                      String cropType,
                      double quantityAvailable,
                      String unit,
                      double askingPrice,
                      String location,
                      String status) {
        this(sellerId, cropType, quantityAvailable, unit,
                askingPrice, location, status, null);
    }

    // ===== New constructor (with optional photo URL) =====
    public NewListing(String sellerId,
                      String cropType,
                      double quantityAvailable,
                      String unit,
                      double askingPrice,
                      String location,
                      String status,
                      String photoUrl) {
        this.sellerId = sellerId;
        this.cropType = cropType;
        this.quantityAvailable = quantityAvailable;
        this.unit = unit;
        this.askingPrice = askingPrice;
        this.location = location;
        this.status = status;
        this.photos = (photoUrl != null && !photoUrl.isEmpty())
                ? new String[]{ photoUrl }
                : null;
    }

    // ===== Getters =====
    public String getSellerId()          { return sellerId; }
    public String getCropType()          { return cropType; }
    public double getQuantityAvailable() { return quantityAvailable; }
    public String getUnit()              { return unit; }
    public double getAskingPrice()       { return askingPrice; }
    public String getLocation()          { return location; }
    public String getStatus()            { return status; }
    public String[] getPhotos()          { return photos; }
}