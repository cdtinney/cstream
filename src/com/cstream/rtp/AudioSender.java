package com.cstream.rtp;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.cstream.enums.AudioPanPosition;

import jlibrtp.*;

/**
 * Sample code for audio sender. TODO: Needs a clean up
 * 
 * @author bensweett
 *
 */
public class AudioSender implements RTPAppIntf {
	
	private static Logger LOGGER = Logger.getLogger(AudioSender.class.getName());
	
	public RTPSession rtpSession = null;
	static int pktCount = 0;
	static int dataCount = 0;

	private final int EXTERNAL_BUFFER_SIZE = 1024;
	
	// Handles audio sending/receiving and mixing (the lib writes to and from it)
	private SourceDataLine auline;
	private AudioPanPosition curPosition;
	boolean local;


	private File soundFile;
	private AudioInputStream audioInputStream;


	public AudioSender(boolean isLocal, int rtpPort, int rtcpPort)  {
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;

		try {
			rtpSocket = new DatagramSocket(rtpPort);
			rtcpSocket = new DatagramSocket(rtcpPort);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}

		// How should we handle the session? This class builds one each time its called 
		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.RTPSessionRegister(this, null, null);
		//System.out.println("CNAME: " + rtpSession.CNAME());
		this.local = isLocal;
	}


	private boolean setFile(String filename) {
		this.soundFile = new File(filename);
		if (!soundFile.exists()) {
			System.err.println("Wave file not found: " + filename);
			return false;
		}

		return true;
	}

	private AudioFormat encodeAudioFromFile() {

		try {
			this.audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		//AudioFormat format = audioInputStream.getFormat();
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 8000.0), 16, 1, 2, ((float) 8000.0) ,false);
		//System.out.println(format.toString());

		return format;
	}


	public void run(String filename) {
		/*if(RTPSession.rtpDebugLevel > 1) {
			System.out.println("-> Run()");
		} */

		if(!setFile(filename)) {
			return;
		}

		AudioFormat format = encodeAudioFromFile();
		if(format == null) {
			return;
		}

		if(!this.local) {
			// To time the output correctly, we also play at the input:
			auline = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

			try {
				auline = (SourceDataLine) AudioSystem.getLine(info);
				auline.open(format);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			if (auline.isControlSupported(FloatControl.Type.PAN)) {
				FloatControl pan = (FloatControl) auline.getControl(FloatControl.Type.PAN);
				if (this.curPosition == AudioPanPosition.RIGHT)
					pan.setValue(1.0f);
				else if (this.curPosition == AudioPanPosition.LEFT)
					pan.setValue(-1.0f);
			}

			auline.start();
		}


		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		long start = System.currentTimeMillis();

		try {
			while (nBytesRead != -1 && pktCount < 200) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);

				if (nBytesRead >= 0) {
					rtpSession.sendData(abData);
					auline.write(abData, 0, abData.length);
					pktCount++;
				}

				if(pktCount == 100) {
					Enumeration<Participant> iter = this.rtpSession.getParticipants();
					Participant p = null;

					while(iter.hasMoreElements()) {
						p = iter.nextElement();

						String name = "name";
						byte[] nameBytes = name.getBytes();
						String data = "abcd";
						byte[] dataBytes = data.getBytes();


						int ret = rtpSession.sendRTCPAppPacket(p.getSSRC(), 0, nameBytes, dataBytes);
						System.out.println("!!!!!!!!!!!! ADDED APPLICATION SPECIFIC " + ret);
						continue;
					}

					if(p == null)
						System.out.println("No participant with SSRC available :(");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Time: " + (System.currentTimeMillis() - start)/1000 + " s");

		try { Thread.sleep(200);} catch(Exception e) {}

		this.rtpSession.endSession();

		try { Thread.sleep(2000);} catch(Exception e) {}
	}

	@Override
	public int frameSize(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void receiveData(DataFrame arg0, Participant arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void userEvent(int arg0, Participant[] arg1) {
		// TODO Auto-generated method stub

	}

}
