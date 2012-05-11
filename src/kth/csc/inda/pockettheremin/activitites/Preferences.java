package kth.csc.inda.pockettheremin.activitites;

import kth.csc.inda.pockettheremin.R;
import kth.csc.inda.pockettheremin.input.Autotune;
import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.utils.Global.G;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Handles user preferences with the SharedPreferences API.
 * 
 * @see <a href=
 *      "https://developer.android.com/reference/android/content/SharedPreferences.html"
 *      >Check out the Android Dev Guide for more information.</a>
 */
public class Preferences extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Set default preferences by resource.
		 */
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

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

	/**
	 * Loads application preferences into static class fields so that user
	 * settings can be referenced throughout the entire application.
	 */
	public static void loadPreferences(SharedPreferences p) {

		/*
		 * Controls.
		 */
		G.octaves = Integer.parseInt(p.getString("octaves", "2"));
		G.useAutotune = p.getBoolean("autotune", true);
		G.key = Autotune.Note.valueOf(p.getString("key", "A"));
		G.scale = Autotune.Scale.valueOf(p.getString("scale", "MAJOR"));

		/*
		 * Sounds.
		 */
		if (!p.getBoolean("advanced_toggle", false)) {
			Preset preset = Preset.valueOf(p.getString("preset", "THEREMIN"));
			G.synthShape = preset.SYNTH_WAVEFORM;
			G.synthIMD = preset.SYNTH_IMD;
			G.chiptune = preset.SYNTH_CHIPTUNE;
			G.vibratoShape = preset.VIBRATO_SHAPE;
			G.vibratoSpeed = preset.VIBRATO_SPEED;
			G.vibratoDepth = preset.VIBRATO_DEPTH;
			G.tremoloShape = preset.TREMOLO_SHAPE;
			G.tremoloSpeed = preset.TREMOLO_SPEED;
			G.tremoloDepth = preset.TREMOLO_DEPTH;
			G.portamentoSpeed = preset.PORTAMENTO_SPEED;
			G.delayBPM = preset.DELAY_BPM;
			G.delayFeedback = preset.DELAY_FEEDBACK;
			G.delayMix = preset.DELAY_MIX;

		} else { // Use manual user preferences for sounds.

			/*
			 * Synthesizer
			 */
			G.synthIMD = p.getBoolean("imd", false);
			G.chiptune = p.getBoolean("chiptune", false);
			G.synthShape = new Waveform(

			Integer.parseInt(p.getString("synth_sine", "100")) / (double) 100,

			Integer.parseInt(p.getString("synth_square", "0")) / (double) 100,

					Integer.parseInt(p.getString("synth_triangle", "0"))
							/ (double) 100,

					Integer.parseInt(p.getString("synth_sawtooth", "0"))
							/ (double) 100);

			/*
			 * Vibrato
			 */
			if (p.getBoolean("vibrato", true)) {
				G.vibratoShape = new Waveform(

				Integer.parseInt(p.getString("vibrato_sine", "100"))
						/ (double) 100,

				Integer.parseInt(p.getString("vibrato_square", "0"))
						/ (double) 100,

				Integer.parseInt(p.getString("vibrato_triangle", "0"))
						/ (double) 100,

				Integer.parseInt(p.getString("vibrato_sawtooth", "0"))
						/ (double) 100);
				G.vibratoSpeed = Integer.parseInt(p.getString("vibrato_speed",
						"4"));
				G.vibratoDepth = Integer.parseInt(p.getString("vibrato_depth",
						"2"));
			} else
				G.vibratoDepth = 0;

			/*
			 * Tremolo
			 */
			if (p.getBoolean("tremolo", true)) {
				G.tremoloShape = new Waveform(

				Integer.parseInt(p.getString("tremolo_sine", "100"))
						/ (double) 100,

				Integer.parseInt(p.getString("tremolo_square", "0"))
						/ (double) 100,

				Integer.parseInt(p.getString("tremolo_triangle", "0"))
						/ (double) 100,

				Integer.parseInt(p.getString("tremolo_sawtooth", "0"))
						/ (double) 100);
				G.tremoloSpeed = Integer.parseInt(p.getString("tremolo_speed",
						"1"));
				G.tremoloDepth = Integer.parseInt(p.getString("tremolo_depth",
						"10"));
			} else
				G.tremoloDepth = 0;

			/*
			 * Portamento
			 */
			if (p.getBoolean("portamento", true)) {
				G.portamentoSpeed = Integer.parseInt(p.getString(
						"portamento_speed", "10"));
			} else
				G.portamentoSpeed = 0;

			/*
			 * Delay
			 */
			if (p.getBoolean("delay", true)) {
				G.delayBPM = Integer.parseInt(p.getString("delay_time", "100"));
				G.delayFeedback = Integer.parseInt(p.getString(
						"delay_feedback", "10"));
				G.delayMix = Integer.parseInt(p.getString("delay_mix", "50"));
			} else
				G.delayFeedback = G.delayMix = G.delayFeedback = 0;
		}
	}

	static public enum Preset {
		THEREMIN(// Synth
				new Waveform(1.0, 0.0, 0.0, 0.0), false, false,
				// Vibrato
				new Waveform(1.0, 0.0, 0.0, 0.0), 4, 3,
				// Tremolo
				new Waveform(1.0, 0.0, 0.0, 0.0), 1, 10,
				// Portamento
				50,
				// Delay
				5, 25, 15),

		ZELDA(// Synth
				new Waveform(0.0, 0.0, 1.0, 1.0), true, true,
				// Vibrato
				new Waveform(0.0, 0.0, 1.0, 0.0), 9, 2,
				// Tremolo
				new Waveform(1.0, 0.0, 0.0, 0.0), 1, 10,
				// Portamento
				0,
				// Delay
				0, 0, 0),

		MARIO(// Synth
				new Waveform(0.0, 1.0, 0.0, 0.25), true, false,
				// Vibrato
				new Waveform(1.0, 0.0, 1.0, 0.0), 100, 1,
				// Tremolo
				new Waveform(0.0, 1.0, 0.0, 0.0), 100, 20,
				// Portamento
				0,
				// Delay
				0, 0, 0),

		BAGPIPE(// Synth
				new Waveform(0.1, 0.0, 0.0, 1.0), true, false,
				// Vibrato
				new Waveform(0.1, 0.1, 0.1, 0.1), 1, 1,
				// Tremolo
				new Waveform(1.0, 0.0, 0.0, 0.0), 1, 20,
				// Portamento
				25,
				// Delay
				5, 20, 20),

		FLUTTER(// Synth
				new Waveform(0.0, 1.0, 0.0, 0.0), false, false,
				// Vibrato
				null, 0, 0,
				// Tremolo
				new Waveform(0.0, 1.0, 0.0, 0.0), 10, 100,
				// Portamento
				0,
				// Delay
				0, 0, 0),

		SPACE(// Synth
				new Waveform(1.0, 0.0, 0.0, 0.0), false, false,
				// Vibrato
				new Waveform(1.0, 0.0, 0.0, 0.0), 4, 3,
				// Tremolo
				new Waveform(1.0, 0.0, 0.0, 0.0), 1, 10,
				// Portamento
				100,
				// Delay
				10, 50, 50),

		SIREN(// Synth
				new Waveform(0.0, 0, 1.0, 0.0), false, false,
				// Vibrato
				new Waveform(1.0, 0, 0, 0), 5, 50,
				// Tremolo
				null, 0, 0,
				// Portamento
				100,
				// Delay
				0, 0, 0);

		public Waveform SYNTH_WAVEFORM;
		public boolean SYNTH_IMD, SYNTH_CHIPTUNE;
		public Waveform VIBRATO_SHAPE;
		public int VIBRATO_SPEED, VIBRATO_DEPTH;
		public Waveform TREMOLO_SHAPE;
		public int TREMOLO_SPEED, TREMOLO_DEPTH;
		public int PORTAMENTO_SPEED;
		public int DELAY_BPM, DELAY_FEEDBACK, DELAY_MIX;

		Preset(Waveform synthWaveform, boolean synthIMD, boolean synthChiptune,

		Waveform vibratoShape, int vibratoSpeed, int vibratoDepth,

		Waveform tremoloShape, int tremoloSpeed, int tremoloDepth,

		int portamentoSpeed,

		int delayBPM, int delayFeedback, int delayMix

		) {
			SYNTH_WAVEFORM = synthWaveform;
			SYNTH_IMD = synthIMD;
			SYNTH_CHIPTUNE = synthChiptune;
			VIBRATO_SHAPE = vibratoShape;
			VIBRATO_SPEED = vibratoSpeed;
			VIBRATO_DEPTH = vibratoDepth;
			TREMOLO_SHAPE = tremoloShape;
			TREMOLO_SPEED = tremoloSpeed;
			TREMOLO_DEPTH = tremoloDepth;
			PORTAMENTO_SPEED = portamentoSpeed;
			DELAY_BPM = delayBPM;
			DELAY_FEEDBACK = delayFeedback;
			DELAY_MIX = delayMix;
		}
	};
}
