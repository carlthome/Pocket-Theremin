package kth.csc.inda.pockettheremin.activitites;

import java.text.DecimalFormat;
import java.util.List;

import kth.csc.inda.pockettheremin.R;
import kth.csc.inda.pockettheremin.synth.Autotune;
import kth.csc.inda.pockettheremin.synth.LFO;
import kth.csc.inda.pockettheremin.synth.Oscillator;
import kth.csc.inda.pockettheremin.synth.Portamento;
import kth.csc.inda.pockettheremin.synth.Preset;
import kth.csc.inda.pockettheremin.synth.SoundEffect;
import kth.csc.inda.pockettheremin.synth.Waveform;
import kth.csc.inda.pockettheremin.utils.RangedDouble;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity provides a playable theremin by using either the device's
 * sensors or touch input.
 * 
 * To be frank, this is not like a theremin at all since there are no radiowaves
 * involved, but it's still a fun toy.
 * 
 */
public class PocketThereminActivity extends Activity implements
		SensorEventListener, OnTouchListener {

	/*
	 * Sensors
	 */
	private SensorManager sensorManager;
	private Sensor sensors;

	/*
	 * Audio
	 */
	private AudioManager audioManager;
	public static final boolean SAMPLER = true; // TODO Sampler/synth.
	private AudioTrack audio;
	private boolean play;
	private RangedDouble volume, pitch;

	/*
	 * Views
	 */
	private LinearLayout touchAmplitude, touchFrequency, noteIndicator;
	private TextView textAmplitudeMax, textAmplitudeMin, textFrequencyMax,
			textFrequencyMin;

	/*
	 * Preferences
	 */
	private SharedPreferences preferences;
	private Waveform synthWaveform, tremoloShape, vibratoShape;
	private Autotune.Note key;
	private Autotune.Scale scale;
	private boolean useSensors, useMultitouch, useAutotune, synthIMD,
			synthChiptune;
	private int octaves, tremoloSpeed, tremoloDepth, vibratoSpeed,
			vibratoDepth, portamentoSpeed;

	/**
	 * When the app is started: load graphics and find resources.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Load main layout resource.
		 */
		setContentView(R.layout.main);

		/*
		 * Get dynamic views.
		 */
		touchFrequency = (LinearLayout) findViewById(R.id.touchFrequency);
		touchAmplitude = (LinearLayout) findViewById(R.id.touchAmplitude);
		noteIndicator = ((LinearLayout) findViewById(R.id.notes));
		textAmplitudeMax = ((TextView) findViewById(R.id.amplitudeMax));
		textAmplitudeMin = ((TextView) findViewById(R.id.amplitudeMin));
		textFrequencyMax = ((TextView) findViewById(R.id.frequencyMax));
		textFrequencyMin = ((TextView) findViewById(R.id.frequencyMin));

		/*
		 * Set default preferences by resource.
		 */
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

		/*
		 * Get system services.
		 */
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		sensors = sensorManager.getDefaultSensor(Sensor.TYPE_ALL);

		/*
		 * Run tutorial on first launch.
		 */
		boolean hasRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
				.getBoolean("has_run", false);
		if (!hasRun) {
			launchTutorial();

			// Only run tutorial once.
			getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
					.putBoolean("has_run", true).commit();
		}
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
		refreshPreferences();

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
		if (useSensors) {
			/*
			 * TODO Touch input seems to be registered at this stage, even
			 * though is shouldn't be.
			 */
			sensorManager.registerListener(this, sensors,
					SensorManager.SENSOR_DELAY_NORMAL);

			/*
			 * Log available sensors for debugging.
			 */
			List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
			for (Sensor sensor : sensors)
				Log.d("Sensors", sensor.getName());
		} else
			this.findViewById(android.R.id.content).setOnTouchListener(this);

		/*
		 * Refresh audio manager.
		 */
		refreshAudioManager();

		/*
		 * Start executing audio thread.
		 */
		play = true;
		new AudioThread().execute();
	}

	/**
	 * Unregister sensor listeners and kill the audio thread (i.e free up system
	 * resources when the app isn't used anymore).
	 */
	@Override
	protected void onPause() {
		super.onPause();

		if (sensorManager != null)
			sensorManager.unregisterListener(this);

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
		menu.add(Menu.NONE, 1, 1, "Tutorial").setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, 2, 2, "Settings").setIcon(
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
			launchTutorial();
			return true;
		case 2:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		}
		return false;
	}

	/**
	 * Set pitch and volume by touch.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float x, y;
		int pointers = event.getPointerCount();

		if (useMultitouch) {
			/*
			 * The entire activity view is touch enabled but only parts of the
			 * GUI actually updates the pitch and volume, simply by having
			 * divided the screen into pixel-based areas.
			 * 
			 * This is probably not the best solution but it seems to work
			 * fairly consistently.
			 */
			for (int i = 0; i < pointers; i++) {
				x = event.getX(i);
				y = event.getY(i);

				if (x < 70)
					volume.set((view.getHeight() - y)
							* (volume.range / view.getHeight()));

				int shrink = 90; // Avoid overlapping the volume bar.
				if (y > (view.getHeight() - 80) && x > shrink) {
					pitch.set((x - shrink)
							* (pitch.range / (view.getWidth() - shrink))
							+ pitch.minimum);

					// TODO Clean.
					if (useAutotune)
						pitch.set(Math.pow(
								Autotune.TEMPERAMENT,
								((x - shrink)
										* (Math.log(pitch.maximum
												/ pitch.minimum) / Math
													.log(Autotune.TEMPERAMENT)) / (view
										.getWidth() - shrink))
										+ (Math.log(pitch.minimum) / Math
												.log(Autotune.TEMPERAMENT))));

					Log.d("onTouch", "Pitch: " + pitch.get() + ", X:" + x
							+ ", Scaling:" + pitch.range / view.getWidth());
				}

				if (!SAMPLER) { // TODO Sampler/synth.
					audio.setPlaybackRate((int) (x * 440));
					audio.setStereoVolume(y, y);
				}
			}
		} else {
			y = event.getY();
			x = event.getX();

			volume.set((view.getHeight() - y)
					* (volume.range / view.getHeight()));
			pitch.set(x * pitch.range / view.getWidth());

			if (useAutotune) {
				// TODO Implement logarithmic scale if autotune is enabled.
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
	 * Set pitch and amplitude by interpreting sensor values.
	 */
	@Override
	public final void onSensorChanged(SensorEvent event) {
		/*
		 * TODO Remove sensor support. Rely solely on touchscreens instead.
		 */

		Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// TODO Test several devices.
			pitch.set((event.values[1] + SensorManager.STANDARD_GRAVITY)
					* (pitch.range / sensor.getMaximumRange()));
			// volume.set((SensorManager.STANDARD_GRAVITY - event.values[0]) *
			// (volume.range / sensor.getMaximumRange()));

			if (useAutotune) {
				// TODO Implement logarithmic scale if autotune is enabled.
			}

			/*
			 * Print debug information.
			 */
			int i = 0;
			String s = "";
			for (float value : event.values)
				s += (i++) + ": " + value + ", ";
			Log.d(sensor.getName(),
					s + ", Resolution: " + sensor.getResolution() + ", Range: "
							+ sensor.getMaximumRange());
		}

		if (sensor.getType() == Sensor.TYPE_PROXIMITY)
			volume.set(event.values[0]
					* (volume.range / sensor.getMaximumRange()));
	}

	/**
	 * Update the audio manager and the GUI if the user changes volume with
	 * hardware buttons.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP: {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			refreshGraphics();
			return true;
		}
		case KeyEvent.KEYCODE_VOLUME_DOWN: {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			refreshGraphics();
			return true;
		}
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Update graphics just as the the window gains focus and turns visible.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		refreshGraphics();
	}

	/**
	 * Convenience method for updating the GUI in case the user has changed the
	 * app preferences.
	 */
	private void refreshGraphics() {

		/*
		 * Note indicator.
		 */
		// TODO
		/*
		 * if (useAutotune && !useSensor) { LinkedList<Point> points = new
		 * LinkedList<Point>(); int numberOfNotes = octaves * 12; for (int i =
		 * 0; i <= numberOfNotes; i++) points.add(new Point(i *
		 * (noteIndicator.getWidth() / numberOfNotes), 0));
		 * noteIndicator.addView(new DrawPoints(this, points),
		 * noteIndicator.getWidth(), noteIndicator.getHeight()); } else
		 * noteIndicator.removeAllViews();
		 */

		/*
		 * Show buttons based on playing style.
		 */
		if (useSensors) {
			textAmplitudeMax.setVisibility(View.GONE);
			textAmplitudeMin.setVisibility(View.GONE);
			textFrequencyMax.setVisibility(View.GONE);
			textFrequencyMin.setVisibility(View.GONE);
		} else {
			textAmplitudeMax.setVisibility(View.VISIBLE);
			textAmplitudeMin.setVisibility(View.VISIBLE);
			textFrequencyMax.setVisibility(View.VISIBLE);
			textFrequencyMin.setVisibility(View.VISIBLE);

			if (useMultitouch) {
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

		/*
		 * Update maximum and minimum values.
		 */
		textAmplitudeMax.setText(new DecimalFormat("#.#")
				.format(volume.maximum) + "");
		textAmplitudeMin.setText(new DecimalFormat("#.#")
				.format(volume.minimum) + "");
		textFrequencyMin.setText(new DecimalFormat("#").format(pitch.minimum)
				+ " Hz");
		textFrequencyMax.setText(new DecimalFormat("#").format(pitch.maximum)
				+ " Hz");

	}

	/**
	 * Convenience method for updating certain values related to the audio
	 * stream that might change during the activity's life cycle, such as the
	 * maximum volume.
	 */
	private void refreshAudioManager() {
		pitch = new RangedDouble(key.frequency(4 + octaves / 2),
				key.frequency(4 - octaves / 2));
		volume = new RangedDouble(
				audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
				AudioTrack.getMinVolume());
	}

	/**
	 * Convenience method that loads app preferences into class fields.
	 */
	private void refreshPreferences() {
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		 * Playing style.
		 */
		useSensors = preferences.getBoolean("sensors", false);
		useMultitouch = preferences.getBoolean("multitouch", true);
		octaves = Integer.parseInt(preferences.getString("octaves", "2"));
		useAutotune = preferences.getBoolean("autotune", true);
		key = Autotune.Note.valueOf(preferences.getString("key", "A"));
		scale = Autotune.Scale.valueOf(preferences.getString("scale", "MAJOR"));

		/*
		 * Sounds.
		 */
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
			synthIMD = preferences.getBoolean("imd", false);
			synthChiptune = preferences.getBoolean("chiptune", false);

			if (preferences.getBoolean("vibrato", true)) {
				vibratoShape = Waveform.valueOf(preferences.getString(
						"vibrato_waveform", "TRIANGLE"));
				vibratoSpeed = Integer.parseInt(preferences.getString(
						"vibrato_speed", "4"));
				vibratoDepth = Integer.parseInt(preferences.getString(
						"vibrato_depth", "2"));
			} else
				vibratoDepth = 0;

			if (preferences.getBoolean("tremolo", true)) {
				tremoloShape = Waveform.valueOf(preferences.getString(
						"tremolo_waveform", "SINE"));
				tremoloSpeed = Integer.parseInt(preferences.getString(
						"tremolo_speed", "1"));
				tremoloDepth = Integer.parseInt(preferences.getString(
						"tremolo_depth", "10"));
			} else
				tremoloDepth = 0;

			if (preferences.getBoolean("portamento", true)) {
				portamentoSpeed = Integer.parseInt(preferences.getString(
						"portamento_speed", "10"));
			} else
				portamentoSpeed = 0;
		}
	}

	/**
	 * Provide feedback to the user.
	 */
	private void alert(String s, boolean requireDismissal) {
		if (requireDismissal) {
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
	 * Simply launch a new tutorial activity on top of this activity, but also
	 * pass information about the users selected playing style so that the
	 * tutorial can be made relevant.
	 */
	private void launchTutorial() {
		refreshPreferences(); // Make sure preferences are set!

		Intent i = new Intent(this, TutorialActivity.class);

		if (useSensors)
			i.putExtra("tutorial",
					TutorialActivity.Tutorial.ACCELEROMETER.name());
		else {
			if (useMultitouch)
				i.putExtra("tutorial",
						TutorialActivity.Tutorial.MULTITOUCH.name());
			else
				i.putExtra("tutorial", TutorialActivity.Tutorial.TOUCH.name());
		}

		startActivity(i);
	}

	/**
	 * Generate sounds in a separate thread (as an AsyncTask) by sampling a
	 * frequency.
	 * 
	 * First, the frequency and amplitude is determined from the user input in
	 * the GUI thread. Then sound effects are applied, such as LFO frequency
	 * modulation and LFO amplitude modulation. Finally the samples are
	 * generated and queued in an audio track.
	 * 
	 * The audio track is playing simultaneously as this thread is generating
	 * samples. In other words this is a streaming model.
	 */
	private class AudioThread extends AsyncTask<Void, Double, Void> {
		private int sampleRate, bufferSize, audioFormat;
		private Oscillator oscillator;
		private SoundEffect autotune, vibrato, tremolo, portamento;

		protected void onPreExecute() {
			/*
			 * Calculate sample rate and buffer size.
			 */
			audioFormat = (synthChiptune) ? AudioFormat.ENCODING_PCM_8BIT
					: AudioFormat.ENCODING_PCM_16BIT;

			sampleRate = AudioTrack
					.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

			bufferSize = AudioTrack.getMinBufferSize(sampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat);

			/*
			 * Prepare oscillator and sound effects. Sound effects can be null.
			 */
			oscillator = new Oscillator(synthWaveform);
			oscillator.setSampleRate(sampleRate);
			oscillator.setIMD(synthIMD);
			autotune = (useAutotune) ? new Autotune(key, scale, octaves) : null;
			vibrato = (vibratoDepth > 0) ? new LFO(vibratoSpeed, vibratoDepth,
					vibratoShape) : null;
			tremolo = (tremoloDepth > 0) ? new LFO(tremoloSpeed, tremoloDepth,
					tremoloShape) : null;
			portamento = (portamentoSpeed > 0) ? new Portamento(portamentoSpeed)
					: null;

			if (SAMPLER) {

				/*
				 * Setup audio stream.
				 */
				audio = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat,
						bufferSize, AudioTrack.MODE_STREAM);

				/*
				 * Since each sample actually consists of two bytes, cut the
				 * buffer size in half.
				 */
				if (audioFormat == AudioFormat.ENCODING_PCM_16BIT)
					bufferSize = bufferSize / 2;

				/*
				 * Begin audio stream.
				 */
				audio.play();
			} else { // TODO Sampler/synth.

				/*
				 * Setup audio loop.
				 */
				audio = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO, audioFormat,
						bufferSize, AudioTrack.MODE_STATIC);
				oscillator.setFrequency(440);
				short[] samples = oscillator.getSamples(bufferSize);
				audio.write(samples, 0, bufferSize);
				audio.setLoopPoints(0, samples.length / 2, -1);
				audio.play();
				play = false;
			}

			/*
			 * Let sound effects have some playing room by generating smaller
			 * buffers. This introduces occasional buffer underruns and should
			 * be improved upon.
			 */
			bufferSize /= 10;

			Log.d(this.getClass().getSimpleName(), "Sample rate: " + sampleRate
					+ ", Buffer size: " + bufferSize);
		}

		protected Void doInBackground(Void... params) {
			while (play) {

				/*
				 * Help keep sound effects in sync.
				 */
				SoundEffect.clock.tick();

				/*
				 * Write empty buffers until the clock is stable.
				 */
				if (!SoundEffect.clock.isStable()) {
					audio.write(new short[bufferSize], 0, bufferSize);
					continue;
				}

				/*
				 * Set initial frequency and amplitude.
				 */
				double frequency = pitch.get();
				double amplitude = volume.get();

				/*
				 * Effects chain.
				 */
				if (autotune != null)
					frequency = autotune.modify(frequency);

				if (portamento != null)
					frequency = portamento.modify(frequency);

				if (vibrato != null)
					frequency = vibrato.modify(frequency);

				if (tremolo != null)
					amplitude = tremolo.modify(amplitude);

				/*
				 * Generate and write sound samples if in streaming mode
				 * (sampler), or simply adjust the playback rate if in static
				 * loop mode (synth).
				 */
				if (SAMPLER) {
					oscillator.setFrequency(frequency);
					audio.write(oscillator.getSamples(bufferSize), 0,
							bufferSize);
				} else
					audio.setPlaybackRate((int) (frequency));

				/*
				 * Set final volume.
				 */
				audio.setStereoVolume((float) (amplitude / volume.range),
						(float) (amplitude / volume.range));

				/*
				 * Return frequency and amplitude to the GUI thread.
				 */
				publishProgress(frequency, amplitude);
			}
			return null;
		}

		protected void onProgressUpdate(Double... progress) {
			((TextView) findViewById(R.id.frequency))
					.setText(new DecimalFormat("#").format(progress[0]) + " Hz");

			((TextView) findViewById(R.id.amplitude))
					.setText(new DecimalFormat("#.#").format(progress[1]) + "");
		}

		protected void onPostExecute(Void result) {
			if (audio != null)
				audio.release();
		}
	}
}