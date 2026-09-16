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

public class DeclinedFragment extends Fragment implements Show_order.FilterableFragment {

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

        adapter = new OrderAdapter(filteredOrders, order ->
                Toast.makeText(getContext(), "Declined: " + order.getCropType(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        loadDeclinedOrders();

        return view;
    }

    private void loadDeclinedOrders() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getDeclinedOrders(new AgrofastRepository.DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                progressBar.setVisibility(View.GONE);

                allOrders.clear();
                filteredOrders.clear();

                if (orders != null) allOrders.addAll(orders);
                filteredOrders.addAll(allOrders);

                adapter.notifyDataSetChanged();

                if (allOrders.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No declined orders yet");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                DevLogger.logError("DeclinedFragment load", error, null);

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
}