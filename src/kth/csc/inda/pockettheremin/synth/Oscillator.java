package kth.csc.inda.pockettheremin.synth;

public class Oscillator {
	public static final double MAX_FREQUENCY = 20000;
	public static final double MIN_FREQUENCY = 1;

	private long period, x;
	int bufferSize, sampleRate;
	Waveform waveform;
	boolean imd;

	public Oscillator(Waveform waveform) {
		this.waveform = waveform;
	}

	public void setIMD(boolean b) {
		imd = b;
	}

	public void setFrequency(double frequency) {
		if (frequency < MIN_FREQUENCY)
			throw new IllegalArgumentException("Frequency"
					+ (frequency - MIN_FREQUENCY) + "Hz out of bounds.");

		if (frequency > MAX_FREQUENCY)
			throw new IllegalArgumentException("Frequency"
					+ (MAX_FREQUENCY - frequency) + "Hz out of bounds.");

		/*
		 * The sample rate could theoretically become very low occasionally if
		 * the running device becomes busy. Accept the harsh reality of this
		 * fact and just set the frequency to the highest possible value (i.e.
		 * the sample rate itself).
		 */
		if (frequency > sampleRate)
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
		default:
		case SINE:
			y = Math.sin(2 * Math.PI * angle);
			break;
		case SQUARE:
			y = Math.sin(2 * Math.PI * angle) % 2 < 0 ? -1 : 1;
			// Alternative: if (sample < (period / 2)) y = 1.0; else y = -1.0;
			break;
		case TRIANGLE:
			y = Math.abs(2.0 * (angle - Math.floor(angle + 0.5)));
			// Alternative: y = Math.asin(Math.sin(2 * Math.PI * x));
			break;
		case SAWTOOTH:
			y = (2.0 * (angle - Math.floor(angle + 0.5)));
			break;
		case NONE:
			y = 0;
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
