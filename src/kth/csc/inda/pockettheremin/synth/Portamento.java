package kth.csc.inda.pockettheremin.synth;

public class Portamento implements SoundEffect {
	double from, to, current, distance, time, velocity, direction;
	boolean sliding, calculateTravel;
	static final int ERROR_MARGIN = 5; // %

	public Portamento(int time) {
		this.sliding = false;
		this.calculateTravel = true;
		this.time = time;
	}

	@Override
	public double modify(double frequency) {
		return glide(frequency);
	}

	private double glide(double frequency) {
		if (time == 0)
			return frequency;

		if (!sliding) {
			from = current = frequency;
			sliding = true;
			calculateTravel = true;
			return current;
		}

		if (to != frequency) {
			to = frequency;
			calculateTravel = true;
		}

		if (calculateTravel) {
			distance = Math.abs(to - current);
			velocity = (distance / time) / (clock.getFrequency() / 1000);
			direction = Double.compare(to, current);
			calculateTravel = false;
		}

		if (sliding
				&& (Math.min(current, to) / Math.max(current, to) < 100 - ERROR_MARGIN / 100d)) {
			current += direction * velocity;

			/*
			 * We overshot the target, so simply say that we reached the
			 * destination.
			 */
			if ((current > to && direction > 0)
					|| (current < to && direction < 0))
				current = to;

			if ((Math.min(current, to) / Math.max(current, to) > 100 - ERROR_MARGIN / 100d)) {
				sliding = false;
				return to;
			}

			return current;
		}

		return current;
	}
}