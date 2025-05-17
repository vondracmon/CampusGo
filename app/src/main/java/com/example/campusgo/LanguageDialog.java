package com.example.campusgo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class LanguageDialog extends DialogFragment {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_LANGUAGE = "language";

    private String[] languages = {
            "English", "Filipino", "Spanish", "French", "German",
            "Chinese", "Japanese", "Korean", "Arabic", "Hindi"
    };

    // Corresponding locale codes for the languages above
    private String[] localeCodes = {
            "en", "fil", "es", "fr", "de",
            "zh", "ja", "ko", "ar", "hi"
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int selectedLanguageIndex = prefs.getInt(KEY_LANGUAGE, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Language")
                .setSingleChoiceItems(languages, selectedLanguageIndex, (dialog, which) -> {
                    // Save selection immediately on click
                    prefs.edit().putInt(KEY_LANGUAGE, which).apply();

                    // Apply selected locale
                    setLocale(localeCodes[which]);

                    dialog.dismiss();

                    // Recreate activity to apply changes
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = requireActivity().getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
