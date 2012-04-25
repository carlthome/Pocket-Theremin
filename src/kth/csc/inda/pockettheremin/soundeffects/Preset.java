package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.synth.Oscillator.Waveform;

public enum Preset {
	THEREMIN(// Synth
			Waveform.SINE, false, false,
			// Vibrato
			Waveform.TRIANGLE, 3, 4,
			// Tremolo
			Waveform.SINE, 1, 10,
			// Portamento
			25),

	ZELDA(// Synth
			Waveform.TRIANGLE, true, false,
			// Vibrato
			Waveform.TRIANGLE, 10, 1,
			// Tremolo
			Waveform.SINE, 1, 10,
			// Portamento
			0),

	BAGPIPE(// Synth
			Waveform.SAWTOOTH, false, false,
			// Vibrato
			Waveform.NONE, 0, 0,
			// Tremolo
			Waveform.SINE, 1, 5,
			// Portamento
			10),

	FLUTTER(// Synth
			Waveform.SQUARE, true, false,
			// Vibrato
			Waveform.NONE, 0, 0,
			// Tremolo
			Waveform.SQUARE, 10, 100,
			// Portamento
			5),

	SPACE(// Synth
			Waveform.SINE, true, false,
			// Vibrato
			Waveform.SINE, 1, 50,
			// Tremolo
			Waveform.NONE, 0, 0,
			// Portamento
			100);

	public Waveform SYNTH_WAVEFORM;
	public boolean SYNTH_IMD;
	public boolean SYNTH_CHIPTUNE;

	public Waveform VIBRATO_SHAPE;
	public int VIBRATO_SPEED;
	public int VIBRATO_DEPTH;

	public Waveform TREMOLO_SHAPE;
	public int TREMOLO_SPEED;
	public int TREMOLO_DEPTH;

	public int PORTAMENTO_SPEED;

	Preset( // Synth
	Waveform synthWaveform, boolean synthIMD, boolean synthChiptune,
	// Vibrato
			Waveform vibratoShape, int vibratoSpeed, int vibratoDepth,
			// Tremolo
			Waveform tremoloShape, int tremoloSpeed, int tremoloDepth,
			// Portamento
			int portamentoSpeed) {

		SYNTH_WAVEFORM = synthWaveform;
		SYNTH_IMD = synthIMD;
		SYNTH_CHIPTUNE = synthChiptune;
		VIBRATO_SHAPE = vibratoShape;
		VIBRATO_SPEED = vibratoSpeed;
		VIBRATO_DEPTH = vibratoDepth;
		TREMOLO_SHAPE = tremoloShape;
		TREMOLO_SPEED = tremoloSpeed;
		TREMOLO_DEPTH = tremoloDepth;
		PORTAMENTO_SPEED = portamentoSpeed;
	}
};
