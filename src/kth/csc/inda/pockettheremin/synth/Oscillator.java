package kth.csc.inda.pockettheremin.synth;

public class Oscillator implements SoundGenerator {
	private Waveform waveform;
	private boolean imd;

	//private double period, sample;
	private long period, sample; //TODO
	private double sampleRate, y, x;

	public Oscillator(Waveform waveform) {
		this.waveform = waveform;
		imd = false; // Default is false.
	}

	public void setFrequency(double frequency) {

		/*
		 * As a precaution clamp the input value to the maximum or minimum in
		 * case it goes out of the allowed range set by the SoundGenerator
		 * interface.
		 */
		if (frequency < FREQUENCY_MIN)
			frequency = FREQUENCY_MIN;

		if (frequency > FREQUENCY_MAX)
			frequency = FREQUENCY_MAX;

		/*
		 * The sample rate could theoretically become very low occasionally if
		 * the running device becomes busy. Accept the harsh reality of this
		 * fact and just set the frequency to the highest possible value (i.e.
		 * the sample rate itself).
		 */
		if (frequency > sampleRate)
			frequency = sampleRate;

		//period = sampleRate / frequency;
		period = (long) (sampleRate / frequency); //TODO
	}

	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setIMD(boolean b) {
		imd = b;
	}

	@Override
	public short[] getSamples(int amount) {
		short[] samples = new short[amount];

		for (int i = 0; i < samples.length; i++)
			samples[i] = (short) Math.round(getSample() * Short.MAX_VALUE);

		return samples;
	}

	public double getSample() {
		x = sample / (double) period;

		switch (waveform) {
		case SINE:
			y = Math.sin(2 * Math.PI * x);
			break;
		case SQUARE:
			y = Math.sin(2 * Math.PI * x) % 2 < 0 ? -1 : 1;
			// Alternative: if (sample < (period / 2)) y = 1.0; else y = -1.0;
			break;
		case TRIANGLE:
			y = Math.abs(2.0 * (x - Math.floor(x + 0.5)));
			// Alternative: y = Math.asin(Math.sin(2 * Math.PI * x));
			break;
		case SAWTOOTH:
			y = (2.0 * (x - Math.floor(x + 0.5)));
			break;
		default:
		case NONE:
			y = 0;
		}

		if (!imd) {
			sample = (sample + 1) % period;			
		} else {
			sample++;
			if (sample > period)
				sample = 0;
		}

		return y;
	}
}
