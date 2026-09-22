package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * A review row from the `reviews` table.
 * Uses NewUser for joined reviewer/reviewee display.
 */
public class Review {

    @SerializedName("id")
    private String id;

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("reviewer_id")
    private String reviewerId;

    @SerializedName("reviewee_id")
    private String revieweeId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private String createdAt;

    // ===== Optional joins =====
    @SerializedName("reviewer")
    private NewUser reviewer;

    @SerializedName("reviewee")
    private NewUser reviewee;

    public Review() {}

    public String getId()          { return id; }
    public String getOrderId()     { return orderId; }
    public String getReviewerId()  { return reviewerId; }
    public String getRevieweeId()  { return revieweeId; }
    public int    getRating()      { return rating; }
    public String getComment()     { return comment; }
    public String getCreatedAt()   { return createdAt; }

    public String getReviewerName() {
        return (reviewer != null) ? reviewer.getName() : null;
    }

    public String getRevieweeName() {
        return (reviewee != null) ? reviewee.getName() : null;
    }
}