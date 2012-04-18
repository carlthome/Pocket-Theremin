package kth.csc.inda.pockettheremin.soundeffects;

public class Portamento implements SoundEffect {
	float startFrequency, destinationFrequency;
	int delay;
	boolean gliding;

	public Portamento() {
		delay = 10;
	}

	public float getFrequency(float frequency) {
		return glide(frequency);
	}

	private float glide(float frequency) {

		// TODO Think this through...

		if (!gliding)
			destinationFrequency = frequency;

		startFrequency = frequency;

		if (startFrequency != destinationFrequency)
			gliding = true;
		else
			gliding = false;

		while (gliding) {
			float difference = destinationFrequency - startFrequency;

			frequency = (difference / delay) + startFrequency;
		}

		return frequency;
	}

	@Override
	public float getAmplitude(float amplitude) {
		// TODO Auto-generated method stub
		return 0;
	}
}
