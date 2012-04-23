package kth.csc.inda.pockettheremin;

import java.text.DecimalFormat;

import kth.csc.inda.pockettheremin.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.music.Note;
import kth.csc.inda.pockettheremin.music.Scale;
import kth.csc.inda.pockettheremin.soundeffects.Autotune;
import kth.csc.inda.pockettheremin.soundeffects.Portamento;
import kth.csc.inda.pockettheremin.soundeffects.SoundEffect;
import kth.csc.inda.pockettheremin.soundeffects.Tremolo;
import kth.csc.inda.pockettheremin.soundeffects.Vibrato;
import kth.csc.inda.pockettheremin.soundeffects.Preset;
import android.app.Activity;
import android.content.Context;
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
	boolean useSensor;

	/*
	 * Output variables.
	 */
	AudioManager audioManager;
	AudioTrack audioStream;
	AsyncTask<?, ?, ?> soundGenerator;
	double pitch, volume;
	double maxFrequency, minFrequency, frequencyRange;
	double maxAmplitude, minAmplitude, amplitudeRange;

	/*
	 * Basic user settings.
	 */
	boolean useAutotune;
	Note key;
	Scale scale;
	int octaveRange;
	
	/*
	 * Advanced user settings.
	 */
	boolean usePresets, useChiptuneMode, useTremolo, useVibrato, usePortamento;
	Waveform synthWaveform, tremoloWaveform, vibratoWaveform;
	int tremoloSpeed, tremoloDepth, vibratoSpeed, vibratoDepth,
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
		 * Set default preferences according to resource (only once!).
		 */
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

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
	 * Register sensor listeners, load user preferences and start the audio
	 * thread.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").

		/*
		 * Remind user to turn on volume.
		 */
		if (0.1 > audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ (double) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			alert(getString(R.string.low_volume_notice));

		/*
		 * Get user preferences.
		 */
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		useSensor = preferences.getBoolean("accelerometer", false);
		useAutotune = preferences.getBoolean("autotune", true);
		key = Note.valueOf(preferences.getString("key", "A"));
		scale = Scale.valueOf(preferences.getString("scale", "MAJOR"));
		octaveRange = Integer.parseInt(preferences
				.getString("octaveRange", "2"));

		if (preferences.getBoolean("presets", true)) {
			Preset preset = Preset.valueOf(preferences.getString("preset",
					"THEREMIN"));
			synthWaveform = preset.getSynthWaveform();
			useChiptuneMode = preset.isUseChiptuneMode();
			useVibrato = preset.isUseVibrato();
			vibratoWaveform = preset.getVibratoWaveform();
			vibratoSpeed = preset.getVibratoSpeed();
			vibratoDepth = preset.getVibratoDepth();
			useTremolo = preset.isUseTremolo();
			tremoloWaveform = preset.getTremoloWaveform();
			tremoloSpeed = preset.getTremoloSpeed();
			tremoloDepth = preset.getTremoloDepth();
			usePortamento = preset.isUsePortamento();
			portamentoSpeed = preset.getPortamentoSpeed();
		} else {
			synthWaveform = Waveform.valueOf(preferences.getString("waveform",
					"SINE"));
			useChiptuneMode = preferences.getBoolean("chiptuneMode", false);
			useVibrato = preferences.getBoolean("vibrato", true);
			vibratoWaveform = Waveform.valueOf(preferences.getString(
					"vibrato_waveform", "TRIANGLE"));
			vibratoSpeed = Integer.parseInt(preferences.getString(
					"vibrato_speed", "8"));
			vibratoDepth = Integer.parseInt(preferences.getString(
					"vibrato_depth", "4"));
			useTremolo = preferences.getBoolean("tremolo", true);
			tremoloWaveform = Waveform.valueOf(preferences.getString(
					"tremolo_waveform", "SINE"));
			tremoloSpeed = Integer.parseInt(preferences.getString(
					"tremolo_speed", "1"));
			tremoloDepth = Integer.parseInt(preferences.getString(
					"tremolo_depth", "10"));
			usePortamento = preferences.getBoolean("portamento", true);
			portamentoSpeed = Integer.parseInt(preferences.getString(
					"portamento_speed", "100"));
		}

		/*
		 * Calculate operational values.
		 */
		minFrequency = key.frequency(4 - octaveRange / 2); // TODO Improve
															// abstraction.
		maxFrequency = key.frequency(4 + octaveRange / 2); // TODO Improve
															// abstraction.
		frequencyRange = maxFrequency - minFrequency;

		minAmplitude = 0;
		maxAmplitude = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		amplitudeRange = maxAmplitude - minAmplitude;

		/*
		 * Update graphics.
		 */
		((TextView) findViewById(R.id.amplitudeMax)).setText(new DecimalFormat(
				"#.#").format(maxAmplitude) + "");
		((TextView) findViewById(R.id.amplitudeMin)).setText(new DecimalFormat(
				"#.#").format(minAmplitude) + "");
		((TextView) findViewById(R.id.frequencyMin)).setText(new DecimalFormat(
				"#").format(minFrequency) + "Hz");
		((TextView) findViewById(R.id.frequencyMax)).setText(new DecimalFormat(
				"#").format(maxFrequency) + "Hz");

		/*
		 * Register input listeners.
		 */
		if (useSensor)
			sensors.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		else
			this.findViewById(android.R.id.content).setOnTouchListener(this);

		/*
		 * Start executing audio thread.
		 */
		soundGenerator = new AudioThread().execute();
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

		if (soundGenerator != null)
			soundGenerator.cancel(true);

		if (audioStream != null)
			audioStream.release();
	}

	/**
	 * Setup a menu for this activity that allows the user to reach app settings
	 * and an "About this app" page.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "About");
		menu.add(Menu.NONE, 1, 1, "Settings");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Make sure the activity menu actually does what's intended when a menu
	 * item is selected.
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
	private void alert(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Set pitch and volume by touch.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 2) {
				volume = (view.getHeight() - event.getY(0))
						* (amplitudeRange / view.getHeight());

				// TODO Fix even playing with autotune.
				// log_12(x) = Math.log(x)/Math.log(12);
				// (1.05946)12 = 2

				pitch = event.getX(1) * frequencyRange / view.getWidth();
			}
		}

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
	 * Set pitch and volume by interpreting sensor values.
	 */
	@Override
	public final void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;

		/*
		 * Calibrate sensor stepping.
		 */
		double pitchStep = frequencyRange / sensor.getResolution();
		double volumeStep = amplitudeRange / sensor.getResolution();

		/*
		 * Modulate pitch and amplitude just with the accelerometer, or modulate
		 * pitch with the light sensor and amplitude with the proximity sensor.
		 */
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			volume = event.values[0] * volumeStep;
			pitch = (event.values[1] + SensorManager.STANDARD_GRAVITY)
					* pitchStep;
		}
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the user input.
	 */
	private class AudioThread extends AsyncTask<Void, Double, Void> {
		boolean play;
		double frequency;
		double amplitude;
		final int bufferSize = 256;
		final int sampleRate = 44100;
		Oscillator oscillator;
		SoundEffect autotune, tremolo, portamento, vibrato;

		protected void onPreExecute() {
			play = true;
			oscillator = new Oscillator(synthWaveform, bufferSize, sampleRate);

			// Effects
			if (useAutotune)
				autotune = new Autotune(key, scale, octaveRange);
			if (useTremolo)
				tremolo = new Tremolo(tremoloSpeed, tremoloDepth,
						tremoloWaveform, sampleRate, bufferSize);
			if (usePortamento)
				portamento = new Portamento(portamentoSpeed, sampleRate, bufferSize);
			if (useVibrato)
				vibrato = new Vibrato(vibratoSpeed, vibratoDepth,
						vibratoWaveform, sampleRate, bufferSize);

			// Audio stream
			int audioFormat = (useChiptuneMode) ? AudioFormat.ENCODING_PCM_8BIT
					: AudioFormat.ENCODING_PCM_16BIT;
			audioStream = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat,
					AudioTrack
							.getMinBufferSize(sampleRate,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									audioFormat), AudioTrack.MODE_STREAM);
			audioStream.play();
		}

		protected Void doInBackground(Void... params) {
			while (play) {

				/*
				 * Set initial frequency according to input.
				 */
				frequency = PocketThereminActivity.this.pitch + minFrequency;

				if (frequency > maxFrequency)
					frequency = maxFrequency;

				if (frequency < minFrequency)
					frequency = minFrequency;

				/*
				 * Set initial volume according to input.
				 */
				amplitude = PocketThereminActivity.this.volume;
				audioStream.setStereoVolume((float) amplitude,
						(float) amplitude);

				/*
				 * Effects chain.
				 */
				if (autotune != null)
					frequency = autotune.modify(frequency);

				if (vibrato != null)
					frequency = vibrato.modify(frequency);

				if (portamento != null)
					frequency = portamento.modify(frequency);

				if (tremolo != null) {
					amplitude = tremolo.modify(amplitude);
					audioStream.setStereoVolume((float) amplitude,
							(float) amplitude);
				}

				/*
				 * Generate sound samples.
				 */
				oscillator.setFrequency(frequency);
				short[] samples = oscillator.getSamples();

				// Write samples.
				audioStream.write(samples, 0, bufferSize);

				// Return amplitude and frequency to the GUI thread.
				publishProgress(frequency, amplitude);
			}
			return null;
		}

		protected void onProgressUpdate(Double... progress) {
			((TextView) findViewById(R.id.frequency))
					.setText(new DecimalFormat("#").format(progress[0]) + "Hz");
			((TextView) findViewById(R.id.amplitude))
					.setText(new DecimalFormat("#.#").format(progress[1]) + "");
		}

		@Override
		protected void onCancelled() {
			play = false;
		}
	}
}