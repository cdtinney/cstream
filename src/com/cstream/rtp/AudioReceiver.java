package com.cstream.rtp;

import java.net.DatagramSocket;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

import com.cstream.enums.AudioPanPosition;

public class AudioReceiver implements RTPAppIntf {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(AudioReceiver.class.getName());
	
	private RTPSession rtpSession = null;
	private AudioPanPosition curPosition;
	
	private byte[] abData = null;
	private int nBytesRead = 0;
	private int pktCount = 0;
	private int dataCount = 0;
	private int offsetCount = 0;
	private SourceDataLine auline;
	
	public AudioReceiver(int rtpPort, int rtcpPort)  {
		
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(rtpPort);
			rtcpSocket = new DatagramSocket(rtcpPort);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		
		
		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.naivePktReception(true);
		rtpSession.RTPSessionRegister(this,null, null);
		
		//Participant p = new Participant("127.0.0.1", 6001, 6002);		
		//rtpSession.addParticipant(p);
	}
		
	public void run() {
		
		AudioFormat.Encoding encoding =  new AudioFormat.Encoding("PCM_SIGNED");
		AudioFormat format = new AudioFormat(encoding,((float) 8000.0), 16, 1, 2, ((float) 8000.0) ,false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		auline = null;
		
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
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (this.curPosition == AudioPanPosition.RIGHT)
				pan.setValue(1.0f);
			else if (this.curPosition == AudioPanPosition.LEFT)
				pan.setValue(-1.0f);
		}
		
		auline.start();
		try {
			while (nBytesRead != -1) {
				// Used to write audiot to auline here,
				// now moved directly to receiveData.
				try { Thread.sleep(1000); } catch(Exception e) { }
			}
		} finally {
			auline.drain();
			auline.close();
		}
		
	}
	
	
	@Override
	public int frameSize(int arg0) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void receiveData(DataFrame frame, Participant p) {
		
		if (auline != null) {
			byte[] data = frame.getConcatenatedData();
			auline.write(data, 0, data.length);
			
		}
		
		
		pktCount++;
	}

	@Override
	public void userEvent(int arg0, Participant[] arg1) {
		// TODO Auto-generated method stub
	}

}
