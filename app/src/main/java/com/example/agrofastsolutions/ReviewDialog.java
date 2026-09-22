package com.example.agrofastsolutions;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.agrofastsolutions.repository.AgrofastRepository;
import com.example.agrofastsolutions.util.DevLogger;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

/**
 * Bottom-sheet dialog that lets the current user rate + review the other
 * party in an order.
 *
 * Usage:
 *   ReviewDialog.newInstance(orderId, revieweeId, revieweeName)
 *       .show(getSupportFragmentManager(), "review_dialog");
 */
public class ReviewDialog extends BottomSheetDialogFragment {

    private static final String ARG_ORDER_ID      = "order_id";
    private static final String ARG_REVIEWEE_ID   = "reviewee_id";
    private static final String ARG_REVIEWEE_NAME = "reviewee_name";

    // Callback so the caller can refresh the UI after a successful submit
    public interface OnReviewSubmitted {
        void onSubmitted();
    }

    private OnReviewSubmitted listener;

    public void setListener(OnReviewSubmitted listener) {
        this.listener = listener;
    }

    public static ReviewDialog newInstance(String orderId,
                                           String revieweeId,
                                           String revieweeName) {
        ReviewDialog d = new ReviewDialog();
        Bundle b = new Bundle();
        b.putString(ARG_ORDER_ID, orderId);
        b.putString(ARG_REVIEWEE_ID, revieweeId);
        b.putString(ARG_REVIEWEE_NAME, revieweeName);
        d.setArguments(b);
        return d;
    }

    private int selectedRating = 0;
    private ImageView[] stars = new ImageView[5];
    private TextView tvTitle;
    private EditText etComment;
    private MaterialButton btnSubmit, btnCancel;
    private AgrofastRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new AgrofastRepository(requireContext());

        tvTitle   = view.findViewById(R.id.tvReviewTitle);
        etComment = view.findViewById(R.id.etReviewComment);
        btnSubmit = view.findViewById(R.id.btnSubmitReview);
        btnCancel = view.findViewById(R.id.btnCancelReview);

        stars[0] = view.findViewById(R.id.star1);
        stars[1] = view.findViewById(R.id.star2);
        stars[2] = view.findViewById(R.id.star3);
        stars[3] = view.findViewById(R.id.star4);
        stars[4] = view.findViewById(R.id.star5);

        // Title
        String name = getArguments() != null
                ? getArguments().getString(ARG_REVIEWEE_NAME, "this user")
                : "this user";
        tvTitle.setText("Rate " + name);

        // Star tap handlers
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }

        // Default to 5 stars (positive default)
        setRating(5);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(
                    i < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_empty
            );
        }
    }

    private void submitReview() {
        if (selectedRating < 1) {
            Toast.makeText(getContext(),
                    "Please pick a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId    = getArguments() != null ? getArguments().getString(ARG_ORDER_ID) : null;
        String revieweeId = getArguments() != null ? getArguments().getString(ARG_REVIEWEE_ID) : null;
        String comment    = etComment.getText() != null
                ? etComment.getText().toString().trim()
                : "";

        if (orderId == null || revieweeId == null) {
            Toast.makeText(getContext(),
                    "Missing order info", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        repository.submitReview(orderId, revieweeId, selectedRating, comment,
                new AgrofastRepository.DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(),
                                "Thanks for your review!",
                                Toast.LENGTH_SHORT).show();

                        if (listener != null) listener.onSubmitted();
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                        DevLogger.logError("ReviewDialog submit", error, null);
                        Toast.makeText(getContext(),
                                DevLogger.toUserMessage(error),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}