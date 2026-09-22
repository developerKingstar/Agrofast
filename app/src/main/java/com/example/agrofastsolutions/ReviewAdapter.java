package com.example.agrofastsolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> items;

    public ReviewAdapter(List<Review> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Review r = items.get(position);

        String reviewerName = r.getReviewerName();
        h.tvReviewerName.setText(reviewerName != null ? reviewerName : "User");

        // Star display — "★★★★★" filled to rating, "☆☆☆☆☆" rest
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < r.getRating() ? "★" : "☆");
        }
        h.tvStars.setText(stars.toString());

        String comment = r.getComment();
        if (comment != null && !comment.trim().isEmpty()) {
            h.tvComment.setText(comment);
            h.tvComment.setVisibility(View.VISIBLE);
        } else {
            h.tvComment.setVisibility(View.GONE);
        }

        String date = formatDate(r.getCreatedAt());
        h.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            java.text.SimpleDateFormat in =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            java.util.Date d = in.parse(iso.substring(0, 19));
            if (d == null) return "";
            java.text.SimpleDateFormat out =
                    new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US);
            return out.format(d);
        } catch (Exception e) {
            return "";
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvStars, tvComment, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvStars        = itemView.findViewById(R.id.tvReviewStars);
            tvComment      = itemView.findViewById(R.id.tvReviewComment);
            tvDate         = itemView.findViewById(R.id.tvReviewDate);
        }
    }
}