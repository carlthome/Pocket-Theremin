package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.utils.Global;

/**
 * Sampler consisting of three oscillators: one for sound generation, one for AM
 * LFO and one for FM LFO. There's also support for portamento.
 * 
 * This sampler should be placed first in the audio chain.
 */
public class Synth extends Sampler implements Global {

	private double frequency, volume;
	private Oscillator synth, tremolo, vibrato;
	private double tremoloDepth, vibratoDepth;
	private double portamentoSpeed, portamentoStep, newFrequency;

	public Synth(Sampler input) {
		super(input);
		synth = new Oscillator();
		tremolo = new Oscillator();
		vibrato = new Oscillator();
	}

	@Override
	protected short processSample(short s) {

		// Set initial frequency and volume.
		double frequency = this.frequency;
		double volume = this.volume;

		// Portamento
		portamento();

		// Vibrato
		frequency = vibrato(frequency);

		// Tremolo
		volume = tremolo(volume);

		// Set final frequency and volume.
		synth.setFrequency(frequency);
		synth.setVolume(volume);

		// Get sample.
		double sample = synth.getSample();

		// Process sample.
		short ss = (short) Math.round(sample * (Short.MAX_VALUE / 2));

		if (DEBUG) {
			if (volume < 0 || volume > 1)
				throw new IllegalArgumentException();
			if (sample < -1 || sample > 1)
				throw new IllegalArgumentException();
		}

		return ss;
	}

	public void setFrequency(double frequency) {
		if (portamentoSpeed == 0)
			this.frequency = frequency;
		else {
			newFrequency = frequency;
			double step = (newFrequency - this.frequency)
					/ (double) portamentoSpeed;
			double samples = AudioThread.SAMPLE_RATE / 1000;
			portamentoStep = step / samples;
		}
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	private void portamento() {
		if (frequency != newFrequency)
			if (Math.abs(frequency - newFrequency) < 0.1f)
				frequency = newFrequency;
			else
				frequency += portamentoStep;
		else
			frequency = newFrequency;
	}

	private double vibrato(double frequency) {
		double lfoValue = vibrato.getSample() * vibratoDepth;
		return frequency *= Math.pow(2.0, lfoValue);
	}

	private double tremolo(double volume) {
		double lfoValue = (tremolo.getSample() + 1.0) / 2.0;
		double tremble = 1.0 - (tremoloDepth * lfoValue);
		return volume *= tremble;
	}

	public void setShape(Waveform shape) {
		synth.setShape(shape);
	}

	public void setImd(boolean imd) {
		synth.setImd(imd);
	}

	public void setTremoloShape(Waveform shape) {
		tremolo.setShape(shape);
	}

	public void setTremoloDepth(double tremoloDepthInPercent) {
		tremoloDepth = (tremoloDepthInPercent / (double) 100);
	}

	public void setTremoloSpeed(double speedInHertz) {
		tremolo.setFrequency(speedInHertz);
	}

	public void setVibratoShape(Waveform shape) {
		vibrato.setShape(shape);
	}

	public void setVibratoDepth(double vibratoDepthInPercent) {
		vibratoDepth = (vibratoDepthInPercent / (double) 100);
	}

	public void setVibratoSpeed(double speedInHertz) {
		vibrato.setFrequency(speedInHertz);
	}

	public void setPortamentoSpeed(double speedInMillis) {
		portamentoSpeed = speedInMillis;
	}
}