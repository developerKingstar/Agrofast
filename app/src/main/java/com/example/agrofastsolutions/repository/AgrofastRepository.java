package com.example.agrofastsolutions.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.agrofastsolutions.Listing;
import com.example.agrofastsolutions.NewListing;
import com.example.agrofastsolutions.NewOffer;
import com.example.agrofastsolutions.NewUser;
import com.example.agrofastsolutions.Offer;
import com.example.agrofastsolutions.Order;
import com.example.agrofastsolutions.StatusUpdate;
import com.example.agrofastsolutions.UpdateListing;
import com.example.agrofastsolutions.api.ApiClient;
import com.example.agrofastsolutions.api.ApiService;
import com.example.agrofastsolutions.NewOrder;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgrofastRepository {

    private static final String TAG = "AgrofastRepo";

    private final ApiService apiService;
    private final SharedPreferences prefs;

    public AgrofastRepository(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        prefs = context.getSharedPreferences("agrofast_prefs", Context.MODE_PRIVATE);
    }

    // ==========================================
    // USER ID FETCH
    // ==========================================

    private String getCurrentUserId() {
        return prefs.getString("user_id", "");
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

    public void createListing(String cropType,
                              double quantity,
                              double price,
                              String location,
                              DataCallback<Void> callback) {

        String sellerId = getCurrentUserId();

        Log.d(TAG, "createListing: seller=" + sellerId +
                " crop=" + cropType +
                " qty=" + quantity +
                " price=" + price +
                " location=" + location);

        NewListing listing = new NewListing(
                sellerId,
                cropType,
                quantity,
                "kg",
                price,
                location,
                "active"
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
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
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