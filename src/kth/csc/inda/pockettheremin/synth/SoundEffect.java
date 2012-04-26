package kth.csc.inda.pockettheremin.synth;

import android.os.SystemClock;
import android.util.Log;

public interface SoundEffect {
	static final class MasterClock {
		long time, tick, timestamp;
		double frequency;
		boolean stable;

		public void tick() {
			tick++; // Approximate frequency

			long newTimestamp = SystemClock.elapsedRealtime();
			long dt = newTimestamp - timestamp;
			timestamp = newTimestamp;

			if (dt != newTimestamp)
				time += dt;

			/*
			 * When 1000ms (one second) has passed: store the calculated frequency
			 * and reset the clock.
			 */
			if (time > 1000) {
				frequency = tick;
				Log.i(this.getClass().getSimpleName(), frequency + "Hz");

				if (frequency >= 10) // 10 Hz is guaranteed for sound effects.
					stable = true;
				else
					stable = false;

				tick = time = 0; // Reset counters.
			}
		}

		public double getFrequency() {
			return frequency;
		}

		public boolean isStable() {
			return stable;
		}
	}
	
	static final MasterClock clock = new MasterClock();

	public double modify(double input);

	public void sync();
}
