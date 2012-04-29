package kth.csc.inda.pockettheremin.synth;

public interface SoundGenerator {
	public static double FREQUENCY_MAX = 20000.00; // Hz
	public static double FREQUENCY_MIN = 1.00; // Hz

	public short[] getSamples(int amount);

	public void setSampleRate(double sampleRate);

	public void setFrequency(double frequency);
}
