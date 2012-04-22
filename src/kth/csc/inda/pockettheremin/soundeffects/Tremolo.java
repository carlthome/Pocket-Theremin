package kth.csc.inda.pockettheremin.soundeffects;

// TODO Allow parameters such as speed, shape and depth.
public class Tremolo implements SoundEffect {
	int sampleRate, sampleSize;
	int speed, depth;
	float attentuation;
	double angle, increment;
	
	public Tremolo(int speed, int depth, int sampleRate, int sampleSize) {
		this.speed = speed;
		this.depth = depth;
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		
		increment = (2 * Math.PI) / sampleSize;
	}

	@Override
	public float modify(float amplitude) {
		return (tremble(amplitude));
	}

	private float tremble(float amplitude) {
		attentuation = 1 + percentageToDecimal(depth) * ((float) Math.sin(angle));
		angle += speed * increment;

		return amplitude * attentuation;
	}
	
	private float percentageToDecimal(int percentage) {
		float decimal = 0.00f;
		decimal = (percentage / (float) 100);
		return decimal;
	}
}
