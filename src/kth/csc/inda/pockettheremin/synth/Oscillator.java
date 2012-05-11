package kth.csc.inda.pockettheremin.synth;

import kth.csc.inda.pockettheremin.utils.Global;
import kth.csc.inda.pockettheremin.utils.Range;

/**
 * Oscillator with support for additive synthesis of the four basic waveforms.
 */
public class Oscillator implements Global {
	private Range frequency;
	private Waveform shape;
	private long period, sample;
	private boolean imd;

	public Oscillator() {
		frequency = new Range(20000, 1);
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
		y += shape.triangle
				* (Math.asin(Math.sin(2 * Math.PI * x)) * 2 / Math.PI);
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

		return y;
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
				sine = Math.floor(sine / sum * 100) / 100;
				square = Math.floor(square / sum * 100) / 100;
				triangle = Math.floor(triangle / sum * 100) / 100;
				sawtooth = Math.floor(sawtooth / sum * 100) / 100;
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