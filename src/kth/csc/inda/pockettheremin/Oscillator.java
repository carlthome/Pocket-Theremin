package kth.csc.inda.pockettheremin;

import java.util.MissingFormatArgumentException;

public class Oscillator {
	int sampleSize, sampleRate;
	static final double CIRCLE = 2 * Math.PI;
	
	Waveform waveform;
	private long period;
	private long sample;

	public enum Waveform {
		SINE, COSINE, TAN, SQUARE, TRIANGLE, SAWTOOTH, PHASE;
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
		case COSINE:
			y = Math.cos(CIRCLE * x);
			break;
		case TAN:
			y = Math.tan(CIRCLE * x);
			break;
		case SQUARE:
			y = Math.sin(CIRCLE * x) % 2 < 0 ? -1 : 1;
			break;
		case TRIANGLE:
			//y = Math.asin(Math.sin(circle * x));
			y = Math.abs(2.0 * (x - Math.floor(x + 0.5)));
			break;
		case SAWTOOTH: 
			y = (2.0 * (x - Math.floor(x + 0.5)));
			break;
		case PHASE:
			/*
			 * if (x > 0.5*circle) y = sin(increment * 0.5*circle / pi); else y
			 * = sin((increment - 0.5*circle) * pi / (1 - 0.5*circle) + pi);
			 */
			break;
		default:
			throw new MissingFormatArgumentException("No waveform was set.");
		}

		sample = (sample + 1) % period;
		return y;
	}
}
