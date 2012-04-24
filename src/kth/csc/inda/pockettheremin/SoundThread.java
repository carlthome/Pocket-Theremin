package kth.csc.inda.pockettheremin;

import kth.csc.inda.pockettheremin.Oscillator.Waveform;
import kth.csc.inda.pockettheremin.soundeffects.SoundEffect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

class SoundThread implements Runnable {

	static final int bufferSize = 2048;
	static final int sampleRate = 44100;
	//TODO
	//boolean useChiptuneMode = false;
	//int audioFormat = (useChiptuneMode) ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;

	public static AudioTrack audioStream = new AudioTrack(
			AudioManager.STREAM_MUSIC, sampleRate,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT, AudioTrack.getMinBufferSize(
					sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);

	Thread t;
	boolean play = true;
	double frequency, amplitude;
	Oscillator oscillator;
	SoundEffect autotune, tremolo, portamento, vibrato;
	long time;

	SoundThread(String name) {
		t = new Thread(this, name);
		oscillator = new Oscillator(Waveform.SINE, bufferSize, sampleRate);
		t.start();
	}

	public void run() {
		audioStream.play();
		Log.i(t.getName(), "Thread started.");
		while (play) {
			short[] samples = oscillator.getSamples();

			Log.d(t.getName(), "Buffer:" + samples.toString());

			audioStream.write(samples, 0, bufferSize);
		}
		Log.i(t.getName(), "Thread finished.");
		audioStream.release();
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
		oscillator.setFrequency(frequency);
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
		audioStream.setStereoVolume((float) amplitude, (float) amplitude);
	}

	public void cancel() {
		play = false;
	}
}