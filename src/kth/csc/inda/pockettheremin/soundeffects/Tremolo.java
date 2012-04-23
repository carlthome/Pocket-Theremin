package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.Oscillator;
import kth.csc.inda.pockettheremin.Oscillator.Waveform;

public class Tremolo implements SoundEffect {
	int sampleRate, sampleSize;
	int speed, depth;
	Oscillator oscillator;
	
	public Tremolo(int speed, int depth, Waveform waveform, int sampleRate, int sampleSize) {
		this.speed = speed;
		this.depth = depth;
		oscillator = new Oscillator(waveform, sampleSize, sampleSize);
	}

	@Override
	public double modify(double amplitude) {
		return (tremble(amplitude));
	}
	
	private double tremble(double amplitude) {
		oscillator.setFrequency(speed);
		double attentuation = 1 + (depth / (double) 100) * oscillator.getSample();
		return amplitude * attentuation;
	}
}
