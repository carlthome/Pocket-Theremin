package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.music.Note;
import kth.csc.inda.pockettheremin.music.Scale;

final public class Autotune implements SoundEffect {
	int prime = 57; // A4 is index 57 of the available notes.
	Note key;
	int octaveRange;
	int[] scaleSteps;
	double[] scaleFrequencies;

	public Autotune(Note key, Scale scale, int octaveRange) {
		this.key = key;
		this.octaveRange = octaveRange;
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
		scaleFrequencies = new double[(octaveRange * scaleSteps.length) + 1];

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
				scaleFrequencies[step + (scaleSteps.length * octave)] = absoluteFrequency(note);
				note += scaleSteps[step];
			}
		}
		scaleFrequencies[scaleFrequencies.length - 1] = absoluteFrequency(note);
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