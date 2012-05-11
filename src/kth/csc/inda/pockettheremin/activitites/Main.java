package kth.csc.inda.pockettheremin.activitites;

import java.util.Timer;
import java.util.TimerTask;

import kth.csc.inda.pockettheremin.R;
import kth.csc.inda.pockettheremin.input.Autotune;
import kth.csc.inda.pockettheremin.synth.AudioThread;
import kth.csc.inda.pockettheremin.utils.Global;
import kth.csc.inda.pockettheremin.utils.Range;
import kth.csc.inda.pockettheremin.view.GraphView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This activity provides a playable theremin by using either the device's
 * sensors or touch input.
 * 
 * To be frank, this is not like a theremin at all since there are no radio
 * waves involved, but it's still a fun toy.
 * 
 * This is the application's main activity. Every other activity should be
 * created after this one, so that translucent backgrounds can make use of this
 * activity's view, and so that user preferences are properly loaded from the
 * application resources.
 */
public class Main extends Activity implements OnTouchListener, Global {

	private AudioThread audioThread;
	private Timer graphThread;
	private AudioManager audioManager;
	private Autotune autotune;
	private GraphView graph;
	private boolean hasMultiTouch;

	/**
	 * Load graphics and find resources on application launch.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Load view.
		 */
		graph = new GraphView(this);
		setContentView(graph);

		/*
		 * Set default preferences by resource.
		 */
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);

		/*
		 * Get system services.
		 */
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		/*
		 * Check for multi-touch support. Distinct multi-touch support was
		 * implemented in API 8.
		 */
		hasMultiTouch = false; // Assume false.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			hasMultiTouch = this.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);

		/*
		 * Run tutorial on first launch.
		 */
		boolean hasRun = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("has_run", false);
		if (!hasRun) {
			startActivity(new Intent(this, Tutorial.class));
			PreferenceManager.getDefaultSharedPreferences(this).edit()
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
		Preferences.loadPreferences(PreferenceManager
				.getDefaultSharedPreferences(this));

		/*
		 * Remind the user to turn up the volume on their device.
		 */
		if (0.1 > audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(getString(R.string.dialog_low_volume_headline))
					.setMessage(getString(R.string.dialog_low_volume_text))
					.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									refreshAudioManager();
								}
							});
			builder.show();
		}

		/*
		 * Setup autotune.
		 */
		autotune = (G.useAutotune) ? new Autotune(G.key, G.scale, G.octaves)
				: null;

		/*
		 * Register touch listener.
		 */
		this.findViewById(android.R.id.content).setOnTouchListener(this);

		/*
		 * Refresh audio manager.
		 */
		refreshAudioManager();

		/*
		 * Start audio thread.
		 */
		audioThread = new AudioThread();
		audioThread.start();

		/*
		 * Start graphics thread.
		 */
		graphThread = new Timer();
		graphThread.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						graph.invalidate();
					}
				});
			}
		}, 0, 33); // Roughly 30FPS.
	}

	/**
	 * Free up system resources when the activity isn't visible anymore.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (audioThread != null) {
			audioThread.cancel();
			audioThread = null;
		}

		if (graphThread != null) {
			graphThread.cancel();
			graphThread.purge();
			graphThread = null;
		}
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
	 * Activity menu functionality.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			startActivity(new Intent(this, About.class));
			return true;
		case 1:
			startActivity(new Intent(this, Tutorial.class));
			return true;
		case 2:
			startActivity(new Intent(this, Preferences.class));
			return true;
		}
		return false;
	}

	/**
	 * Interpret touch events to set frequency and volume.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float x, y;
		final int width = view.getWidth();
		final int height = view.getHeight();

		if (hasMultiTouch) {
			/*
			 * The running device features multi-touch.
			 */
			int pointers = event.getPointerCount();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_MOVE: // TODO Allow one synth per pointer.
				for (int i = 0; i < pointers; i++) {
					x = event.getX(i);
					y = event.getY(i);

					setFrequency(x, width);
					setVolume(y, height);
				}
				break;
			}
		} else {
			/*
			 * The device cannot handle multi-touch. Either because the current
			 * Android version is too old and doesn't support multi-touch, or
			 * because the running device doesn't allow it.
			 */
			x = event.getX();
			y = event.getY();
			setFrequency(x, width);
			setVolume(y, height);
		}

		return true; // Consume event so that ACTION_MOVE is handled.
	}

	/**
	 * Set frequency.
	 */
	private void setFrequency(float x, int width) {
		if (!G.useAutotune) {
			G.frequency.set((x) * (G.frequency.range / (width))
					+ G.frequency.min);
		} else {
			G.frequency
					.set(autotune.snap(Math.pow(
							Autotune.TEMPERAMENT,
							(x
									* (Math.log(G.frequency.max
											/ G.frequency.min) / Math
												.log(Autotune.TEMPERAMENT)) / width)
									+ (Math.log(G.frequency.min) / Math
											.log(Autotune.TEMPERAMENT)))

					));
		}

		if (DEBUG)
			Log.d("onTouch", "Frequency: " + G.frequency.get() + ", X:" + x);
	}

	/**
	 * Set volume.
	 */
	private void setVolume(float y, int height) {
		G.volume.set(-1
				* (2 * Math.signum(y - (height / 2)) * Math.abs(y
						- (height / 2))) * (G.volume.range / height));

		if (DEBUG)
			Log.d("onTouch", "Volume: " + G.volume.get() + ", Y:" + y);
	}

	/**
	 * Update the audio manager and the GUI if the user changes the volume with
	 * hardware buttons.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP: {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			return true;
		}
		case KeyEvent.KEYCODE_VOLUME_DOWN: {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			refreshAudioManager();
			return true;
		}
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Convenience method for updating certain values related to the audio
	 * stream that might change during the activity's life cycle, such as the
	 * maximum volume.
	 */
	private void refreshAudioManager() {

		/*
		 * Remember the current values.
		 */
		double oldPitch = 0, oldVolume = 0;
		if (G.frequency != null)
			oldPitch = G.frequency.get();

		if (G.volume != null)
			oldVolume = G.volume.get();

		/*
		 * Create new ranges according to changed settings.
		 */
		G.frequency = new Range(G.key.frequency(4 + G.octaves / 2),
				G.key.frequency(4 - G.octaves / 2));
		// G.volume = new
		// Range(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
		// AudioTrack.getMinVolume());
		G.volume = new Range(
				audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
				-audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

		/*
		 * Restore the current values within the new ranges. The current value
		 * might be clamped to the new maximum or minimum.
		 */
		G.frequency.set(oldPitch);
		G.volume.set(oldVolume);
	}

}