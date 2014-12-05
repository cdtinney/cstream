package com.cstream.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cstream.logging.LogLevel;

public class IOSocket {

	private static Logger LOGGER = Logger.getLogger(IOSocket.class.getName());
	
	private final static String SOCKET_IO_NS = "/socket.io/";
	private final static String WEBSOCKET_NS = "websocket/";
	private final static String IO_1 = "1/";
	
	private final static String KEEP_ALIVE = "KEEP_ALIVE";
	
	private IOWebSocket webSocket;
	private String webSocketAddress;
	private URL connection;
	private String sessionID;
	
	// Timeouts are in seconds
	private int heartTimeOut;
	private int closingTimeout;
	
	// Transport types supported by the socket.io server
	private String[] transports;
	
	private MessageCallback callback;
	
	private boolean connected;
	private boolean open;
	
	public IOSocket(String address, MessageCallback callback){
		webSocketAddress = address;
		this.callback = callback;
	}
	
	public void connect() throws IOException {
		
		// Check for socket.io name space
		String namespace = "";
		int i = webSocketAddress.lastIndexOf("/");
		if (webSocketAddress.charAt(i-1) != '/') {
			namespace = webSocketAddress.substring(i);
			webSocketAddress = webSocketAddress.substring(0, i);
		}

		// Perform handshake - we want to use an http/https URL for the handshake rather than a websocket URL
		String url = webSocketAddress.replace("ws://", "https://");
		URL connection = new URL(url + SOCKET_IO_NS + IO_1); 
		InputStream stream = connection.openStream();
		Scanner in = new Scanner(stream);
		String response = in.nextLine(); 
		in.close();
		
		LOGGER.log(LogLevel.DEBUG, "Socket server returned handshake response: " + response);
		
		// Process handshake response
		// example: 4d4f185e96a7b:15:10:websocket,xhr-polling
		// format: sessionID:heartbeatTimeout:closeTimeout:[supportedTransports]
		if (response.contains(":")) {
			String[] data = response.split(":");
			setSessionID(data[0]);
			setHeartTimeOut(Integer.parseInt(data[1]));
			setClosingTimeout(Integer.parseInt(data[2]));
			setTransports(data[3].split(","));
		}
		
		
		String fullWsAddress = webSocketAddress + SOCKET_IO_NS + IO_1 + WEBSOCKET_NS + sessionID;
		LOGGER.log(LogLevel.DEBUG, "Attemping to connect to websocket: " + fullWsAddress);
		
		webSocket = new IOWebSocket(URI.create(fullWsAddress), this, callback);
		webSocket.setNamespace(namespace);
		webSocket.connect();
		
	}
	
	public void emit(String event, JSONObject... message) throws IOException, InterruptedException {
		
		try {
			
			JSONObject data = new JSONObject();
			JSONArray args = new JSONArray();
			
			for (JSONObject arg : message) {
				args.put(arg);
			}
			
			data.put("name", event);
			data.put("args", args);
			IOMessage packet = new IOMessage(IOMessage.EVENT, webSocket.getNamespace(), data.toString());
			webSocket.sendMessage(packet);
			
		} catch (JSONException e) {
			e.printStackTrace();
			
		}
		
	}

	public void sendKeepAlive(String id) throws IOException, InterruptedException {

		try {
			JSONObject obj = new JSONObject("{type: " + KEEP_ALIVE + ", id: " + id + " }");
			IOMessage packet = new IOMessage(IOMessage.JSONMSG, webSocket.getNamespace(), obj.toString());
			webSocket.sendMessage(packet);
			
		} catch (JSONException e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void send(String endpoint, JSONObject obj) throws IOException, InterruptedException {
		
		IOMessage packet = new IOMessage(IOMessage.JSONMSG, webSocket.getNamespace(), obj.toString());
		webSocket.sendMessage(packet);
		
	}
	
	public void send(String message) throws IOException, InterruptedException {
		
		IOMessage packet = new IOMessage(IOMessage.MESSAGE, webSocket.getNamespace(), message);
		webSocket.sendMessage(packet);
		
	}
	
	public synchronized void disconnect() {
		
		if (connected) {
			
			try {
				
				if (open) {
					webSocket.sendMessage(new IOMessage(IOMessage.DISCONNECT, webSocket.getNamespace(), ""));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			onDisconnect();
		}	
		
	}

	synchronized void onOpen() {
		open = true;
	}
	
	synchronized void onClose() {
		open = false;
	}
	
	synchronized void onConnect() {
		
		if (!connected) {
			connected = true;
			callback.onConnect();
		}
		
	}
	
	synchronized void onDisconnect() {
		
		// If we were previously connected, we may want to re-connect
		boolean wasConnected = connected;
		
		connected = false;
		
		if (open) {
			
			try {
				webSocket.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
		}
		
		if (wasConnected) {
			callback.onDisconnect();
			
			// TODO: Attempt to reconnect for a specified duration
			
		}
	}

	public void setConnection(URL connection) {
		this.connection = connection;
	}

	public URL getConnection() {
		return connection;
	}

	public void setHeartTimeOut(int heartTimeOut) {
		this.heartTimeOut = heartTimeOut;
	}

	public int getHeartTimeOut() {
		return heartTimeOut;
	}

	public void setClosingTimeout(int closingTimeout) {
		this.closingTimeout = closingTimeout;
	}

	public int getClosingTimeout() {
		return closingTimeout;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setTransports(String[] transports) {
		this.transports = transports;
	}

	public String[] getTransports() {
		return transports;
	}

}
