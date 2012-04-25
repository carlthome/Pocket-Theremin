package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.activitites.PocketThereminActivity;


public class Portamento implements SoundEffect {
	double from, to, current, distance, time, sampleRate, velocity, direction;
	boolean gliding, init;

	public Portamento(int time) {
		this.gliding = false;
		this.init = true;
		this.time = time;
	}

	@Override
	public double modify(double frequency) {
		return glide(frequency);
	}

	private double glide(double frequency) {
		if (time == 0)
			return frequency;

		if (!gliding) {
			from = current = frequency;
			gliding = true;
			init = true;
			return current;
		}

		if (to != frequency) {
			to = frequency;
			init = true;
		}

		if (init) {
			distance = Math.abs(to - current);
			velocity = (distance / time) / (frequency / 1000);
			direction = Double.compare(to, current);
			init = false;
		}

		if (gliding && (Math.min(current, to) / Math.max(current, to) < 0.95)) {
			current += direction * velocity;

			if (Math.abs(current - to) < 10)
				gliding = false;

			return current;
		}

		return current;
	}

	@Override
	public void sync() {
		sampleRate = PocketThereminActivity.clock.getFrequency();
	}
}
