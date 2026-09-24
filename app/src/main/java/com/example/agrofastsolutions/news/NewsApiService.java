package com.example.agrofastsolutions.news;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    // GET https://newsdata.io/api/1/latest?apikey=...&q=agriculture&country=tz&language=en
    @GET("latest")
    Call<NewsDataResponse> getAgriculturalNews(
            @Query("apikey") String apiKey,
            @Query("q") String query,           // "agriculture OR farming"
            @Query("country") String country,    // "tz"
            @Query("language") String language   // "en"
    );
}