package com.cstream.socket.io;

public class Message extends IOMessage{
	
	public Message(String message){
		super(IOMessage.MESSAGE, -1, "", message);
	}
}
