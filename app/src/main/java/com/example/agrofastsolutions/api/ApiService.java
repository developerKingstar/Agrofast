package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.Listing;
import com.example.agrofastsolutions.NewListing;
import com.example.agrofastsolutions.NewOffer;
import com.example.agrofastsolutions.NewOrder;
import com.example.agrofastsolutions.NewUser;
import com.example.agrofastsolutions.Offer;
import com.example.agrofastsolutions.Order;
import com.example.agrofastsolutions.StatusUpdate;
import com.example.agrofastsolutions.UpdateListing;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.PATCH;
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
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(name,location),seller:users!offers_seller_id_fkey(name,location)")
    Call<List<Offer>> getOfferById(@Query("offer_id") String offerIdEq);


    // ==========================================
    // OFFERS — with joined listing + user data
    // ==========================================

    // Sent offers (I am the buyer)
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(name,location),seller:users!offers_seller_id_fkey(name,location)&order=created_at.desc")
    Call<List<Offer>> getSentOffers(
            @Query("buyer_id") String buyerIdEq
    );

    // Received offers (I am the seller)
    @GET("offers?select=*,listing:listings!offers_listing_id_fkey(crop_type),buyer:users!offers_buyer_id_fkey(name,location),seller:users!offers_seller_id_fkey(name,location)&order=created_at.desc")
    Call<List<Offer>> getReceivedOffers(
            @Query("seller_id") String sellerIdEq,
            @Query("status") String statusEq
    );

    // ==========================================
    // ORDERS — with joined listing + buyer + seller
    // ==========================================

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(name),seller:users!orders_seller_id_fkey(name)&order=accepted_at.desc")
    Call<List<Order>> getActiveOrders(
            @Query("or") String orFilter,
            @Query("status") String statusEq
    );

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(name),seller:users!orders_seller_id_fkey(name)&order=accepted_at.desc")
    Call<List<Order>> getCompletedOrders(
            @Query("or") String orFilter,
            @Query("status") String statusEq
    );

    @GET("orders?select=*,listing:listings!orders_listing_id_fkey(crop_type),buyer:users!orders_buyer_id_fkey(name),seller:users!orders_seller_id_fkey(name)&order=accepted_at.desc")
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
}