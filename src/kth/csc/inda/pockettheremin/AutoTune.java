package kth.csc.inda.pockettheremin;

/**
 * Let A in the fourth octave be 440hz in accordance with modern convention.
 * 
 * TODO Document this class properly.
 */
public class AutoTune {
	int tonic = 57; //A4 is index 57 of the available notes.
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

	public double snap(double frequency, double[] scale) {
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

	public double[] getScale(int[] steps) {
		double[] scale = new double[steps.length + 1];

		int index = tonic;
		for (int counter = 0; counter < steps.length; counter++) {
			scale[counter] = notes[index];
			index += steps[counter];
		}
		//TODO Improve loop invariant so that the octave is set before leaving the loop.
		scale[scale.length - 1] = notes[index]; // Octave

		return scale;
	}
	
	public void setKey(String key) {
		//TODO Parse string and interpret tonic.
		
		// if (KEY OF C) 
		// tonic = 57 - 4;
	}

	public double[] getMajorScale() {
		return getScale(new int[] { 2, 2, 1, 2, 2, 2, 1 });
	}

	public double[] getMinorScale() {
		return getScale(new int[] { 2, 1, 2, 2, 1, 2 }); //TODO Verify sequence.
	}
}