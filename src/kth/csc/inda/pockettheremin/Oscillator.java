package kth.csc.inda.pockettheremin;

import java.util.MissingFormatArgumentException;

public class Oscillator {
	static final double CIRCLE = 2 * Math.PI;
	private long period, sample;
	int sampleSize, sampleRate;
	Waveform waveform;

	public enum Waveform {
		SINE, SQUARE1, SQUARE2, SQUARE3, TRIANGLE, SAWTOOTH;
	};

	public Oscillator(Waveform waveform, int samplesize, int samplerate) {
		this.waveform = waveform;
		this.sampleRate = samplerate;
		this.sampleSize = samplesize;
	}

	public void setFrequency(double frequency) {
		period = (long) (sampleRate / frequency);
	}

	public short[] getSamples() {
		short[] samples = new short[sampleSize];

		for (int i = 0; i < samples.length; i++)
			samples[i] = (short) (getSample() * Short.MAX_VALUE);

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

		sample = (sample + 1) % period;
		return y;
	}
}
