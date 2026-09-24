package com.example.agrofastsolutions;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private OnOrderActionListener actionListener;
    private OnOrderSettledListener settledListener;

    // ---------- Interfaces ----------
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public interface OnOrderActionListener {
        void onSettle(Order order);
        void onDecline(Order order, String reason, double feeAmount);
    }

    public interface OnOrderSettledListener {
        void onOrderSettled(Order order);
    }

    // ---------- Constructors ----------

    // Full constructor
    public OrderAdapter(List<Order> orders,
                        OnOrderClickListener listener,
                        OnOrderActionListener actionListener,
                        OnOrderSettledListener settledListener) {
        this.orders = orders;
        this.listener = listener;
        this.actionListener = actionListener;
        this.settledListener = settledListener;
    }

    // 3-arg — no settled listener
    public OrderAdapter(List<Order> orders,
                        OnOrderClickListener listener,
                        OnOrderActionListener actionListener) {
        this(orders, listener, actionListener, null);
    }

    // 2-arg — click only
    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this(orders, listener, null, null);
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
        holder.bind(order, listener, actionListener, settledListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // ================================================================
    //  VIEW HOLDER
    // ================================================================
    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgCrop;
        private TextView tvCropType, tvBuyer, tvSeller, tvQuantity, tvPrice, tvStatus, tvNote;
        private TextView tvOrderContact;
        private TextView tvDeclineInfo;
        private TextView tvGraceInfo;
        private LinearLayout orderActionButtons;
        private MaterialButton btnSettle, btnDeclineOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop            = itemView.findViewById(R.id.imgOrderCrop);
            tvCropType         = itemView.findViewById(R.id.tvOrderCropType);
            tvBuyer            = itemView.findViewById(R.id.tvOrderBuyer);
            tvSeller           = itemView.findViewById(R.id.tvOrderSeller);
            tvQuantity         = itemView.findViewById(R.id.tvOrderQuantity);
            tvPrice            = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus           = itemView.findViewById(R.id.tvOrderStatus);
            tvNote             = itemView.findViewById(R.id.tvOrderNote);
            tvOrderContact     = itemView.findViewById(R.id.tvOrderContact);
            tvDeclineInfo      = itemView.findViewById(R.id.tvDeclineInfo);
            tvGraceInfo        = itemView.findViewById(R.id.tvGraceInfo);
            orderActionButtons = itemView.findViewById(R.id.orderActionButtons);
            btnSettle          = itemView.findViewById(R.id.btnSettle);
            btnDeclineOrder    = itemView.findViewById(R.id.btnDeclineOrder);
        }

        public void bind(Order order,
                         OnOrderClickListener listener,
                         OnOrderActionListener actionListener,
                         OnOrderSettledListener settledListener) {

            // ---------- Basic fields ----------
            tvCropType.setText(order.getCropType() != null ? order.getCropType() : "Crop");
            tvBuyer.setText("Buyer: " + (order.getBuyerName() != null ? order.getBuyerName() : "—"));
            tvSeller.setText("Seller: " + (order.getSellerName() != null ? order.getSellerName() : "—"));
            tvQuantity.setText("Qty: " + order.getQuantity() + "kg");
            tvPrice.setText(order.getCurrency() + " " + order.getPrice());
            tvStatus.setText(order.getStatus() != null ? order.getStatus().toUpperCase() : "—");
            tvNote.setText(order.getNote() != null ? order.getNote() : "");

            // ---------- Contact reveal ----------
            String buyerPhone  = order.getBuyerPhone();
            String sellerPhone = order.getSellerPhone();
            boolean hasBuyer   = buyerPhone  != null && !buyerPhone.isEmpty();
            boolean hasSeller  = sellerPhone != null && !sellerPhone.isEmpty();

            if (hasBuyer || hasSeller) {
                StringBuilder contactText = new StringBuilder();
                if (hasBuyer)  contactText.append("🛒 Buyer:  ").append(buyerPhone);
                if (hasSeller) {
                    if (contactText.length() > 0) contactText.append("\n");
                    contactText.append("🌾 Seller: ").append(sellerPhone);
                }
                tvOrderContact.setText(contactText.toString());
                tvOrderContact.setVisibility(View.VISIBLE);

                tvOrderContact.setOnClickListener(v -> {
                    if (hasBuyer && hasSeller) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Call which party?")
                                .setItems(new String[]{
                                        "🛒 Buyer:  " + buyerPhone,
                                        "🌾 Seller: " + sellerPhone
                                }, (dialog, which) -> {
                                    String phone = (which == 0) ? buyerPhone : sellerPhone;
                                    dialPhone(v, phone);
                                })
                                .show();
                    } else {
                        dialPhone(v, hasBuyer ? buyerPhone : sellerPhone);
                    }
                });
            } else {
                tvOrderContact.setVisibility(View.GONE);
            }

            // ---------- Declined info ----------
            boolean isDeclined = "declined".equalsIgnoreCase(order.getStatus());
            if (isDeclined) {
                String reason    = order.getDeclineReason();
                double fee       = order.getDeclineFeeAmount();
                String feeStatus = order.getDeclineFeeStatus();

                StringBuilder info = new StringBuilder();
                info.append("📋 Reason: ")
                        .append(reason != null && !reason.isEmpty() ? reason : "—");

                if (fee > 0) {
                    info.append("\n⚠️ Nuisance fee: TSh ")
                            .append(String.format(java.util.Locale.US, "%,.0f", fee));
                    if (feeStatus != null && !feeStatus.isEmpty()) {
                        info.append("  (").append(feeStatus).append(")");
                    }
                } else {
                    info.append("\n✅ Fee: None");
                }

                tvDeclineInfo.setText(info.toString());
                tvDeclineInfo.setVisibility(View.VISIBLE);

                if (fee <= 0) {
                    tvGraceInfo.setText("✅ No fee — declined within 24h grace period");
                    tvGraceInfo.setVisibility(View.VISIBLE);
                } else {
                    tvGraceInfo.setVisibility(View.GONE);
                }
            } else {
                tvDeclineInfo.setVisibility(View.GONE);
                tvGraceInfo.setVisibility(View.GONE);
            }

            // ---------- Action buttons ----------
            boolean isConfirmed = "confirmed".equalsIgnoreCase(order.getStatus());

            if (isConfirmed && actionListener != null) {
                orderActionButtons.setVisibility(View.VISIBLE);

                btnSettle.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Mark order as settled?")
                            .setMessage("This will move the order to Completed and unlock review + favourite options.")
                            .setPositiveButton("Mark Settled", (d, w) -> {
                                if (actionListener != null) {
                                    actionListener.onSettle(order);
                                }
                                if (settledListener != null) {
                                    settledListener.onOrderSettled(order);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                btnDeclineOrder.setOnClickListener(v ->
                        showDeclineDialog(v, order, actionListener));
            } else {
                orderActionButtons.setVisibility(View.GONE);
                btnSettle.setOnClickListener(null);
                btnDeclineOrder.setOnClickListener(null);
            }

            // ---------- Status pill ----------
            if (order.getStatus() != null) {
                android.util.TypedValue tv = new android.util.TypedValue();
                itemView.getContext().getTheme().resolveAttribute(
                        com.google.android.material.R.attr.colorOnPrimary,
                        tv,
                        true
                );
                int onPrimary = tv.data;
                tvStatus.setBackgroundResource(R.drawable.pill_translucent);
                tvStatus.setTextColor(onPrimary);
            }

            // ---------- Crop image ----------
            imgCrop.setImageResource(getCropIcon(order.getCropType()));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOrderClick(order);
            });
        }

        // ---------- Decline dialog ----------
        private void showDeclineDialog(View v, Order order, OnOrderActionListener actionListener) {
            final String[] reasons = {
                    "Price not agreed",
                    "Crop quality issue",
                    "Quantity mismatch",
                    "Buyer not reachable",
                    "Seller not reachable",
                    "Changed mind",
                    "Other"
            };

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Why are you declining?")
                    .setItems(reasons, (d, which) -> {
                        String reason = reasons[which];
                        double fee = calculateFee(order);
                        if (actionListener != null) {
                            actionListener.onDecline(order, reason, fee);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        // ---------- Fee calculation ----------
        private static final long GRACE_PERIOD_MS = 24L * 60 * 60 * 1000;
        private static final double NUISANCE_FEE_RATE = 0.10;

        private double calculateFee(Order order) {
            if (order.getAcceptedAt() == null) {
                android.util.Log.d("OrderAdapter", "calculateFee: no accepted_at → fee = 0");
                return 0;
            }
            try {
                String iso = order.getAcceptedAt();
                if (iso.length() > 19) iso = iso.substring(0, 19);

                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                                java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                java.util.Date accepted = sdf.parse(iso);
                if (accepted == null) {
                    android.util.Log.d("OrderAdapter", "calculateFee: unparseable → fee = 0");
                    return 0;
                }

                long elapsedMs = System.currentTimeMillis() - accepted.getTime();
                long hoursElapsed = elapsedMs / (1000L * 60 * 60);

                if (elapsedMs <= GRACE_PERIOD_MS) {
                    android.util.Log.d("OrderAdapter",
                            "calculateFee: " + hoursElapsed + "h → within grace → fee = 0");
                    return 0;
                }

                double orderValue = order.getPrice() * order.getQuantity();
                double fee = orderValue * NUISANCE_FEE_RATE;

                android.util.Log.d("OrderAdapter",
                        "calculateFee: " + hoursElapsed + "h → past grace → fee = TSh " + fee);
                return fee;

            } catch (Exception e) {
                android.util.Log.e("OrderAdapter", "calculateFee: parse error → fee = 0", e);
                return 0;
            }
        }

        private void dialPhone(View v, String phone) {
            try {
                Intent dial = new Intent(Intent.ACTION_DIAL);
                dial.setData(Uri.parse("tel:" + phone));
                v.getContext().startActivity(dial);
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Could not open dialer", Toast.LENGTH_SHORT).show();
            }
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