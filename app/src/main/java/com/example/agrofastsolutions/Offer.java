package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

public class Offer {

    // ===== Direct columns =====
    @SerializedName("offer_id")
    private String offerId;

    @SerializedName("listing_id")
    private String listingId;

    @SerializedName("buyer_id")
    private String buyerId;

    @SerializedName("seller_id")
    private String sellerId;

    @SerializedName("quantity_requested")
    private int quantity;

    @SerializedName("offered_price")
    private int price;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("responded_at")
    private String respondedAt;

    // ===== Joined objects (filled by Supabase when we use select=*,...) =====
    @SerializedName("listing")
    private ListingJoin listing;

    @SerializedName("buyer")
    private UserJoin buyer;

    @SerializedName("seller")
    private UserJoin seller;

    // ===== Client-side only (not from DB) =====
    private String counterpartyName;
    private String currency = "TSh";
    private String note;

    // Empty constructor needed by Gson
    public Offer() {}

    // Sample constructor (used in tests/fragment sample data)
    public Offer(String cropType, String counterpartyName, int quantity,
                 int price, String currency, String status, String note) {
        this.listing = new ListingJoin();
        this.listing.cropType = cropType;
        this.counterpartyName = counterpartyName;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.note = note;
    }

    // ===== Getters =====
    public String getOfferId()     { return offerId; }
    public String getListingId()   { return listingId; }
    public String getBuyerId()     { return buyerId; }
    public String getSellerId()    { return sellerId; }
    public int    getQuantity()    { return quantity; }
    public int    getPrice()       { return price; }
    public String getCurrency()    { return currency; }
    public String getStatus()      { return status; }
    public String getCreatedAt()   { return createdAt; }
    public String getRespondedAt() { return respondedAt; }
    public String getNote()        { return note; }

    // Crop type — pulled from nested listing
    public String getCropType() {
        if (listing != null && listing.cropType != null) return listing.cropType;
        return "Crop";
    }

    // Buyer name — pulled from nested buyer
    public String getBuyerUserId()  { return (buyer  != null) ? buyer.userId  : null; }
    public String getSellerUserId() { return (seller != null) ? seller.userId : null; }
    public String getBuyerName() {
        return (buyer != null) ? buyer.name : null;
    }

    // Seller name — pulled from nested seller
    public String getSellerName() {
        return (seller != null) ? seller.name : null;
    }

    // Used by adapter — shows whichever name is relevant
    public String getCounterpartyName() {
        if (counterpartyName != null) return counterpartyName;
        if (buyer != null && buyer.name != null) return buyer.name;
        if (seller != null && seller.name != null) return seller.name;
        return "Unknown";
    }

    public String getListingFirstPhotoUrl() {
        if (listing != null && listing.photos != null && listing.photos.length > 0) {
            return listing.photos[0];
        }
        return null;
    }

    // ===== Setters =====
    public void setOfferId(String v)          { offerId = v; }
    public void setListingId(String v)        { listingId = v; }
    public void setBuyerId(String v)          { buyerId = v; }
    public void setSellerId(String v)         { sellerId = v; }
    public void setQuantity(int v)            { quantity = v; }
    public void setPrice(int v)               { price = v; }
    public void setCurrency(String v)         { currency = v; }
    public void setStatus(String v)           { status = v; }
    public void setNote(String v)             { note = v; }
    public void setCounterpartyName(String v) { counterpartyName = v; }

    // ===== Nested classes matching the joined JSON =====
    public static class ListingJoin {
        @SerializedName("crop_type")
        public String cropType;

        @SerializedName("photos")
        public String[] photos;
    }


    public static class UserJoin {
        @SerializedName("user_id")
        public String userId;

        @SerializedName("name")
        public String name;

        @SerializedName("location")
        public String location;
    }
}