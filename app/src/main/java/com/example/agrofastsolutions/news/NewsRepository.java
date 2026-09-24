package com.example.agrofastsolutions.news;

import androidx.annotation.NonNull;

import com.example.agrofastsolutions.BuildConfig;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsRepository {

    private static final String API_KEY = BuildConfig.NEWSDATA_API_KEY;
    private static final String BASE_URL = "https://newsdata.io/api/1/";

    private final NewsApiService api;

    public interface NewsCallback {
        void onSuccess(List<NewsDataResponse.NewsArticle> articles);
        void onError(String error);
    }

    public NewsRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(NewsApiService.class);
    }

    /**
     * Fetch agriculture news.
     * @param country  "tz" for Tanzania, or null for global
     */
    public void fetchAgricultureNews(String country, NewsCallback callback) {
        String query = "agriculture OR farming OR crops OR livestock";

        api.getAgriculturalNews(API_KEY, query, country, "en")
                .enqueue(new Callback<NewsDataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NewsDataResponse> call,
                                           @NonNull Response<NewsDataResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            NewsDataResponse body = response.body();

                            if ("success".equalsIgnoreCase(body.status)
                                    && body.results != null
                                    && !body.results.isEmpty()) {
                                callback.onSuccess(body.results);
                            } else {
                                callback.onError("No agriculture articles found");
                            }
                        } else {
                            String msg = "HTTP " + response.code();
                            if (response.errorBody() != null) {
                                try { msg += ": " + response.errorBody().string(); }
                                catch (Exception ignored) {}
                            }
                            DevLogger.logError("NewsRepository", msg, null);
                            callback.onError(msg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NewsDataResponse> call,
                                          @NonNull Throwable t) {
                        DevLogger.logError("NewsRepository network", t.getMessage(), t);
                        callback.onError(t.getMessage() != null
                                ? t.getMessage() : "Network error");
                    }
                });
    }
}