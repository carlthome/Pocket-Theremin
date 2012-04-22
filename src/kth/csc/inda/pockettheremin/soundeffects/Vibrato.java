package kth.csc.inda.pockettheremin.soundeffects;

public class Vibrato implements SoundEffect {
	int sampleRate, sampleSize;
	int range = 5; // percent up and down
	int speed = 8;
	float pitch;
	double angle, increment;
	
	public Vibrato(int sampleRate, int sampleSize) {
		this.sampleRate = sampleRate;
		this.sampleSize = sampleSize;
		
		increment = (2 * Math.PI) / sampleSize;
	}
	
	@Override
	public float modify(float frequency) {
		return (pitch(frequency));
	}

	private float pitch(float frequency) {
		pitch = 1 + percentageToDecimal(range) * ((float) Math.sin(angle));
		angle += speed * increment;

		return frequency * pitch;
	}

	private float percentageToDecimal(int percentage) {
		float decimal = 0.00f;
		decimal = (percentage / (float) 100);
		return decimal;
	}
}
