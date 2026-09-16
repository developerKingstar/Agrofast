package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A minimal payload for creating an order in Supabase.
 * Only includes columns that actually exist in the 'orders' table.
 */
public class NewOrder {

    @SerializedName("offer_id")
    private String offerId;

    @SerializedName("buyer_id")
    private String buyerId;

    @SerializedName("seller_id")
    private String sellerId;

    @SerializedName("listing_id")
    private String listingId;

    @SerializedName("final_quantity")
    private int finalQuantity;

    @SerializedName("final_price")
    private int finalPrice;

    @SerializedName("status")
    private String status;

    public NewOrder(String offerId, String buyerId, String sellerId,
                    String listingId, int finalQuantity, int finalPrice,
                    String status) {
        this.offerId = offerId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.listingId = listingId;
        this.finalQuantity = finalQuantity;
        this.finalPrice = finalPrice;
        this.status = status;
    }

    // Getters (Gson only needs them for serialize, but good practice)
    public String getOfferId()      { return offerId; }
    public String getBuyerId()      { return buyerId; }
    public String getSellerId()     { return sellerId; }
    public String getListingId()    { return listingId; }
    public int    getFinalQuantity(){ return finalQuantity; }
    public int    getFinalPrice()   { return finalPrice; }
    public String getStatus()       { return status; }
}