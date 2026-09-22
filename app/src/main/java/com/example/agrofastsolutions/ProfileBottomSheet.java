package com.example.agrofastsolutions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Slides up from the bottom when the profile icon is tapped.
 * Shows photo (tap to change), name, status, Change Password, Edit Details.
 */
public class ProfileBottomSheet extends BottomSheetDialogFragment {

    private ImageView ivProfilePhoto;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvStatus = view.findViewById(R.id.tv_profile_status);

        // Load real name from prefs (saved at login/signup)
        SupabaseAuthManager auth = new SupabaseAuthManager(requireContext());
        String userName = auth.getCurrentUserName();
        tvName.setText((userName == null || userName.isEmpty()) ? "User" : userName);
        tvStatus.setText("Active");

        // ✅ Load the real profile photo from SharedPreferences
        loadProfilePhoto();

        // Modern (non-deprecated) way to open the device's photo picker.
        // Must be registered before the view/dialog is shown.
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {
                    if (uri != null) {
                        ivProfilePhoto.setImageURI(uri);
                        // TODO: upload `uri` to your backend / Supabase Storage,
                        // then save the resulting photo URL for this user.
                    }
                }
        );

        // Tapping either the photo or the little camera badge opens the picker
        //ivProfilePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        //view.findViewById(R.id.iv_edit_badge).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Change Password row
        view.findViewById(R.id.row_change_password).setOnClickListener(v -> {
            dismiss();
            new ChangePasswordDialog().show(requireActivity().getSupportFragmentManager(), "change_password");
        });

        // Edit Details row
        view.findViewById(R.id.row_edit_details).setOnClickListener(v -> {
            dismiss();
            startActivity(new Intent(requireContext(), ProfileActivity.class));
        });
    }

    // ==========================================
    // PROFILE PHOTO — load from SharedPreferences via Glide
    // ==========================================
    private void loadProfilePhoto() {
        if (ivProfilePhoto == null) return;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("agrofast_prefs", Context.MODE_PRIVATE);
        String photoUrl = prefs.getString("profile_photo_url", "");

        if (photoUrl != null && !photoUrl.isEmpty()) {
            // ✅ Real photo exists — load it with Glide
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_account)
                    .circleCrop()
                    .into(ivProfilePhoto);
        } else {
            // ⚙️ No photo yet — show the default icon
            ivProfilePhoto.setImageResource(R.drawable.ic_account);
        }
    }
}