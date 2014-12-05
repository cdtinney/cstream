package com.cstream.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IOSocket {
	
	private IOWebSocket webSocket;
	private URL connection;
	private String sessionID;
	private int heartTimeOut;
	private int closingTimeout;
	private String[] protocals;
	private String webSocketAddress;
	private MessageCallback callback;
	
	private boolean connecting;
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

		// Perform handshake
		String url = webSocketAddress.replace("ws://", "https://");
		URL connection = new URL(url+"/socket.io/1/"); //handshake url
		InputStream stream = connection.openStream();
		Scanner in = new Scanner(stream);
		String response = in.nextLine(); //pull the response
		System.out.println(response);
		in.close();
		
		// process handshake response
		// example: 4d4f185e96a7b:15:10:websocket,xhr-polling
		if (response.contains(":")) {
			String[] data = response.split(":");
			setSessionID(data[0]);
			setHeartTimeOut(Integer.parseInt(data[1]));
			setClosingTimeout(Integer.parseInt(data[2]));
			setProtocals(data[3].split(","));
		}
		
		connecting = true;
		webSocket = new IOWebSocket(URI.create(webSocketAddress + "/socket.io/1/websocket/"+ sessionID), this, callback);
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
	

	public void send(String endpoint, JSONObject message) throws IOException, InterruptedException {
		
		IOMessage packet = new IOMessage(IOMessage.JSONMSG, webSocket.getNamespace(), message.toString());
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
			connecting = false;
			
			callback.onConnect();
		}
		
	}
	
	synchronized void onDisconnect() {
		
		boolean wasConnected = connected;
		
		connected = false;
		connecting = false;
		
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


	public void setProtocals(String[] protocals) {
		this.protocals = protocals;
	}


	public String[] getProtocals() {
		return protocals;
	}

}
