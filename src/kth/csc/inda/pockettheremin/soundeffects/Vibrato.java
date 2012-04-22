package kth.csc.inda.pockettheremin.soundeffects;

public class Vibrato implements SoundEffect {
	int sampleRate, sampleSize;
	int speed, depth;
	float pitch;
	double angle, increment;

	public Vibrato(int speed, int depth, int sampleRate, int sampleSize) {
		this.speed = speed;
		this.depth = depth;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;

		increment = (2 * Math.PI) / sampleSize;
	}

	@Override
	public float modify(float frequency) {
		return (vibrate(frequency));
	}

	private float vibrate(float frequency) {
		pitch = 1 + percentageToDecimal(depth) * ((float) Math.sin(angle));
		angle += speed * increment;

		return frequency * pitch;
	}

	private float percentageToDecimal(int percentage) {
		float decimal = 0.00f;
		decimal = (percentage / (float) 100);
		return decimal;
	}
}
