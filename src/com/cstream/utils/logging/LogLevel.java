package com.cstream.utils.logging;

import java.util.logging.Level;

public class LogLevel extends Level {

	private static final long serialVersionUID = -4654239157971525597L;
	public static final Level TRACE = new LogLevel("TRACE", Level.FINE.intValue() + 1);
	public static final Level STATUS = new LogLevel("STATUS", Level.INFO.intValue() + 1);
	public static final Level DEBUG = new LogLevel("DEBUG", Level.FINER.intValue() + 1);
	
	public LogLevel(String name, int value) {
		super(name, value);
	}

}
