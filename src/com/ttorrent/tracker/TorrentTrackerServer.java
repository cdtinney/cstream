package com.ttorrent.tracker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import com.cstream.torrent.TorrentManager;
import com.cstream.tracker.TrackerClient;

public class TorrentTrackerServer {

	private static Logger LOGGER = Logger.getLogger(TorrentTrackerServer.class.getName());
	
	private static final int BUFFER_SIZE = 65536;
	private static final int PORT = 6970;
	
	private Server server;
	
	private TrackerClient client;
	
	public TorrentTrackerServer() {
		
		server = new Server(PORT);
 
        ContextHandler context = new ContextHandler();
        context.setContextPath("/*");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        context.setHandler(new TorrentTransferHandler());
        
        server.setHandler(context);
        
        start();
		
	}
	
	public TorrentTrackerServer(TrackerClient client) {
		this();
		
		this.client = client;
	}
	
	private void start() {
		
		new Thread(() -> {
			
			try {
				server.start();
				server.join();
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
		}).start();
		
	}
	
	public class TorrentTransferHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			
			LOGGER.info("Torrent POST requested received");
			
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			String agent = request.getHeader("User-Agent");
			if (!agent.contains("cstream")) {
	        	response.getWriter().println("Cannot process GET from applications other than cstream");
	        	return;
			}
	        
	        if (client == null) {
	        	LOGGER.warning("Client is null");
	        	response.getWriter().println("Client is null");
	        	return;
	        }
	        
			String songId = request.getHeader("songId");
	        String fileName = client.findSongPathById(songId);
	        
	        LOGGER.info("Request for song: " + songId + " received");
	        
	        if (fileName == null) {
	        	LOGGER.warning("Could not find requested song: " + songId);
	        	response.getWriter().println("Error - could not find song: " + songId);
	        	return;
	        }
	        
	        response.setHeader("filename", fileName);
			
			response.setBufferSize(BUFFER_SIZE);
	        OutputStream outStream = response.getOutputStream();

	        try (FileInputStream stream = new FileInputStream(TorrentManager.createTorrentFile(fileName))) {
	        	
	            int bytesRead;
	            byte[] buffer = new byte[BUFFER_SIZE];
	            
	            while( (bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) > 0 ) {
	                outStream.write(buffer, 0, bytesRead);
	                outStream.flush();
	            }
	            
	        } finally   {
	            outStream.close();
	            
	        }
	        
	    }
		
	}

}
