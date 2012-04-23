package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.Oscillator;
import kth.csc.inda.pockettheremin.Oscillator.Waveform;

public class Vibrato implements SoundEffect {
	int sampleRate, sampleSize;
	int speed, depth;
	Oscillator oscillator;

	public Vibrato(int speed, int depth, Waveform waveform, int sampleRate, int sampleSize) {
		this.speed = speed;
		this.depth = depth;
		oscillator = new Oscillator(waveform, sampleSize, sampleSize);
	}

	@Override
	public double modify(double frequency) {
		return (vibrate(frequency));
	}

	private double vibrate(double frequency) {
		oscillator.setFrequency(speed);
		double pitch = 1 + (depth / (double) 100) * oscillator.getSample();
		return frequency * pitch;
	}
}
