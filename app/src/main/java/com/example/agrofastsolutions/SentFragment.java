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

public class SentFragment extends Fragment implements Show_order.FilterableFragment {

    private RecyclerView recyclerView;
    private OfferAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private List<Offer> allOffers = new ArrayList<>();
    private List<Offer> filteredOffers = new ArrayList<>();
    private AgrofastRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        repository = new AgrofastRepository(requireContext());

        adapter = new OfferAdapter(filteredOffers, offer -> {
            // TODO: Navigate to offer detail
            Toast.makeText(getContext(), "Offer: " + offer.getCropType(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        loadSentOffers();

        return view;
    }

    private void loadSentOffers() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getSentOffers(new AgrofastRepository.DataCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> offers) {
                progressBar.setVisibility(View.GONE);
                allOffers.clear();

                // Safe handling
                if (offers != null) {
                    allOffers.addAll(offers);
                }

                filteredOffers.clear();
                filteredOffers.addAll(allOffers);
                adapter.notifyDataSetChanged();

                if (allOffers.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("You haven't sent any offers yet");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                // Dev: full detail to Logcat (tag: AGROFAST_DEV)
                DevLogger.logError("SentFragment load", error, null);

                // User: friendly message
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
        filteredOffers.clear();
        if (query.isEmpty()) {
            filteredOffers.addAll(allOffers);
        } else {
            for (Offer offer : allOffers) {
                if (offer.getCropType().toLowerCase().contains(query.toLowerCase()) ||
                        offer.getSellerName().toLowerCase().contains(query.toLowerCase())) {
                    filteredOffers.add(offer);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}