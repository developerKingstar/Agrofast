package com.example.agrofastsolutions;

import android.util.Log;

import com.example.agrofastsolutions.api.ApiClient;
import com.example.agrofastsolutions.api.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupabaseTest {
    private static final String TAG = "SupabaseTest";

    public static void runSimpleTest() {
        Log.d(TAG, "========== STARTING SUPABASE TEST ==========");

        ApiService api = ApiClient.getClient().create(ApiService.class);

        // Test 1: Fetch offers
        api.getSentOffers("eq.11111111-1111-1111-1111-111111111111")
                .enqueue(new Callback<List<Offer>>() {
                    @Override
                    public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                        Log.d(TAG, "URL: " + call.request().url());
                        Log.d(TAG, "HTTP Code: " + response.code());

                        if (response.isSuccessful()) {
                            List<Offer> offers = response.body();
                            Log.d(TAG, "✅ SUCCESS! Offers count: " + (offers != null ? offers.size() : 0));
                            if (offers != null) {
                                for (Offer o : offers) {
                                    Log.d(TAG, "  → Offer: " + o.getOfferId() +
                                            " | Buyer: " + o.getBuyerId() +
                                            " | Status: " + o.getStatus());
                                }
                            }
                        } else {
                            try {
                                String err = response.errorBody() != null ?
                                        response.errorBody().string() : "null";
                                Log.e(TAG, "❌ ERROR CODE " + response.code() + ": " + err);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to read error", e);
                            }
                        }
                        Log.d(TAG, "========== TEST COMPLETE ==========");
                    }

                    @Override
                    public void onFailure(Call<List<Offer>> call, Throwable t) {
                        Log.e(TAG, "❌ NETWORK FAILURE: " + t.getMessage(), t);
                        Log.d(TAG, "========== TEST COMPLETE ==========");
                    }
                });
    }
}
