package com.cstream.rtp;

import java.util.logging.Logger;

import com.biasedbit.efflux.participant.RtpParticipant;
import com.biasedbit.efflux.session.RtpSession;
import com.biasedbit.efflux.session.RtpSessionEventListener;

public class RTPEventListener implements RtpSessionEventListener {

	private static Logger LOGGER = Logger.getLogger(RTPEventListener.class.getName());
	
	@Override
	public void participantDataUpdated(RtpSession arg0, RtpParticipant arg1) {
		LOGGER.info("Participant data updated: " + arg1.getSsrc());
	}

	@Override
	public void participantDeleted(RtpSession arg0, RtpParticipant arg1) {
		LOGGER.info("Participant Deteled: " + arg1.getSsrc());
		
	}

	@Override
	public void participantJoinedFromControl(RtpSession arg0, RtpParticipant arg1) {
		LOGGER.info("Participant Joined From Control: " + arg1.getSsrc());
		
	}

	@Override
	public void participantJoinedFromData(RtpSession arg0, RtpParticipant arg1) {
		LOGGER.info("Participant Joined From Data: " + arg1.getSsrc());
		
	}

	@Override
	public void participantLeft(RtpSession arg0, RtpParticipant arg1) {
		LOGGER.info("Participant Left: " + arg1.getSsrc());
		
	}

	@Override
	public void resolvedSsrcConflict(RtpSession arg0, long arg1, long arg2) {
		LOGGER.info("Fixed SSRC Conflict: " + arg1 + " " + arg2);
		
	}

	@Override
	public void sessionTerminated(RtpSession arg0, Throwable arg1) {
		LOGGER.info("Session terminated: " + arg1.getMessage());
		
	}

}
