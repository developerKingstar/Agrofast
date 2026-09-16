package com.example.agrofastsolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgCrop;
        private TextView tvCropType, tvBuyer, tvSeller, tvQuantity, tvPrice, tvStatus, tvNote;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop = itemView.findViewById(R.id.imgOrderCrop);
            tvCropType = itemView.findViewById(R.id.tvOrderCropType);
            tvBuyer = itemView.findViewById(R.id.tvOrderBuyer);
            tvSeller = itemView.findViewById(R.id.tvOrderSeller);
            tvQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvNote = itemView.findViewById(R.id.tvOrderNote);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            tvCropType.setText(order.getCropType() != null ? order.getCropType() : "Crop");
            tvBuyer.setText("Buyer: " + (order.getBuyerName() != null ? order.getBuyerName() : "—"));
            tvSeller.setText("Seller: " + (order.getSellerName() != null ? order.getSellerName() : "—"));
            tvQuantity.setText("Qty: " + order.getQuantity() + "kg");
            tvPrice.setText(order.getCurrency() + " " + order.getPrice());
            tvStatus.setText(order.getStatus() != null ? order.getStatus().toUpperCase() : "—");
            tvNote.setText(order.getNote() != null ? order.getNote() : "");

            // Status color
            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case "confirmed":
                        tvStatus.setBackgroundResource(R.drawable.status_accepted);
                        tvStatus.setTextColor(0xFF0C447C);
                        break;
                    case "completed":
                        tvStatus.setBackgroundResource(R.drawable.status_completed);
                        tvStatus.setTextColor(0xFF27500A);
                        break;
                    case "declined":
                        tvStatus.setBackgroundResource(R.drawable.status_declined);
                        tvStatus.setTextColor(0xFFB71C1C);
                        break;
                    default:
                        tvStatus.setBackgroundResource(R.drawable.status_default);
                        tvStatus.setTextColor(0xFF4E342E);
                        break;
                }
            }

            imgCrop.setImageResource(getCropIcon(order.getCropType()));
            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }

        private int getCropIcon(String cropType) {
            if (cropType == null) return android.R.drawable.ic_menu_gallery;
            switch (cropType.toLowerCase()) {
                case "maize": return android.R.drawable.ic_menu_gallery;
                case "rice": return android.R.drawable.ic_menu_agenda;
                case "beans": return android.R.drawable.ic_menu_my_calendar;
                default: return android.R.drawable.ic_menu_upload;
            }
        }
    }
}