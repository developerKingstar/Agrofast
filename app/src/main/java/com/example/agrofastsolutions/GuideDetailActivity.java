package com.example.agrofastsolutions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GuideDetailActivity extends AppCompatActivity {

    private TextView txtGuideTitle;
    private TextView txtGuideContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_guide_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find views
        txtGuideTitle = findViewById(R.id.txtGuideTitle);
        txtGuideContent = findViewById(R.id.txtGuideContent);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Get the selected guide
        String guideType = getIntent().getStringExtra("guide_type");

        // Display the correct guide
        displayGuide(guideType);
    }

    private void displayGuide(String guideType) {
        if (guideType == null) {
            showDefaultGuide();
            return;
        }

        switch (guideType) {
            case "buy":
                showBuyGuide();
                break;
            case "sell":
                showSellGuide();
                break;
            case "fee":
                showFeeGuide();
                break;
            case "ratings":
                showRatingsGuide();
                break;
            default:
                showDefaultGuide();
                break;
        }
    }

    private void showBuyGuide() {
        txtGuideTitle.setText(R.string.BuyCrops);

        String content =
                "How to buy crops\n\n" +
                        "1. Open Buy\n" +
                        "From the Dashboard or Menu, select Buy.\n\n" +
                        "2. Search for a crop\n" +
                        "Search for the crop you want, such as maize, rice or beans.\n\n" +
                        "3. Check the listings\n" +
                        "Review the available crop listings. Check the location, available quantity and asking price.\n\n" +
                        "4. Select a listing\n" +
                        "Open the listing that best matches what you need.\n\n" +
                        "5. Enter your quantity\n" +
                        "Specify how much of the crop you want to buy.\n\n" +
                        "6. Send an offer\n" +
                        "Send your offer to the farmer. You may provide an optional counter-price when supported.\n\n" +
                        "7. Wait for the farmer\n" +
                        "The farmer can accept or decline your offer. If accepted, the offer becomes an order.\n\n" +
                        "8. Complete the transaction\n" +
                        "After acceptance, you and the farmer can communicate directly to arrange logistics and final settlement.\n\n" +
                        "Note: Agrofast does not process payments directly in version 1. " +
                        "Payment and settlement happen directly between the farmer and customer.";

        txtGuideContent.setText(content);
    }

    private void showSellGuide() {
        txtGuideTitle.setText(R.string.sell_harvest);

        String content =
                "How to sell your harvest\n\n" +
                        "1. Open Sell\n" +
                        "From the Dashboard or Menu, select Sell.\n\n" +
                        "2. Create a listing\n" +
                        "Enter the details of the crop you want to sell.\n\n" +
                        "3. Enter crop information\n" +
                        "Provide the crop type, available quantity, asking price, location and harvest date.\n\n" +
                        "4. Add photos\n" +
                        "You can add photos of the harvest to help customers understand the product.\n\n" +
                        "5. Publish the listing\n" +
                        "Review the information and publish your listing.\n\n" +
                        "6. Receive offers\n" +
                        "Customers can find your listing and send offers specifying the quantity they want.\n\n" +
                        "7. Review the offer\n" +
                        "Check the customer's information, requested quantity and location.\n\n" +
                        "8. Accept or decline\n" +
                        "You can accept an offer or decline it. An accepted offer becomes an order.\n\n" +
                        "9. Complete the order\n" +
                        "After acceptance, communicate with the customer to arrange logistics and final settlement.";

        txtGuideContent.setText(content);
    }

    private void showFeeGuide() {
        txtGuideTitle.setText(R.string.nuisance);

        String content =
                "Understanding the nuisance fee\n\n" +
                        "The nuisance-fee rule helps discourage users from making commitments and then abandoning accepted orders without a valid reason.\n\n" +
                        "1. 24-hour grace period\n" +
                        "After an offer is accepted and becomes an order, either party can cancel within 24 hours without a penalty.\n\n" +
                        "2. Cancellation after 24 hours\n" +
                        "Cancelling an accepted order after the 24-hour grace period is treated as a penalized decline.\n\n" +
                        "3. Fee ledger\n" +
                        "A nuisance-fee ledger entry is created for the penalized decline.\n\n" +
                        "4. Reputation\n" +
                        "Declined-order counts and unsettled nuisance-fee status can be visible as reputation signals to other users.\n\n" +
                        "5. No direct payment in version 1\n" +
                        "Agrofast version 1 does not process payments directly. " +
                        "The nuisance fee is therefore enforced through reputation and access rules rather than automatic payment collection.\n\n" +
                        "Important:\n" +
                        "Pending offers can be withdrawn without a penalty. " +
                        "Offers that receive no response can also expire without a fee according to the application's expiry rules.";

        txtGuideContent.setText(content);
    }

    private void showRatingsGuide() {
        txtGuideTitle.setText(R.string.ratings);

        String content =
                "Ratings and reviews\n\n" +
                        "1. Complete an order\n" +
                        "Ratings and reviews are available after an order has been completed.\n\n" +
                        "2. Open the completed order\n" +
                        "Go to My Space and find the completed order.\n\n" +
                        "3. Rate the other user\n" +
                        "Give a rating from 1 to 5 stars based on your experience.\n\n" +
                        "4. Write a review\n" +
                        "You can add a comment describing your experience.\n\n" +
                        "5. Build trust\n" +
                        "Ratings are connected to the completed order and contribute to the user's visible reputation.\n\n" +
                        "Why ratings matter:\n" +
                        "Agrofast uses ratings and reviews as part of its trust system, helping farmers and customers make better decisions when dealing with each other.";

        txtGuideContent.setText(content);
    }

    @SuppressLint("SetTextI18n")
    private void showDefaultGuide() {
        txtGuideTitle.setText(R.string.user_guide);
        txtGuideContent.setText("Select a topic from the User Guide to learn how to use Agrofast.");
    }
}