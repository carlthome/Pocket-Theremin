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
	public float modify(float frequency) {
		return (vibrate(frequency));
	}

	private float vibrate(float frequency) {
		float pitch = 1 + (depth / (float) 100) * oscillator.getWaveValue(speed);
		return frequency * pitch;
	}
}
