//package org.wikijava.sound.playWave;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound {

    private InputStream waveStream;
    private SourceDataLine dataLine;
    private Info info;
    private AudioFormat audioFormat;
    private AudioInputStream audioInputStream;
    private InputStream bufferedIn;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
		this.waveStream = waveStream;
    }

    public void play() throws PlayWaveException {
		// Starts the music :P
		dataLine.start();

		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

		try {
			System.out.println("Sound played 3");
		    while (readBytes != -1) {
			readBytes = audioInputStream.read(audioBuffer, 0,
				audioBuffer.length);
			if (readBytes >= 0){
			    dataLine.write(audioBuffer, 0, readBytes);
			}
		    }
		} catch (IOException e1) {
			System.out.println("Sound played error");
		    throw new PlayWaveException(e1);
		} finally {
		    // plays what's left and and closes the audioChannel
		    dataLine.drain();
		    dataLine.close();
		}
    }

    public void initSoundFile () {
    	audioInputStream = null;
		try {
			bufferedIn = new BufferedInputStream(this.waveStream); // new
		    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (Exception e3) {}

		// Obtain the information about the AudioInputStream
		audioFormat = audioInputStream.getFormat();
		info = new Info(SourceDataLine.class, audioFormat);

    	// opens the audio channel
		dataLine = null;
		try {
		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
		    dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (Exception e) {}
    }

    public void stop() throws PlayWaveException {
    	dataLine.stop();
    }
}
