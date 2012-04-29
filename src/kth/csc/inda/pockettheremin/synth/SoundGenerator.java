package kth.csc.inda.pockettheremin.synth;

public interface SoundGenerator {
	public static double FREQUENCY_MAX = 20000; // Hz
	public static double FREQUENCY_MIN = 1; // Hz

	public short[] getSamples(int amount);

	public void setSampleRate(int sampleRate);

	public void setFrequency(double frequency);
}
