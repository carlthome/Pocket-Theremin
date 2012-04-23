package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	double from, to, current, distance, time, velocity;
	boolean gliding;

	public Portamento() {
		gliding = false;
		time = 30;
	}

	@Override
	public double modify(double frequency) {
		return glide(frequency);
	}

	private double glide(double frequency) {
		//TODO Fix skipping.
		if (gliding)
			to = frequency;

		if (!gliding && Math.abs(from - to) < 1) {
			from = current = frequency;
			this.gliding = true;
		} else if (Math.abs(current - to) > 10) {
			distance = Math.abs(from - to);
			velocity = distance / time;
			double direction = Double.compare(to, current);
			current += direction * velocity;
		} else {
			this.gliding = false;
			from = to;
		}

		return current;

	}
}
