package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Range;

public class Delay extends Sampler {

	private final Range mix = new Range(100.0, 0.0);
	private final Range time = new Range(2000, 1);
	private final Range feedback = new Range(100.0, 0.0);

	private final short[] memory = new short[AudioThread.SAMPLE_RATE * 3];
	private int readIndex;
	private int writeIndex;

	public Delay(Sampler input) {
		super(input);
	}

	@Override
	protected short processSample(short inputSample) {
		short delayedSample = memory[readIndex++];

		double dry = ((100.0 - mix.get()) * inputSample) / 100.0;
		double wet = (mix.get() * delayedSample) / 100.0;

		short outputSample = (short) (dry + wet);

		inputSample += (delayedSample * feedback.get()) / 100.0;

		memory[writeIndex++] = inputSample;
		readIndex %= memory.length;
		writeIndex %= memory.length;

		return outputSample;
	}

	public void setMix(double mixPercent) {
		mix.set(mixPercent);
	}

	public void setFeedback(double feedbackPercent) {
		feedback.set(feedbackPercent);
	}

	public void setTimeInBPM(int BPM) {
		int delayInMs = (int) ((BPM / (double) 60) * 1000);

		time.set(delayInMs);

		int delayInSamples = (int) (0.001 * time.get() * AudioThread.SAMPLE_RATE);

		readIndex = writeIndex - delayInSamples;
		if (readIndex < 0) {
			readIndex += memory.length;
		}
	}
}