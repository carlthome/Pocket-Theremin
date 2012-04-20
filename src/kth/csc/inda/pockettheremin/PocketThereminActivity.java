package kth.csc.inda.pockettheremin;

import kth.csc.inda.pockettheremin.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.soundeffects.Autotune;
import kth.csc.inda.pockettheremin.soundeffects.Autotune.AutotuneKey;
import kth.csc.inda.pockettheremin.soundeffects.Autotune.AutotuneScale;
import kth.csc.inda.pockettheremin.soundeffects.Portamento;
import kth.csc.inda.pockettheremin.soundeffects.SoundEffect;
import kth.csc.inda.pockettheremin.soundeffects.Tremolo;
import kth.csc.inda.pockettheremin.soundeffects.Vibrato;
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
	 * Input
	 */
	SensorManager sensors;
	Sensor sensor;
	View touch;
	boolean useSensor;

	/*
	 * Output.
	 */
	AudioManager audioManager;
	AudioTrack audioStream;
	AsyncTask<?, ?, ?> soundGenerator;
	float pitch, volume;
	float maxFrequency, minFrequency, frequencyRange;
	int octaveRange;
	Waveform waveform;
	boolean useTremolo, useVibrato, usePortamento, useAutotune,
			useChiptuneMode;
	AutotuneKey key;
	AutotuneScale scale;

	/**
	 * When the app is started: load graphics and find resources.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// The activity is being created.

		/*
		 * Set layout resource.
		 */
		setContentView(R.layout.main);

		/*
		 * Find input.
		 */
		sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		touch = findViewById(R.id.touchInputView);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.
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
				/ (float) audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			alert(getString(R.string.low_volume_notice));

		/*
		 * Get user preferences.
		 */
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		useSensor = preferences.getBoolean("accelerometer", false);
		useTremolo = preferences.getBoolean("tremolo", false);
		useVibrato = preferences.getBoolean("vibrato", false);
		usePortamento = preferences.getBoolean("portamento", false);
		useAutotune = preferences.getBoolean("autotune", false);
		useChiptuneMode = preferences.getBoolean("chiptuneMode", false);
		octaveRange = Integer.parseInt(preferences.getString("octaveRange", "2"));		
		waveform = Waveform.valueOf(preferences.getString("waveform", "SINE"));
		key = AutotuneKey.valueOf(preferences.getString("key", "A"));
		scale = AutotuneScale.valueOf(preferences.getString("scale", "MAJOR"));

		/*
		 * Register input listeners.
		 */
		if (useSensor)
			sensors.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
		else
			touch.setOnTouchListener(this);

		/*
		 * Start audio thread.
		 */
		minFrequency = 440.00f / (float) Math.pow(2, octaveRange / 2);
		maxFrequency = 440.00f * (float) Math.pow(2, octaveRange / 2);
		frequencyRange = maxFrequency - minFrequency;
		soundGenerator = new AudioThread().execute();

		//alert(getString(R.string.resume));
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

		if (touch != null)
			touch.setOnTouchListener(null);

		if (soundGenerator != null)
			soundGenerator.cancel(true);
		
		if (audioStream != null)
			audioStream.release();

		//alert(getString(R.string.pause));
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
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
	public boolean onTouchEvent(MotionEvent event) {
		int width = touch.getWidth();
		int height = touch.getHeight();
		int x = (int) event.getX();
		int y = (int) event.getY();

		Log.d("onTouchEvent", "(" + x + ", " + y + ")");

		/*
		 * Calibrate input.
		 */
		float pitchStep = frequencyRange / height;
		float volumeStep = 1.0f / width;

		/*
		 * Set volume and pitch.
		 */
		volume = x * volumeStep;
		pitch = y * pitchStep;

		return false;
	}

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
		Log.d("onSensorChanged (" + sensor.getName() + ")", "Value: "
				+ event.values[0] + ", Resolution: " + sensor.getResolution()
				+ ", Range: " + sensor.getMaximumRange());

		/*
		 * Calibrate sensor stepping.
		 */
		float pitchStep = frequencyRange / sensor.getResolution();
		float volumeStep = 1.0f / sensor.getResolution();

		/*
		 * Modulate pitch and amplitude just with the accelerometer, or modulate
		 * pitch with the light sensor and amplitude with the proximity sensor.
		 */
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			// TODO Filter noise with a high-pass filter.
			volume = event.values[0] * volumeStep;
			pitch = (event.values[1] + SensorManager.STANDARD_GRAVITY)
					* pitchStep;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		Log.d("Touch Event", "(" + x + ", " + y + ")");

		return false;
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the user input.
	 */
	private class AudioThread extends AsyncTask<Float, Float, Void> {
		boolean play;
		int sampleSize = 256;
		int sampleRate = 44100;
		Oscillator oscillator;
		SoundEffect autotune, tremolo, portamento, vibrato;
		float frequency, amplitude;

		protected void onPreExecute() {
			play = true;
			oscillator  = new Oscillator(waveform, sampleSize, sampleRate);

			// Effects
			if (useAutotune)
				autotune = new Autotune(key, scale, octaveRange);
			if (useTremolo)
				tremolo = new Tremolo();
			if (usePortamento)
				portamento = new Portamento();
			if (useVibrato)
				vibrato = new Vibrato();

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

		protected Void doInBackground(Float... params) {
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
				audioStream.setStereoVolume(amplitude, amplitude);

				/*
				 * Effects chain.
				 */
				if (autotune != null) //TODO Combine autotune and portamento.
					frequency = autotune.modify(frequency);

				if (vibrato != null)
					frequency = vibrato.modify(frequency);

				if (portamento != null)
					frequency = portamento.modify(frequency);

				if (tremolo != null) {
					amplitude = tremolo.modify(amplitude);
					audioStream.setStereoVolume(amplitude, amplitude);
				}

				/*
				 * Generate sound samples.
				 */
				short[] samples = oscillator.getSamples(frequency);

				// Write samples.
				audioStream.write(samples, 0, sampleSize);

				// Return amplitude and frequency to the GUI thread.
				publishProgress(frequency, amplitude);
			}
			return null;
		}

		protected void onProgressUpdate(Float... progress) {
			((TextView) findViewById(R.id.textFrequency)).setText(progress[0]
					.shortValue() + "Hz");
			((TextView) findViewById(R.id.textAmplitude)).setText(progress[1]
					.shortValue() + "%"); // TODO Should be decibel?
		}

		@Override
		protected void onCancelled() {
			play = false;
		}
	}
}