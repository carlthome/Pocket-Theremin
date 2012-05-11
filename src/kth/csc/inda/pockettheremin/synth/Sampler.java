package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Global.G;

/**
 * This is an element in a linked list. Samplers generate audio samples in
 * buffers that can be written to the audio hardware.
 * 
 * By chaining samplers together the audio thread only has to ask the last
 * element in the linked list to generate the buffer. That sampler will itself
 * ask its preceding sampler in the chain to generate the buffers, which then
 * will ask its own preceding sampler and so on, effectively generating buffers
 * through a sound effect chain. Pretty sweet!
 */
public abstract class Sampler {
	private Sampler input;

	protected abstract short processSample(short sample);

	Sampler(Sampler input) {
		this.input = input;
	}

	public void chain(Sampler input) {
		this.input = input;
	}

	public void fillBuffer(byte[] samples) {

		/*
		 * Get parent buffer.
		 */
		if (input != null)
			input.fillBuffer(samples);

		for (int i = 0, j = 0; i < AudioThread.SAMPLES_PER_BUFFER; i++) {

			/*
			 * Convert bytes into short sample.
			 */
			short sample;
			if (G.chiptune)
				// 8-bit PCM
				sample = (short) samples[i]; //TODO
			else
				// 16-bit PCM
				sample = (short) ((samples[j + 1] << 8) + samples[j]);

			/*
			 * Process the short sample.
			 */
			sample = processSample(sample);

			/*
			 * Store the processed sample as bytes.
			 */
			if (G.chiptune) { // 8-bit PCM
				samples[i] = (byte) (sample >> 8);
			} else { // 16-bit PCM
				samples[j] = (byte) (sample & 0xff);
				samples[j + 1] = (byte) ((sample >>> 8) & 0xff);
				j += 2;
			}
		}
	}
}