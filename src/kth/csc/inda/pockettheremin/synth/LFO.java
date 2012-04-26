package kth.csc.inda.pockettheremin.synth;

public class LFO implements SoundEffect {
	int speed, depth;
	Oscillator oscillator;

	public LFO(int speed, int depth, Waveform waveform) {
		this.speed = speed;
		this.depth = depth;
		oscillator = new Oscillator(waveform);
	}

	@Override
	public double modify(double frequency) {
		return (oscillate(frequency));
	}

	private double oscillate(double frequency) {
		if (speed == 0 || depth == 0)
			return frequency;

		oscillator.setFrequency(speed);

		double pitch = 1 + (depth / (double) 100) * oscillator.getSample();
		return frequency * pitch;
	}

	@Override
	public void sync() {
		oscillator.setSampleRate((int) clock.getFrequency());
	}
}