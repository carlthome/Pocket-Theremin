package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	int sampleRate, bufferSize;
	double from, to, current, distance, time, velocity;
	boolean gliding;

	public Portamento(int time, int sampleRate, int bufferSize) {
		this.gliding = false;
		this.time = time;
		this.sampleRate = sampleRate;
		this.bufferSize = bufferSize;
	}

	@Override
	public double modify(double frequency) {
		return glide(frequency);
	}

	double newFreq, freq;

	private double glide(double frequency) {
		// TODO Fix skipping.

		/*
		 * Log.e("Portamento (Gliding:" + gliding + ")", "Current:" + current +
		 * ", From:" + from + ", To:" + to);
		 */

		if (gliding && from != to)
			to = frequency;

		if (!gliding && Math.abs(from - to) < 1) {
			from = current = frequency;
			this.gliding = true;
		} else if (Math.abs(current - to) > 10) {
			distance = Math.abs(from - to);
			velocity = (distance / time); // / (sampleRate / 1000);
			double direction = Double.compare(to, current);
			current += direction * velocity;
		} else {
			this.gliding = false;
			from = current = to;
		}

		return current;

	}
}
