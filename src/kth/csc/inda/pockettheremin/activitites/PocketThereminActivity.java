package kth.csc.inda.pockettheremin.activitites;

import java.text.DecimalFormat;

import kth.csc.inda.pockettheremin.R;
import kth.csc.inda.pockettheremin.music.Note;
import kth.csc.inda.pockettheremin.music.Scale;
import kth.csc.inda.pockettheremin.soundeffects.Autotune;
import kth.csc.inda.pockettheremin.soundeffects.MasterClock;
import kth.csc.inda.pockettheremin.soundeffects.Portamento;
import kth.csc.inda.pockettheremin.soundeffects.Preset;
import kth.csc.inda.pockettheremin.soundeffects.SoundEffect;
import kth.csc.inda.pockettheremin.soundeffects.Tremolo;
import kth.csc.inda.pockettheremin.soundeffects.Vibrato;
import kth.csc.inda.pockettheremin.synth.Oscillator;
import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity provides a playable theremin by using either the device's
 * accelerometer or touch input.
 * 
 * To be frank, this is not like a theremin at all since there are no radiowaves
 * involved, but it's still a fun toy.
 * 
 * @param <MyActivity>
 * 
 */
public class PocketThereminActivity extends Activity implements
		SensorEventListener, OnTouchListener {

	/*
	 * Activity instance variables.
	 */
	SharedPreferences preferences;

	/*
	 * Input variables.
	 */
	SensorManager sensors;
	Sensor sensor;
	View touchAmplitude, touchFrequency;
	boolean useSensor, multitouch;

	/*
	 * Output variables.
	 */
	AudioManager audioManager;
	public static final MasterClock clock = new MasterClock();
	boolean play;
	double pitch, amplitude;
	double frequency, volume;
	double frequencyMax, frequencyMin, frequencyRange;
	double amplitudeMax, amplitudeMin, amplitudeRange;
	float volumeMax, volumeMin, volumeRange;

	/*
	 * User settings.
	 */
	Waveform synthWaveform, tremoloShape, vibratoShape;
	boolean useAutotune, synthIMD, synthChiptune;
	Note key;
	Scale scale;
	int octaves, tremoloSpeed, tremoloDepth, vibratoSpeed, vibratoDepth,
			portamentoSpeed;

	/**
	 * When the app is started: load graphics and find resources.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Set layout resource.
		 */
		setContentView(R.layout.main);

		/*
		 * Set default preferences according to resource when the app is
		 * started.
		 */
		PreferenceManager.setDefaultValues(this, R.layout.preferences, true);

		/*
		 * Find input.
		 */
		sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		touchFrequency = findViewById(R.id.touchFrequency);
		touchAmplitude = findViewById(R.id.touchAmplitude);

		/*
		 * Find necessary system services.
		 */
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Register sensor listeners, load user preferences, provide instructions
	 * and start the audio thread.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		/*
		 * Get user preferences.
		 */
		refreshSharedPreferences();

		/*
		 * Configure the audio manager and refresh related graphics.
		 */
		refreshAudioManager();

		/*
		 * Remind the user to turn up the volume on their device.
		 */
		if (0.1 > audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ (double) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			alert(getString(R.string.notice_low_volume), false);

		/*
		 * Register input listeners.
		 */
		if (useSensor) {
			/*
			 * TODO Touch input seems to be registered at this stage, even
			 * though is shouldn't be.
			 */
			sensors.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
			alert(getString(R.string.notice_accelerometer_input_instructions),
					false);
		} else {
			this.findViewById(android.R.id.content).setOnTouchListener(this);
			if (multitouch)
				alert(getString(R.string.notice_multitouch_input_instructions),
						false);
			else
				alert(getString(R.string.notice_touch_input_instructions),
						false);
		}

		/*
		 * Set GUI according to playing style.
		 */
		refreshGUI();

		/*
		 * Start executing audio thread.
		 */
		new AudioThread().execute();
	}

	/**
	 * Unregister sensor listeners and kill the audio thread (i.e free up system
	 * resources when the app isn't used anymore).
	 */
	@Override
	protected void onPause() {
		super.onPause();

		if (sensors != null)
			sensors.unregisterListener(this);

		if (touchFrequency != null)
			touchFrequency.setOnTouchListener(null);

		if (touchAmplitude != null)
			touchAmplitude.setOnTouchListener(null);

		play = false;
	}

	/**
	 * Activity menu layout.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, 1, 1, "Settings").setIcon(
				android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Activity menu functionality (i.e. make sure the right stuff happens when
	 * a menu item is selected by the user).
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case 1:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		}
		return false;
	}

	/**
	 * Provide feedback to the user.
	 */
	private void alert(String s, boolean requireUserToDismissAlert) {
		if (requireUserToDismissAlert) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(s)
					.setCancelable(true)
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			builder.show();
		} else
			// A simple toast will do.
			Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG)
					.show();
	}

	/**
	 * Set pitch and volume by touch.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float x, y;
		int pointers = event.getPointerCount();

		/*
		 * TODO Fix an even playing area when autotune is enabled. (i.e.
		 * distribute touch areas according to 12-TET instead of linearly)
		 * 
		 * log_12(x) = Math.log(x)/Math.log(12); (1.05946)12 = 2
		 */
		if (multitouch) {
			/*
			 * The entire activity view is actually touch enabled but only parts
			 * of the GUI actually update the pitch and volume, by dividing the
			 * screen into pixel-based areas.
			 * 
			 * This is probably not the best solution but it seems to work
			 * fairly consistently.
			 */
			for (int i = 0; i < pointers; i++) {
				x = event.getX(i);
				y = event.getY(i);

				if (x < 80)
					amplitude = (view.getHeight() - y)
							* (amplitudeRange / view.getHeight());

				int shrink = 90; // Avoid overlapping the volume bar.
				if (y > (view.getHeight() - 80) && x > shrink) {
					pitch = (x - shrink) * frequencyRange
							/ (view.getWidth() - shrink);
				}
			}
		} else {
			y = event.getY();
			x = event.getX();

			amplitude = (view.getHeight() - y)
					* (amplitudeRange / view.getHeight());
			pitch = x * frequencyRange / view.getWidth();
		}
		setFrequency(pitch);
		setVolume(amplitude);

		return true;
	}

	/**
	 * Log sensor accuracy changes for debugging. Required by the
	 * OnTouchListener interface.
	 */
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(sensor.getName(), "onAccuracyChanged: " + accuracy);
	}

	/**
	 * Set pitch and amplitude by interpreting accelerometer values.
	 */
	@Override
	public final void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			amplitude = event.values[0]
					* (amplitudeRange / sensor.getResolution());
			pitch = (event.values[1] + SensorManager.STANDARD_GRAVITY)
					* (frequencyRange / sensor.getResolution());

			setFrequency(pitch);
			setVolume(amplitude);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			setVolume(amplitude);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			setVolume(amplitude);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Utility method for updating the GUI in case the user has changed the app
	 * preferences.
	 */
	private void refreshGUI() {
		TextView textAmplitudeMax, textAmplitudeMin, textFrequencyMax, textFrequencyMin;

		textAmplitudeMax = ((TextView) findViewById(R.id.amplitudeMax));
		textAmplitudeMin = ((TextView) findViewById(R.id.amplitudeMin));
		textFrequencyMax = ((TextView) findViewById(R.id.frequencyMax));
		textFrequencyMin = ((TextView) findViewById(R.id.frequencyMin));

		if (useSensor) {
			textAmplitudeMax.setVisibility(View.GONE);
			textAmplitudeMin.setVisibility(View.GONE);
			textFrequencyMax.setVisibility(View.GONE);
			textFrequencyMin.setVisibility(View.GONE);
		} else {
			textAmplitudeMax.setVisibility(View.VISIBLE);
			textAmplitudeMin.setVisibility(View.VISIBLE);
			textFrequencyMax.setVisibility(View.VISIBLE);
			textFrequencyMin.setVisibility(View.VISIBLE);

			if (multitouch) {
				textAmplitudeMax.setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.arrow_up), null,
						null);
				textAmplitudeMin.setCompoundDrawablesWithIntrinsicBounds(null,
						null, null,
						getResources().getDrawable(R.drawable.arrow_down));
				textFrequencyMax.setCompoundDrawablesWithIntrinsicBounds(null,
						null, getResources()
								.getDrawable(R.drawable.arrow_right), null);
				textFrequencyMin.setCompoundDrawablesWithIntrinsicBounds(
						getResources().getDrawable(R.drawable.arrow_left),
						null, null, null);
			} else {
				textFrequencyMin.setCompoundDrawables(null, null, null, null);
				textAmplitudeMin.setCompoundDrawablesWithIntrinsicBounds(null,
						null, null,
						getResources().getDrawable(R.drawable.arrow_down_left));
			}
		}
	}

	/**
	 * Utility method for updating certain values related to the audio stream
	 * that might change during the activity's life cycle, such as the maximum
	 * volume.
	 */
	private void refreshAudioManager() {

		/*
		 * Calculate operational values.
		 */
		frequencyMin = key.frequency(4 - octaves / 2);
		frequencyMax = key.frequency(4 + octaves / 2);
		frequencyRange = frequencyMax - frequencyMin;

		amplitudeMin = 0;
		amplitudeMax = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		amplitudeRange = amplitudeMax - amplitudeMin;

		volumeMax = AudioTrack.getMaxVolume();
		volumeMin = AudioTrack.getMinVolume();
		volumeRange = volumeMax - volumeMin;

		/*
		 * Update graphics.
		 */
		((TextView) findViewById(R.id.amplitudeMax)).setText(new DecimalFormat(
				"#.#").format(amplitudeMax) + "");
		((TextView) findViewById(R.id.amplitudeMin)).setText(new DecimalFormat(
				"#.#").format(amplitudeMin) + "");
		((TextView) findViewById(R.id.frequencyMin)).setText(new DecimalFormat(
				"#").format(frequencyMin) + "Hz");
		((TextView) findViewById(R.id.frequencyMax)).setText(new DecimalFormat(
				"#").format(frequencyMax) + "Hz");
	}

	/**
	 * Whenever called preferences are loaded into fields.
	 */
	private void refreshSharedPreferences() {

		/*
		 * Get user preferences.
		 */
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		useSensor = preferences.getBoolean("accelerometer", false);
		useAutotune = preferences.getBoolean("autotune", true);
		key = Note.valueOf(preferences.getString("key", "A"));
		scale = Scale.valueOf(preferences.getString("scale", "MAJOR"));
		octaves = Integer.parseInt(preferences.getString("octaves", "2"));
		multitouch = preferences.getBoolean("multitouch", true);
		if (!preferences.getBoolean("advanced_toggle", false)) {

			/*
			 * Use instrument presets for sounds.
			 */
			Preset preset = Preset.valueOf(preferences.getString("preset",
					"THEREMIN"));
			synthWaveform = preset.SYNTH_WAVEFORM;
			synthIMD = preset.SYNTH_IMD;
			synthChiptune = preset.SYNTH_CHIPTUNE;
			vibratoShape = preset.VIBRATO_SHAPE;
			vibratoSpeed = preset.VIBRATO_SPEED;
			vibratoDepth = preset.VIBRATO_DEPTH;
			tremoloShape = preset.TREMOLO_SHAPE;
			tremoloSpeed = preset.TREMOLO_SPEED;
			tremoloDepth = preset.TREMOLO_DEPTH;
			portamentoSpeed = preset.PORTAMENTO_SPEED;

		} else {

			/*
			 * Use manual user preferences for sounds.
			 */
			synthWaveform = Waveform.valueOf(preferences.getString("waveform",
					"SINE"));
			synthIMD = preferences.getBoolean("imd", true);
			synthChiptune = preferences.getBoolean("chiptune", false);

			if (preferences.getBoolean("vibrato", true)) {
				vibratoShape = Waveform.valueOf(preferences.getString(
						"vibrato_waveform", "TRIANGLE"));
				vibratoSpeed = Integer.parseInt(preferences.getString(
						"vibrato_speed", "80"));
				vibratoDepth = Integer.parseInt(preferences.getString(
						"vibrato_depth", "4"));
			} else
				vibratoDepth = 0;

			if (preferences.getBoolean("tremolo", true)) {
				tremoloShape = Waveform.valueOf(preferences.getString(
						"tremolo_waveform", "SINE"));
				tremoloSpeed = Integer.parseInt(preferences.getString(
						"tremolo_speed", "50"));
				tremoloDepth = Integer.parseInt(preferences.getString(
						"tremolo_depth", "10"));
			} else
				tremoloDepth = 0;

			if (preferences.getBoolean("portamento", true)) {
				portamentoSpeed = Integer.parseInt(preferences.getString(
						"portamento_speed", "100"));
			} else
				portamentoSpeed = 0;
		}
	}

	private void setVolume(double amplitude) {
		volume = (amplitude / amplitudeRange) * volumeRange;

		if (volume > volumeMax)
			volume = frequencyMax;

		if (volume < volumeMin)
			volume = volumeMin;
	}

	private void setFrequency(double pitch) {
		frequency = pitch + frequencyMin;

		if (frequency > frequencyMax)
			frequency = frequencyMax;

		if (frequency < frequencyMin)
			frequency = frequencyMin;
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the user input.
	 */
	private class AudioThread extends AsyncTask<Void, Double, Void> {
		AudioTrack audioStream;
		final int bufferSize = 1024;
		final int sampleRate = 44100;
		Oscillator oscillator;
		SoundEffect autotune, tremolo, portamento, vibrato;

		protected void onPreExecute() {
			play = true;
			oscillator = new Oscillator(synthWaveform);
			oscillator.setSampleRate(sampleRate);

			/*
			 * Effects.
			 */
			oscillator.setIMD(synthIMD);
			if (useAutotune)
				autotune = new Autotune(key, scale, octaves);
			vibrato = new Vibrato(vibratoSpeed, vibratoDepth, vibratoShape);
			portamento = new Portamento(portamentoSpeed);
			tremolo = new Tremolo(tremoloSpeed, tremoloDepth, tremoloShape,
					sampleRate, bufferSize);
			int audioFormat = (synthChiptune) ? AudioFormat.ENCODING_PCM_8BIT
					: AudioFormat.ENCODING_PCM_16BIT;

			/*
			 * Audio stream.
			 */
			audioStream = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat,
					AudioTrack
							.getMinBufferSize(sampleRate,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									audioFormat), AudioTrack.MODE_STREAM);

			// Begin stream.
			audioStream.play();
		}

		protected Void doInBackground(Void... params) {
			while (play) {
				clock.tick(); // Help sound effects keep in sync.
				if (!clock.isStable()) 
					// Write empty buffers until the master clock is stable.
					audioStream.write(new short[bufferSize], 0, bufferSize);
				
				else {

					/*
					 * Set initial values by user input.
					 */
					setFrequency(pitch);
					setVolume(amplitude);

					/*
					 * Effects chain.
					 */
					if (autotune != null)
						frequency = autotune.modify(frequency);

					if (portamento != null) {
						portamento.sync();
						frequency = portamento.modify(frequency);
					}

					if (vibrato != null) {
						vibrato.sync();
						frequency = vibrato.modify(frequency);
					}

					if (tremolo != null) {
						tremolo.sync();
						volume = tremolo.modify(volume);
					}

					/*
					 * Generate sound samples.
					 */
					oscillator.setFrequency(frequency);
					short[] samples = oscillator.getSamples(bufferSize);

					/*
					 * Write samples.
					 */
					audioStream.write(samples, 0, bufferSize);

					/*
					 * Set stream volume.
					 */
					audioStream.setStereoVolume((float) volume, (float) volume);

					/*
					 * Return amplitude and frequency to the GUI thread.
					 */
					publishProgress(frequency, volume);
				}
			}
			return null;
		}

		protected void onProgressUpdate(Double... progress) {
			((TextView) findViewById(R.id.frequency))
					.setText(new DecimalFormat("#").format(progress[0]) + "Hz");
			((TextView) findViewById(R.id.amplitude))
					.setText(new DecimalFormat("###").format(progress[1] * 100)
							+ "%");
			// ((TextView) findViewById(R.id.amplitude)).setText(new
			// DecimalFormat("#.#").format(amplitude) + "");
		}

		protected void onPostExecute(Void result) {
			if (audioStream != null)
				audioStream.release();
		}
	}

}