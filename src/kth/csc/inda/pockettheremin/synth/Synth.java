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
	private double portamentoSpeed, frequencyStep, newFrequency;
	private double fadeSpeed, volumeStep, newVolume;

	public Synth(Sampler input) {
		super(input);
		fadeSpeed = 10;
		synth = new Oscillator();
		tremolo = new Oscillator();
		vibrato = new Oscillator();
	}

	@Override
	protected short processSample(short s) {

		// Set initial frequency and volume.
		double frequency = this.frequency;
		double volume = this.volume / G.volume.range * 2;

		// Portamento
		portamento();

		// Fade
		fade();

		// Vibrato
		frequency = vibrato(frequency);

		// Tremolo
		volume = tremolo(volume);

		// Set oscillation frequency
		synth.setFrequency(frequency);

		if (DEBUG)
			if (volume < -1 || volume > 1)
				throw new IllegalArgumentException();

		// Get and process sample.
		short sample = (short) Math.round(synth.getSample() * volume
				* Short.MAX_VALUE);

		return sample;
	}

	public void setFrequency(double frequency) {
		if (portamentoSpeed == 0)
			this.frequency = frequency;
		else {
			newFrequency = frequency;
			double step = (newFrequency - this.frequency)
					/ portamentoSpeed;
			double samples = AudioThread.SAMPLE_RATE / 1000;
			frequencyStep = step / samples;
		}
	}

	public void setVolume(double volume) {
		if (fadeSpeed == 0)
			this.volume = volume;
		else {
			newVolume = volume;
			double step = (newVolume - this.volume) / fadeSpeed;
			double samples = AudioThread.SAMPLE_RATE / 1000;
			volumeStep = step / samples;
		}
	}

	private void fade() {
		if (volume != newVolume)
			if (Math.abs(volume - newVolume) < 0.1f)
				volume = newVolume;
			else
				volume += volumeStep;
		else
			volume = newVolume;
	}

	private void portamento() {
		if (frequency != newFrequency)
			if (Math.abs(frequency - newFrequency) < 0.1f)
				frequency = newFrequency;
			else
				frequency += frequencyStep;
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
		tremoloDepth = (tremoloDepthInPercent / 100);
	}

	public void setTremoloSpeed(double speedInHertz) {
		tremolo.setFrequency(speedInHertz);
	}

	public void setVibratoShape(Waveform shape) {
		vibrato.setShape(shape);
	}

	public void setVibratoDepth(double vibratoDepthInPercent) {
		vibratoDepth = (vibratoDepthInPercent / 100);
	}

	public void setVibratoSpeed(double speedInHertz) {
		vibrato.setFrequency(speedInHertz);
	}

	public void setPortamentoSpeed(double speedInMillis) {
		portamentoSpeed = speedInMillis;
	}
}