package kth.csc.inda.pockettheremin.soundeffects;


/**
 * Let A in the fourth octave be 440hz in accordance with modern convention.
 * 
 * TODO Document this class properly.
 */
public class Autotune implements SoundEffect {
	int tonic = 57; //A4 is index 57 of the available notes.
	final float[] notes = {
			16.35f, // C0
			17.32f, 18.35f,
			19.45f,
			20.60f,
			21.83f,
			23.12f,
			24.50f,
			25.96f,
			27.50f, // A0
			29.14f, 30.87f, 32.70f, 34.65f, 36.71f,
			38.89f,
			41.20f,
			43.65f,
			46.25f,
			49.00f,
			51.91f,
			55.00f, // A1
			58.27f, 61.74f, 65.41f, 69.30f, 73.42f, 77.78f,
			82.41f,
			87.31f,
			92.50f,
			98.00f,
			103.83f,
			110.00f, // A2
			116.54f, 123.47f, 130.81f, 138.59f, 146.83f, 155.56f, 164.81f,
			174.61f,
			185.00f,
			196.00f,
			207.65f,
			220.00f, // A3
			233.08f, 246.94f, 261.63f, 277.18f, 293.66f, 311.13f, 329.63f, 349.23f,
			369.99f,
			392.00f,
			415.30f,
			440.00f, // A4
			466.16f, 493.88f, 523.25f, 554.37f, 587.33f, 622.25f, 659.26f, 698.46f,
			739.99f, 783.99f,
			830.61f,
			880.00f, // A5
			932.33f, 987.77f, 1046.50f, 1108.73f, 1174.66f, 1244.51f, 1318.51f,
			1396.91f, 1479.98f, 1567.98f, 1661.22f,
			1760.00f, // A6
			1864.66f, 1975.53f, 2093.00f, 2217.46f, 2349.32f, 2489.02f, 2637.02f,
			2793.83f, 2959.96f, 3135.96f, 3322.44f, 3520.00f, // A7
			3729.31f, 3951.07f, 4186.01f, 4434.92f, 4698.64f, 4978.03f };
	
	public float getFrequency(float frequency) {
		return snap(frequency, getMajorScale());
	}

	private float snap(float frequency, float[] scale) {
		float min = Float.MAX_VALUE;
		float closestNote = frequency;

		for (float note : scale) {
			final float  diff = Math.abs(note - frequency);

			if (diff < min) {
				min = diff;
				closestNote = note;
			}
		}

		return closestNote;
	}

	public float[] getScale(int[] steps, int octaves) throws IllegalArgumentException {
		float[] scale = new float[(octaves * steps.length) + 1]; // We always want the final tonic of one more octave so add one.

		if (octaves > 6 || octaves < 0) 
			throw new IllegalArgumentException();

		//TODO Fix odd octave ranges.
		int octavesUp = (octaves / 2);
		int octavesDown = (octaves / 2);
		
		int note = tonic - (12*octavesDown);
		for (int octave = 0; octave < octaves; octave++) {
			for (int step = 0; step < steps.length; step++) {
				scale[step + (steps.length*octave)] = notes[note];
				note += steps[step];
			} 
		}
		scale[scale.length - 1] = notes[tonic+(12*octavesUp)]; // Final tonic in the scale.

		return scale;
	}
	
	public void setKey(String key) {
		//TODO Parse string and interpret tonic.
		
		// if (KEY OF C) 
		// tonic = 57 - 4;
	}

	public float[] getMajorScale() {
		return getScale(new int[] { 2, 2, 1, 2, 2, 2, 1 }, 4);
	}

	public float[] getMinorScale() { //TODO Fix erroneous sequence.
		return getScale(new int[] { 2, 1, 2, 2, 1, 2 }, 4); 
	}

	@Override
	public float getAmplitude(float amplitude) {
		// TODO Auto-generated method stub
		return 0;
	}
}