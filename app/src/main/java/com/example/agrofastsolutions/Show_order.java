package com.example.agrofastsolutions;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Show_order extends AppCompatActivity {

    // Tabs
    private MaterialButton btnSent, btnReceived, btnOrders, btnCompleted, btnDeclined;
    private TextInputEditText etSearch;

    // Fragments
    private Fragment currentFragment;
    private SentFragment sentFragment;
    private ReceivingFragment receivingFragment;
    private PendingFragment pendingFragment;
    private CompletedFragment completedFragment;
    private DeclinedFragment declinedFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);


        // Initialize views
        initViews();

        // Set default tab (Sent)
        selectTab(btnSent);

    }

    private void initViews() {
        btnSent = findViewById(R.id.btnSentOffers);
        btnReceived = findViewById(R.id.btnReceivingOffers);
        btnOrders = findViewById(R.id.btnPendingOrders);
        btnCompleted = findViewById(R.id.btnCompletedOrders);
        btnDeclined = findViewById(R.id.btnDeclinedOrders);
        etSearch = findViewById(R.id.etSearchOrders);

        // Set click listeners
        btnSent.setOnClickListener(v -> selectTab(btnSent));
        btnReceived.setOnClickListener(v -> selectTab(btnReceived));
        btnOrders.setOnClickListener(v -> selectTab(btnOrders));
        btnCompleted.setOnClickListener(v -> selectTab(btnCompleted));
        btnDeclined.setOnClickListener(v -> selectTab(btnDeclined));

        // Search listener
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFragments(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void selectTab(MaterialButton selectedButton) {
        // Reset all button colors
        resetTabColors();

        // Highlight selected tab
        selectedButton.setBackgroundColor(getColor(R.color.agrofast_dark_green));
        selectedButton.setTextColor(getColor(android.R.color.white));

        // Show corresponding fragment
        Fragment fragment = getFragmentForTab(selectedButton);
        if (fragment != null) {
            replaceFragment(fragment);
        }
    }

    private Fragment getFragmentForTab(MaterialButton button) {
        if (button.getId() == R.id.btnSentOffers) {
            if (sentFragment == null) sentFragment = new SentFragment();
            return sentFragment;
        } else if (button.getId() == R.id.btnReceivingOffers) {
            if (receivingFragment == null) receivingFragment = new ReceivingFragment();
            return receivingFragment;
        } else if (button.getId() == R.id.btnPendingOrders) {
            if (pendingFragment == null) pendingFragment = new PendingFragment();
            return pendingFragment;
        } else if (button.getId() == R.id.btnCompletedOrders) {
            if (completedFragment == null) completedFragment = new CompletedFragment();
            return completedFragment;
        } else if (button.getId() == R.id.btnDeclinedOrders) {
            if (declinedFragment == null) declinedFragment = new DeclinedFragment();
            return declinedFragment;
        }
        return null;
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null && fragment != currentFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
            currentFragment = fragment;
        }
    }

    private void resetTabColors() {
        int defaultColor = getColor(R.color.agrofast_green);

        btnSent.setBackgroundColor(defaultColor);
        btnReceived.setBackgroundColor(defaultColor);
        btnOrders.setBackgroundColor(defaultColor);
        btnCompleted.setBackgroundColor(defaultColor);
        btnDeclined.setBackgroundColor(defaultColor);

        btnSent.setTextColor(getColor(android.R.color.white));
        btnReceived.setTextColor(getColor(android.R.color.white));
        btnOrders.setTextColor(getColor(android.R.color.white));
        btnCompleted.setTextColor(getColor(android.R.color.white));
        btnDeclined.setTextColor(getColor(android.R.color.white));
    }

    private void filterFragments(String query) {
        // Pass search query to current fragment
        if (currentFragment instanceof FilterableFragment) {
            ((FilterableFragment) currentFragment).filter(query);
        }
    }

    // Interface for fragments that support filtering
    public interface FilterableFragment {
        void filter(String query);
    }
}