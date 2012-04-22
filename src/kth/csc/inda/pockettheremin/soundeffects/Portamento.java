package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	float from, to, current, distance, time, velocity;
	boolean gliding;

	public Portamento() {
		gliding = false;
		time = 30;
	}

	@Override
	public float modify(float frequency) {
		return glide(frequency);
	}

	private float glide(float frequency) {
		if (gliding)
			to = frequency;

		if (!gliding && Math.abs(from - to) < 1) {
			from = current = frequency;
			this.gliding = true;
		} else if (Math.abs(current - to) > 10) {
			distance = Math.abs(from - to);
			velocity = distance / time;
			float direction = Float.compare(to, current);
			current += direction * velocity;
		} else {
			this.gliding = false;
			from = to;
		}

		return current;

	}
}
