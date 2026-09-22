package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal payload for declining an order.
 * Only includes the fields we're changing.
 */
public class OrderDeclineUpdate {

    @SerializedName("status")
    private String status;

    @SerializedName("decline_reason")
    private String declineReason;

    @SerializedName("decline_fee_amount")
    private double declineFeeAmount;

    @SerializedName("decline_fee_status")
    private String declineFeeStatus;

    public OrderDeclineUpdate(String status, String declineReason,
                              double declineFeeAmount, String declineFeeStatus) {
        this.status = status;
        this.declineReason = declineReason;
        this.declineFeeAmount = declineFeeAmount;
        this.declineFeeStatus = declineFeeStatus;
    }

    public String getStatus()           { return status; }
    public String getDeclineReason()    { return declineReason; }
    public double getDeclineFeeAmount() { return declineFeeAmount; }
    public String getDeclineFeeStatus() { return declineFeeStatus; }
}