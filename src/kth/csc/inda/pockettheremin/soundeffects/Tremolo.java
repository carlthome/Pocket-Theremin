package kth.csc.inda.pockettheremin.soundeffects;


/**
 * The volume goes from 0.0 to 1.0.
 * 
 * TODO Document this class properly.
 */
public class Tremolo implements SoundEffect {

	// TODO Allow parameters such as speed, shape and depth.
	float attentuation = 0.0f;
	int direction = 1;

	public float getAmplitude(float amplitude) {
		step();
		return attentuation;
	}

	private void step() {
		if (attentuation >= 1.0f)
			direction = -1;
		else if (attentuation <= 0.0f)
			direction = 1;
		attentuation += 0.1f * direction;
	}

	@Override
	public float getFrequency(float frequency) {
		// TODO Auto-generated method stub
		return 0;
	}
}
