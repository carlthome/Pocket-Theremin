package kth.csc.inda.pockettheremin.soundeffects;

public class Vibrato implements SoundEffect {
	// TODO Allow parameters such as speed, shape and depth.

	int range = 1; // percent up and down
	float pitch;
	float baseFrequency, currentFrequency;
	double angle;
	double increment = (2 * Math.PI) / 512;

	@Override
	public float modify(float frequency) {

		// Store new base frequencies
		float quotient = (baseFrequency / frequency);
		if (1 + percentageToDecimal(range) < quotient
				|| quotient < 1 - percentageToDecimal(range))
			this.baseFrequency = frequency;

		// Pitch a tiny bit.
		step();

		// Return pitched frequency.
		return currentFrequency;
	}

	private void step() {
		pitch = percentageToDecimal(range) * ((float) Math.sin(angle));
		angle += increment;

		currentFrequency = Math.signum(pitch) * baseFrequency;
	}

	private float percentageToDecimal(int percentage) {
		float decimal = 0.00f;
		decimal = (percentage / (float) 100);
		return decimal;
	}
}
