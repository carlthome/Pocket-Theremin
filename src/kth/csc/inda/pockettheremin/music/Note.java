package kth.csc.inda.pockettheremin.music;

/**
 * Fourth octave notes with frequencies and index according to key position in a
 * piano.
 */
public enum Note {
	C(40, 261.63), Csharp(41, 277.18), D(42, 293.66), Dsharp(43, 311.13), E(44,
			329.63), F(45, 349.23), Fsharp(46, 369.99), G(47, 392.00), Gsharp(
			48, 415.30), A(49, 440.00), Asharp(50, 466.16), B(51, 493.88);

	private final int index;
	private final double frequency;

	Note(int index, double frequency) {
		this.index = index;
		this.frequency = frequency;
	}

	public double frequency() {
		return this.frequency;
	}

	public double frequency(int octave) {
		octave -= 4; // Method assumes 0 equals octave 0
		return frequency * Math.pow(2, octave);
	}

	public int index() {
		return this.index;
	}
};
