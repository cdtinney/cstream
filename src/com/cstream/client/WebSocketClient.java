package com.cstream.client;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.cstream.logging.LogLevel;
import com.cstream.socket.IOSocket;
import com.cstream.socket.MessageCallback;
import com.cstream.tracker.TrackerPeer;

public class WebSocketClient {
	
	private static Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());

	private final static String SERVER_URL = "https://cstream-tracker-venom889.c9.io";
	
	private static final String PEER_CONNECT = "PEER_CONNECT";
	private static final String PEER_DISCONNECT = "PEER_DISCONNECT";
	
	private static final int KEEP_ALIVE = 10;
	
	// Model
	private TrackerPeer peer;
	
	// Use this socket to communicate to the tracker server
	private IOSocket socket;
	
	public WebSocketClient(TrackerPeer peer) {
		this.peer = peer;
	}
	
	public void connect() {
		
		if (socket != null) {
			LOGGER.warning("Web socket is already connected");
			return;
		}
		
		socket = new IOSocket(SERVER_URL.replace("https", "ws"), new TrackerCallback());
		
		try {
			socket.connect();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void disconnect() {
		
		if (socket == null) {
			return;
		}
		
		socket.disconnect();
		
	}
	
	/*
	 * Schedules a task that sends a KEEP_ALIVE message to the  socket server once every 10 seconds.
	 * If the server doesn't receive the message, it will kill the socket and remove the peer from the tracker.
	 */
	private void startKeepAlive() {
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		Runnable task = new Runnable() {

			@Override
			public void run() {
				
				try {
					LOGGER.log(LogLevel.DEBUG, "Sending KEEP_ALIVE from ID = " + peer.getId());
					socket.sendKeepAlive(peer.getId());
					
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		};
		
		executor.scheduleAtFixedRate(task, 0, KEEP_ALIVE, TimeUnit.SECONDS);
		
	}
	
	private class TrackerCallback implements MessageCallback {

		@Override
		public void on(String event, JSONObject... data) {
			
			switch(event) {
			
				case PEER_CONNECT:
					LOGGER.info("PEER_CONNECT event received: " + data);
					//updateLibrary();
					break;
				
				case PEER_DISCONNECT:
					LOGGER.info("PEER_DISCONNECT event received: " + data);
					//updateLibrary();
					break;
					
				default:
					LOGGER.info("Socket event received - " + event + " - " + data);						
				
			}
			
		}

		@Override
		public void onMessage(String message) {
			LOGGER.info("Message received: " + message);
		}

		@Override
		public void onMessage(JSONObject json) {
			LOGGER.info("JSON message received: " + json);
		}

		@Override
		public void onConnect() {
			LOGGER.info("Connected to socket server");
			startKeepAlive();
		}

		@Override
		public void onDisconnect() {
			LOGGER.info("Disconnected from socket server");
		}
		
	}
	
}
