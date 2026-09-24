package com.example.agrofastsolutions.news;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.agrofastsolutions.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final List<NewsDataResponse.NewsArticle> articles;

    public NewsAdapter(List<NewsDataResponse.NewsArticle> articles) {
        this.articles = articles;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsDataResponse.NewsArticle article = articles.get(position);

        holder.tvTitle.setText(article.title != null ? article.title : "Untitled");
        holder.tvSource.setText(article.getDisplaySource());
        holder.tvDate.setText(article.getDisplayDate());

        // Hide the placeholder by default — Glide will show it back if loading fails
        holder.tvPlaceholder.setVisibility(View.VISIBLE);
        holder.ivThumb.setImageDrawable(null);   // clear any recycled image

        if (article.imageUrl != null && !article.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(article.imageUrl)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable
                                                    com.bumptech.glide.load.engine.GlideException e,
                                                    Object model,
                                                    @NonNull com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                    boolean isFirstResource) {
                            // Image failed → keep placeholder visible
                            holder.tvPlaceholder.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull android.graphics.drawable.Drawable resource,
                                                       @NonNull Object model,
                                                       com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                       @NonNull com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            // Image loaded → hide placeholder
                            holder.tvPlaceholder.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.ivThumb);
        }
        // else: no URL → placeholder stays visible, no Glide call

        holder.itemView.setOnClickListener(v -> {
            if (article.link != null && !article.link.isEmpty()) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(article.link));
                v.getContext().startActivity(browser);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    // ---------- ViewHolder ----------
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvSource, tvDate, tvPlaceholder;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb  = itemView.findViewById(R.id.iv_news_thumb);
            tvTitle  = itemView.findViewById(R.id.tv_news_title);
            tvPlaceholder = itemView.findViewById(R.id.tv_news_placeholder);
            tvSource = itemView.findViewById(R.id.tv_news_source);
            tvDate   = itemView.findViewById(R.id.tv_news_date);
        }
    }
}