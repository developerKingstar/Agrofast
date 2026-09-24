package com.example.agrofastsolutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;

import java.util.ArrayList;
import java.util.List;

public class PendingFragment extends Fragment implements Show_order.FilterableFragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private AgrofastRepository repository;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBarOrders);
        tvEmptyState = view.findViewById(R.id.tvEmptyStateOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        repository = new AgrofastRepository(requireContext());

        adapter = new OrderAdapter(
                filteredOrders,
                order -> Toast.makeText(getContext(),
                        "Order: " + order.getCropType(), Toast.LENGTH_SHORT).show(),
                new OrderAdapter.OnOrderActionListener() {
                    @Override
                    public void onSettle(Order order) {
                        handleSettle(order);
                    }

                    @Override
                    public void onDecline(Order order, String reason, double feeAmount) {
                        handleDecline(order, reason, feeAmount);
                    }
                },
                new OrderAdapter.OnOrderSettledListener() {
                    @Override
                    public void onOrderSettled(Order order) {
                        // Small delay so the network PATCH completes first
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed(() -> showPostSettlePopup(order), 800);
                    }
                }
        );
        recyclerView.setAdapter(adapter);

        loadActiveOrders();

        return view;
    }

    private void showPostSettlePopup(Order order) {

        if (!isAdded() || getActivity() == null) return;

        android.content.SharedPreferences prefs =
                requireActivity().getSharedPreferences("agrofast_prefs",
                        android.content.Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", "");

        boolean iAmBuyer = currentUserId != null && currentUserId.equals(order.getBuyerId());
        String otherUserId = iAmBuyer ? order.getSellerUserId() : order.getBuyerUserId();
        String otherName   = iAmBuyer ? order.getSellerName()   : order.getBuyerName();
        if (otherName == null || otherName.isEmpty()) otherName = "the other party";

        final String finalOtherId   = otherUserId;
        final String finalOtherName = otherName;

        new androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                .setTitle("Order settled! 🎉")
                .setMessage("Would you like to rate " + finalOtherName + " or add them to favourites?")
                .setPositiveButton("⭐ Rate", (d, w) -> {
                    if (finalOtherId == null || finalOtherId.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Could not identify the other party",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!isAdded()) return;

                    androidx.fragment.app.FragmentManager fm = getParentFragmentManager();
                    if (fm.isStateSaved()) {
                        Toast.makeText(getContext(),
                                "Please try again in a moment",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ReviewDialog dialog = ReviewDialog.newInstance(
                            order.getOrderId(), finalOtherId, finalOtherName);
                    dialog.show(fm, "review_dialog");
                })
                .setNeutralButton("❤️ Favourite", (d, w) -> {
                    if (finalOtherId == null || finalOtherId.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Could not identify the other party",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AgrofastRepository repo = new AgrofastRepository(requireContext());
                    repo.addFavourite(finalOtherId,
                            new AgrofastRepository.DataCallback<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(),
                                            finalOtherName + " added to favourites ❤️",
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String error) {
                                    DevLogger.logError("PendingFragment favourite", error, null);
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(),
                                            DevLogger.toUserMessage(error),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Skip", null)
                .show();
    }

    private void loadActiveOrders() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getActiveOrders(new AgrofastRepository.DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                progressBar.setVisibility(View.GONE);
                allOrders.clear();
                if (orders != null) allOrders.addAll(orders);
                filteredOrders.clear();
                filteredOrders.addAll(allOrders);
                adapter.notifyDataSetChanged();

                if (allOrders.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No active orders");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                DevLogger.logError("PendingFragment load", error, null);

                Toast.makeText(getContext(),
                        DevLogger.toUserMessage(error),
                        Toast.LENGTH_SHORT).show();

                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText(DevLogger.toUserMessage(error));
            }
        });
    }

    @Override
    public void filter(String query) {
        filteredOrders.clear();
        if (query.isEmpty()) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getCropType().toLowerCase().contains(query.toLowerCase()) ||
                        (order.getBuyerName() != null && order.getBuyerName().toLowerCase().contains(query.toLowerCase())) ||
                        (order.getSellerName() != null && order.getSellerName().toLowerCase().contains(query.toLowerCase()))) {
                    filteredOrders.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ==========================================
    // SETTLE ORDER
    // ==========================================
    private void handleSettle(Order order) {
        progressBar.setVisibility(View.VISIBLE);

        repository.settleOrder(order.getOrderId(), new AgrofastRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Order settled! Check Completed tab.",
                        Toast.LENGTH_LONG).show();
                loadActiveOrders();  // refresh list
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                DevLogger.logError("PendingFragment settle", error, null);
                Toast.makeText(getContext(),
                        DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ==========================================
    // DECLINE ORDER
    // ==========================================
    private void handleDecline(Order order, String reason, double feeAmount) {
        progressBar.setVisibility(View.VISIBLE);

        repository.declineOrder(order.getOrderId(), reason, feeAmount,
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressBar.setVisibility(View.GONE);

                        String msg = feeAmount > 0
                                ? "Order declined. Nuisance fee applied: TSh " + (int) feeAmount
                                : "Order declined. No fee (within 24h grace period).";

                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        loadActiveOrders();  // refresh list
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        DevLogger.logError("PendingFragment decline", error, null);
                        Toast.makeText(getContext(),
                                DevLogger.toUserMessage(error), Toast.LENGTH_LONG).show();
                    }
                });
    }
}