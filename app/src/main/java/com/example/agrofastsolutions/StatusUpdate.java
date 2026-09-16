package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A minimal payload for updating just the status of an offer/order.
 * Prevents Supabase from complaining about unknown columns like 'currency'.
 */
public class StatusUpdate {

    @SerializedName("status")
    private String status;

    public StatusUpdate(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}