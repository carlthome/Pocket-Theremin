package kth.csc.inda.pockettheremin.activitites;

import kth.csc.inda.pockettheremin.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * Handles user preferences with the SharedPreferences API. Check out the
 * Android Dev Guide for more information.
 */
public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Get preferences from XML resource.
		 */
		addPreferencesFromResource(R.layout.preferences);

		/*
		 * Toggle between using sound presets or the advanced sound effect
		 * settings.
		 */
		CheckBoxPreference advancedToggle = (CheckBoxPreference) findPreference("advanced_toggle");
		advancedToggle.setOnPreferenceChangeListener(this);

		/*
		 * Make sure to run the toggle once on launch.
		 */
		onPreferenceChange(advancedToggle, advancedToggle.isChecked());
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("advanced_toggle")) {
			if (newValue.equals(false)) {
				findPreference("sound_effects").setEnabled(false);
				findPreference("preset").setEnabled(true);
				return true;
			} else if (newValue.equals(true)) {
				findPreference("sound_effects").setEnabled(true);
				findPreference("preset").setEnabled(false);
				return true;
			}
		}

		return false;
	}
}