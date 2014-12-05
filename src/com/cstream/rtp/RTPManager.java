package com.cstream.rtp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.cstream.efflux.packet.DataPacket;
import com.cstream.efflux.participant.RtpParticipant;
import com.cstream.efflux.participant.RtpParticipantInfo;
import com.cstream.efflux.session.MultiParticipantSession;
import com.cstream.efflux.session.RtpSession;
import com.cstream.efflux.session.RtpSessionDataListener;

/**
 * Sample code for audio sender.
 * 
 * @author bensweett
 *
 */
public class RTPManager {
	
	private static Logger LOGGER = Logger.getLogger(RTPManager.class.getName());
	
	private final int BUFFER_SIZE = 4096;
	
	private RtpParticipant participant = null;
	private MultiParticipantSession session = null;
	
	private SourceDataLine outLine;
	private SourceDataLine inLine;

	// Recommended ports: 6001, 6002
	// TODO: These values might need to be changed based on the peer
	public RTPManager(String host, int rtpPort, int rtcpPort)  {
		
		this.participant = RtpParticipant.createReceiver(host, rtpPort, rtcpPort);
		participant.getInfo().setSsrc(6969);
		
		this.session = new MultiParticipantSession("id", 8, participant);

		addListeners();
		
	}
	
	// TODO: Proper Encoding for Playback? needs the base format with the decoded format?
	public void playDataFromStream() {
		
		AudioFormat format = null;
		
		try {
			rawplay(format);
			
		} catch (IOException | LineUnavailableException e) {
			LOGGER.warning("IOException or Line Unavailable while playing from stream");
			
		}
		
	}
	
	public void sendFile(String path) {
		
		AudioInputStream toSend = getAudioStreamFromFile(path);
		
		if(toSend == null) {
			return;
		}
		
		toSend = getEncodedAudioStream(toSend);
		
		int nBytesRead = 0;
		byte[] abData = new byte[BUFFER_SIZE];
		long start = System.currentTimeMillis();
		
		try {
			
			while (nBytesRead != -1) {
					
				nBytesRead = toSend.read(abData, 0, abData.length);
				
				if (nBytesRead >= 0) {
					session.sendData(abData, System.currentTimeMillis(), false); // last value? is boolean called Marked? 
					
					outLine.write(abData, 0, abData.length);
					
				}
			}
			
		} catch (IOException e) {
			LOGGER.warning("IO Exception while writing audio stream to RTP");
			return;
		}
			
		LOGGER.info("Stream Duration: " + (System.currentTimeMillis() - start)/1000 + " s");
			
	}
	
	private void addListeners() {
		
		this.session.addEventListener(new RTPEventListener());
		
		this.session.addDataListener(new RtpSessionDataListener() {
					
			@Override
			public void dataPacketReceived(RtpSession rtp, RtpParticipantInfo info, DataPacket packet) {
				
				if (inLine != null) {
					byte[] data = packet.getDataAsArray();
					inLine.write(data, 0, data.length);
				}
				
			}
			
		});
	}

	private AudioInputStream getAudioStreamFromFile(String path) {
		
		AudioInputStream in = null;
		
		try {
			File audioFile = new File(path);
			in = AudioSystem.getAudioInputStream(audioFile);
		} catch (UnsupportedAudioFileException | IOException e) {
			LOGGER.warning("Exception setting up AudioInputStream for sending audio");
			e.printStackTrace();
		}
		
		return in;
	}

	private AudioInputStream getEncodedAudioStream(AudioInputStream in) {

		AudioInputStream out = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
		out = AudioSystem.getAudioInputStream(decodedFormat, in);
		return out;
		
	}

	private void rawplay(AudioFormat targetFormat) throws IOException, LineUnavailableException {
		
		byte[] data = new byte[BUFFER_SIZE];
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
		inLine = (SourceDataLine) AudioSystem.getLine(info);
		inLine.open(targetFormat);
		
		if (inLine != null) {
			
			// Start
			inLine.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				
				// TODO - Read the file and cache it. Then, we need to add ourselves as a peer!
				
				// TODO: Need a way of stopping the stream once its complete
				// NOTE: Reading is done in the packet listener
				
				// 1s timeout - if no data is received, close
				try { Thread.sleep(1000); } catch(Exception e) { }
				//nBytesRead = inLine.read(data, 0, data.length);
				
			}
			
			// Stop
			inLine.drain();
			inLine.stop();
			inLine.close();
			
		}
		
	}
	
}
