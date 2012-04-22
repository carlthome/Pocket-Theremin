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
	public float modify(float amplitude) {
		return (tremble(amplitude));
	}
	
	private float tremble(float amplitude) {
		float attentuation = 1 + (depth / (float) 100) * oscillator.getWaveValue(speed);
		return amplitude * attentuation;
	}
}
