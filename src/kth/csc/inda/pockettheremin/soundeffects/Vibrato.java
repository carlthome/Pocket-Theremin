package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.activitites.PocketThereminActivity;
import kth.csc.inda.pockettheremin.synth.Oscillator;
import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;

public class Vibrato implements SoundEffect {
	MasterClock clock = PocketThereminActivity.clock;
	int speed, depth;
	Oscillator oscillator;

	public Vibrato(int speed, int depth, Waveform waveform) {
		this.speed = speed;
		this.depth = depth;
		oscillator = new Oscillator(waveform);
	}

	@Override
	public double modify(double frequency) {
		return (vibrate(frequency));
	}

	private double vibrate(double frequency) {
		if (speed == 0 || depth == 0)
			return frequency;

		oscillator.setFrequency(speed);

		double pitch = 1 + (depth / (double) 100) * oscillator.getSample();
		return frequency * pitch;
	}

	@Override
	public void sync() {
		oscillator.setSampleRate((int) PocketThereminActivity.clock
				.getFrequency());
	}
}
