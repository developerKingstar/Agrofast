package com.example.agrofastsolutions;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Pending extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Pending() {
        // Required empty public constructor
    }

    public static Pending newInstance(String param1, String param2) {
        Pending fragment = new Pending();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflate the fragment layout and store it in a View variable
        View view = inflater.inflate(R.layout.fragment_pending, container, false);

        // 2. Find the CardView using the inflated 'view'
        CardView cardView = view.findViewById(R.id.cardOrder1);

        // 3. Set the OnClickListener
        if (cardView != null) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use requireContext() or getActivity() instead of Show_order.this
                    Intent intent = new Intent(requireContext(), Edit_and_suspend.class);
                    startActivity(intent);
                }
            });
        }

        // 4. Return the root view
        return view;
    }
}
