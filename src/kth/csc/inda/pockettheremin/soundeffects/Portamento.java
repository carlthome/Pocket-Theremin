package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	float from, to, current, distance, time, velocity;
	boolean gliding;

	public Portamento() {
		gliding = false;
		time = 100; // ms
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
			distance = Math.abs(from - to);
			velocity = distance / time;
		}

		if (gliding) {
			if (to > current)
				return current += (int) (velocity + 0.5);

			if (to < current)
				return current -= (int) (velocity + 0.5);

			if (to == current)
				gliding = false;

			return to;
		} else
			return current;
	}
}
