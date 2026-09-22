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

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.VH> {

    public interface OnUserClickListener {
        void onUserClick(NewUser user);   // open profile viewer
    }

    private final List<NewUser> items;
    private final OnUserClickListener listener;

    public SearchUserAdapter(List<NewUser> items, OnUserClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite, parent, false);   // reuse same row layout
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NewUser u = items.get(position);

        h.tvName.setText(u.getName() != null ? u.getName() : "User");

        String loc = u.getLocation();
        h.tvLocation.setText(loc != null && !loc.isEmpty() ? loc : "—");

        h.tvRating.setText(String.format(Locale.US, "★ %.1f", u.getRatingAvg()));

        String photo = u.getProfilePhotoUrl();
        if (photo != null && !photo.isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(photo)
                    .placeholder(R.drawable.ic_account)
                    .circleCrop()
                    .into(h.ivPhoto);
        } else {
            h.ivPhoto.setImageResource(R.drawable.ic_account);
        }

        // No remove button on search results — hide it
        h.btnRemove.setVisibility(View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(u);
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