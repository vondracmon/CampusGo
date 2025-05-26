package com.example.campusgo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, languageCode);
        }
        return updateResourcesLegacy(context, languageCode);
    }

    public static void applySavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        int langIndex = prefs.getInt("language", 0);
        String[] codes = {"en", "fil", "es", "fr", "de", "zh", "ja", "ko", "ar", "hi"};
        String langCode = codes[langIndex];

        setLocale(context, langCode);
    }

    public static Context wrap(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        int langIndex = prefs.getInt("language", 0);
        String[] codes = {"en", "fil", "es", "fr", "de", "zh", "ja", "ko", "ar", "hi"};
        String langCode = codes[langIndex];

        return setLocale(context, langCode);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        return context;
    }
}
