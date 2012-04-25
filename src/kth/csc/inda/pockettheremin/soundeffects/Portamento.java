package kth.csc.inda.pockettheremin.soundeffects;


public class Portamento implements SoundEffect {
	int sampleRate, bufferSize;
	double from, to, current, distance, time, velocity, direction;
	boolean gliding, init;

	public Portamento(int time, int sampleRate, int bufferSize) {
		this.gliding = false;
		this.init = true;
		this.time = time;
		this.sampleRate = sampleRate;
		this.bufferSize = bufferSize;
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
			velocity = (distance / time);
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
}
