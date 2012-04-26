package kth.csc.inda.pockettheremin.synth;

final public class Autotune implements SoundEffect {
	Note key;
	int octaves;
	int[] scaleSteps;
	double[] scaleFrequencies;

	/**
	 * Fourth octave notes with their base frequencies and index according to
	 * key position in a piano.
	 */
	public enum Note {
		C(40, 261.63), Csharp(41, 277.18), D(42, 293.66), Dsharp(43, 311.13), E(
				44, 329.63), F(45, 349.23), Fsharp(46, 369.99), G(47, 392.00), Gsharp(
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

	public enum Scale {
		MAJOR, MINOR, CHROMATIC;

		public int[] steps() {
			switch (this) {
			case MAJOR: {
				int[] steps = { 2, 2, 1, 2, 2, 2, 1 };
				return steps;
			}
			case MINOR: {
				int[] steps = { 2, 1, 2, 2, 1, 2, 2 };
				return steps;
			}
			case CHROMATIC: {
				int[] steps = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
				return steps;
			}
			}
			throw new AssertionError(this);
		}
	}

	public Autotune(Note key, Scale scale, int octaves) {
		this.key = key;
		this.octaves = octaves;
		scaleSteps = scale.steps();
		setupScale();
	}

	@Override
	public double modify(double frequency) {
		return snap(frequency);
	}

	private double snap(double frequency) {
		double min = Double.MAX_VALUE;
		double closestNote = frequency;

		for (double note : scaleFrequencies) {
			final double diff = Math.abs(note - frequency);

			if (diff < min) {
				min = diff;
				closestNote = note;
			}
		}

		return closestNote;
	}

	private void setupScale() {
		scaleFrequencies = new double[(octaves * scaleSteps.length) + 1];

		if (!(0 < octaves || octaves < 6))
			throw new IllegalArgumentException();

		int note = key.index() - (12 * (octaves / 2));
		for (int octave = 0; octave < octaves; octave++) {
			for (int step = 0; step < scaleSteps.length; step++) {
				scaleFrequencies[step + (scaleSteps.length * octave)] = getFrequency(note);
				note += scaleSteps[step];
			}
		}
		scaleFrequencies[scaleFrequencies.length - 1] = getFrequency(note);
	}

	private double getFrequency(int note) {
		double temperament = Math.pow((double) 2, (double) 1 / 12);
		return (key.frequency() * Math.pow(temperament, note - key.index()));
	}

	/**
	 * Autotune isn't time dependent so this method is probably redundant.
	 */
	@Override
	public void sync() {
	}
}