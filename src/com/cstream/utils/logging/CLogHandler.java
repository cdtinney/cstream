package com.cstream.utils.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.cstream.media.MediaBarView;

public class CLogHandler extends Handler {
	
	private static MediaBarView view;
	
	public static void setHandler(Logger logger) {
	
		Logger parent = logger.getParent();
		
		for (Handler handler: parent.getHandlers()) {
			parent.removeHandler(handler);
		}
		
		CLogHandler customHandler = new CLogHandler();
		customHandler.setFormatter(new LogFormatter());
		customHandler.setLevel(LogLevel.TRACE);	
		
		parent.setLevel(LogLevel.DEBUG);
		parent.addHandler(customHandler);
		
	}
	
	public static void setView(MediaBarView view) {
		CLogHandler.view = view;
	}

	@Override
	public void publish(LogRecord r) {
		
		if (r.getLevel() == LogLevel.STATUS && view != null) {
			view.setStatusText(r.getMessage());
		}
		
		// Ignore FINE log messages - these are generated by JavaFX
		if (r.getLevel() == LogLevel.FINE) {
			return;
		}
		
		System.out.println(getFormatter().format(r));
		
	}

	@Override
	public void close() throws SecurityException {
		// Do nothing
	}

	@Override
	public void flush() {
		// Do nothing
	}
	
}
