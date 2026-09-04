package com.example.agrofastsolutions;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // TODO: replace with the real logged-in user's name/status once auth exists
        tvName.setText("Kingstar");
        tvStatus.setText("Active");

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
        ivProfilePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        view.findViewById(R.id.iv_edit_badge).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Change Password row
        view.findViewById(R.id.row_change_password).setOnClickListener(v -> {
            dismiss();
            new ChangePasswordDialog().show(getParentFragmentManager(), "change_password");
        });

        // Edit Details row
        view.findViewById(R.id.row_edit_details).setOnClickListener(v -> {
            dismiss();
            // TODO: startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });
    }
}
