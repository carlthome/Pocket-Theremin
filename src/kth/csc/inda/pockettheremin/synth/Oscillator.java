package kth.csc.inda.pockettheremin.synth;

import java.util.MissingFormatArgumentException;

public class Oscillator {
	static final boolean useExperimentalNewPortamento = true;

	static final double CIRCLE = 2 * Math.PI;
	private long period, x;
	int bufferSize, sampleRate;
	
	Waveform waveform;
	boolean imd;

	public enum Waveform {
		SINE, SQUARE, TRIANGLE, SAWTOOTH, NONE;
	};

	public Oscillator(Waveform waveform) {
		this.waveform = waveform;
	}

	public void setIMD (boolean b) {
		imd = b;
	}

	public void setFrequency(double frequency) {
		if (frequency > sampleRate) //TODO
			frequency = sampleRate;
		
		
		period = (long) (sampleRate / frequency);
	}
	
	public void setSampleRate(int frequency) {
		sampleRate = frequency;
	}

	public short[] getSamples(int bufferSize) {
		short[] samples = new short[bufferSize];
		
		for (int i = 0; i < samples.length; i++)
			samples[i] = (short) Math.round(getSample() * Short.MAX_VALUE);

		return samples;
	}

	public double getSample() {
		double y = 0;
		double angle = x / (double) period;

		switch (waveform) {
		case SINE:
			y = Math.sin(CIRCLE * angle);
			break;
		case SQUARE:
			y = Math.sin(CIRCLE * angle) % 2 < 0 ? -1 : 1;
			// Alternative: if (sample < (period / 2)) y = 1.0; else y = -1.0;
			break;
		case TRIANGLE:
			y = Math.abs(2.0 * (angle - Math.floor(angle + 0.5)));
			// Alternative: y = Math.asin(Math.sin(circle * x));
			break;
		case SAWTOOTH:
			y = (2.0 * (angle - Math.floor(angle + 0.5)));
			break;
		case NONE:
			y = 0;
		default:
			throw new MissingFormatArgumentException("No waveform was set.");
		}

		if (!imd) {
			x = (x + 1) % period;
		} else {
			x++;
			if (x > period)
				x = 0;
		}

		return y;
	}
}
