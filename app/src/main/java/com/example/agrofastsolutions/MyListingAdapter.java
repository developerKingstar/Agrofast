package com.example.agrofastsolutions;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyListingAdapter extends RecyclerView.Adapter<MyListingAdapter.MyListingViewHolder> {

    private List<Listing> listings;
    private OnMyListingClickListener listener;

    // ===== Click callback =====
    public interface OnMyListingClickListener {
        void onMyListingClick(Listing listing);
    }

    public MyListingAdapter(List<Listing> listings, OnMyListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_listing_card, parent, false);
        return new MyListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing, listener);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    // ===== ViewHolder =====
    static class MyListingViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgMyListing;
        private TextView tvMyCrop, tvMyQty, tvMyLocation, tvMyStatus, tvMyPrice;

        public MyListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMyListing  = itemView.findViewById(R.id.imgMyListing);
            tvMyCrop      = itemView.findViewById(R.id.tvMyCrop);
            tvMyQty       = itemView.findViewById(R.id.tvMyQty);
            tvMyLocation  = itemView.findViewById(R.id.tvMyLocation);
            tvMyStatus    = itemView.findViewById(R.id.tvMyStatus);
            tvMyPrice     = itemView.findViewById(R.id.tvMyPrice);
        }

        public void bind(Listing listing, OnMyListingClickListener listener) {

            // Crop
            tvMyCrop.setText(listing.getCropType() != null
                    ? listing.getCropType() : "Crop");

            // Quantity + unit
            String unit = (listing.getUnit() != null) ? listing.getUnit() : "kg";
            tvMyQty.setText("Qty: " + formatNumber(listing.getQuantityAvailable()) + " " + unit);

            // Location
            tvMyLocation.setText("📍 " + (listing.getLocation() != null
                    ? listing.getLocation() : "Unknown"));

            // Price
            tvMyPrice.setText("TSh " + formatNumber(listing.getAskingPrice()) + "/" + unit);

            // Status badge + color
            String status = listing.getStatus() != null
                    ? listing.getStatus().toLowerCase() : "active";
            tvMyStatus.setText(status.toUpperCase());

            switch (status) {
                case "active":
                    tvMyStatus.setBackgroundResource(R.drawable.status_completed);
                    tvMyStatus.setTextColor(Color.parseColor("#27500A")); // green
                    break;
                case "fulfilled":
                    tvMyStatus.setBackgroundResource(R.drawable.status_accepted);
                    tvMyStatus.setTextColor(Color.parseColor("#0C447C")); // blue
                    break;
                case "expired":
                    tvMyStatus.setBackgroundResource(R.drawable.status_declined);
                    tvMyStatus.setTextColor(Color.parseColor("#B71C1C")); // red
                    break;
                default:
                    tvMyStatus.setBackgroundResource(R.drawable.status_default);
                    tvMyStatus.setTextColor(Color.parseColor("#4E342E"));
                    break;
            }

            // Fade inactive listings slightly
            if ("expired".equalsIgnoreCase(status) || "fulfilled".equalsIgnoreCase(status)) {
                itemView.setAlpha(0.55f);
            } else {
                itemView.setAlpha(1.0f);
            }

            // Crop icon
            imgMyListing.setImageResource(getCropIcon(listing.getCropType()));

            // Whole card is clickable
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMyListingClick(listing);
            });
        }

        // Format 500.0 → "500", 500.5 → "500.5"
        private String formatNumber(double value) {
            if (value == (long) value) return String.valueOf((long) value);
            return String.valueOf(value);
        }

        // Crop icon placeholder
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