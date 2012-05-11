package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Range;

public class Delay extends Sampler {

	private final Range mix;
	private final Range time;
	private final Range feedback;

	private final short[] memory = new short[AudioThread.SAMPLE_RATE];
	private int readIndex;
	private int writeIndex;

	public Delay(Sampler input) {
		super(input);
		mix = new Range(100.0, 0.0);
		time = new Range(2000, 1);
		feedback = new Range(100.0, 0.0);
	}

	@Override
	protected short processSample(short sample) {

		/*
		 * Calculate output sample.
		 */
		short outputSample;

		double dry = sample * ((mix.max - mix.get()) / mix.range);
		double wet = memory[readIndex] * mix.getPercent();

		if (dry + wet > Short.MAX_VALUE)
			outputSample = Short.MAX_VALUE;
		else if (dry + wet < Short.MIN_VALUE)
			outputSample = Short.MIN_VALUE;
		else
			outputSample = (short) (dry + wet);

		/*
		 * Calculate and store sample in memory.
		 */
		double newSample = sample + memory[readIndex] * feedback.getPercent();
		if (newSample > Short.MAX_VALUE)
			memory[writeIndex] = Short.MAX_VALUE;
		else if (newSample < Short.MIN_VALUE)
			memory[writeIndex] = Short.MIN_VALUE;
		else
			memory[writeIndex] = (short) newSample;

		/*
		 * Update counters.
		 */
		readIndex = (readIndex + 1) % memory.length;
		writeIndex = (writeIndex + 1) % memory.length;

		/*
		 * Return resulting sample.
		 */
		return outputSample;
	}

	public void setMix(double mixPercent) {
		mix.set(mixPercent);
	}

	public void setFeedback(double feedbackPercent) {
		feedback.set(feedbackPercent);
	}

	public void setTimeInBPM(int BPM) { // TODO Not really BPM.
		int delayInMs = (int) ((BPM / (double) 60) * 1000);
		time.set(delayInMs);
		int delayInSamples = (int) (time.get() / 1000 * AudioThread.SAMPLE_RATE);

		readIndex = writeIndex - delayInSamples;
		if (readIndex < 0) {
			readIndex += memory.length;
		}
	}
}