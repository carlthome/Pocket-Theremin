package kth.csc.inda.pockettheremin;

import java.util.Arrays;

import kth.csc.inda.pockettheremin.soundeffects.Autotune;
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
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity provides a playable theremin by using the device's ambient
 * light sensor for determining pitch and the device's proximity sensor to
 * determine amplitude. Most device's proximity sensors only support two
 * amplitudes (on/off).
 * 
 * To be frank, this is not like a theremin at all since there are no radiowaves
 * involved, but it's still a fun toy.
 * 
 * The theremin will reach higher frequencies if there's more light in the room.
 * Turn on the lights!
 * 
 */
public class PocketThereminActivity extends Activity implements
		SensorEventListener {

	/*
	 * TODO Create both a sampler and a synth as two separate classes and allow
	 * the AsyncTask that generates sounds by pitch to use one of the two.
	 */

	/*
	 * TODO Use the new ViewPager layout but with backwards-compatibility from
	 * 1.6 and onward.
	 */

	/*
	 * TODO Find out why Xperia phones don't seem to provide a light sensor
	 * through the SensorManager. For now the accelerometer is used instead.
	 * Fun!
	 */

	/*
	 * TODO Sweep between frequencies. (i.e. portamento-mode since sensor
	 * resolution might be limited)
	 */

	/*
	 * TODO Improve sensor stepping between frequencies.
	 */

	/*
	 * TODO Emulate the sound of a theremin (instead of a pure sine curve).
	 * Perhaps use a mp3 for sampling?
	 */

	/*
	 * TODO Let the user select sensor.
	 */

	static final boolean DEBUG = true;

	/*
	 * Sensor variables.
	 */
	private SensorManager sensors;
	private Sensor light, proximity, accelerometer;
	private boolean useAccelerometer;

	/*
	 * Sound variables.
	 */
	private AsyncTask<?, ?, ?> soundGenerator;
	AudioTrack audioStream;
	private float pitch, volume;
	private int maxFrequency, minFrequency;

	/*
	 * User preferences.
	 */
	private boolean useAutotune, useTremolo, usePortamento, useVibrato,
			useNintendoMode;
	WaveForm waveForm;

	public enum WaveForm {
		SINE, SQUARE, TRIANGLE, SAWTOOTH,
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, 0, "About");
		menu.add(Menu.NONE, 1, 1, "Settings");
		return super.onCreateOptionsMenu(menu);
	}

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
	 * When the app is started: load graphics and find the sensors.
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Set content view.
		 */
		setContentView(R.layout.main);

		/*
		 * Find sensors with the SensorManager.
		 */
		sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		light = sensors.getDefaultSensor(Sensor.TYPE_LIGHT);
		accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		proximity = sensors.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (DEBUG)
			for (Sensor sensor : sensors.getSensorList(Sensor.TYPE_ALL))
				Log.d("Sensors", sensor.getName());
	}

	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.i(sensor.getName(), "onAccuracyChanged: " + accuracy);
	}

	/**
	 * Set theremin pitch according to how much light hits the ambient light
	 * sensor. Also, try to set amplitude according to the proximity sensor if
	 * there is support for more than two values. Otherwise simply let the
	 * proximity sensor turn the theremin on and off.
	 */
	@Override
	public final void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		int type = sensor.getType();
		if (DEBUG) {
			Log.d(sensor.getName(), "onSensorChanged: " + event.values[0]);
			Log.d(sensor.getName(), "Resolution: " + sensor.getResolution()
					+ ", Range: " + sensor.getMaximumRange());
		}

		/*
		 * Calibrate sensor stepping.
		 */
		float sensorSteps = sensor.getMaximumRange() / sensor.getResolution();
		float pitchStep = (maxFrequency - minFrequency)
				/ sensor.getResolution(); // TODO
		float volumeStep = 1.0f / sensor.getResolution();

		/*
		 * Modulate pitch and amplitude just with the accelerometer, or modulate
		 * pitch with the light sensor and amplitude with the proximity sensor.
		 */
		if (useAccelerometer && type == Sensor.TYPE_ACCELEROMETER) {
			volume = event.values[0] * volumeStep;
			pitch = event.values[1] * pitchStep;
		} else { // Don't use accelerometer

			// Set volume.
			if (type == Sensor.TYPE_PROXIMITY)
				volume = -1 * event.values[0];

			// Set pitch.
			if (type == Sensor.TYPE_LIGHT) // TODO Not working?
				pitch = event.values[0] * pitchStep;
			else if (light == null && type == Sensor.TYPE_ACCELEROMETER) // Fallback.
				pitch = event.values[1] * pitchStep;
		}
	}

	/**
	 * Register sensor listeners, load user preferences and start the audio
	 * thread.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		/*
		 * Register sensors.
		 */
		sensors.registerListener(this, proximity,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensors.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensors.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);

		/*
		 * Get user preferences.
		 */
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		minFrequency = 16; // TODO Should be an user preference.
		maxFrequency = 5000; // TODO Should be an user preference.
		waveForm = WaveForm.SINE; // TODO Make into user preference.
		useTremolo = preferences.getBoolean("tremolo", false);
		usePortamento = preferences.getBoolean("portamento", false);
		useVibrato = preferences.getBoolean("vibrato", true);
		useAutotune = preferences.getBoolean("autotune", true);
		useNintendoMode = preferences.getBoolean("nintendoMode", false);
		useAccelerometer = preferences.getBoolean("accelerometer", false);

		/*
		 * Start audio thread.
		 */
		soundGenerator = new AudioThread().execute();
		alert("Ready...");
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

		if (soundGenerator != null)
			soundGenerator.cancel(true);

		alert("Paused...");
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the sensor provided pitch.
	 */
	private class AudioThread extends AsyncTask<Void, Float, String> {
		boolean running;
		int buffersize = 1024;
		int sampleRate = 44100;
		SoundEffect autotune, tremolo, portamento, vibrato;
		float frequency, amplitude, angle;

		protected void onPreExecute() {
			running = true;

			if (useAutotune)
				autotune = new Autotune();
			if (useTremolo)
				tremolo = new Tremolo();
			if (usePortamento)
				portamento = new Portamento();
			if (useVibrato)
				vibrato = new Vibrato();

			int audioFormat = (useNintendoMode) ? AudioFormat.ENCODING_PCM_8BIT
					: AudioFormat.ENCODING_PCM_16BIT;
			audioStream = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat,
					AudioTrack
							.getMinBufferSize(sampleRate,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									audioFormat), AudioTrack.MODE_STREAM);
			audioStream.play();
		}

		protected String doInBackground(Void... params) {
			while (running) {

				/*
				 * Set initial frequency according to the sensor.
				 */
				frequency = PocketThereminActivity.this.pitch + minFrequency;

				if (frequency > maxFrequency)
					frequency = maxFrequency;

				if (frequency < minFrequency)
					frequency = minFrequency;

				/*
				 * Set initial volume according to the sensor.
				 */
				amplitude = PocketThereminActivity.this.volume;
				audioStream.setStereoVolume(amplitude, amplitude);

				/*
				 * Effects chain.
				 */

				if (autotune != null)
					frequency = autotune.getFrequency(frequency);

				if (vibrato != null)
					frequency = vibrato.getFrequency(frequency);

				if (portamento != null)
					frequency = portamento.getFrequency(frequency);

				if (tremolo != null) {
					amplitude = tremolo.getAmplitude(amplitude);
					audioStream.setStereoVolume(amplitude, amplitude);
				}

				/*
				 * Generate sound samples.
				 */
				short[] samples = new short[buffersize];
				for (int i = 0; i < samples.length; i++) {

					switch (waveForm) {
					case SINE:
						samples[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
						break;
					case SQUARE: // TODO
						if (samples[i - 1] != Short.MAX_VALUE)
							samples[i] = Short.MAX_VALUE;
						else
							samples[i] = -1 * Short.MAX_VALUE;
						break;
					case TRIANGLE: // TODO
						break;
					case SAWTOOTH: // TODO
						break;
					}

					angle += (float) ((2 * Math.PI * frequency) / sampleRate);
				}

				// Write samples.
				audioStream.write(samples, 0, buffersize);

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
			running = false;
		}

		protected void onPostExecute(String result) {
			audioStream.release();
		}
	}

	/**
	 * Provide feedback to the user.
	 */
	private void alert(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}