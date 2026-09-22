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
import java.util.Locale;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.VH> {

    public interface OnFavouriteClickListener {
        void onFavouriteClick(Favourite favourite);   // open profile
        void onRemoveClick(Favourite favourite);      // remove from favourites
    }

    private final List<Favourite> items;
    private final OnFavouriteClickListener listener;

    public FavouriteAdapter(List<Favourite> items, OnFavouriteClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Favourite f = items.get(position);

        h.tvName.setText(f.getFavoriteName() != null ? f.getFavoriteName() : "User");

        String loc = f.getFavoriteLocation();
        h.tvLocation.setText(loc != null && !loc.isEmpty() ? loc : "—");

        double rating = f.getFavoriteRating();
        h.tvRating.setText(String.format(Locale.US, "★ %.1f", rating));

        String photo = f.getFavoritePhotoUrl();
        if (photo != null && !photo.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(photo)
                    .placeholder(R.drawable.ic_account)
                    .circleCrop()
                    .into(h.ivPhoto);
        } else {
            h.ivPhoto.setImageResource(R.drawable.ic_account);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFavouriteClick(f);
        });

        h.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(f);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvLocation, tvRating;
        ImageView btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            ivPhoto    = itemView.findViewById(R.id.ivFavPhoto);
            tvName     = itemView.findViewById(R.id.tvFavName);
            tvLocation = itemView.findViewById(R.id.tvFavLocation);
            tvRating   = itemView.findViewById(R.id.tvFavRating);
            btnRemove  = itemView.findViewById(R.id.btnFavRemove);
        }
    }
}