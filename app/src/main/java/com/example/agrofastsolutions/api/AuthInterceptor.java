package com.example.agrofastsolutions.api;

import com.example.agrofastsolutions.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("apikey", Constants.SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + Constants.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .method(original.method(), original.body())
                .build();

        //  Log what we're sending
        android.util.Log.d("AuthInterceptor", "→ " + request.method() + " " + request.url());
        android.util.Log.d("AuthInterceptor", "apikey header: " +
                (Constants.SUPABASE_ANON_KEY.length() > 20 ?
                        Constants.SUPABASE_ANON_KEY.substring(0, 20) + "..." :
                        Constants.SUPABASE_ANON_KEY));

        return chain.proceed(request);
    }
}