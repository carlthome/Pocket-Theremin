package kth.csc.inda.pockettheremin.activitites;

import kth.csc.inda.pockettheremin.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TutorialActivity extends Activity implements OnTouchListener {

	private int instruction;
	LinearLayout layout1, layout2;
	Animation animation1, animation2;
	TextView instructionSubject, instructionBody, instructionAction;
	TextView pointLeft, pointDown;

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
		 * Register rouch listener.
		 */
		this.findViewById(android.R.id.content).setOnTouchListener(this);

		/*
		 * Find layout resources.
		 */
		layout1 = (LinearLayout) findViewById(R.id.animation1);
		layout2 = (LinearLayout) findViewById(R.id.animation2);
		instructionSubject = (TextView) findViewById(R.id.instruction_subject);
		instructionBody = (TextView) findViewById(R.id.instruction_body);
		instructionAction = (TextView) findViewById(R.id.instruction_action);
		pointLeft = (TextView) findViewById(R.id.point_left);
		pointDown = (TextView) findViewById(R.id.point_down);

		/*
		 * Prepare animations.
		 */
		animation1();
		animation2();

		/*
		 * Start tutorial.
		 */
		runTutorial();
	}

	private void animation1() {
		// layout1.setVisibility(View.VISIBLE);
		// animation = AnimationUtils.loadAnimation(this, R.anim.up_down);
		animation1 = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f,
				TranslateAnimation.ABSOLUTE, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f);
		animation1.setDuration(2000);
		animation1.setRepeatCount(-1);
		animation1.setRepeatMode(Animation.REVERSE);
		animation1.setInterpolator(new LinearInterpolator());

		animation1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
	}

	private void animation2() {
		// layout.setVisibility(View.VISIBLE);
		// animation = AnimationUtils.loadAnimation(this, R.anim.up_down);

		animation2 = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.0f - 0.3f,
				TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
				0f);
		animation2.setDuration(4000);
		animation2.setRepeatCount(-1);
		animation2.setRepeatMode(Animation.REVERSE);
		animation2.setInterpolator(new LinearInterpolator());

		animation2.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		instruction++;
		runTutorial();
		return false;
	}

	private void runTutorial() {
		switch (instruction) {
		case 0:
			instructionSubject.setText("How to play");
			instructionBody
					.setText("This is a quick instruction on how to play music with the Pocket Theremin.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 1:
			layout1.setAnimation(animation1);
			instructionSubject.setText("How to control volume");
			instructionBody
					.setText("Slide your left thumb across the left bar to control volume. The higher you go the louder it gets.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 2:
			layout1.clearAnimation();
			layout2.setAnimation(animation2);
			instructionSubject.setText("How to select notes");
			instructionBody
					.setText("Slide your right thumb across the bottom bar to select notes. The further right you go the higher the note.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 3:
			layout1.setAnimation(animation1);
			layout2.setAnimation(animation2);
			instructionSubject.setText("How to play melodies");
			instructionBody
					.setText("Combine these two movements to perform music. With a little practice you\'ll be churning out melodies in no time.");
			instructionAction.setText("Touch the screen to continue.");
			break;
		case 4:
			instructionSubject.setText("Let\'s play!");
			instructionBody
					.setText("There are different sound settings available in the menu. Make sure to try them out. You can also restart this tutorial in the menu by selecting \"Help\".");
			instructionAction.setText("Touch the screen to start playing.");
			break;
		default:
			this.finish(); // Tutorial is finished. Kill the activity.	
		}
	}
}