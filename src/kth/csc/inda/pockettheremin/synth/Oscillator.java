package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Global;
import kth.csc.inda.pockettheremin.utils.Range;

/**
 * Oscillator with support for additive synthesis of the four basic waveforms.
 */
public class Oscillator implements Global {
	private Range frequency = new Range(20000.00, 1.00);
	private Range volume = new Range(1.0, 0.0);
	private Waveform shape;
	private long period, sample;
	private boolean imd;

	public Oscillator() {
		volume.set(1.0);
		imd = false;
	}

	public double getSample() {
		period = (long) (AudioThread.SAMPLE_RATE / frequency.get());
		double x = sample / (double) period;
		double y = 0;

		if (shape == null)
			return 0;

		y += shape.sine * (Math.sin(2 * Math.PI * x));
		y += shape.square * (Math.sin(2 * Math.PI * x) % 2 < 0 ? -1 : 1);
		y += shape.triangle * (Math.asin(Math.sin(2 * Math.PI * x)));
		y += shape.sawtooth * (2.0 * (x - Math.floor(x + 0.5)));

		if (!imd) {
			sample = (sample + 1) % period;
		} else {
			sample++;
			if (sample > period)
				sample = 0;
		}

		if (DEBUG)
			if (y < -1 || y > 1)
				throw new IllegalArgumentException();

		return y * volume.get();
	}

	public void setImd(boolean imd) {
		this.imd = imd;
	}

	public void setShape(Waveform shape) {
		this.shape = shape;
	}

	public void setFrequency(double frequency) {
		this.frequency.set(frequency);
	}

	public void setVolume(double volume) {
		this.volume.set(volume);
	}

	public static class Waveform {
		private double sine, square, triangle, sawtooth;

		public Waveform(double sine, double square, double triangle,
				double sawtooth) {
			this.sine = sine;
			this.square = square;
			this.triangle = triangle;
			this.sawtooth = sawtooth;

			evenOut();
		}

		/*
		 * Convert values to parts of 100%.
		 */
		private void evenOut() {
			double sum = sine + square + triangle + sawtooth;
			if (sum != 1.0) {
				sine = sine / sum;
				square = square / sum;
				triangle = triangle / sum;
				sawtooth = sawtooth / sum;
			}
		}

		public void setSine(double sine) {
			this.sine = sine;
		}

		public void setSquare(double square) {
			this.square = square;
		}

		public void setTriangle(double triangle) {
			this.triangle = triangle;
		}

		public void setSawtooth(double sawtooth) {
			this.sawtooth = sawtooth;
		}
	}
}