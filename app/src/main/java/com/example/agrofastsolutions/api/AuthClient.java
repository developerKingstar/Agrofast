package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A separate Retrofit client for Supabase Auth endpoints.
 * Auth lives at /auth/v1/ — different base URL from PostgREST (/rest/v1/).
 */
public class AuthClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new AuthHeaderInterceptor())  // only adds apikey
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            String baseUrl = Constants.SUPABASE_URL + "/auth/v1/";
            android.util.Log.d("AuthClient", "Auth base URL: " + baseUrl);

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}