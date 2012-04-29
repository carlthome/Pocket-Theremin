package kth.csc.inda.pockettheremin.synth;

import android.os.SystemClock;
import android.util.Log;

public interface SoundEffect {
	static final class Clock {

		/*
		 * Guaranteed Hz for sound effect operations.
		 */
		private static final double MINIMUM_FREQUENCY = 10.00;

		private long time, timestamp;
		private double tick, frequency;
		private boolean stable;

		public void tick() {
			tick++; // Approximate frequency.

			long newTimestamp = SystemClock.elapsedRealtime();
			long dt = newTimestamp - timestamp;
			timestamp = newTimestamp;

			if (dt != newTimestamp)
				time += dt;

			/*
			 * When 1000 ms (one second) has passed: store the calculated
			 * frequency and reset the clock.
			 */
			if (time > 1000) {				
				frequency = tick;
				Log.i(this.getClass().getSimpleName(), frequency + "Hz");

				if (frequency >= MINIMUM_FREQUENCY)
					stable = true;
				else
					stable = false;

				// Reset counters.
				tick = 0;
				time = 0;
			}
		}

		public double getFrequency() {
			return frequency;
		}

		public boolean isStable() {
			return stable;
		}
	}

	static final Clock clock = new Clock();

	public double modify(double input);
}
