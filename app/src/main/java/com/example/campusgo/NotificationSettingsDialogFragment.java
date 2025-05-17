package com.example.campusgo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NotificationSettingsDialogFragment extends DialogFragment {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_NOTIF_EMAIL = "notif_email";
    private static final String KEY_NOTIF_PUSH = "notif_push";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        Switch emailNotifSwitch = new Switch(requireContext());
        emailNotifSwitch.setText("Email Notifications");
        emailNotifSwitch.setChecked(prefs.getBoolean(KEY_NOTIF_EMAIL, true));

        Switch pushNotifSwitch = new Switch(requireContext());
        pushNotifSwitch.setText("Push Notifications");
        pushNotifSwitch.setChecked(prefs.getBoolean(KEY_NOTIF_PUSH, true));

        layout.addView(emailNotifSwitch);
        layout.addView(pushNotifSwitch);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notification Settings")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_NOTIF_EMAIL, emailNotifSwitch.isChecked());
                    editor.putBoolean(KEY_NOTIF_PUSH, pushNotifSwitch.isChecked());
                    editor.apply();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
