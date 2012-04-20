package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	float from, to, current, distance, speed, step;
	boolean gliding;

	public Portamento() {
		speed = 1; // TODO Make speed independent of distance between notes.
		gliding = false;
	}

	@Override
	public float modify(float frequency) {
		return glide(frequency);
	}

	private float glide(float frequency) {
		if (!gliding) {
			from = current = frequency;
			gliding = true;
		} else {
			to = frequency;
			distance = Math.signum(to - from);
			step = speed;
		}

		if (gliding) {
			if (to > current)
				return current += step;

			if (to < current)
				return current -= step;

			if (to == current)
				gliding = false;

			return to;
		} else
			return current;
	}
	
	public boolean isGliding() {
		return gliding;
	}
}
