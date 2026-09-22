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

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private OnOrderActionListener actionListener;

    // Click on whole card
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    // Click on Settle / Decline buttons
    public interface OnOrderActionListener {
        void onSettle(Order order);
        void onDecline(Order order, String reason, double feeAmount);
    }

    // Constructor (backward compatible)
    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this(orders, listener, null);
    }

    public OrderAdapter(List<Order> orders,
                        OnOrderClickListener listener,
                        OnOrderActionListener actionListener) {
        this.orders = orders;
        this.listener = listener;
        this.actionListener = actionListener;
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
        holder.bind(order, listener, actionListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgCrop;
        private TextView tvCropType, tvBuyer, tvSeller, tvQuantity, tvPrice, tvStatus, tvNote;
        private TextView tvOrderContact;

        private TextView tvDeclineInfo;       // ✅ NEW
        private TextView tvGraceInfo;         // ✅ NEW
        private LinearLayout orderActionButtons;
        private MaterialButton btnSettle, btnDeclineOrder;



        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCrop             = itemView.findViewById(R.id.imgOrderCrop);
            tvCropType          = itemView.findViewById(R.id.tvOrderCropType);
            tvBuyer             = itemView.findViewById(R.id.tvOrderBuyer);
            tvSeller            = itemView.findViewById(R.id.tvOrderSeller);
            tvQuantity          = itemView.findViewById(R.id.tvOrderQuantity);
            tvPrice             = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus            = itemView.findViewById(R.id.tvOrderStatus);
            tvNote              = itemView.findViewById(R.id.tvOrderNote);
            tvOrderContact      = itemView.findViewById(R.id.tvOrderContact);
            tvDeclineInfo       = itemView.findViewById(R.id.tvDeclineInfo);
            tvGraceInfo         = itemView.findViewById(R.id.tvGraceInfo);
            orderActionButtons  = itemView.findViewById(R.id.orderActionButtons);
            btnSettle           = itemView.findViewById(R.id.btnSettle);
            btnDeclineOrder     = itemView.findViewById(R.id.btnDeclineOrder);
        }

        public void bind(Order order, OnOrderClickListener listener, OnOrderActionListener actionListener) {
            tvCropType.setText(order.getCropType() != null ? order.getCropType() : "Crop");
            tvBuyer.setText("Buyer: " + (order.getBuyerName() != null ? order.getBuyerName() : "—"));
            tvSeller.setText("Seller: " + (order.getSellerName() != null ? order.getSellerName() : "—"));
            tvQuantity.setText("Qty: " + order.getQuantity() + "kg");
            tvPrice.setText(order.getCurrency() + " " + order.getPrice());
            tvStatus.setText(order.getStatus() != null ? order.getStatus().toUpperCase() : "—");
            tvNote.setText(order.getNote() != null ? order.getNote() : "");

            // ==========================================
            // CONTACT REVEAL — labeled Buyer / Seller
            // ==========================================
            String buyerPhone  = order.getBuyerPhone();
            String sellerPhone = order.getSellerPhone();

            boolean hasBuyer  = (buyerPhone  != null && !buyerPhone.isEmpty());
            boolean hasSeller = (sellerPhone != null && !sellerPhone.isEmpty());

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

            // ==========================================
            // DECLINED INFO — reason + fee (or grace-period note)
            // ==========================================
            boolean isDeclined = "declined".equalsIgnoreCase(order.getStatus());

            if (isDeclined) {
                String reason = order.getDeclineReason();
                double fee = order.getDeclineFeeAmount();
                String feeStatus = order.getDeclineFeeStatus();

                // --- Decline info box (always shown for declined) ---
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

                // --- Grace-period note (only when no fee was charged) ---
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


            // ==========================================
            // ACTION BUTTONS — show only when status = "confirmed"
            // ==========================================
            boolean isConfirmed = "confirmed".equalsIgnoreCase(order.getStatus());

            if (isConfirmed && actionListener != null) {
                orderActionButtons.setVisibility(View.VISIBLE);

                // SETTLE button
                // SETTLE button
                btnSettle.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Mark order as settled?")
                            .setMessage("This will move the order to Completed and unlock review + favourite options.")
                            .setPositiveButton("Mark Settled", (d, w) -> {
                                if (actionListener != null) {
                                    actionListener.onSettle(order);
                                }
                                // Show the follow-up popup a beat later, so the user sees the state change
                                v.postDelayed(() -> showPostSettlePopup(v, order), 600);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
                // DECLINE button
                btnDeclineOrder.setOnClickListener(v -> showDeclineDialog(v, order, actionListener));
            } else {
                orderActionButtons.setVisibility(View.GONE);
                btnSettle.setOnClickListener(null);
                btnDeclineOrder.setOnClickListener(null);
            }

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
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOrderClick(order);
            });
        }

        // ==========================================
        // DECLINE DIALOG — reason + auto grace period check
        // ==========================================
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

        // ==========================================
        // POST-SETTLE POPUP — offer Review + Favourite
        // ==========================================
        private void showPostSettlePopup(View v, Order order) {

            // Who is the "other party"?
            android.content.Context ctx = v.getContext();
            android.content.SharedPreferences prefs =
                    ctx.getSharedPreferences("agrofast_prefs", android.content.Context.MODE_PRIVATE);
            String currentUserId = prefs.getString("user_id", "");

            boolean iAmBuyer = currentUserId != null
                    && currentUserId.equals(order.getBuyerId());

            String otherUserId = iAmBuyer ? order.getSellerUserId() : order.getBuyerUserId();
            String otherName   = iAmBuyer ? order.getSellerName()   : order.getBuyerName();
            if (otherName == null || otherName.isEmpty()) otherName = "the other party";

            final String finalOtherId   = otherUserId;
            final String finalOtherName = otherName;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Order settled! 🎉")
                    .setMessage("Would you like to rate " + finalOtherName + " or add them to favourites?")
                    .setPositiveButton("⭐ Rate", (d, w) -> {
                        if (finalOtherId == null || finalOtherId.isEmpty()) {
                            Toast.makeText(v.getContext(),
                                    "Could not identify the other party",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (v.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                            ReviewDialog dialog = ReviewDialog.newInstance(
                                    order.getOrderId(), finalOtherId, finalOtherName);
                            dialog.show(((androidx.fragment.app.FragmentActivity) v.getContext())
                                    .getSupportFragmentManager(), "review_dialog");
                        }
                    })
                    .setNeutralButton("❤️ Favourite", (d, w) -> {
                        if (finalOtherId == null || finalOtherId.isEmpty()) {
                            Toast.makeText(v.getContext(),
                                    "Could not identify the other party",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AgrofastRepository repo = new AgrofastRepository(v.getContext());
                        repo.addFavourite(finalOtherId, new AgrofastRepository.DataCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(v.getContext(),
                                        finalOtherName + " added to favourites ❤️",
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                DevLogger.logError("OrderAdapter favourite", error, null);
                                Toast.makeText(v.getContext(),
                                        DevLogger.toUserMessage(error),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Skip", null)
                    .show();
        }

        // ==========================================
        // GRACE PERIOD + NUISANCE FEE
        //   Rule:
        //     - ≤ 24h since accepted_at → fee = 0
        //     - > 24h since accepted_at → fee = 10% of (price × qty)
        //   NOTE: calculated client-side (acceptable for demo).
        // ==========================================
        private static final long GRACE_PERIOD_MS = 24L * 60 * 60 * 1000;  // 24 hours
        private static final double NUISANCE_FEE_RATE = 0.10;              // ✅ 10%

        private double calculateFee(Order order) {
            if (order.getAcceptedAt() == null) {
                android.util.Log.d("OrderAdapter",
                        "calculateFee: no accepted_at → fee = 0");
                return 0;
            }

            try {
                // Supabase sends ISO 8601: "2026-09-21T14:30:00.123456+00:00"
                String iso = order.getAcceptedAt();
                if (iso.length() > 19) iso = iso.substring(0, 19);   // trim to fit pattern

                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                                java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                java.util.Date accepted = sdf.parse(iso);
                if (accepted == null) {
                    android.util.Log.d("OrderAdapter",
                            "calculateFee: unparseable accepted_at → fee = 0");
                    return 0;
                }

                long elapsedMs = System.currentTimeMillis() - accepted.getTime();
                long hoursElapsed = elapsedMs / (1000L * 60 * 60);

                boolean withinGrace = elapsedMs <= GRACE_PERIOD_MS;

                if (withinGrace) {
                    android.util.Log.d("OrderAdapter",
                            "calculateFee: " + hoursElapsed +
                                    "h elapsed → within 24h grace → fee = 0");
                    return 0;
                }

                // ✅ 10% of order value
                double orderValue = order.getPrice() * order.getQuantity();
                double fee = orderValue * NUISANCE_FEE_RATE;

                android.util.Log.d("OrderAdapter",
                        "calculateFee: " + hoursElapsed + "h elapsed → past grace → " +
                                "orderValue = TSh " + orderValue +
                                " → 10% fee = TSh " + fee);

                return fee;

            } catch (Exception e) {
                android.util.Log.e("OrderAdapter",
                        "calculateFee: parse error → fee = 0", e);
                return 0;
            }
        }

        private void dialPhone(View v, String phone) {
            try {
                Intent dial = new Intent(Intent.ACTION_DIAL);
                dial.setData(Uri.parse("tel:" + phone));
                v.getContext().startActivity(dial);
            } catch (Exception e) {
                Toast.makeText(v.getContext(),
                        "Could not open dialer", Toast.LENGTH_SHORT).show();
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