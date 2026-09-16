package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

public class Order {

    // ===== Direct columns =====
    @SerializedName("order_id")
    private String orderId;

    @SerializedName("offer_id")
    private String offerId;

    @SerializedName("buyer_id")
    private String buyerId;

    @SerializedName("seller_id")
    private String sellerId;

    @SerializedName("listing_id")
    private String listingId;

    @SerializedName("final_quantity")
    private int quantity;

    @SerializedName("final_price")
    private int price;

    @SerializedName("accepted_at")
    private String acceptedAt;

    @SerializedName("status")
    private String status;

    @SerializedName("decline_reason")
    private String declineReason;

    @SerializedName("decline_fee_amount")
    private double declineFeeAmount;

    @SerializedName("decline_fee_status")
    private String declineFeeStatus;

    // ===== Joined objects =====
    @SerializedName("listing")
    private ListingJoin listing;

    @SerializedName("buyer")
    private UserJoin buyer;

    @SerializedName("seller")
    private UserJoin seller;

    // ===== Client-side only =====
    private String currency = "TSh";
    private String note;

    public Order() {}

    // Sample constructor (used in fragments/tests)
    public Order(String cropType, String buyerName, String sellerName,
                 int quantity, int price, String currency,
                 String status, String note) {
        this.listing = new ListingJoin();
        this.listing.cropType = cropType;
        this.buyer = new UserJoin();
        this.buyer.name = buyerName;
        this.seller = new UserJoin();
        this.seller.name = sellerName;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.note = note;
    }

    // ===== Getters =====
    public String getOrderId()          { return orderId; }
    public String getOfferId()          { return offerId; }
    public String getBuyerId()          { return buyerId; }
    public String getSellerId()         { return sellerId; }
    public String getListingId()        { return listingId; }
    public int    getQuantity()         { return quantity; }
    public int    getPrice()            { return price; }
    public String getCurrency()         { return currency; }
    public String getStatus()           { return status; }
    public String getAcceptedAt()       { return acceptedAt; }
    public String getDeclineReason()    { return declineReason; }
    public double getDeclineFeeAmount() { return declineFeeAmount; }
    public String getDeclineFeeStatus() { return declineFeeStatus; }
    public String getNote()             { return note; }

    // Crop type from nested listing
    public String getCropType() {
        if (listing != null && listing.cropType != null) return listing.cropType;
        return "Crop";
    }

    // Names from nested users
    public String getBuyerName() {
        return (buyer != null) ? buyer.name : null;
    }

    public String getSellerName() {
        return (seller != null) ? seller.name : null;
    }

    // ===== Setters =====
    public void setOrderId(String v)          { orderId = v; }
    public void setOfferId(String v)          { offerId = v; }
    public void setBuyerId(String v)          { buyerId = v; }
    public void setSellerId(String v)         { sellerId = v; }
    public void setListingId(String v)        { listingId = v; }

    public void setQuantity(int v)            { quantity = v; }

    public void setPrice(int v)               { price = v; }
    public void setCurrency(String v)         { currency = v; }

    public void setStatus(String v)           { status = v; }
    public void setDeclineReason(String v)    { declineReason = v; }
    public void setDeclineFeeAmount(double v) { declineFeeAmount = v; }
    public void setDeclineFeeStatus(String v) { declineFeeStatus = v; }
    public void setNote(String v)             { note = v; }

    // ===== Nested classes matching the joined JSON =====
    public static class ListingJoin {
        @SerializedName("crop_type")
        public String cropType;
    }

    public static class UserJoin {
        @SerializedName("name")
        public String name;

        @SerializedName("location")
        public String location;
    }
}