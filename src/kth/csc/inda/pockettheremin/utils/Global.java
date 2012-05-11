package kth.csc.inda.pockettheremin.utils;
import kth.csc.inda.pockettheremin.input.Autotune;
import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;

public interface Global {
	public static final boolean DEBUG = false;

	static final class G {
		public static Range frequency, volume;
		public static boolean useSensors, useMultitouch, useAutotune;

		public static int octaves;
		public static Autotune.Note key;
		public static Autotune.Scale scale;

		public static Waveform synthShape;
		public static boolean synthIMD, chiptune;

		public static Waveform tremoloShape;
		public static int tremoloSpeed, tremoloDepth;

		public static Waveform vibratoShape; 
		public static int vibratoSpeed, vibratoDepth;

		public static int portamentoSpeed;
		
		public static int delayFeedback, delayMix, delayBPM;
	}
}