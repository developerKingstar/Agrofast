package com.example.agrofastsolutions;

import com.google.gson.annotations.SerializedName;

/**
 * Payload for inserting a new review into the `reviews` table.
 * Server fills in `id` and `created_at` automatically.
 */
public class NewReview {

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

    public NewReview(String orderId,
                     String reviewerId,
                     String revieweeId,
                     int rating,
                     String comment) {
        this.orderId    = orderId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.rating     = rating;
        this.comment    = comment;
    }

    public String getOrderId()    { return orderId; }
    public String getReviewerId() { return reviewerId; }
    public String getRevieweeId() { return revieweeId; }
    public int    getRating()     { return rating; }
    public String getComment()    { return comment; }
}