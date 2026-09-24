package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

public class Listing {

    // ===== Direct columns from the 'listings' table =====
    @SerializedName("listing_id")
    private String listingId;

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

    @SerializedName("harvest_date")
    private String harvestDate;

    @SerializedName("photos")
    private String[] photos;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    // ===== Joined seller object (from users table) =====
    @SerializedName("seller")
    private SellerJoin seller;

    // Empty constructor for Gson
    public Listing() {}

    // ===== Getters =====
    public String getListingId()       { return listingId; }
    public String getSellerId()        { return sellerId; }
    public String getCropType()        { return cropType; }
    public double getQuantityAvailable() { return quantityAvailable; }
    public String getUnit()            { return unit; }
    public double getAskingPrice()     { return askingPrice; }
    public String getLocation()        { return location; }
    public String getHarvestDate()     { return harvestDate; }
    public String[] getPhotos()        { return photos; }
    public String getStatus()          { return status; }
    public String getCreatedAt()       { return createdAt; }

    public String getSellerName() {
        return (seller != null) ? seller.name : "Unknown";
    }

    public String getSellerLocation() {
        return (seller != null) ? seller.location : location;
    }

    /**
     * Convenience: first photo URL, or null if none.
     */
    public String getFirstPhotoUrl() {
        if (photos != null && photos.length > 0) {
            return photos[0];
        }
        return null;
    }

    // ===== Setters (used when creating new listings later) =====
    public void setListingId(String x)          { listingId = x; }
    public void setSellerId(String x)           { sellerId = x; }
    public void setCropType(String x)           { cropType = x; }
    public void setQuantityAvailable(double x)  { quantityAvailable = x; }
    public void setUnit(String x)               { unit = x; }
    public void setAskingPrice(double x)        { askingPrice = x; }
    public void setLocation(String x)           { location = x; }
    public void setHarvestDate(String x)        { harvestDate = x; }
    public void setPhotos(String[] x)           { photos = x; }
    public void setStatus(String x)             { status = x; }

    // ===== Nested seller class (matches the joined JSON) =====
    public static class SellerJoin {
        @SerializedName("name")
        public String name;

        @SerializedName("location")
        public String location;
    }
}