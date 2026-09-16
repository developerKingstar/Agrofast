package com.example.agrofastsolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    // ===== Modes =====
    public static final int MODE_SENT     = 0;  // no buttons
    public static final int MODE_RECEIVED = 1;  // show Accept/Decline

    private List<Offer> offers;
    private OnOfferClickListener listener;
    private OnOfferActionListener actionListener;
    private int mode;

    // Click on the whole card
    public interface OnOfferClickListener {
        void onOfferClick(Offer offer);
    }

    // Click on Accept/Decline
    public interface OnOfferActionListener {
        void onAccept(Offer offer);
        void onDecline(Offer offer);
    }

    // Constructor for SENT mode (no buttons)
    public OfferAdapter(List<Offer> offers, OnOfferClickListener listener) {
        this(offers, listener, null, MODE_SENT);
    }

    // Full constructor
    public OfferAdapter(List<Offer> offers,
                        OnOfferClickListener listener,
                        OnOfferActionListener actionListener,
                        int mode) {
        this.offers = offers;
        this.listener = listener;
        this.actionListener = actionListener;
        this.mode = mode;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer_card, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.bind(offer, listener, actionListener, mode);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgCrop;
        private TextView tvCropType, tvCounterparty, tvQuantity, tvPrice, tvStatus, tvNote;
        private LinearLayout actionButtons;
        private MaterialButton btnAccept, btnDecline;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop        = itemView.findViewById(R.id.imgCrop);
            tvCropType     = itemView.findViewById(R.id.tvCropType);
            tvCounterparty = itemView.findViewById(R.id.tvCounterparty);
            tvQuantity     = itemView.findViewById(R.id.tvQuantity);
            tvPrice        = itemView.findViewById(R.id.tvPrice);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvNote         = itemView.findViewById(R.id.tvNote);
            actionButtons  = itemView.findViewById(R.id.actionButtons);
            btnAccept      = itemView.findViewById(R.id.btnAccept);
            btnDecline     = itemView.findViewById(R.id.btnDecline);
        }

        public void bind(Offer offer,
                         OnOfferClickListener listener,
                         OnOfferActionListener actionListener,
                         int mode) {
            tvCropType.setText(offer.getCropType() != null ? offer.getCropType() : "Crop");
            tvCounterparty.setText(offer.getCounterpartyName());
            tvQuantity.setText("Qty: " + offer.getQuantity() + "kg");
            tvPrice.setText(offer.getCurrency() + " " + offer.getPrice() + "/kg");
            tvStatus.setText(offer.getStatus() != null ? offer.getStatus().toUpperCase() : "—");
            tvNote.setText(offer.getNote() != null ? offer.getNote() : "");

            // Status color
            if (offer.getStatus() != null) {
                switch (offer.getStatus()) {
                    case "pending":
                        tvStatus.setBackgroundResource(R.drawable.status_pending);
                        tvStatus.setTextColor(0xFF633806);
                        break;
                    case "accepted":
                        tvStatus.setBackgroundResource(R.drawable.status_accepted);
                        tvStatus.setTextColor(0xFF0C447C);
                        break;
                    case "completed":
                        tvStatus.setBackgroundResource(R.drawable.status_completed);
                        tvStatus.setTextColor(0xFF27500A);
                        break;
                    case "declined":
                    case "expired":
                    case "withdrawn":
                    case "rejected":
                        tvStatus.setBackgroundResource(R.drawable.status_declined);
                        tvStatus.setTextColor(0xFFB71C1C);
                        break;
                    default:
                        tvStatus.setBackgroundResource(R.drawable.status_default);
                        tvStatus.setTextColor(0xFF4E342E);
                        break;
                }
            }

            // ===== Show Accept/Decline buttons ONLY in Received mode AND pending status =====
            boolean showButtons = (mode == MODE_RECEIVED)
                    && "pending".equalsIgnoreCase(offer.getStatus())
                    && actionListener != null;

            if (showButtons) {
                actionButtons.setVisibility(View.VISIBLE);

                btnAccept.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onAccept(offer);
                });

                btnDecline.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onDecline(offer);
                });
            } else {
                actionButtons.setVisibility(View.GONE);
                btnAccept.setOnClickListener(null);
                btnDecline.setOnClickListener(null);
            }

            imgCrop.setImageResource(getCropIcon(offer.getCropType()));
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOfferClick(offer);
            });
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