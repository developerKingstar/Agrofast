package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.Favourite;
import com.example.agrofastsolutions.Listing;
import com.example.agrofastsolutions.NewFavourite;
import com.example.agrofastsolutions.NewListing;
import com.example.agrofastsolutions.NewOffer;
import com.example.agrofastsolutions.NewOrder;
import com.example.agrofastsolutions.NewReview;
import com.example.agrofastsolutions.NewUser;
import com.example.agrofastsolutions.Offer;
import com.example.agrofastsolutions.Order;
import com.example.agrofastsolutions.OrderDeclineUpdate;
import com.example.agrofastsolutions.ProfilePhotoUpdate;
import com.example.agrofastsolutions.Review;
import com.example.agrofastsolutions.StatusUpdate;
import com.example.agrofastsolutions.UpdateListing;
import com.example.agrofastsolutions.UpdateProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
//import retrofit2.http.PUT;

public interface ApiService {

    // Create a new user in public.users (after Supabase Auth signup)
    @POST("users")
    Call<Void> createPublicUser(
            @Body NewUser user,
            @Header("Prefer") String prefer
    );

    // ==========================================
    // LISTINGS (Buy flow)
    // ==========================================

    // Fetch ALL active listings (with joined seller name + location)
    @GET("listings?select=*,seller:users!listings_seller_id_fkey(name,location)&status=eq.active&order=created_at.desc")
    Call<List<Listing>> getAllListings();

    // Search active listings by crop type (case-insensitive contains)
    @GET("listings?select=*,seller:users!listings_seller_id_fkey(name,location)&status=eq.active&order=created_at.desc")
    Call<List<Listing>> searchListings(
            @Query("crop_type") String cropFilter
    );

    // ==========================================
    // LISTINGS (Sell flow)
    // ==========================================

    // Create a new listing
    @POST("listings")
    Call<Void> createListing(
            @Body NewListing listing,
            @Header("Prefer") String prefer
    );

    // Get MY listings (filtered by seller_id)
    @GET("listings?select=*&order=created_at.desc")
    Call<List<Listing>> getMyListings(
            @Query("seller_id") String sellerIdEq
    );

    // Update a listing's status (e.g., mark as expired / fulfilled)
    @PATCH("listings")
    Call<Void> updateListingStatus(
            @Query("listing_id") String listingIdEq,
            @Body StatusUpdate update,
            @Header("Prefer") String prefer
    );



    // Update specific fields of a listing (quantity, price, location)
    @PATCH("listings")
    Call<Void> updateListingFields(
            @Query("listing_id") String listingIdEq,
            @Body UpdateListing update,
            @Header("Prefer") String prefer
    );

    // ==========================================
    // OFFERS (Buyer creates offer)
    // ==========================================

    @POST("offers")
    Call<Void> createNewOffer(
            @Body NewOffer offer,
            @Header("Prefer") String prefer
    );

    //---------------------------------------------------------------------------
    // MY SPACE SECTION
    //---------------------------------------------------------------------------

    // Fetch a single offer by its ID
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(user_id,name,location),seller:users!offers_seller_id_fkey(user_id,name,location)")
    Call<List<Offer>> getOfferById(@Query("offer_id") String offerIdEq);


    // ==========================================
    // OFFERS — with joined listing + user data
    // ==========================================

    // Sent offers (I am the buyer)
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(user_id,name,location),seller:users!offers_seller_id_fkey(user_id,name,location)&order=created_at.desc")
    Call<List<Offer>> getSentOffers(
            @Query("buyer_id") String buyerIdEq
    );

    // Received offers (I am the seller)
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(user_id,name,location),seller:users!offers_seller_id_fkey(user_id,name,location)&order=created_at.desc")
    Call<List<Offer>> getReceivedOffers(
            @Query("seller_id") String sellerIdEq,
            @Query("status") String statusEq
    );

    // ==========================================
    // ORDERS — with joined listing + buyer + seller
    // ==========================================

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(user_id,name,phone),seller:users!orders_seller_id_fkey(user_id,name,phone)&order=accepted_at.desc")
    Call<List<Order>> getActiveOrders(
            @Query("or") String orFilter,
            @Query("status") String statusEq
    );

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(user_id,name,phone),seller:users!orders_seller_id_fkey(user_id,name,phone)&order=accepted_at.desc")
    Call<List<Order>> getCompletedOrders(
            @Query("or") String orFilter,
            @Query("status") String statusEq
    );

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(user_id,name,phone),seller:users!orders_seller_id_fkey(user_id,name,phone)&order=accepted_at.desc")
    Call<List<Order>> getDeclinedOrders(
            @Query("or") String orFilter,
            @Query("status") String statusEq
    );

