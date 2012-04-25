package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.activitites.PocketThereminActivity;
import kth.csc.inda.pockettheremin.synth.Oscillator;
import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;

public class Tremolo implements SoundEffect {
	int sampleRate, bufferSize;
	int speed, depth;
	Oscillator oscillator;

	public Tremolo(int speed, int depth, Waveform waveform, int sampleRate,
			int bufferSize) {
		this.speed = speed;
		this.depth = depth;
		oscillator = new Oscillator(waveform);

	}

	@Override
	public double modify(double amplitude) {
		return (tremble(amplitude));
	}

	private double tremble(double amplitude) {
		if (speed == 0 || depth == 0)
			return amplitude;
		
		oscillator.setFrequency(speed);

		double attentuation = 1 + (depth / (double) 100)
				* oscillator.getSample();
		return amplitude * attentuation;
	}

	@Override
	public void sync() {
		oscillator.setSampleRate((int) PocketThereminActivity.clock
				.getFrequency());
	}
}
