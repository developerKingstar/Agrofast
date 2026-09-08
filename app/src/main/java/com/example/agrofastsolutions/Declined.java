package com.example.agrofastsolutions;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Declined extends Fragment {

    public Declined() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_declined, container, false);
        // TODO: card tap not wired to anything yet -- add a real
        // order-details/review screen later if needed, same as Receiving.
    }
}