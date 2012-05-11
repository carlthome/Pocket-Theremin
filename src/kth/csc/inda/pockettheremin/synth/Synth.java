package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.utils.Global;
import kth.csc.inda.pockettheremin.utils.Range;

/**
 * Sampler consisting of three oscillators: one for sound generation, one for AM
 * LFO and one for FM LFO. There's also support for portamento.
 * 
 * This sampler should be placed first in the audio chain.
 */
public class Synth extends Sampler implements Global {

	private final Range frequency, volume;
	private Oscillator synth, tremolo, vibrato;
	private double tremoloDepth, vibratoDepth;
	private double portamentoSpeed, portamentoStep, newFrequency;

	public Synth(Sampler input) {
		super(input);
		frequency = new Range(G.frequency.max, G.frequency.min);
		volume = new Range(G.volume.max, G.volume.min);
		synth = new Oscillator();
		tremolo = new Oscillator();
		vibrato = new Oscillator();
	}

	@Override
	protected short processSample(short s) {

		// Set initial frequency and volume.
		double frequency = this.frequency.get();
		double volume = this.volume.get() / this.volume.range * 2;

		// Portamento
		portamento();

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
		short sample = (short) Math.round(synth.getSample() * volume * Short.MAX_VALUE);

		return sample;
	}

	public void setFrequency(double frequency) {
		if (portamentoSpeed == 0)
			this.frequency.set(frequency);
		else {
			newFrequency = frequency;
			double step = (newFrequency - this.frequency.get())
					/ (double) portamentoSpeed;
			double samples = AudioThread.SAMPLE_RATE / 1000;
			portamentoStep = step / samples;
		}
	}

	public void setVolume(double volume) {
		this.volume.set(volume);
	}

	private void portamento() {
		if (frequency.get() != newFrequency)
			if (Math.abs(frequency.get() - newFrequency) < 0.1f)
				frequency.set(newFrequency);
			else
				frequency.set(frequency.get() + portamentoStep);
		else
			frequency.set(newFrequency);
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