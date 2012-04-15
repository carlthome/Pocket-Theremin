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
 * @author Carl
 * 
 */
public class PocketThereminActivity extends Activity implements
		SensorEventListener {
	/*
	 * // TODO Find out why Xperia phones don't seem to provide a light sensor
	 * through the SensorManager. For now the accelerometer is used instead.
	 * Fun!
	 */
	
	/*
	 * TODO Emulate the sound of a theremin (instead of a pure sinus curve).
	 */

	static final boolean DEBUG = true;

	private SensorManager mSensorManager;
	private Sensor mLight, mProximity, mAccelerometer;

	AsyncTask soundGenerator;
	AudioTrack sound;
	protected float pitch, amplitude;
	boolean play;

	/**
	 * When the app is started: load graphics and find the sensors.
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Find sensors with the SensorManager.
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (DEBUG) {
			pitch = 1000;
			amplitude = 100;
		}

		if (DEBUG)
			for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL))
				Log.e("Sensors", sensor.getName());

		alert("Ready..."); // TODO Provide more detailed info to the user.
	}

	// TODO What is the use of this method?
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (DEBUG)
			alert(Integer.toString(accuracy));

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
			pitch = (event.values[0] * 20) + 1000;
			//alert("Current pitch:" + pitch);
		} else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (play) {
				play = false;
				alert("Stop.");
				soundGenerator.cancel(true);
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
		mSensorManager.registerListener(this, mProximity,
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mLight,
				SensorManager.SENSOR_DELAY_GAME);
	}

	/**
	 * Free up system resources when the theremin app isn't used anymore. Also,
	 * make sure to kill any ongoing sounds!
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		sound.release();
	}

	private class SoundGenerator extends AsyncTask<Void, Void, Void> {
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

		protected Void doInBackground(Void... params) {
			while (PocketThereminActivity.this.play) {
				for (int i = 0; i < samples.length; i++) {
					frequency = PocketThereminActivity.this.pitch; //TODO Interpret sensor input here.
					
					increment = (float) (2 * Math.PI * frequency) / sampleRate;
					samples[i] = (short) ((float) Math.sin(angle) * Short.MAX_VALUE);
					angle += increment;
				}

				sound.write(samples, 0, samples.length);
			}
			return null;
		}

		protected void onPostExecute(Void result) { // TODO Redundant method?
			sound.release(); // TODO Flush first?
		}
	}

	/**
	 * Provide popups to the user.
	 */
	private void alert(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}