package kth.csc.inda.pockettheremin.activitites;

import kth.csc.inda.pockettheremin.R;
import kth.csc.inda.pockettheremin.R.layout;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);

		/*
		 * Toggle between enabling/disabling either sound presets or the
		 * advanced sound effect settings.
		 */
		CheckBoxPreference advancedToggle = (CheckBoxPreference) findPreference("advanced_toggle");
		advancedToggle.setOnPreferenceChangeListener(this);
		onPreferenceChange(advancedToggle, advancedToggle.isChecked()); // Run once on launch!
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
