package com.ttorrent.tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cstream.util.OSUtils;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

public class TorrentTracker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentTracker.class);
	
	private static final String TORRENT_DIR = System.getProperty("user.home") + (OSUtils.isWindows() ? "\\cstream-tracker\\" : "/cstream-tracker/");
	
	private static final int TRACKER_PORT = 6969;
	private Tracker tracker;
	
	private static final int HTTP_PORT = 6970;
	private Server httpServer;
	
	public TorrentTracker() { }
	
	public void start() {
		
		try {
			
			tracker = new Tracker(new InetSocketAddress(TRACKER_PORT));
			
			// Announce all of the torrents we're already tracking
			announceAll();
			
			tracker.start();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		initHttpServer();
		
	}
	
	public void announceAll() {
		
		for (TrackedTorrent torrent : loadTrackedTorrents()) {
			tracker.announce(torrent);			
		}
		
	}
	
	private List<TrackedTorrent> loadTrackedTorrents() {
		
		FilenameFilter filter = (file, name) -> {
		    return name.endsWith(".torrent");
		};

		List<TrackedTorrent> tracked = new ArrayList<TrackedTorrent>();
		for (File f : new File(TORRENT_DIR).listFiles(filter)) {
			
			try {
				tracked.add(TrackedTorrent.load(f));
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
		}
		
		return tracked;
		
	}
	
	private void initHttpServer() {
		
		httpServer = new Server(HTTP_PORT);
 
		// Set up the POST handler for uploading torrents
        ContextHandler context = new ContextHandler();
        context.setContextPath("/upload");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setHandler(new TorrentUploadHandler());
        httpServer.setHandler(context);
        
        // Create the directory to store torrents if it doesn't exist
        File directory = new File(TORRENT_DIR);
        if (!directory.exists()) {
        	LOGGER.info("Creating directory to store torrents: " + TORRENT_DIR);
        	directory.mkdir();
        }
        
		// Start the HTTP server on a new thread
		new Thread(() -> {
			
			try {
				httpServer.start();
				httpServer.join();
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
		}).start();
		
	}
	
	public class TorrentUploadHandler extends HandlerWrapper {

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			
			LOGGER.info("POST - torrent upload request received");
			
			response.setContentType("text/html;charset=utf-8");
			baseRequest.setHandled(true);
			
			String agent = request.getHeader("User-Agent");
			if (!agent.contains("cstream")) {
	        	LOGGER.warn("Cannot process GET from applications other than cstream");
	        	return;
			}
			
			String filename = request.getHeader("filename");
			if (filename == null) {
				LOGGER.warn("Request header did not contain a valid filename");
				return;
			}
			
			LOGGER.info("Attemping to upload .torrent: " + filename);
			
			try {
				
				InputStream input = request.getInputStream();
				byte[] bytes = IOUtils.toByteArray(input);
				
				// Attempt to create a new TrackedTorrent object
				TrackedTorrent torrent = new TrackedTorrent(bytes);
				
				// Announce it!
				TrackedTorrent existing = tracker.announce(torrent);
				
				// If the objects are not equal, we're already tracking this torrent. 
				if (torrent != existing) {
					LOGGER.info("Torrent uploaded is already being tracked: " + torrent.getName());
					
				// Otherwise, it's new, so write it out to file
				} else {
					
					File file = new File(TORRENT_DIR + filename + ".torrent");	
					OutputStream output = new FileOutputStream(file);
					torrent.save(output);
					
					LOGGER.info("Torrent uploaded: " + file.getName());
					
				}

				// Send an OK response
				response.setStatus(HttpServletResponse.SC_OK);
				return;
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
	    }
		
	}

	public static void main(String[] args) {
		
		TorrentTracker tracker = new TorrentTracker();
		tracker.start();
		
	}

}
