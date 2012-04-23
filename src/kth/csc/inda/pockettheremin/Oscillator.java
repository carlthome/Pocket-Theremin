package kth.csc.inda.pockettheremin;

import java.util.MissingFormatArgumentException;

import android.util.FloatMath;

public class Oscillator {
	int sampleSize, sampleRate;
	float angle;
	Waveform waveform;

	public enum Waveform {
		SINE, TAN, SQUARE, TRIANGLE, SAWTOOTH;
	};

	public Oscillator(Waveform waveform, int samplesize, int samplerate) {
		this.waveform = waveform;
		this.sampleRate = samplerate;
		this.sampleSize = samplesize;
	}

	public short[] getSamples(float frequency) {
		short[] samples = new short[sampleSize];

		for (int i = 0; i < samples.length; i++) 
			samples[i] = (short) (getNext(frequency, false) * Short.MAX_VALUE);

		return samples;
	}

	public float getNext(float increment, boolean fixedSampleSize) {
		float y;
	
		switch (waveform) {
		case SINE:
			y = FloatMath.sin(angle);
			break;
		case TAN:
			y = FloatMath.sin(angle) / FloatMath.cos(angle);
			break;
		case SQUARE:
			y = (FloatMath.sin(angle) % 2 < 0 ? -1 : 1);
			break;
		case TRIANGLE: 
			y = (float) Math.asin(Math.sin(angle));
			break;
		case SAWTOOTH: // TODO This is probably wrong.
			y = (float) Math.abs(Math.asin(Math.sin(angle)));
			break;
		default:
			throw new MissingFormatArgumentException("No waveform was set.");
		}

		float circle = (float) (2 * Math.PI);
		
		if (fixedSampleSize)
			increment /= sampleSize;
		else
			increment /= sampleRate;
		
		angle += (increment * circle) % circle;
		if (angle > circle) angle = 0;

		return y;
	}
}
