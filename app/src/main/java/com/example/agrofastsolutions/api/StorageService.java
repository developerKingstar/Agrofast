package com.example.agrofastsolutions.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface StorageService {

    /**
     * Upload a file to a Supabase Storage bucket.
     * Path is encoded by Retrofit, but we pass it pre-encoded
     * so slashes in the path are preserved.
     */
    @Multipart
    @POST("storage/v1/object/{path}")
    Call<ResponseBody> uploadFile(
            @Path(value = "path", encoded = true) String bucketPath,
            @Header("Authorization") String bearerToken,
            @Header("apikey") String apiKey,
            @Header("x-upsert") String upsert,
            @Part MultipartBody.Part file,
            @Part("cacheControl") RequestBody cacheControl
    );
}