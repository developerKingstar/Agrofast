package com.example.agrofastsolutions.news;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsDataResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("totalResults")
    public int totalResults;

    @SerializedName("results")
    public List<NewsArticle> results;

    public static class NewsArticle {

        @SerializedName("article_id")
        public String articleId;

        @SerializedName("title")
        public String title;

        @SerializedName("link")
        public String link;

        @SerializedName("description")
        public String description;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("pubDate")
        public String pubDate;

        @SerializedName("source_id")
        public String sourceId;

        @SerializedName("source_name")
        public String sourceName;

        // ---------- Convenience getters ----------

        public String getDisplaySource() {
            return (sourceName != null && !sourceName.isEmpty()) ? sourceName : sourceId;
        }

        public String getDisplayDate() {
            if (pubDate == null || pubDate.length() < 16) return "";
            // "2026-09-23 14:30:00" → "Sep 23, 14:30"
            try {
                String[] parts = pubDate.split(" ");
                String[] dateParts = parts[0].split("-");
                String[] timeParts = parts[1].split(":");
                String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                        "Jul","Aug","Sep","Oct","Nov","Dec"};
                int monthIdx = Integer.parseInt(dateParts[1]) - 1;
                return months[monthIdx] + " " + dateParts[2] + ", " + timeParts[0] + ":" + timeParts[1];
            } catch (Exception e) {
                return pubDate;
            }
        }
    }
}