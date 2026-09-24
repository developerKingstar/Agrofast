package com.example.agrofastsolutions.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.agrofastsolutions.Constants;
import com.example.agrofastsolutions.Listing;
import com.example.agrofastsolutions.MarketPrice;
import com.example.agrofastsolutions.NewListing;
import com.example.agrofastsolutions.NewOffer;
import com.example.agrofastsolutions.NewUser;
import com.example.agrofastsolutions.Offer;
import com.example.agrofastsolutions.Order;
import com.example.agrofastsolutions.Favourite;
import com.example.agrofastsolutions.NewFavourite;
import com.example.agrofastsolutions.NewReview;
import com.example.agrofastsolutions.Review;
import com.example.agrofastsolutions.OrderDeclineUpdate;
import com.example.agrofastsolutions.ProfilePhotoUpdate;
import com.example.agrofastsolutions.StatusUpdate;
import com.example.agrofastsolutions.UpdateListing;
import com.example.agrofastsolutions.UpdateProfile;
import com.example.agrofastsolutions.api.ApiClient;
import com.example.agrofastsolutions.api.ApiService;
import com.example.agrofastsolutions.NewOrder;
import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgrofastRepository {

    private static final String TAG = "AgrofastRepo";

    private final ApiService apiService;
    private final SharedPreferences prefs;

    private final Context appContext;

    public AgrofastRepository(Context context) {
        this.appContext = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = context.getSharedPreferences("agrofast_prefs", Context.MODE_PRIVATE);
    }

    // ==========================================
    // USER ID FETCH
    // ==========================================

    private String getCurrentUserId() {
        return prefs.getString("user_id", "");
    }

    /**
     * Fetch ANY user's public profile by ID.
     * Used by UserProfileViewerActivity.
     */
    public void getUserById(String userId, DataCallback<NewUser> callback) {

        Log.d(TAG, "getUserById: " + userId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Missing user id");
            return;
        }

        String select = "user_id,name,email,phone,location,profile_photo_url,rating_avg,review_count,created_at,status";

        apiService.getUserByIdWithSelect("eq." + userId, select)
                .enqueue(new Callback<List<NewUser>>() {
                    @Override
                    public void onResponse(Call<List<NewUser>> call,
                                           Response<List<NewUser>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {
                            callback.onSuccess(response.body().get(0));
                        } else {
                            String errorBody = "User not found";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("getUserById", errorBody, null);
                            callback.onError("Could not load user");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NewUser>> call, Throwable t) {
                        DevLogger.logError("getUserById", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }
    // ==========================================
    // CREATE PUBLIC USER (after signup)
    // ==========================================
    public void createPublicUser(String userId,
                                 String email,
                                 String phone,
                                 String name,
                                 String location,
                                 DataCallback<Void> callback) {

        Log.d(TAG, "createPublicUser: " + email + " / " + userId);

        NewUser newUser = new NewUser(userId, email, phone, name, location, "active");

        apiService.createPublicUser(newUser, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "public user created");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("createPublicUser", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("createPublicUser", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }


    // ==========================================
    // LISTINGS (Buy flow)
    // ==========================================

    public void getAllListings(DataCallback<List<Listing>> callback) {
        Log.d(TAG, "getAllListings");

        apiService.getAllListings().enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "listings loaded: " + response.body().size() + " items");
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("getAllListings", errorBody, null);
                    callback.onError("HTTP " + response.code() + ": " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                DevLogger.logError("getAllListings", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void searchListings(String cropQuery, DataCallback<List<Listing>> callback) {
        Log.d(TAG, "searchListings: " + cropQuery);

        if (cropQuery == null || cropQuery.trim().isEmpty()) {
            getAllListings(callback);
            return;
        }

        String filter = "ilike.*" + cropQuery.trim().toLowerCase() + "*";

        apiService.searchListings(filter).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "search results: " + response.body().size() + " items");
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("searchListings", errorBody, null);
                    callback.onError("HTTP " + response.code() + ": " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                DevLogger.logError("searchListings", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // LISTINGS (Sell flow)
    // ==========================================

    /**
     * Create a listing WITHOUT a photo (backward compatible).
     */
    public void createListing(String cropType,
                              double quantity,
                              double price,
                              String location,
                              DataCallback<Void> callback) {
        createListing(cropType, quantity, price, location, null, callback);
    }

    /**
     * Create a listing WITH an optional photo URL.
     * Pass photoUrl = null if no photo.
     */
    public void createListing(String cropType,
                              double quantity,
                              double price,
                              String location,
                              String photoUrl,
                              DataCallback<Void> callback) {

        String sellerId = getCurrentUserId();

        Log.d(TAG, "createListing: seller=" + sellerId +
                " crop=" + cropType +
                " qty=" + quantity +
                " price=" + price +
                " location=" + location +
                " photo=" + (photoUrl != null ? "yes" : "no"));

        NewListing listing = new NewListing(
                sellerId,
                cropType,
                quantity,
                "kg",
                price,
                location,
                "active",
                photoUrl          // ← null or URL
        );

        apiService.createListing(listing, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "listing created successfully");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("createListing", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("createListing", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    public void getMyListings(DataCallback<List<Listing>> callback) {
        String sellerId = getCurrentUserId();
        Log.d(TAG, "getMyListings for seller: " + sellerId);

        apiService.getMyListings("eq." + sellerId).enqueue(new Callback<List<Listing>>() {
            @Override
            public void onResponse(Call<List<Listing>> call, Response<List<Listing>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "my listings loaded: " + response.body().size() + " items");
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    DevLogger.logError("getMyListings", errorBody, null);
                    callback.onError("HTTP " + response.code() + ": " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<List<Listing>> call, Throwable t) {
                DevLogger.logError("getMyListings", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void deactivateListing(String listingId, DataCallback<Void> callback) {
        Log.d(TAG, "deactivateListing: " + listingId);

        StatusUpdate patch = new StatusUpdate("expired");

        apiService.updateListingStatus("eq." + listingId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "listing deactivated");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("deactivateListing", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("deactivateListing", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // UPDATE LISTING (edit)
    // ==========================================
    public void updateListing(String listingId,
                              double quantity,
                              double price,
                              String location,
                              DataCallback<Void> callback) {

        Log.d(TAG, "updateListing: " + listingId +
                " qty=" + quantity + " price=" + price + " loc=" + location);

        UpdateListing patch = new UpdateListing(quantity, price, location);

        apiService.updateListingFields("eq." + listingId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "listing updated");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("updateListing", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("updateListing", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // CREATE OFFER
    // ==========================================

    public void sendOffer(String listingId,
                          String sellerId,
                          double quantity,
                          double offeredPrice,
                          DataCallback<Void> callback) {

        String buyerId = getCurrentUserId();

        Log.d(TAG, "sendOffer: listing=" + listingId +
                " buyer=" + buyerId +
                " seller=" + sellerId +
                " qty=" + quantity +
                " price=" + offeredPrice);

        NewOffer offer = new NewOffer(
                listingId,
                buyerId,
                sellerId,
                quantity,
                offeredPrice,
                "pending"
        );

        apiService.createNewOffer(offer, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "offer created successfully");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("sendOffer", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("sendOffer", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // OFFERS
    // ==========================================

    public void getSentOffers(DataCallback<List<Offer>> callback) {
        String userId = getCurrentUserId();
        Log.d(TAG, "getSentOffers for user: " + userId);

        apiService.getSentOffers("eq." + userId).enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                handleListResponse(response, call, callback, "sent offers");
            }

            @Override
            public void onFailure(Call<List<Offer>> call, Throwable t) {
                DevLogger.logError("getSentOffers", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void getReceivedOffers(DataCallback<List<Offer>> callback) {
        String userId = getCurrentUserId();
        Log.d(TAG, "getReceivedOffers for user: " + userId);

        apiService.getReceivedOffers("eq." + userId, "eq.pending")
                .enqueue(new Callback<List<Offer>>() {
                    @Override
                    public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                        handleListResponse(response, call, callback, "received offers");
                    }

                    @Override
                    public void onFailure(Call<List<Offer>> call, Throwable t) {
                        DevLogger.logError("getReceivedOffers", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }


    // ==========================================
    // ACCEPT OFFER
    // ==========================================
    public void acceptOffer(String offerId, DataCallback<Void> callback) {
        StatusUpdate patch = new StatusUpdate("accepted");

        apiService.updateOffer("eq." + offerId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            String errorBody = "Accept failed";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("acceptOffer (step 1)", errorBody, null);
                            callback.onError("Accept failed (HTTP " + response.code() + ")");
                            return;
                        }
                        fetchOfferForOrder(offerId, callback);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("acceptOffer (step 1)", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    private void fetchOfferForOrder(String offerId, DataCallback<Void> callback) {
        apiService.getOfferById("eq." + offerId).enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    DevLogger.logError("fetchOfferForOrder",
                            "Failed to retrieve offer after accept", null);
                    callback.onError("Could not retrieve accepted offer");
                    return;
                }
                Offer offer = response.body().get(0);
                insertOrderFromOffer(offer, callback);
            }

            @Override
            public void onFailure(Call<List<Offer>> call, Throwable t) {
                DevLogger.logError("fetchOfferForOrder", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    private void insertOrderFromOffer(Offer offer, DataCallback<Void> callback) {

        NewOrder newOrder = new NewOrder(
                offer.getOfferId(),
                offer.getBuyerId(),
                offer.getSellerId(),
                offer.getListingId(),
                offer.getQuantity(),
                offer.getPrice(),
                "confirmed"
        );

        apiService.createOrder(newOrder, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                            return;
                        }

                        // ✅ 409 → order already exists, treat as success
                        if (response.code() == 409) {
                            Log.d(TAG, "Order already exists for offer " +
                                    offer.getOfferId() + " — treating as success");
                            callback.onSuccess(null);
                            return;
                        }

                        String errorBody = "Unknown error";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception ignored) {}

                        DevLogger.logError("insertOrderFromOffer", errorBody, null);
                        callback.onError("Order creation failed (HTTP " + response.code() + ")");
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("insertOrderFromOffer", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // DECLINE OFFER
    // ==========================================
    public void declineOffer(String offerId, DataCallback<Void> callback) {
        StatusUpdate patch = new StatusUpdate("rejected");

        apiService.updateOffer("eq." + offerId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Decline failed";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("declineOffer", errorBody, null);
                            callback.onError("Decline failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("declineOffer", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // SETTLE ORDER — mark as completed
    // ==========================================
    public void settleOrder(String orderId, DataCallback<Void> callback) {
        Log.d(TAG, "settleOrder: " + orderId);

        StatusUpdate patch = new StatusUpdate("completed");

        apiService.updateOrder("eq." + orderId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "order settled");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Settle failed";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("settleOrder", errorBody, null);
                            callback.onError("Settle failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("settleOrder", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // DECLINE ORDER — mark as declined + record fee
    // ==========================================
    public void declineOrder(String orderId,
                             String reason,
                             double feeAmount,
                             DataCallback<Void> callback) {
        Log.d(TAG, "declineOrder: " + orderId +
                " reason=" + reason + " fee=" + feeAmount);

        // ✅ Use OrderDeclineUpdate (NOT Order)
        OrderDeclineUpdate patch = new OrderDeclineUpdate(
                "declined",
                reason,
                feeAmount,
                feeAmount > 0 ? "owed" : "none"
        );

        apiService.declineOrder("eq." + orderId, patch, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "order declined");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Decline failed";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("declineOrder", errorBody, null);
                            callback.onError("Decline failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("declineOrder", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // ORDERS
    // ==========================================

    public void getActiveOrders(DataCallback<List<Order>> callback) {
        String userId = getCurrentUserId();
        String orFilter = "(buyer_id.eq." + userId + ",seller_id.eq." + userId + ")";
        Log.d(TAG, "getActiveOrders, filter: " + orFilter);

        apiService.getActiveOrders(orFilter, "eq.confirmed").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                handleListResponse(response, call, callback, "active orders");
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                DevLogger.logError("getActiveOrders", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void getCompletedOrders(DataCallback<List<Order>> callback) {
        String userId = getCurrentUserId();
        String orFilter = "(buyer_id.eq." + userId + ",seller_id.eq." + userId + ")";

        apiService.getCompletedOrders(orFilter, "eq.completed").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                handleListResponse(response, call, callback, "completed orders");
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                DevLogger.logError("getCompletedOrders", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void getDeclinedOrders(DataCallback<List<Order>> callback) {
        String userId = getCurrentUserId();
        String orFilter = "(buyer_id.eq." + userId + ",seller_id.eq." + userId + ")";

        apiService.getDeclinedOrders(orFilter, "eq.declined").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                handleListResponse(response, call, callback, "declined orders");
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                DevLogger.logError("getDeclinedOrders", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // MARKET PRICES
    // ==========================================

    private static final String MARKET_SELECT =
            "crop_name,region,unit,price,price_low,price_high,weekly_change,monthly_change,published_date";

    /**
     * Fetch all market prices, newest first.
     */
    public void getMarketPrices(DataCallback<List<MarketPrice>> callback) {
        Log.d(TAG, "getMarketPrices");

        apiService.getMarketPrices(MARKET_SELECT, "published_date.desc")
                .enqueue(new Callback<List<MarketPrice>>() {
                    @Override
                    public void onResponse(Call<List<MarketPrice>> call,
                                           Response<List<MarketPrice>> response) {
                        handleListResponse(response, call, callback, "market prices");
                    }

                    @Override
                    public void onFailure(Call<List<MarketPrice>> call, Throwable t) {
                        DevLogger.logError("getMarketPrices", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Search market prices by crop name (case-insensitive, partial).
     */
    public void searchMarketPrices(String query,
                                   DataCallback<List<MarketPrice>> callback) {
        Log.d(TAG, "searchMarketPrices: " + query);

        if (query == null || query.trim().isEmpty()) {
            getMarketPrices(callback);
            return;
        }

        String filter = "ilike.*" + query.trim().toLowerCase() + "*";

        apiService.searchMarketPrices(filter, MARKET_SELECT, "published_date.desc")
                .enqueue(new Callback<List<MarketPrice>>() {
                    @Override
                    public void onResponse(Call<List<MarketPrice>> call,
                                           Response<List<MarketPrice>> response) {
                        handleListResponse(response, call, callback, "market search");
                    }

                    @Override
                    public void onFailure(Call<List<MarketPrice>> call, Throwable t) {
                        DevLogger.logError("searchMarketPrices", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // REVIEWS
    // ==========================================

    /**
     * Submit a review for the other party in an order.
     * Supabase trigger updates users.rating_avg + review_count.
     */
    public void submitReview(String orderId,
                             String revieweeId,
                             int rating,
                             String comment,
                             DataCallback<Void> callback) {

        String reviewerId = getCurrentUserId();

        Log.d(TAG, "submitReview: order=" + orderId
                + " reviewer=" + reviewerId
                + " reviewee=" + revieweeId
                + " rating=" + rating);

        if (reviewerId == null || reviewerId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }
        if (revieweeId == null || revieweeId.isEmpty()) {
            callback.onError("Missing reviewee");
            return;
        }

        NewReview payload = new NewReview(
                orderId,
                reviewerId,
                revieweeId,
                rating,
                comment
        );

        apiService.submitReview(payload, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "review submitted");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Review failed";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("submitReview", errorBody, null);
                            callback.onError("Review failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("submitReview", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Load all reviews about a user, newest first.
     * Uses joined reviewer info for display.
     */
    public void getReviewsForUser(String userId,
                                  DataCallback<List<Review>> callback) {

        Log.d(TAG, "getReviewsForUser: " + userId);

        String select = "*,reviewer:users!reviews_reviewer_id_fkey(user_id,name,profile_photo_url)";
        String order  = "created_at.desc";

        apiService.getReviewsForUser("eq." + userId, select, order)
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(Call<List<Review>> call,
                                           Response<List<Review>> response) {
                        handleListResponse(response, call, callback, "reviews");
                    }

                    @Override
                    public void onFailure(Call<List<Review>> call, Throwable t) {
                        DevLogger.logError("getReviewsForUser", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Check if the current user already reviewed a given order.
     * Returns the review if found (empty list otherwise).
     */
    public void checkMyReviewForOrder(String orderId,
                                      DataCallback<List<Review>> callback) {

        String reviewerId = getCurrentUserId();
        Log.d(TAG, "checkMyReviewForOrder: order=" + orderId + " reviewer=" + reviewerId);

        if (reviewerId == null || reviewerId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        apiService.getReviewForOrderByUser(
                        "eq." + orderId,
                        "eq." + reviewerId,
                        "id",
                        1)
                .enqueue(new Callback<List<Review>>() {
                    @Override
                    public void onResponse(Call<List<Review>> call,
                                           Response<List<Review>> response) {
                        handleListResponse(response, call, callback, "my review check");
                    }

                    @Override
                    public void onFailure(Call<List<Review>> call, Throwable t) {
                        DevLogger.logError("checkMyReviewForOrder", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // FAVOURITES
    // ==========================================

    /**
     * Add a user to my favourites.
     */
    public void addFavourite(String favoritedUserId,
                             DataCallback<Void> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "addFavourite: " + userId + " → " + favoritedUserId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }
        if (favoritedUserId == null || favoritedUserId.isEmpty()) {
            callback.onError("Missing user");
            return;
        }
        if (userId.equals(favoritedUserId)) {
            callback.onError("You can't favourite yourself");
            return;
        }

        NewFavourite payload = new NewFavourite(userId, favoritedUserId);

        apiService.addFavourite(payload, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if (response.isSuccessful()) {
                            Log.d(TAG, "favourite added");
                            callback.onSuccess(null);
                            return;
                        }

                        // 409 = unique constraint → already favourited → treat as success
                        if (response.code() == 409) {
                            Log.d(TAG, "already favourited — treating as success");
                            callback.onSuccess(null);
                            return;
                        }

                        String errorBody = "Favourite failed";
                        try {
                            if (response.errorBody() != null)
                                errorBody = response.errorBody().string();
                        } catch (Exception ignored) {}
                        DevLogger.logError("addFavourite", errorBody, null);
                        callback.onError("Favourite failed (HTTP " + response.code() + ")");
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("addFavourite", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Remove a user from my favourites.
     */
    public void removeFavourite(String favoritedUserId,
                                DataCallback<Void> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "removeFavourite: " + userId + " ✂ " + favoritedUserId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        apiService.removeFavourite("eq." + userId, "eq." + favoritedUserId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "favourite removed");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Remove failed";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("removeFavourite", errorBody, null);
                            callback.onError("Remove failed (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("removeFavourite", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * List my favourites with joined user info.
     */
    public void getFavourites(DataCallback<List<Favourite>> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "getFavourites for: " + userId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        String select = "*,favorite_user:users!favourites_favorited_user_id_fkey(user_id,name,profile_photo_url,location,rating_avg)";
        String order  = "created_at.desc";

        apiService.getFavourites("eq." + userId, select, order)
                .enqueue(new Callback<List<Favourite>>() {
                    @Override
                    public void onResponse(Call<List<Favourite>> call,
                                           Response<List<Favourite>> response) {
                        handleListResponse(response, call, callback, "favourites");
                    }

                    @Override
                    public void onFailure(Call<List<Favourite>> call, Throwable t) {
                        DevLogger.logError("getFavourites", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Check whether a specific user is already in my favourites.
     * onSuccess is called with the list — non-empty means "yes".
     */
    public void checkFavourite(String favoritedUserId,
                               DataCallback<List<Favourite>> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "checkFavourite: " + userId + " ? " + favoritedUserId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        apiService.checkFavourite(
                        "eq." + userId,
                        "eq." + favoritedUserId,
                        "id",
                        1)
                .enqueue(new Callback<List<Favourite>>() {
                    @Override
                    public void onResponse(Call<List<Favourite>> call,
                                           Response<List<Favourite>> response) {
                        handleListResponse(response, call, callback, "favourite check");
                    }

                    @Override
                    public void onFailure(Call<List<Favourite>> call, Throwable t) {
                        DevLogger.logError("checkFavourite", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // USER SEARCH (for FavouritesActivity)
    // ==========================================

    /**
     * Search users by name (case-insensitive contains).
     * Skips the current user.
     */
    public void searchUsers(String query,
                            DataCallback<List<NewUser>> callback) {

        Log.d(TAG, "searchUsers: " + query);

        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(new java.util.ArrayList<>());
            return;
        }

        String filter = "ilike.*" + query.trim().toLowerCase() + "*";
        String select = "user_id,name,profile_photo_url,location,rating_avg,review_count";
        String currentId = getCurrentUserId();

        apiService.searchUsers(filter, select, 30)
                .enqueue(new Callback<List<NewUser>>() {
                    @Override
                    public void onResponse(Call<List<NewUser>> call,
                                           Response<List<NewUser>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            String errorBody = "Search failed";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("searchUsers", errorBody, null);
                            callback.onError("Search failed (HTTP " + response.code() + ")");
                            return;
                        }

                        // Filter out myself
                        List<NewUser> results = new java.util.ArrayList<>();
                        for (NewUser u : response.body()) {
                            if (currentId != null && !currentId.isEmpty()
                                    && currentId.equals(u.getUserId())) {
                                continue;   // skip me
                            }
                            results.add(u);
                        }

                        Log.d(TAG, "searchUsers: " + results.size() + " results");
                        callback.onSuccess(results);
                    }

                    @Override
                    public void onFailure(Call<List<NewUser>> call, Throwable t) {
                        DevLogger.logError("searchUsers", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    // ==========================================
    // PROFILE
    // ==========================================

    /**
     * Update user's name, phone, location.
     */
    public void updateProfile(String name, String phone, String location,
                              DataCallback<Void> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "updateProfile for: " + userId);

        UpdateProfile update = new UpdateProfile(name, phone, location);

        apiService.updateUserProfile("eq." + userId, update, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "profile updated");
                            callback.onSuccess(null);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("updateProfile", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("updateProfile", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Upload a profile photo to Supabase Storage, then save its URL in the user's row.
     *
     * Flow:
     *   1. Upload file to storage bucket at {userId}/{timestamp}.jpg
     *   2. Get the public URL of the uploaded file
     *   3. PATCH user's profile_photo_url in public.users
     *
     * @param fileBytes  Byte content of the image
     * @param extension  File extension (e.g., "jpg" — no dot)
     * @param callback   Called with the new public URL on success
     */
    public void uploadProfilePhoto(byte[] fileBytes, String extension,
                                   DataCallback<String> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "uploadProfilePhoto for: " + userId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        // ✅ Get a fresh token before uploading
        SupabaseAuthManager authManager = new SupabaseAuthManager(appContext);

        // ⚠️ Need a context here — see note below
        // (We'll store context as a field — see the constructor change)

        authManager.getFreshAccessToken(new SupabaseAuthManager.TokenCallback() {
            @Override
            public void onResult(String freshToken) {
                if (freshToken == null || freshToken.isEmpty()) {
                    callback.onError("Session expired. Please log in again.");
                    return;
                }
                doUpload(fileBytes, extension, userId, freshToken, callback);
            }
        });
    }

    // ==========================================
    // LISTING PHOTO UPLOAD
    // ==========================================

    /**
     * Upload a photo for a listing to Supabase Storage.
     *
     * Flow (mirrors uploadProfilePhoto):
     *   1. Get a fresh access token
     *   2. Upload file to listing-photos/{userId}/{timestamp}.{ext}
     *   3. Return the public URL
     *
     * The caller then passes this URL to createListing(...).
     *
     * @param fileBytes  Byte content of the image
     * @param extension  File extension (e.g., "jpg" — no dot)
     * @param callback   Called with the new public URL on success
     */
    public void uploadListingPhoto(byte[] fileBytes, String extension,
                                   DataCallback<String> callback) {

        String userId = getCurrentUserId();
        Log.d(TAG, "uploadListingPhoto for: " + userId);

        if (userId == null || userId.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        SupabaseAuthManager authManager = new SupabaseAuthManager(appContext);

        authManager.getFreshAccessToken(new SupabaseAuthManager.TokenCallback() {
            @Override
            public void onResult(String freshToken) {
                if (freshToken == null || freshToken.isEmpty()) {
                    callback.onError("Session expired. Please log in again.");
                    return;
                }
                doListingUpload(fileBytes, extension, userId, freshToken, callback);
            }
        });
    }

    private void doListingUpload(byte[] fileBytes, String extension,
                                 String userId, String accessToken,
                                 DataCallback<String> callback) {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName  = timestamp + "." + extension;
        String storagePath = userId + "/" + fileName;

        okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/*"), fileBytes);
        okhttp3.MultipartBody.Part filePart =
                okhttp3.MultipartBody.Part.createFormData("file", fileName, fileBody);

        okhttp3.RequestBody cacheBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("text/plain"), "3600");

        String bearer = "Bearer " + accessToken;

        com.example.agrofastsolutions.api.StorageService storageService =
                com.example.agrofastsolutions.api.StorageClient.getClient()
                        .create(com.example.agrofastsolutions.api.StorageService.class);

        String fullPath = Constants.BUCKET_LISTING_PHOTOS + "/" + storagePath;

        storageService.uploadFile(fullPath, bearer, Constants.SUPABASE_ANON_KEY,
                        "true", filePart, cacheBody)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(Call<okhttp3.ResponseBody> call,
                                           Response<okhttp3.ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            String errorBody = "Upload failed";
                            try {
                                if (response.errorBody() != null)
                                    errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("uploadListingPhoto (upload)", errorBody, null);
                            callback.onError("Upload failed (HTTP " + response.code() + ")");
                            return;
                        }

                        String publicUrl = Constants.publicStorageUrl(
                                Constants.BUCKET_LISTING_PHOTOS, storagePath);
                        Log.d(TAG, "listing photo uploaded: " + publicUrl);
                        callback.onSuccess(publicUrl);
                    }

                    @Override
                    public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                        DevLogger.logError("uploadListingPhoto (upload)", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    private void doUpload(byte[] fileBytes, String extension,
                          String userId, String accessToken,
                          DataCallback<String> callback) {

        // Build the storage path: {userId}/{timestamp}.jpg
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = timestamp + "." + extension;
        String storagePath = userId + "/" + fileName;

        // Build the file part
        okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/*"), fileBytes);
        okhttp3.MultipartBody.Part filePart =
                okhttp3.MultipartBody.Part.createFormData("file", fileName, fileBody);

        okhttp3.RequestBody cacheBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("text/plain"), "3600");

        String bearer = "Bearer " + accessToken;
        Log.d(TAG, "Upload bearer token (first 40 chars): " +
                (accessToken.length() > 40 ? accessToken.substring(0, 40) + "..." : accessToken));

        com.example.agrofastsolutions.api.StorageService storageService =
                com.example.agrofastsolutions.api.StorageClient.getClient()
                        .create(com.example.agrofastsolutions.api.StorageService.class);

        String fullPath = Constants.BUCKET_PROFILE_PHOTOS + "/" + storagePath;

        storageService.uploadFile(fullPath, bearer, Constants.SUPABASE_ANON_KEY,
                        "true", filePart, cacheBody)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(Call<okhttp3.ResponseBody> call,
                                           Response<okhttp3.ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            String errorBody = "Upload failed";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("uploadProfilePhoto (upload)", errorBody, null);
                            callback.onError("Upload failed (HTTP " + response.code() + ")");
                            return;
                        }

                        String publicUrl = Constants.publicStorageUrl(
                                Constants.BUCKET_PROFILE_PHOTOS, storagePath);
                        Log.d(TAG, "photo uploaded: " + publicUrl);

                        savePhotoUrlToUser(userId, publicUrl, callback);
                    }

                    @Override
                    public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                        DevLogger.logError("uploadProfilePhoto (upload)", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }
    private void savePhotoUrlToUser(String userId, String photoUrl,
                                    DataCallback<String> callback) {
        ProfilePhotoUpdate update = new ProfilePhotoUpdate(photoUrl);

        apiService.updateProfilePhoto("eq." + userId, update, "return=minimal")
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "profile photo URL saved");
                            callback.onSuccess(photoUrl);
                        } else {
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception ignored) {}
                            DevLogger.logError("savePhotoUrlToUser", errorBody, null);
                            callback.onError("HTTP " + response.code() + ": " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        DevLogger.logError("savePhotoUrlToUser", t.getMessage(), t);
                        callback.onError("Network: " + t.getMessage());
                    }
                });
    }

    /**
     * Fetch the current user's full profile.
     */
    public void getCurrentUserProfile(DataCallback<NewUser> callback) {
        String userId = getCurrentUserId();
        Log.d(TAG, "getCurrentUserProfile: " + userId);

        apiService.getUserById("eq." + userId).enqueue(new Callback<List<NewUser>>() {
            @Override
            public void onResponse(Call<List<NewUser>> call, Response<List<NewUser>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    DevLogger.logError("getCurrentUserProfile",
                            "Empty or failed response: HTTP " + response.code(), null);
                    callback.onError("Could not load profile");
                }
            }

            @Override
            public void onFailure(Call<List<NewUser>> call, Throwable t) {
                DevLogger.logError("getCurrentUserProfile", t.getMessage(), t);
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    // ==========================================
    // GENERIC RESPONSE HANDLER
    // ==========================================
    private <T> void handleListResponse(Response<List<T>> response,
                                        Call<List<T>> call,
                                        DataCallback<List<T>> callback,
                                        String label) {
        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, label + " loaded: " + response.body().size() + " items");
            callback.onSuccess(response.body());
        } else {
            String errorBody = "Unknown error";
            try {
                if (response.errorBody() != null) {
                    errorBody = response.errorBody().string();
                }
            } catch (Exception e) {
                DevLogger.logError(label, "Error reading error body", e);
            }
            DevLogger.logError(label, "HTTP " + response.code() + " | " + errorBody, null);
            callback.onError("HTTP " + response.code() + ": " + errorBody);
        }
    }

    // ==========================================
    // CALLBACK INTERFACE
    // ==========================================
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}