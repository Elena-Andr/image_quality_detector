package com.example.elenaa.myapplication;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // Attach OnPreferenceChangeListener so the UI can be updated when the preference changes
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_blur_fast_mode_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_glare_fast_mode_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_noise_fast_mode_key)));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        preference.setSummary(stringValue);

        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }
}