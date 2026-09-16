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

public class ReceivingFragment extends Fragment implements Show_order.FilterableFragment {

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

        adapter = new OfferAdapter(
                filteredOffers,
                offer -> Toast.makeText(getContext(),
                        "Offer from " + offer.getBuyerName(), Toast.LENGTH_SHORT).show(),
                new OfferAdapter.OnOfferActionListener() {
                    @Override public void onAccept(Offer offer) { handleAccept(offer); }
                    @Override public void onDecline(Offer offer) { handleDecline(offer); }
                },
                OfferAdapter.MODE_RECEIVED
        );

        recyclerView.setAdapter(adapter);

        loadReceivedOffers();

        return view;
    }

    private void loadReceivedOffers() {
        progressBar.setVisibility(View.VISIBLE);

        repository.getReceivedOffers(new AgrofastRepository.DataCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> offers) {
                progressBar.setVisibility(View.GONE);

                allOffers.clear();
                filteredOffers.clear();

                if (offers != null) allOffers.addAll(offers);
                filteredOffers.addAll(allOffers);

                adapter.notifyDataSetChanged();

                if (allOffers.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No received offers yet");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                DevLogger.logError("ReceivingFragment load", error, null);

                Toast.makeText(getContext(),
                        DevLogger.toUserMessage(error),
                        Toast.LENGTH_SHORT).show();

                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText(DevLogger.toUserMessage(error));
            }
        });
    }

    private void handleAccept(Offer offer) {
        progressBar.setVisibility(View.VISIBLE);

        repository.acceptOffer(offer.getOfferId(), new AgrofastRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressBar.setVisibility(View.GONE);

                allOffers.remove(offer);
                filteredOffers.remove(offer);
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(),
                        "Offer accepted! Order created.",
                        Toast.LENGTH_SHORT).show();

                if (allOffers.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No received offers yet");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                DevLogger.logError("ReceivingFragment accept", error, null);

                Toast.makeText(getContext(),
                        DevLogger.toUserMessage(error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDecline(Offer offer) {
        progressBar.setVisibility(View.VISIBLE);

        repository.declineOffer(offer.getOfferId(), new AgrofastRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressBar.setVisibility(View.GONE);

                allOffers.remove(offer);
                filteredOffers.remove(offer);
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(),
                        "Offer declined.",
                        Toast.LENGTH_SHORT).show();

                if (allOffers.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("No received offers yet");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                DevLogger.logError("ReceivingFragment decline", error, null);

                Toast.makeText(getContext(),
                        DevLogger.toUserMessage(error),
                        Toast.LENGTH_SHORT).show();
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
                        (offer.getBuyerName() != null &&
                                offer.getBuyerName().toLowerCase().contains(query.toLowerCase()))) {
                    filteredOffers.add(offer);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}