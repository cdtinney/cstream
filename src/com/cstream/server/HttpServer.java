package com.cstream.server;

import java.io.File;
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

import com.cstream.tracker.TrackerClient;
import com.cstream.util.OSUtils;

public class HttpServer {

	private static Logger LOGGER = Logger.getLogger(HttpServer.class.getName());

	private final static String DEFAULT_BASE_DIR = System.getProperty("user.home");
	
	private static final int BUFFER_SIZE = 65536;
	private static final int PORT = 8080;
	
	private static String TORRENT_DIR = "";
	
	private Server server;
	
	private TrackerClient client;
	
	public HttpServer() {
		
		TORRENT_DIR = DEFAULT_BASE_DIR + (OSUtils.isWindows() ? "\\cstream\\torrent\\" : "/cstream/torrent/");
		
		server = new Server(PORT);
 
        ContextHandler context = new ContextHandler();
        context.setContextPath("/*");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        context.setHandler(new TransferHandler());
        
        server.setHandler(context);
        
        start();
		
	}
	
	public HttpServer(TrackerClient client) {
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
	
	public class TransferHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			// TODO - Ignore based on user-agent? How?
			System.out.println("request for: " + request.getHeader("User-Agent"));
			
			String songId = request.getHeader("songId");
			
			response.setBufferSize(BUFFER_SIZE);
	        OutputStream outStream = response.getOutputStream();
	        
	        String fileName = client.findSongPathById(songId);
	        if (fileName == null) {
	        	LOGGER.warning("Could not find requested song: " + songId);
	        	response.getWriter().println("Error - could not find song: " + songId);
	        	return;
	        }

	        try (FileInputStream stream = new FileInputStream(new File(TORRENT_DIR + fileName + ".torrent"))) {
	        	
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
