package kth.csc.inda.pockettheremin.soundeffects;

final public class Autotune implements SoundEffect {
	int prime = 57; // A4 is index 57 of the available notes.
	int[] scaleSteps;
	double[] scale;

	public enum AutotuneScale {
		MAJOR, MINOR, CHROMATIC;
	};

	public enum AutotuneKey {
		A, Bb, B, C, Db, D, Eb, E, F, Gb, G, Ab;
	};

	final double[] notes = {
			16.35, // C0
			17.32, 18.35,
			19.45,
			20.60,
			21.83,
			23.12,
			24.50,
			25.96,
			27.50, // A0
			29.14, 30.87, 32.70, 34.65, 36.71,
			38.89,
			41.20,
			43.65,
			46.25,
			49.00,
			51.91,
			55.00, // A1
			58.27, 61.74, 65.41, 69.30, 73.42, 77.78,
			82.41,
			87.31,
			92.50,
			98.00,
			103.83,
			110.00, // A2
			116.54, 123.47, 130.81, 138.59, 146.83, 155.56, 164.81,
			174.61,
			185.00,
			196.00,
			207.65,
			220.00, // A3
			233.08, 246.94, 261.63, 277.18, 293.66, 311.13, 329.63, 349.23,
			369.99,
			392.00,
			415.30,
			440.00, // A4
			466.16, 493.88, 523.25, 554.37, 587.33, 622.25, 659.26, 698.46,
			739.99, 783.99,
			830.61,
			880.00, // A5
			932.33, 987.77, 1046.50, 1108.73, 1174.66, 1244.51, 1318.51,
			1396.91, 1479.98, 1567.98, 1661.22,
			1760.00, // A6
			1864.66, 1975.53, 2093.00, 2217.46, 2349.32, 2489.02, 2637.02,
			2793.83, 2959.96, 3135.96, 3322.44, 3520.00, // A7
			3729.31, 3951.07, 4186.01, 4434.92, 4698.64, 4978.03 };

	public Autotune(AutotuneKey key, AutotuneScale scale, int octaveRange) {
		switch (key) {
		case A:
			prime = 57;
			break;
		case Bb:
			prime = 57 + 1;
			break;
		case B:
			prime = 57 + 2;
			break;
		case C:
			prime = 57 + 3;
			break;
		case Db:
			prime = 57 + 4;
			break;
		case D:
			prime = 57 + 5;
			break;
		case Eb:
			prime = 57 + 6;
			break;
		case E:
			prime = 57 + 7;
			break;
		case F:
			prime = 57 + 8;
			break;
		case Gb:
			prime = 57 + 9;
			break;
		case G:
			prime = 57 + 10;
			break;
		case Ab:
			prime = 57 + 11;
			break;
		}

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

		setupScale(octaveRange);
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

	private void setupScale(int octaveRange) {
		scale = new double[(octaveRange * scaleSteps.length) + 1];

		if (octaveRange > 6 || octaveRange < 0)
			throw new IllegalArgumentException();

		//TODO Allow odd octave ranges.
		int octavesUp = 1;
		int octavesDown = 0;
		if (octaveRange > 1) {
			octavesUp = (octaveRange + 1) / 2;
			octavesDown = octaveRange / 2;
		}

		int note = prime - (12 * octavesDown);
		for (int octave = 0; octave < octaveRange; octave++) {
			for (int step = 0; step < scaleSteps.length; step++) {
				scale[step + (scaleSteps.length * octave)] = notes[note];
				note += scaleSteps[step];
			}
		}
		scale[scale.length - 1] = notes[prime + (12 * octavesUp)]; // Octave
	}
}