package com.example.agrofastsolutions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.agrofastsolutions.auth.SupabaseAuthManager;
import com.example.agrofastsolutions.util.DevLogger;

/**
 * Change password popup.
 * Re-authenticates with current password, then updates via Supabase Auth.
 */
public class ChangePasswordDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null);

        EditText etCurrent = view.findViewById(R.id.et_current_password);
        EditText etNew     = view.findViewById(R.id.et_new_password);
        EditText etConfirm = view.findViewById(R.id.et_confirm_password);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                String current = etCurrent.getText().toString();
                String newPass = etNew.getText().toString();
                String confirm = etConfirm.getText().toString();

                // Validation
                if (TextUtils.isEmpty(current)) { etCurrent.setError("Enter current password"); return; }
                if (TextUtils.isEmpty(newPass)) { etNew.setError("Enter new password"); return; }
                if (newPass.length() < 6)       { etNew.setError("At least 6 characters"); return; }
                if (!newPass.equals(confirm))   { etConfirm.setError("Passwords don't match"); return; }
                if (newPass.equals(current))    { etNew.setError("New must differ from current"); return; }

                // Call Supabase
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Saving...");

                SupabaseAuthManager auth = new SupabaseAuthManager(requireContext());
                auth.changePassword(current, newPass, new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String ignored) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Save");

                        Toast.makeText(requireContext(),
                                "Password updated successfully.",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Save");

                        DevLogger.logError("ChangePasswordDialog", error, null);
                        Toast.makeText(requireContext(),
                                DevLogger.toUserMessage(error),
                                Toast.LENGTH_LONG).show();
                    }
                });
            });
        });

        return dialog;
    }
}