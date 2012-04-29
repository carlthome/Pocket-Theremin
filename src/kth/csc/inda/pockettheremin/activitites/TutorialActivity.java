package kth.csc.inda.pockettheremin.activitites;

import kth.csc.inda.pockettheremin.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TutorialActivity extends Activity implements OnTouchListener {

	/*
	 * There are multiple playing styles and therefore it's neccessary to
	 * provide multiple tutorials.
	 */
	public enum Tutorial {
		MULTITOUCH, TOUCH, ACCELEROMETER;
	};

	Tutorial tutorial;
	private int frame;
	private LinearLayout body, volume, pitch;
	private TextView instructionSubject, instructionBody, instructionAction;
	private TextView pointLeft, pointDown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Blur the previous activity and load the layout resource.
		 */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.tutorial);

		/*
		 * Register touch listener.
		 */
		this.findViewById(android.R.id.content).setOnTouchListener(this);

		/*
		 * Find layout resources.
		 */
		// body = (LinearLayout) findViewById(android.R.id.content);
		volume = (LinearLayout) findViewById(R.id.volume_control);
		pitch = (LinearLayout) findViewById(R.id.pitch_control);
		instructionSubject = (TextView) findViewById(R.id.instruction_subject);
		instructionBody = (TextView) findViewById(R.id.instruction_body);
		instructionAction = (TextView) findViewById(R.id.instruction_action);
		pointLeft = (TextView) findViewById(R.id.point_left);
		pointDown = (TextView) findViewById(R.id.point_down);

		/*
		 * Select tutorial based on user preference.
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			tutorial = Tutorial.valueOf(extras.getString("tutorial"));

		/*
		 * Run tutorial.
		 */
		runTutorial();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		runTutorial();
		return false; // We only care about POINTER_DOWN.
	}

	private void runTutorial() {

		switch (tutorial) {
		case MULTITOUCH:
			multiTouchTutorial();
			break;
		case TOUCH:
			singleTouchTutorial();
			break;
		case ACCELEROMETER:
			accelerometerTutorial();
			break;
		default:
			throw new AssertionError(this);
		}

		frame++;
	}

	private void multiTouchTutorial() {
		Animation animation1 = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f);
		animation1.setDuration(1500);
		animation1.setRepeatCount(-1);
		animation1.setRepeatMode(Animation.REVERSE);
		animation1.setInterpolator(new AccelerateDecelerateInterpolator());
		Animation animation2 = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f,
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f);
		animation2.setDuration(2000);
		animation2.setRepeatCount(-1);
		animation2.setRepeatMode(Animation.REVERSE);
		animation2.setInterpolator(new AccelerateDecelerateInterpolator());

		switch (frame) {
		case 0:
			instructionSubject.setText("How to play");
			instructionBody
					.setText("This is a quick instruction on how to play music with the Pocket Theremin.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 1:
			volume.setAnimation(animation1);
			instructionSubject.setText("How to control volume");
			instructionBody
					.setText("Slide your left thumb across the leftmost bar to control volume. The higher you go the louder it gets.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 2:
			volume.clearAnimation();
			pitch.setAnimation(animation2);
			instructionSubject.setText("How to select notes");
			instructionBody
					.setText("Slide your right thumb across the bottom bar to select notes. The further right the higher the note.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 3:
			volume.setAnimation(animation1);
			pitch.setAnimation(animation2);
			instructionSubject.setText("How to play melodies");
			instructionBody
					.setText("Combine these two movements to perform music. With a little practice you\'ll be churning out melodies in no time.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 4:
			instructionSubject.setText("Let\'s play!");
			instructionBody
					.setText("There are different sounds available in the menu. Make sure to try them out.");
			instructionAction.setText("Touch the screen to start playing.");
			break;
		default:
			this.finish(); // Tutorial is finished. Kill the activity.
		}
	}

	private void singleTouchTutorial() {
		//TODO Improve instruction.

		Animation animation1 = new TranslateAnimation(
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f);
		animation1.setDuration(1500);
		animation1.setRepeatCount(-1);
		animation1.setRepeatMode(Animation.REVERSE);
		animation1.setInterpolator(new AccelerateDecelerateInterpolator());

		Animation animation2 = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f,
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f);
		animation2.setDuration(2000);
		animation2.setRepeatCount(-1);
		animation2.setRepeatMode(Animation.REVERSE);
		animation2.setInterpolator(new AccelerateDecelerateInterpolator());

		Animation animation3 = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, -0.075f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.55f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.05f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.05f - 0.2f);
		animation3.setDuration(2000);
		animation3.setRepeatCount(-1);
		animation3.setRepeatMode(Animation.REVERSE);
		animation3.setInterpolator(new AccelerateDecelerateInterpolator());

		switch (frame) {
		case 0:
			pitch.setVisibility(View.INVISIBLE);
			volume.setVisibility(View.INVISIBLE);

			instructionSubject.setText("How to play");
			instructionBody
					.setText("This is a quick instruction on how to play music with the Pocket Theremin.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 1:
			volume.setVisibility(View.VISIBLE);
			volume.setAnimation(animation1);

			instructionSubject.setText("How to control volume");
			instructionBody
					.setText("Slide your finger up and down to control volume. The higher you go the louder it gets.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 2:
			volume.clearAnimation();
			volume.setVisibility(View.INVISIBLE);

			pitch.setVisibility(View.VISIBLE);
			pitch.setAnimation(animation2);

			instructionSubject.setText("How to select notes");
			instructionBody
					.setText("Slide your finger left and right across to select notes. The further right the higher the note.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 3:
			pointDown.setText("Volume and pitch");
			pitch.setAnimation(animation3);

			instructionSubject.setText("How to play melodies");
			instructionBody
					.setText("Combine these two movements to perform music. With a little practice you\'ll be churning out melodies in no time.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 4:
			instructionSubject.setText("Let\'s play!");
			instructionBody
					.setText("There are different sounds available in the menu. Make sure to try them out.");
			instructionAction.setText("Touch the screen to start playing.");
			break;
		default:
			this.finish(); // Tutorial is finished. Kill the activity.
		}
	}

	private void accelerometerTutorial() {
		volume.setVisibility(View.INVISIBLE);
		pitch.setVisibility(View.INVISIBLE);

		//TODO Improve instruction.
		switch (frame) {
		case 0:
			instructionSubject.setText("How to play");
			instructionBody.setText("This is a quick instruction on how to play music with the accelerometer.\n\nNote that the accelerometer support is still experimental and might not work with your device.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 1:
			instructionSubject.setText("Position your device");
			instructionBody.setText("The accelerometer depends on earth\'s gravity. First, place your device flat on a table.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 2:
			instructionSubject.setText("How to control volume");
			instructionBody.setText("Angle your device towards you to increase volume. A perpendicular angle with the table is the most loud.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 3:
			instructionSubject.setText("How to select notes");
			instructionBody.setText("Rotate your device like a steering wheel to select notes. The further right the higher the note.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 4:
			instructionSubject.setText("How to play melodies");
			instructionBody.setText("Combine these two movements to perform music. With a little practice you\'ll be churning out melodies in no time.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 5:
			instructionSubject.setText("Let\'s play!");
			instructionBody.setText("There are different sounds available in the menu. Make sure to try them out.");
			instructionAction.setText("Touch the screen to start playing.");
			break;
		default:
			this.finish(); // Tutorial is finished. Kill the activity.
		}
	}
}