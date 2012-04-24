package kth.csc.inda.pockettheremin.soundeffects;

import kth.csc.inda.pockettheremin.Oscillator.Waveform;

//TODO Place presets here so the main activity is less of a mess.
public enum Preset {
	THEREMIN(Waveform.SINE, false, true, Waveform.TRIANGLE, 80, 4, true,
			Waveform.SINE, 10, 10, true, 25),

	ZELDA(Waveform.SQUARE1, true, false, Waveform.TRIANGLE, 8, 10, false,
			Waveform.SINE, 1, 10, false, 0),

	SPACE(Waveform.TRIANGLE, false, true, Waveform.SINE, 1, 100, true,
			Waveform.SQUARE1, 10, 100, true, 100);

	final Waveform synthWaveform;
	final boolean useChiptuneMode;
	final boolean useVibrato;
	final Waveform vibratoWaveform;
	final int vibratoSpeed;
	final int vibratoDepth;
	final boolean useTremolo;
	final Waveform tremoloWaveform;
	final int tremoloSpeed;
	final int tremoloDepth;
	final boolean usePortamento;
	final int portamentoSpeed;

	Preset(Waveform synthWaveform, boolean useChiptuneMode, boolean useVibrato,
			Waveform vibratoWaveform, int vibratoSpeed, int vibratoDepth,
			boolean useTremolo, Waveform tremoloWaveform, int tremoloSpeed,
			int tremoloDepth, boolean usePortamento, int portamentoSpeed) {
		this.synthWaveform = synthWaveform;
		this.useChiptuneMode = useChiptuneMode;
		this.useVibrato = useVibrato;
		this.vibratoWaveform = vibratoWaveform;
		this.vibratoSpeed = vibratoSpeed;
		this.vibratoDepth = vibratoDepth;
		this.useTremolo = useTremolo;
		this.tremoloWaveform = tremoloWaveform;
		this.tremoloSpeed = tremoloSpeed;
		this.tremoloDepth = tremoloDepth;
		this.usePortamento = usePortamento;
		this.portamentoSpeed = portamentoSpeed;
	}

	public Waveform getSynthWaveform() {
		return synthWaveform;
	}

	public boolean isUseChiptuneMode() {
		return useChiptuneMode;
	}

	public boolean isUseVibrato() {
		return useVibrato;
	}

	public Waveform getVibratoWaveform() {
		return vibratoWaveform;
	}

	public int getVibratoSpeed() {
		return vibratoSpeed;
	}

	public int getVibratoDepth() {
		return vibratoDepth;
	}

	public boolean isUseTremolo() {
		return useTremolo;
	}

	public Waveform getTremoloWaveform() {
		return tremoloWaveform;
	}

	public int getTremoloSpeed() {
		return tremoloSpeed;
	}

	public int getTremoloDepth() {
		return tremoloDepth;
	}

	public boolean isUsePortamento() {
		return usePortamento;
	}

	public int getPortamentoSpeed() {
		return portamentoSpeed;
	}

};
