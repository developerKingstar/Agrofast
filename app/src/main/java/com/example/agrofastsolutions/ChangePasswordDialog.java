package com.example.agrofastsolutions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Simple "Change Password" popup with current/new/confirm fields.
 * The actual password-change API call is a TODO until auth is wired up.
 */
public class ChangePasswordDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        EditText etCurrent = view.findViewById(R.id.et_current_password);
        EditText etNew = view.findViewById(R.id.et_new_password);
        EditText etConfirm = view.findViewById(R.id.et_confirm_password);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String current = etCurrent.getText().toString();
                    String newPass = etNew.getText().toString();
                    String confirm = etConfirm.getText().toString();

                    if (newPass.isEmpty() || !newPass.equals(confirm)) {
                        Toast.makeText(getContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: call your backend/auth API here to actually change the
                    // password, passing `current` and `newPass` for verification.
                    Toast.makeText(getContext(), "Password updated (placeholder)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
