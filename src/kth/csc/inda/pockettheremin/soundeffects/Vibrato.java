package kth.csc.inda.pockettheremin.soundeffects;

public class Vibrato implements SoundEffect {
	// TODO Allow parameters such as speed, shape and depth.

	float range = 2; // percent
	float steps = range * 2;
	int direction = 1;
	float vibrato = 1.0f;
	float baseFrequency, currentFrequency;

	@Override
	public float getFrequency(float frequency) {

		// Store new frequencies
		if (frequency != this.baseFrequency)
			this.baseFrequency = frequency;

		// Pitch a tiny bit.
		step();

		// Return pitched frequency.
		return currentFrequency;

	}

	private void step() {

		if (vibrato >= 1.0f + (range / 100))
			direction = -1;
		else if (vibrato <= 1.0f - (range / 100))
			direction = 1;
		float step = ((1.0f + (range / 100)) - (1.0f - (range / 100))) / steps;
		vibrato += step * direction;
		currentFrequency = vibrato * baseFrequency;
	}

	@Override
	public float getAmplitude(float amplitude) {
		// TODO Auto-generated method stub
		return 0;
	}
}
