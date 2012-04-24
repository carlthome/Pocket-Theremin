package kth.csc.inda.pockettheremin;

import kth.csc.inda.pockettheremin.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.PocketThereminActivity;
import kth.csc.inda.pockettheremin.soundeffects.Tremolo;
import android.media.AudioTrack;
import android.util.Log;

class TremoloThread implements Runnable {
	Thread t;

	boolean play = true;
	final int bufferSize = 128;
	final int sampleRate = 44100;
	Oscillator oscillator;
	double amplitude;
	Tremolo tremolo ;

	TremoloThread(String name, double frequency) {
		t = new Thread(this, name);
		
		//Waveform waveform = Waveform.SINE;
		//oscillator = new Oscillator(waveform, bufferSize, sampleRate);
		//oscillator.setFrequency(frequency);
		
		tremolo = new Tremolo(100, 20, Waveform.SINE, sampleRate, bufferSize);
		t.start();		
	}

	public void run() {
		Log.i(t.getName(), "Thread started.");
		
		while (play) {
			amplitude = tremolo.modify(amplitude);			
			Log.i(t.getName(), "Amplitude: " + amplitude);
			//SoundThread.audioStream.setStereoVolume((float) amplitude, (float) amplitude);
		}
		Log.i(t.getName(), "Thread finished.");
	}

	public void cancel() {
		play = false;
	}
}