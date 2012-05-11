package kth.csc.inda.pockettheremin.synth;

public class Send extends Sampler {
	Send(Sampler input) {
		super(input);
	}

	private static short[] SAMPLES = new short[AudioThread.SAMPLES_PER_BUFFER];
	private final short[] samples = new short[AudioThread.SAMPLES_PER_BUFFER];
	int i;

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
