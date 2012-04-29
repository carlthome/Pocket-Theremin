package kth.csc.inda.pockettheremin.synth;

public enum Preset {
	THEREMIN(// Synth
			Waveform.SINE, false, false,
			// Vibrato
			Waveform.SINE, 4, 3,
			// Tremolo
			Waveform.SINE, 1, 10,
			// Portamento
			100),

	ZELDA(// Synth
			Waveform.TRIANGLE, true, false,
			// Vibrato
			Waveform.TRIANGLE, 9, 2,
			// Tremolo
			Waveform.SINE, 1, 10,
			// Portamento
			0),

	BAGPIPE(// Synth
			Waveform.SAWTOOTH, true, false,
			// Vibrato
			Waveform.SINE, 1, 1,
			// Tremolo
			Waveform.NONE, 0, 0,
			// Portamento
			25),

	FLUTTER(// Synth
			Waveform.SQUARE, true, false,
			// Vibrato
			Waveform.NONE, 0, 0,
			// Tremolo
			Waveform.SQUARE, 10, 100,
			// Portamento
			0),

	SPACE(// Synth
			Waveform.SINE, false, false,
			// Vibrato
			Waveform.SINE, 1, 50,
			// Tremolo
			Waveform.NONE, 0, 0,
			// Portamento
			100);

	public Waveform SYNTH_WAVEFORM;
	public boolean SYNTH_IMD, SYNTH_CHIPTUNE;
	public Waveform VIBRATO_SHAPE;
	public int VIBRATO_SPEED, VIBRATO_DEPTH;
	public Waveform TREMOLO_SHAPE;
	public int TREMOLO_SPEED, TREMOLO_DEPTH;
	public int PORTAMENTO_SPEED;

	Preset(Waveform synthWaveform, boolean synthIMD, boolean synthChiptune,

	Waveform vibratoShape, int vibratoSpeed, int vibratoDepth,

	Waveform tremoloShape, int tremoloSpeed, int tremoloDepth,

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
