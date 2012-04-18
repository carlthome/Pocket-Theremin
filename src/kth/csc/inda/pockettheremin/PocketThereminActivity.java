package kth.csc.inda.pockettheremin;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
	 * TODO Remove the noise that occurs when play=false and the sound loop is
	 * stopped.
	 */

	/*
	 * TODO Emulate the sound of a theremin (instead of a pure sine curve).
	 * Perhaps use a mp3 for sampling?
	 */

	/*
	 * TODO Prepare a preference menu.
	 */

	/*
	 * TODO Let the user select sensor.
	 */

	static final boolean DEBUG = true;
	private SensorManager sensors;
	private Sensor light, proximity, accelerometer;
	private AsyncTask soundGenerator;
	private AudioTrack sound;
	private float pitch;
	private double amplitude;
	private int maxFrequency, minFrequency;
	private boolean play, autotune, tremolo, portamento, vibrato;

	/**
	 * When the app is started: load graphics and find the sensors.
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Find sensors with the SensorManager.
		sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		light = sensors.getDefaultSensor(Sensor.TYPE_LIGHT);
		accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		proximity = sensors.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (DEBUG) {
			amplitude = 0; // TODO Should be set by a sensor.
			minFrequency = 20; // TODO Should be an user preference.
			maxFrequency = 20000; // TODO Should be an user preference.
		}

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
		 * Calculate sensor stepping.
		 */
		float step = (maxFrequency - minFrequency) / sensor.getMaximumRange();

		/*
		 * Modulate pitch and amplitude just with the accelerometer, or modulate
		 * pitch with the light sensor and amplitude with the proximity sensor.
		 */
		boolean useAccelerometer = false; // TODO Set this by user preference.

		if (useAccelerometer) {
			if (type == Sensor.TYPE_ACCELEROMETER) {
				pitch = event.values[0] * step;
				amplitude = event.values[1] * step;

				if (amplitude <= 0) { // TODO Calibrate values.
					play = false;
				} else {
					play = true;
					/*
					 * TODO Leave the generator on for the entire app duration
					 * and just attenuate the amplitude instead.
					 */
					soundGenerator = new SoundGenerator().execute();
				}
			}
		} else {
			if (type == Sensor.TYPE_PROXIMITY) {
				amplitude = event.values[0];

				if (play)
					play = false;
				else {
					play = true;
					/*
					 * TODO Leave the generator on for the entire app duration
					 * and just attenuate the amplitude instead.
					 */
					soundGenerator = new SoundGenerator().execute();
				}

			} else if (type == Sensor.TYPE_LIGHT) // TODO Not working?
				pitch = event.values[0] * step;
			else if (light == null && type == Sensor.TYPE_ACCELEROMETER) // Fallback.
				pitch = event.values[1] * step;
		}
	}

	/**
	 * Register sensor listeners.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		sensors.registerListener(this, proximity,
				SensorManager.SENSOR_DELAY_FASTEST);
		sensors.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensors.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);

		alert("Ready...");
	}

	/**
	 * Free up system resources when the theremin app isn't used anymore. Also,
	 * make sure to kill any ongoing sounds!
	 */
	@Override
	protected void onPause() {
		super.onPause();
		sensors.unregisterListener(this);

		if (sound.getState() == AudioTrack.STATE_INITIALIZED)
			sound.release();

		alert("Paused...");
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the sensor provided pitch.
	 */
	private class SoundGenerator extends AsyncTask<Void, Double, String> {

		float increment, angle;
		int buffersize = 1024;
		int sampleRate = 44100;

		float attentuation = 0.0f;
		int direction = 1;
		double previousFrequency;
		private AutoTune tuner;
		private double[] scale;

		protected void onPreExecute() {
			tuner = new AutoTune();
			scale = tuner.getMajorScale();

			if (DEBUG)
				Log.d("Scale", "" + Arrays.toString(scale));

			int minSize = AudioTrack.getMinBufferSize(sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			sound = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minSize,
					AudioTrack.MODE_STREAM);
			sound.play();
		}

		protected String doInBackground(Void... params) {
			while (play) {

				// Select the correct frequency modified by pitch.
				double frequency = PocketThereminActivity.this.pitch
						+ minFrequency;
				if (frequency > maxFrequency)
					frequency = maxFrequency;
				if (DEBUG)
					Log.d("Autotune", "Frequency before snap: " + frequency);

				frequency = tuner.snap(frequency, scale);

				// TODO Portamento.
				double difference = frequency - previousFrequency;
				previousFrequency = frequency;
				frequency += difference / buffersize;

				// Generate sound samples.
				short samples[] = new short[buffersize];
				for (int i = 0; i < samples.length; i++) {
					//TODO Look for rounding errors. The sound drops pitch after some time.
					increment = (float) (2 * Math.PI * frequency) / sampleRate;
					samples[i] = (short) ((float) Math.signum(Math.sin(angle)) * Short.MAX_VALUE);
					angle += increment;
				}

				if (DEBUG)
					Log.d("Sound Buffer: ", "Frequency: " + frequency
							+ ", Attentuation: " + attentuation
							+ ", Tremolo Direction: " + direction);

				// Tremolo
				// TODO Make sure tremolo is independent of buffer size.
				sound.setStereoVolume(attentuation, attentuation);
				if (attentuation >= 1.0f)
					direction = -1;
				else if (attentuation <= 0.0f)
					direction = 1;
				attentuation += 0.1f * direction;

				// Write samples.
				sound.write(samples, 0, buffersize);

				// Return amplitude and frequency to the GUI thread.
				publishProgress(frequency, amplitude);
			}

			return "Done.";
		}

		protected void onProgressUpdate(Double... progress) {
			// TODO Separate label and value for easier localization.
			((TextView) findViewById(R.id.textFrequency)).setText("Frequency: "
					+ progress[0].shortValue());
			((TextView) findViewById(R.id.textAmplitude)).setText("Amplitude: "
					+ progress[1].shortValue());
		}

		protected void onPostExecute(String result) {
			sound.release();
		}
	}

	/**
	 * Provide feedback to the user.
	 */
	private void alert(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}