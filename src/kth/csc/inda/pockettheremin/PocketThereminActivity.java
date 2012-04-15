package kth.csc.inda.pockettheremin;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
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

	static final boolean DEBUG = true;

	private SensorManager mSensorManager;
	private Sensor mLight, mProximity; //TODO Find out why Xperia phones don't seem to provide a light sensor through the SensorManager.
	private ToneGenerator toneGenerator; //TODO Implement a sweeping tone generator.
	private int pitch, amplitude;
	
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
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		alert("Loaded..."); // TODO Provide more detailed info to the user.
		
pitch = ToneGenerator.TONE_CDMA_ABBR_ALERT;
amplitude = 100;

		if (DEBUG)
			for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL))
				Log.d("SENSORS", sensor.getName());
	}

	/**
	 * Set theremin pitch according to how much light hits the ambient light
	 * sensor. Also try to set amplitude according to the proximity sensor if
	 * there is support for more than two values.
	 */
	@Override
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {

		if (sensor.getType() == Sensor.TYPE_LIGHT) 
			pitch = accuracy;
		else if (sensor.getType() == Sensor.TYPE_PROXIMITY && sensor.getResolution() > 1) 
			amplitude = accuracy;

		Log.i(sensor.getName(), "onAccuracyChanged: " + accuracy);
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;

		if (sensor.getType() == Sensor.TYPE_PROXIMITY) 
			toneGenerator.startTone(pitch); //TODO Play tone continuously while updating both pitch and amplitude. (i.e. don't use the ToneGenerator directly)

		Log.i(sensor.getName(), "onSensorChanged: " + event.values[0]);
	}

	/**
	 * Instantiate the tone generator and register sensor listeners.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// Register listeners for the sensors.
		mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);

		// Prepare a new tone generator.
		toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
	}

	/**
	 * Free up system resources when the theremin app isn't used anymore.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		mSensorManager.unregisterListener(this);
		toneGenerator.release();
	}

	/**
	 * Provide popups to the user.
	 */
	private void alert(String s) { // TODO Make sure this method isn't redundant.
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}