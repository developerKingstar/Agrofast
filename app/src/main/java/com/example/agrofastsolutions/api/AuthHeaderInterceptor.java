package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds ONLY the apikey header (required by Supabase Auth).
 * Does NOT add Authorization — because that's the whole point of auth.
 */
public class AuthHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("apikey", Constants.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .method(original.method(), original.body())
                .build();

        return chain.proceed(request);
    }
}