    // ==========================================
    // CREATE / UPDATE
    // ==========================================

    @POST("offers")
    Call<List<Offer>> createOffer(
            @Body Offer offer,
            @Header("Prefer") String prefer
    );

    @PATCH("offers")
    Call<Void> updateOffer(
            @Query("offer_id") String offerIdEq,
            @Body StatusUpdate update,
            @Header("Prefer") String prefer
    );



    @POST("orders")
    Call<Void> createOrder(
            @Body NewOrder order,
            @Header("Prefer") String prefer
    );

    @PATCH("orders")
    Call<Void> updateOrder(
            @Query("order_id") String orderIdEq,
            @Body StatusUpdate update,
            @Header("Prefer") String prefer
    );

    // Decline order with reason + fee (uses a richer payload)
    @PATCH("orders")
    Call<Void> declineOrder(
            @Query("order_id") String orderIdEq,
            @Body OrderDeclineUpdate update,
            @Header("Prefer") String prefer
    );

    // ==========================================================
    // REVIEWS
    // ==========================================================

    /**
     * Submit a new review.
     * Server fills id + created_at. Trigger updates rating_avg + review_count.
     */
    @POST("reviews")
    Call<Void> submitReview(
            @Body NewReview review,
            @Header("Prefer") String prefer
    );

    /**
     * Fetch reviews written about a user (reviewee).
     * Example select:
     *   "*,reviewer:users!reviews_reviewer_id_fkey(user_id,name,profile_photo_url)"
     * Example order:
     *   "created_at.desc"
     */
    @GET("reviews")
    Call<List<Review>> getReviewsForUser(
            @Query("reviewee_id") String revieweeIdEq,
            @Query("select") String select,
            @Query("order") String order
    );

    /**
     * Check if the current user already reviewed a specific order.
     * (Used to prevent double-review prompts.)
     */
    @GET("reviews")
    Call<List<Review>> getReviewForOrderByUser(
            @Query("order_id") String orderIdEq,
            @Query("reviewer_id") String reviewerIdEq,
            @Query("select") String select,
            @Query("limit") int limit
    );

    // ==========================================================
    // FAVOURITES
    // ==========================================================

    /**
     * Add a user to favourites.
     */
    @POST("favourites")
    Call<Void> addFavourite(
            @Body NewFavourite favourite,
            @Header("Prefer") String prefer
    );

    /**
     * Remove a favourite (by composite key).
     */
    @DELETE("favourites")
    Call<Void> removeFavourite(
            @Query("user_id")           String userIdEq,
            @Query("favorited_user_id") String favoritedUserIdEq
    );

    @GET("favourites")
    Call<List<Favourite>> getFavourites(
            @Query("user_id") String userIdEq,
            @Query("select")  String select,
            @Query("order")   String order
    );

    @GET("favourites")
    Call<List<Favourite>> checkFavourite(
            @Query("user_id")           String userIdEq,
            @Query("favorited_user_id") String favoritedUserIdEq,
            @Query("select")            String select,
            @Query("limit")             int limit
    );
    // ==========================================================
    // USER SEARCH (for FavouritesActivity)
    // ==========================================================

    /**
     * Search users by name (case-insensitive, partial).
     * Example nameIlike: "ilike.*john*"
     * Example select:  "user_id,name,profile_photo_url,location,rating_avg,review_count"
     */
    @GET("users")
    Call<List<NewUser>> searchUsers(
            @Query("name") String nameIlike,
            @Query("select") String select,
            @Query("limit") int limit
    );

    // ==========================================
    // PROFILE (Sell/MySpace bundle)
    // ==========================================

    // Update user's name, phone, location
    @PATCH("users")
    Call<Void> updateUserProfile(
            @Query("user_id") String userIdEq,
            @Body UpdateProfile update,
            @Header("Prefer") String prefer
    );

    // Update ONLY the profile photo URL
    @PATCH("users")
    Call<Void> updateProfilePhoto(
            @Query("user_id") String userIdEq,
            @Body ProfilePhotoUpdate update,
            @Header("Prefer") String prefer
    );

    // Fetch a user by ID (with all fields)
    @GET("users?select=*")
    Call<List<NewUser>> getUserById(
            @Query("user_id") String userIdEq
    );

    /**
     * Fetch a user by ID with a caller-supplied select string.
     * Used by UserProfileViewerActivity and any place that needs
     * a specific subset of fields.
     */
    @GET("users")
    Call<List<NewUser>> getUserByIdWithSelect(
            @Query("user_id") String userIdEq,
            @Query("select")  String select
    );
}