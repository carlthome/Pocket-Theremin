package kth.csc.inda.pockettheremin.soundeffects;

final public class Autotune implements SoundEffect {
	int prime = 57; // A4 is index 57 of the available notes.
	int[] scaleSteps;
	Note key;
	int octaveRange;
	double[] scale;

	public enum Scale {
		MAJOR, MINOR, CHROMATIC;
	};

	/**
	 * Fourth octave notes with frequencies and index according to key position
	 * in a piano.
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
			octave -= 4;
			return frequency * Math.pow(2, octave);
		}

		public int index() {
			return this.index;
		}
	};

	public Autotune(Note key, Scale scale, int octaveRange) {
		this.key = key;
		this.octaveRange = octaveRange;

		switch (scale) {
		case MAJOR: {
			int[] major = { 2, 2, 1, 2, 2, 2, 1 };
			scaleSteps = major;
			break;
		}
		case MINOR: {
			int[] minor = { 2, 1, 2, 2, 1, 2, 2 };
			scaleSteps = minor;
			break;
		}
		case CHROMATIC: {
			int[] chromatic = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			scaleSteps = chromatic;
			break;
		}
		}

		setupScale();
	}

	@Override
	public double modify(double frequency) {
		return snap(frequency);
	}

	private double snap(double frequency) {
		double min = Double.MAX_VALUE;
		double closestNote = frequency;

		for (double note : scale) {
			final double diff = Math.abs(note - frequency);

			if (diff < min) {
				min = diff;
				closestNote = note;
			}
		}

		return closestNote;
	}

	private void setupScale() {
		scale = new double[(octaveRange * scaleSteps.length) + 1];

		if (octaveRange > 6 || octaveRange < 0)
			throw new IllegalArgumentException();

		// TODO Allow odd octave ranges.
		int octavesUp = 1;
		int octavesDown = 0;
		if (octaveRange > 1) {
			octavesUp = (octaveRange + 1) / 2;
			octavesDown = octaveRange / 2;
		}

		int note = key.index() - (12 * octavesDown);
		for (int octave = 0; octave < octaveRange; octave++) {
			for (int step = 0; step < scaleSteps.length; step++) {
				scale[step + (scaleSteps.length * octave)] = absoluteFrequency(note);
				note += scaleSteps[step];
			}
		}
		scale[scale.length - 1] = absoluteFrequency(note);
	}

	private double absoluteFrequency(int n) {
		double frequency;

		int a = key.index(); // A4 is the 49th piano key.
		double A = key.frequency(); // A is 440Hz.

		double temp = Math.pow((double) 2, (double) 1 / 12);
		double temp2 = Math.pow(temp, n - a);
		double temp3 = A * temp2;
		frequency = temp3;
		return frequency;
	}
}