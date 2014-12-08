package com.cstream.playback;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.cstream.torrent.TorrentManager;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class LocalAudioPlayback {
	
	private SourceDataLine audioLine;
	
	private AudioState state = AudioState.CLOSED;
	
	public enum AudioState {
		PLAYING,
		PAUSED,
		BUFFERING,
		CLOSED
	};
	
	public boolean isPlaying() {
		return AudioState.PLAYING.equals(this.state);
	}
	
	public boolean isClosed() {
		return AudioState.CLOSED.equals(this.state);
	}
	
	public boolean isPaused() {
		return AudioState.PAUSED.equals(this.state);
	}
	
	public void togglePause() {
		
		if (isPlaying()) {
			this.state = AudioState.PAUSED;
			
		} else if (isPaused()) {
			this.state = AudioState.PLAYING;
		}
	}

	public void stop() {
		this.state = AudioState.CLOSED;
	}
	
	public int getPosition() {
		
		if (audioLine == null) {
			return 0;
		}
		
		return (int) audioLine.getMicrosecondPosition() / 1000000;
		
	}
	
	public void playFile(String fileName, LineListener listener) {
		
		String path = TorrentManager.FILE_DIR + fileName;
		
		new Thread(() -> {
			
			play(path, listener);
			
		}).start();
		
	}
	
	@SuppressWarnings("deprecation")
	private void play(String path, LineListener listener) {

		String filename = path;

		// Create a container object
		IContainer container = IContainer.make();

		// Open up the container
		if (container.open(filename, IContainer.Type.READ, null) < 0) {
			throw new IllegalArgumentException("could not open file: " + filename);
		}
		
		state = AudioState.BUFFERING;

		// query how many streams the call to open found
		int numStreams = container.getNumStreams();

		// and iterate through the streams to find the first audio stream
		int audioStreamId = -1;
		IStreamCoder audioCoder = null;

		for (int i=0; i < numStreams; i++) {
			
			IStream stream = container.getStream(i);
			
			// Get the configured decoder that can decode this stream
			IStreamCoder coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				audioStreamId = i;
				audioCoder = coder;
				break;
			}
			
		}

		if (audioStreamId == -1) {
			throw new RuntimeException("could not find audio stream in container: " + filename);
		}

		// Open up the decoder
		if (audioCoder.open() < 0) {
			throw new RuntimeException("could not open audio decoder for container: " + filename);
		}

		// Prepare the Java sound system
		openJavaSound(audioCoder, listener);

		// Now, we start walking through the container looking at each packet.
		IPacket packet = IPacket.make();
		
		while (!isClosed()) {
			
			if (isPaused()) {
				
				try {
					Thread.sleep(200);
					continue;
				} catch (InterruptedException ex) { }
				
			}
			
			if (container.readNextPacket(packet) >= 0) {
				
				state = AudioState.PLAYING;
				
				// Now we have a packet, let's see if it belongs to our audio stream
				if (packet.getStreamIndex() == audioStreamId) {
					
					/*
					 * We allocate a set of samples with the same number of channels as the
					 * coder tells us is in this buffer.
					 * 
					 * We also pass in a buffer size (1024 in our example), although Xuggler
					 * will probably allocate more space than just the 1024 (it's not important why).
					 */
					IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());

					/*
					 * A packet can actually contain multiple sets of samples (or frames of samples
					 * in audio-decoding speak).  So, we may need to call decode audio multiple
					 * times at different offsets in the packet's data.  We capture that here.
					 */
					int offset = 0;

					// Process all data
					while (offset < packet.getSize()) {
						
						int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
						if (bytesDecoded < 0) {
							throw new RuntimeException("got error decoding audio in: " + filename);
						}
						
						offset += bytesDecoded;
						
						/*
						 * Some decoder will consume data in a packet, but will not be able to construct
						 * a full set of samples yet.  Therefore you should always check if you
						 * got a complete set of samples from the decoder
						 */
						if (samples.isComplete()) {
							playJavaSound(samples);
						}
						
					}
					
				} else {
					
				
					// This packet isn't part of our audio stream, so we just silently drop it.
					do {} while(false);
					
				}
				
			} else {
				
				try {
					Thread.sleep(500);
					
				} catch (Exception e) { }
				
				state = AudioState.CLOSED;
				
			}
			
		}
		
		closeJavaSound();

		if (audioCoder != null) {
			audioCoder.close();
			audioCoder = null;
		}

		if (container !=null) {
			container.close();
			container = null;
		}

	}

	private void openJavaSound(IStreamCoder aAudioCoder, LineListener listener) {

		AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
				(int)IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
				aAudioCoder.getChannels(),
				true, /* xuggler defaults to signed 16 bit samples */
				false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

		try {
			
			audioLine = (SourceDataLine) AudioSystem.getLine(info);
			audioLine.addLineListener(listener);
			
			// Try opening the line
			audioLine.open(audioFormat);
			
			// Try starting the line
			audioLine.start();
			
		} catch (LineUnavailableException e) {
			throw new RuntimeException("could not open audio line " + e.getMessage());
			
		}

	}

	private void playJavaSound(IAudioSamples aSamples) {
		
		if (audioLine == null) {
			return;
		}
		
		byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
		audioLine.write(rawBytes, 0, aSamples.getSize());
		
	}

	private void closeJavaSound() {
		
		if (audioLine != null) {
			
			// Wait for the line to finish playing
			audioLine.drain();

			// Close the line
			audioLine.close();
			audioLine = null;
			
		}
		
	}
	
}
