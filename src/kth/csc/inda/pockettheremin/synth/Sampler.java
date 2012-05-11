package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Global;

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
public abstract class Sampler implements Global {
	private Sampler input;

	Sampler(Sampler input) {
		this.input = input;
	}

	protected abstract short processSample(short sample);

	final public void chain(Sampler input) {
		this.input = input;
	}

	final public void fillBuffer(byte[] buffer) {

		/*
		 * Get parent buffer.
		 */
		if (input != null)
			input.fillBuffer(buffer);

		for (int i = 0, j = 0; i < AudioThread.SAMPLES_PER_BUFFER; i++) {

			/*
			 * Convert bytes into short sample.
			 */
			short sample;
			if (G.chiptune)
				sample = (short) (buffer[j + 1] << 8);
			else
				sample = (short) ((buffer[j + 1] << 8) | (buffer[j] & 0xFF));

			/*
			 * Process the short sample.
			 */
			sample = processSample(sample);

			/*
			 * Store the processed sample as bytes.
			 */
			byte high = (byte) ((sample >> 8) & 0xFF);
			byte low = (byte) (sample & 0xFF);
			if (G.chiptune) {
				// Simulate 8-bit PCM (unsigned, little endian) by downsampling.
				buffer[j + 1] = (byte) (high ^ 0x00000080);
			} else {
				// 16-bit PCM (signed, little endian)
				buffer[j] = low;
				buffer[j + 1] = high;
			}

			j += 2;
		}
	}
}