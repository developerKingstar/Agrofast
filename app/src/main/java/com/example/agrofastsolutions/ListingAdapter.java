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

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private final List<Listing> listings;
    private final OnListingClickListener listener;

    public interface OnListingClickListener {
        void onListingClick(Listing listing);
    }

    public ListingAdapter(List<Listing> listings, OnListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing_card, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing, listener);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgCrop;
        private final TextView tvCropType, tvSeller, tvQuantity, tvLocation, tvPrice;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop    = itemView.findViewById(R.id.imgCrop);
            tvCropType = itemView.findViewById(R.id.tvCropType);
            tvSeller   = itemView.findViewById(R.id.tvSeller);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Listing listing, OnListingClickListener listener) {

            tvCropType.setText(listing.getCropType() != null
                    ? listing.getCropType() : "Crop");

            tvSeller.setText("Seller: " + listing.getSellerName());

            String unit = (listing.getUnit() != null) ? listing.getUnit() : "kg";
            tvQuantity.setText("Qty: " + formatNumber(listing.getQuantityAvailable()) + " " + unit);

            tvLocation.setText("📍 " + (listing.getLocation() != null
                    ? listing.getLocation() : "Unknown"));

            tvPrice.setText("TSh " + formatNumber(listing.getAskingPrice()) + "/" + unit);

            // ===== Photo load =====
            String photoUrl = listing.getFirstPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .placeholder(getCropIcon(listing.getCropType()))
                        .error(getCropIcon(listing.getCropType()))
                        .centerCrop()
                        .into(imgCrop);
            } else {
                imgCrop.setImageResource(getCropIcon(listing.getCropType()));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onListingClick(listing);
            });
        }

        private String formatNumber(double value) {
            if (value == (long) value) return String.valueOf((long) value);
            return String.valueOf(value);
        }

        private int getCropIcon(String cropType) {
            if (cropType == null) return android.R.drawable.ic_menu_gallery;
            switch (cropType.toLowerCase()) {
                case "maize": return android.R.drawable.ic_menu_gallery;
                case "rice":  return android.R.drawable.ic_menu_agenda;
                case "beans": return android.R.drawable.ic_menu_my_calendar;
                default:      return android.R.drawable.ic_menu_upload;
            }
        }
    }
}