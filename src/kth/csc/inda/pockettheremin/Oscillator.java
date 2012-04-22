package kth.csc.inda.pockettheremin;

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

		float circle = (float) (2 * Math.PI);
		float t = (frequency / sampleRate);
		float increment = circle * t;
		for (int i = 0; i < samples.length; i++) {
			
			//samples[i] = (short) (getWaveValue(1) * Short.MAX_VALUE);
			
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
			case TRIANGLE:
				samples[i] = (short) ((float) Math.asin(Math.sin(angle)) * Short.MAX_VALUE);
				
				//samples[i] = (short) (((((8 / Math.pow(Math.PI, 2)) * (Math.sin(angle) - Math.sin(3*angle)/9 + Math.sin(5*angle)/25)))) * Short.MAX_VALUE);
				
				break;
			case SAWTOOTH: // TODO This is probably wrong.
				
				//((8 / pow(M_PI,2)) * (sin(theta) - sin(3*theta)/9 + sin(5*theta)/25)) * amplitude
				
				samples[i] = (short) (((2/Math.PI) * (Math.pow(-1, i+1) * (Math.sin(circle * i * frequency * t)) / (i))) * Short.MAX_VALUE);
				//samples[i] = (short) ((t - Math.floor(t + 1/2)) * Short.MAX_VALUE);
				//samples[i] = (short) ((t - Math.floor(t + 1/2)) * Short.MAX_VALUE);
				//samples[i] = (short) ((float) Math.abs(Math.asin(Math.sin(angle))) * Short.MAX_VALUE);
				break;
			}

			angle += increment % circle;

			if (angle > circle)
				angle = 0;
		}

		return samples;
	}

	public float getWaveValue(int speed) {
		float y;

		float circle = (float) (2 * Math.PI);
		float increment = circle / sampleSize;
		
		// float increment = circle * (frequency / samplerate);
		
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
		case TRIANGLE: // TODO This is probably wrong.
			//y = kx + m;
			y = (float) Math.asin(Math.sin(angle));
			
			//y = (float) (0.5 * Math.signum(  Math.sin(550*2*Math.PI*i/rate)   )   +  0.5  *   Math.asin(Math.sin(450*2*Math.PI*i/rate) )  );
			  
			break;
		case SAWTOOTH: // TODO This is probably wrong.
			y = (FloatMath.sin(angle) % 2 < 0 ? -1 : angle);
			break;
		default:
			y = 0;
		}

		angle += (speed * increment) % circle; //TODO Modulo redundant?
		if (angle > circle) angle = 0;

		return y;
	}
}
