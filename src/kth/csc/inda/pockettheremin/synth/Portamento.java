package kth.csc.inda.pockettheremin.synth;

public class Portamento implements SoundEffect {
	double from, to, current, distance, time, velocity, direction;
	boolean sliding, calculateTravel;

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

			/*
			 * If the sample rate is too low then portamento cannot be performed
			 * so simple do one full step instead.
			 */
			if (velocity > distance)
				velocity = distance;

			direction = Double.compare(to, current);
			calculateTravel = false;
		}

		if (sliding && (Math.min(current, to) / Math.max(current, to) < 0.95)) {
			current += direction * velocity;

			if (Math.abs(current - to) < 10)
				sliding = false;

			return current;
		}

		return current;
	}
}
