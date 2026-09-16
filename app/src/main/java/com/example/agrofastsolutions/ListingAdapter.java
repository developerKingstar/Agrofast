package com.example.agrofastsolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private List<Listing> listings;
    private OnListingClickListener listener;

    // ===== Click callback interface =====
    public interface OnListingClickListener {
        void onListingClick(Listing listing);
    }

    // ===== Constructor =====
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

    // ===== ViewHolder =====
    static class ListingViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgCrop;
        private TextView tvCropType, tvSeller, tvQuantity, tvLocation, tvPrice;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop     = itemView.findViewById(R.id.imgCrop);
            tvCropType  = itemView.findViewById(R.id.tvCropType);
            tvSeller    = itemView.findViewById(R.id.tvSeller);
            tvQuantity  = itemView.findViewById(R.id.tvQuantity);
            tvLocation  = itemView.findViewById(R.id.tvLocation);
            tvPrice     = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(Listing listing, OnListingClickListener listener) {

            // Crop type
            tvCropType.setText(listing.getCropType() != null
                    ? listing.getCropType() : "Crop");

            // Seller name
            tvSeller.setText("Seller: " + listing.getSellerName());

            // Quantity (show "500 kg" or "500" if unit is null)
            String unit = (listing.getUnit() != null) ? listing.getUnit() : "kg";
            tvQuantity.setText("Qty: " + formatNumber(listing.getQuantityAvailable()) + " " + unit);

            // Location
            tvLocation.setText("📍 " + (listing.getLocation() != null
                    ? listing.getLocation() : "Unknown"));

            // Price badge (as "TSh 900/kg")
            tvPrice.setText("TSh " + formatNumber(listing.getAskingPrice()) + "/" + unit);

            // Crop icon (placeholder for now, we'll swap later)
            imgCrop.setImageResource(getCropIcon(listing.getCropType()));

            // Whole card is clickable
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onListingClick(listing);
            });
        }

        // ===== Helper: format doubles without trailing ".0" =====
        private String formatNumber(double value) {
            if (value == (long) value) {
                return String.valueOf((long) value);   // 500.0 → "500"
            } else {
                return String.valueOf(value);           // 500.5 → "500.5"
            }
        }

        // ===== Helper: pick an icon based on crop type =====
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