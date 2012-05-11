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
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Animated tutorial with instructions about how to use the application.
 * 
 * Assumes to be launched on top of the main activity.
 */
public class Tutorial extends Activity implements OnTouchListener {
	private int instruction;
	private LinearLayout animate;
	private TextView pointLeft, pointDown;
	private TextView instructionSubject, instructionBody, instructionAction;
	private Animation animation1, animation2, animation3;

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
		animate = (LinearLayout) findViewById(R.id.animate);
		pointLeft = (TextView) findViewById(R.id.point_left);
		pointDown = (TextView) findViewById(R.id.point_down);
		instructionSubject = (TextView) findViewById(R.id.instruction_subject);
		instructionBody = (TextView) findViewById(R.id.instruction_body);
		instructionAction = (TextView) findViewById(R.id.instruction_action);

		/*
		 * Create animations.
		 */
		animation1 = new TranslateAnimation(
				Animation.ABSOLUTE, 0,
				Animation.ABSOLUTE, 0,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.3f);
		animation1.setDuration(1500);
		animation1.setRepeatCount(-1);
		animation1.setRepeatMode(Animation.REVERSE);
		animation1.setInterpolator(new AccelerateDecelerateInterpolator());

		animation2 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.3f,
				Animation.ABSOLUTE, 0, 
				Animation.ABSOLUTE, 0);
		animation2.setDuration(2000);
		animation2.setRepeatCount(-1);
		animation2.setRepeatMode(Animation.REVERSE);
		animation2.setInterpolator(new AccelerateDecelerateInterpolator());

		animation3 = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.2f,
				Animation.RELATIVE_TO_PARENT, 0.1f,
				Animation.RELATIVE_TO_PARENT, -0.2f);
		animation3.setDuration(2000);
		animation3.setRepeatCount(-1);
		animation3.setRepeatMode(Animation.REVERSE);
		animation3.setInterpolator(new AccelerateDecelerateInterpolator());

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
		switch (instruction) {
		case 0:
			pointLeft.setVisibility(View.INVISIBLE);
			pointDown.setVisibility(View.INVISIBLE);

			instructionSubject.setText("How to play");
			instructionBody
					.setText("This is a quick instruction on how to play music with the Pocket Theremin.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 1:
			animate.setAnimation(animation1);
			pointLeft.setVisibility(View.VISIBLE);
			pointDown.setVisibility(View.INVISIBLE);

			instructionSubject.setText("How to control volume");
			instructionBody
					.setText("Starting in the middle, slide your finger up or down to increase volume.");
			break;
		case 2:
			animate.setAnimation(animation2);
			pointLeft.setVisibility(View.INVISIBLE);
			pointDown.setVisibility(View.VISIBLE);

			instructionSubject.setText("How to select notes");
			instructionBody
					.setText("Slide your finger left and right to select notes. The further right the higher the note.");
			break;
		case 3:
			animate.setAnimation(animation3);
			pointLeft.setVisibility(View.INVISIBLE);
			pointDown.setVisibility(View.VISIBLE);

			pointDown.setText("Volume and pitch");

			instructionSubject.setText("How to play melodies");
			instructionBody
					.setText("Combine these two movements to perform music. With a little practice you\'ll be churning out melodies in no time.");
			break;
		case 4:
			pointLeft.setVisibility(View.INVISIBLE);
			pointDown.setVisibility(View.INVISIBLE);

			instructionSubject.setText("Let\'s play!");
			instructionBody
					.setText("There are different sounds available in the menu. Make sure to try them out.");
			instructionAction.setText("Touch the screen to start playing.");
			break;
		default:
			this.finish(); // Tutorial is finished. Kill the activity.
		}

		instruction++;
	}
}