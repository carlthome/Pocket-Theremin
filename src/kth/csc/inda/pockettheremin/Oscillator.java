package kth.csc.inda.pockettheremin;

import android.util.FloatMath;

public class Oscillator {
	int samplesize, samplerate;
	float angle;
	Waveform waveform;
	
	public enum Waveform {
		SINE, TAN, SQUARE, TRIANGLE, SAWTOOTH;
	};
	
	public Oscillator (Waveform waveform, int samplesize, int samplerate) {
		this.waveform = waveform;
		this.samplerate = samplerate;
		this.samplesize = samplesize;				
	}
	
	public short[] getSamples(float frequency) {
		short[] samples = new short[samplesize];
		
		float circle = (float) (2 * Math.PI);
		float increment = circle * (frequency / samplerate);
		for (int i = 0; i < samples.length; i++) {

			switch (waveform) {
			case SINE:
				samples[i] = (short) (FloatMath.sin(angle) * Short.MAX_VALUE);
				break;
			case TAN:
				samples[i] = (short) (FloatMath.sin(angle) / FloatMath.cos(angle) * Short.MAX_VALUE);
				break;
			case SQUARE:
				samples[i] = (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : 1) * Short.MAX_VALUE);
				break;
			case TRIANGLE: // TODO This is probably wrong.
				samples[i] = (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : 1) * angle * Short.MAX_VALUE); 
				break;
			case SAWTOOTH: // TODO This is probably wrong.
				samples[i] = (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : angle) * Short.MAX_VALUE); 
				break;
			}

			angle += increment % circle;

			if (angle > circle)
				angle = 0;
		}
		
		return samples;
	}
	
	public short getSample(float frequency) {
		float circle = (float) (2 * Math.PI);
		float increment = circle * (frequency / samplerate);
		
		switch (waveform) {
		case SINE:
			return (short) (FloatMath.sin(angle) * Short.MAX_VALUE);
		case TAN:
			return (short) (FloatMath.sin(angle) / FloatMath.cos(angle) * Short.MAX_VALUE);
		case SQUARE:
			return (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : 1) * Short.MAX_VALUE);
		case TRIANGLE: // TODO This is probably wrong.
			return (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : 1) * angle * Short.MAX_VALUE); 
		case SAWTOOTH: // TODO This is probably wrong.
			return (short) ((FloatMath.sin(angle) % 2 < 0 ? -1 : angle) * Short.MAX_VALUE); 
		}

		angle += increment % circle;

		if (angle > circle)
			angle = 0;
		
		return 0;
	}
}
