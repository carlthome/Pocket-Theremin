package kth.csc.inda.pockettheremin;

import java.util.MissingFormatArgumentException;

public class Oscillator {
	static final double CIRCLE = 2 * Math.PI;
	private long period, sample;
	int bufferSize, sampleRate;
	short[] samples;
	double frequency;
	boolean frequencyChanged;
	Waveform waveform;

	public enum Waveform {
		SINE, SQUARE1, SQUARE2, SQUARE3, TRIANGLE, SAWTOOTH;
	};

	public Oscillator(Waveform waveform, int bufferSize, int sampleRate) {
		this.waveform = waveform;
		this.sampleRate = sampleRate;
		this.bufferSize = bufferSize;
		this.samples = new short[bufferSize];
	}

	public void setFrequency(double frequency) {
		if (this.frequency != frequency) {
			this.frequency = frequency;
			period = (long) (sampleRate / frequency);
			frequencyChanged = true;
		}
		else
			frequencyChanged = false;
	}

	public short[] getSamples() {

		if (frequencyChanged) {
			for (int i = 0; i < samples.length; i++)
				samples[i] = (short) (getSample() * Short.MAX_VALUE);
			frequencyChanged = false;
		}

		return samples;
	}

	public double getSample() {
		double y = 0;
		double x = sample / (double) period;

		switch (waveform) {
		case SINE:
			y = Math.sin(CIRCLE * x);
			break;
		case TRIANGLE:
			y = Math.abs(2.0 * (x - Math.floor(x + 0.5)));
			// y = Math.asin(Math.sin(circle * x));
			break;
		case SAWTOOTH:
			y = (2.0 * (x - Math.floor(x + 0.5)));
			break;
		case SQUARE1:
			y = Math.sin(CIRCLE * x) % 2 < 0 ? -1 : 1;
			break;
		case SQUARE2:
			if (sample < (period / 1.1))
				y = 1.0;
			else
				y = -1.0;
			break;
		case SQUARE3:
			if (sample < (period / 2.5))
				y = 1.0;
			else
				y = -1.0;
			break;
		default:
			throw new MissingFormatArgumentException("No waveform was set.");
		}

		// TODO Avoid garbage collection.
		// TODO Avoid pops.
		sample = (sample + 1) % period;
		
		/*
		if (sample > period)
			sample = 0;
		*/

		return y;
	}
}
