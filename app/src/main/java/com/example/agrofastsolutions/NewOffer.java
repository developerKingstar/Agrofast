package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A minimal payload for creating an offer in Supabase.
 * Only includes columns that actually exist in the 'offers' table.
 * This prevents errors like "Could not find the 'currency' column".
 */
public class NewOffer {

    @SerializedName("listing_id")
    private String listingId;

    @SerializedName("buyer_id")
    private String buyerId;

    @SerializedName("seller_id")
    private String sellerId;

    @SerializedName("quantity_requested")
    private double quantityRequested;

    @SerializedName("offered_price")
    private double offeredPrice;

    @SerializedName("status")
    private String status;

    public NewOffer(String listingId,
                    String buyerId,
                    String sellerId,
                    double quantityRequested,
                    double offeredPrice,
                    String status) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.quantityRequested = quantityRequested;
        this.offeredPrice = offeredPrice;
        this.status = status;
    }

    // ===== Getters (Gson uses these when serializing) =====
    public String getListingId()          { return listingId; }
    public String getBuyerId()            { return buyerId; }
    public String getSellerId()           { return sellerId; }
    public double getQuantityRequested()  { return quantityRequested; }
    public double getOfferedPrice()       { return offeredPrice; }
    public String getStatus()             { return status; }
}