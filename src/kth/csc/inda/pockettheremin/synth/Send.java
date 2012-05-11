package kth.csc.inda.pockettheremin.synth;

public class Send extends Sampler {
	private static short[] SAMPLES = new short[AudioThread.SAMPLES_PER_BUFFER];
	private final short[] samples = new short[AudioThread.SAMPLES_PER_BUFFER];
	int i;

	Send(Sampler input) {
		super(input);
	}

	@Override
	protected short processSample(short sample) {
		samples[i] = sample;

		i++;
		if (i == samples.length) {
			SAMPLES = samples;
			i = 0;
		}
		return sample;
	}

	public static short[] getSamples() {
		return SAMPLES;
	}
}
