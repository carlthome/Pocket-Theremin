package kth.csc.inda.pockettheremin;

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
	 * TODO Find out why Xperia phones don't seem to provide a light sensor
	 * through the SensorManager. For now the accelerometer is used instead.
	 * Fun!
	 */

	/*
	 * TODO Add "cheat mode" where available frequencies align with a harmonic
	 * scale.
	 */

	/*
	 * TODO Sweep between frequencies.
	 */

	/*
	 * TODO Emulate the sound of a theremin (instead of a pure sine curve).
	 */

	/*
	 * TODO Prepare a preference menu.
	 */

	static final boolean DEBUG = true;
	private SensorManager sensors;
	private Sensor light, proximity, accelerometer;
	private AsyncTask soundGenerator;
	private AudioTrack sound;
	private float pitch, amplitude;
	private int maxFrequency, minFrequency;
	private boolean play;

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
			amplitude = 100; // TODO Should be set by a sensor.
			minFrequency = 200; // TODO Should be an user preference.
			maxFrequency = 6000; // TODO Should be an user preference.
		}

		if (DEBUG)
			for (Sensor sensor : sensors.getSensorList(Sensor.TYPE_ALL))
				Log.e("Sensors", sensor.getName());
	}

	// TODO What is the use of this method?
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.i(sensor.getName(), "onAccuracyChanged: " + accuracy);
	}

	/**
	 * Set theremin pitch according to how much light hits the ambient light
	 * sensor. Also, try to set amplitude according to the proximity sensor if
	 * there is support for more than two values.
	 * 
	 * Toggle the theremin with the proximity sensor.
	 * 
	 * Note: currently the light sensor is not employed. Instead the
	 * accelerometer is used.
	 */
	@Override
	public final void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;

		if (sensor.getType() == Sensor.TYPE_LIGHT
				|| sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float step = (maxFrequency - minFrequency) / sensor.getResolution();
			pitch = event.values[0] * step;
		} else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (play) {
				play = false;
				alert("Stop.");
			} else {
				play = true;
				soundGenerator = new SoundGenerator().execute();
				alert("Play.");
			}
		}

		Log.i(sensor.getName(), "onSensorChanged: " + event.values[0]);
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
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency. The frequency is modulated by the sensor provided pitch.
	 */
	private class SoundGenerator extends AsyncTask<Void, Float, String> {
		float frequency, increment, angle;
		short samples[] = new short[1024];
		int sampleRate = 22050;

		protected void onPreExecute() {
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
			while (PocketThereminActivity.this.play) {
				for (int i = 0; i < samples.length; i++) {

					frequency = PocketThereminActivity.this.pitch
							+ minFrequency;

					if (frequency > maxFrequency)
						frequency = maxFrequency;

					increment = (float) (2 * Math.PI * frequency) / sampleRate;
					samples[i] = (short) ((float) android.util.FloatMath.sin(angle) * Short.MAX_VALUE);
					angle += increment;
				}
				// publishProgress(frequency); No, keep it simple!
				sound.write(samples, 0, samples.length);
			}

			return "Done.";
		}

		protected void onProgressUpdate(Float... progress) {
			alert(Float.toString(progress[0]));
		}

		protected void onPostExecute(String result) { // TODO Redundant method?
			alert(result);
			sound.flush();
			sound.release(); // TODO Flush first?
		}
	}

	/**
	 * Provide feedback to the user.
	 */
	private void alert(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}