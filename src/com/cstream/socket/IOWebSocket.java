package com.cstream.socket;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.logging.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cstream.logging.LogLevel;

public class IOWebSocket extends WebSocketClient {

	private static Logger LOGGER = Logger.getLogger(IOWebSocket.class.getName());
	
	private MessageCallback callback;
	private IOSocket ioSocket;
	private String namespace;

	public IOWebSocket(URI uri, IOSocket ioSocket, MessageCallback callback) {
		super(uri);
		this.callback = callback;
		this.ioSocket = ioSocket;
	}
	
	@Override
	public void onError(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onMessage(String msg) {
		
		LOGGER.info("Message received: " + msg);
		IOMessage message = IOMessage.parseMsg(msg);
		
		switch (message.getType()) {	
		
			case IOMessage.HEARTBEAT:
				
				try {
					LOGGER.log(LogLevel.DEBUG, "Sending: 2::");
					send("2::");
					LOGGER.log(LogLevel.DEBUG, "HeartBeat written to server");
					
				} catch (NotYetConnectedException e) {
					e.printStackTrace();
					
				}
				
				break;
				
			case IOMessage.MESSAGE:
				callback.onMessage(message.getMessageData());
				break;
				
			case IOMessage.JSONMSG:
				
				try {
					callback.onMessage(new JSONObject(message.getMessageData()));
					
				} catch (JSONException e) {
					e.printStackTrace();
					
				}
				
				break;
			
			case IOMessage.EVENT:
				
				try {
					
					JSONObject event = new JSONObject(message.getMessageData());
					JSONArray args = event.getJSONArray("args");
					JSONObject[] argsArray = new JSONObject[args.length()];
					
					for (int i = 0; i < args.length(); i++) {
						argsArray[i] = args.getJSONObject(i);
					}
					
					String eventName = event.getString("name");
					
					callback.on(eventName, argsArray);
					
				} catch (JSONException e) {
					LOGGER.warning("JSON Exception - Event Received - " + e.getMessage());
					
				}
				
				break;
	
			case IOMessage.CONNECT:
				ioSocket.onConnect();
				break;
				
			case IOMessage.ACK:
				// TODO - Socket - Handle ACK messages
				LOGGER.info("ACK message received");
				break;
				
			case IOMessage.ERROR:
				// TODO - Socket - Handle ERROR messages
				LOGGER.info("ERROR message received");
				break;
				
			case IOMessage.DISCONNECT:
				// TODO - Socket - Handle DISCONNECT messages
				LOGGER.info("DISCONNECT message received");
				break;
			
		}
		
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
		
		try {
			
			if (namespace != "") {
				init(namespace);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}

		ioSocket.onOpen();
		
	}
	
	@Override
	public void onClose( int code, String reason, boolean remote ) {
		ioSocket.onClose();
		ioSocket.onDisconnect();
	}


	public void init(String path) throws IOException, InterruptedException {
		send("1::"+path);
	}
	
	public void init(String path, String query) throws IOException, InterruptedException {
		this.send("1::"+path+"?"+query);
		
	}
	public void sendMessage(IOMessage message) throws IOException, InterruptedException {
		send(message.toString());
	}
	
	public void sendMessage(String message) throws IOException, InterruptedException {
		send(new Message(message).toString());
	}

	public void setNamespace(String ns) {
		namespace = ns;
	}
	
	public String getNamespace() {
		return namespace;
	}

}
