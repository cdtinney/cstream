package com.ttorrent.tracker;

import java.io.File;
import java.io.FileInputStream;
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

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cstream.util.FileUtils;
import com.cstream.util.OSUtils;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

public class TorrentTracker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentTracker.class);
	
	private static final String TORRENT_DIR = System.getProperty("user.home") + (OSUtils.isWindows() ? "\\cstream-tracker\\" : "/cstream-tracker/");
	private static final int BUFFER_SIZE = 65536;
	
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
 
		// Set up the POST handler for uploading .torrent files
        ContextHandler uploadContext = new ContextHandler();
        uploadContext.setContextPath("/upload");
        uploadContext.setResourceBase(".");
        uploadContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        uploadContext.setHandler(new TorrentUploadHandler());
        //httpServer.setHandler(uploadContext);
        
        // Set up the GET handler for downloading .torrent files
        ContextHandler downloadContext = new ContextHandler();
        downloadContext.setContextPath("/download");
        downloadContext.setResourceBase(".");
        downloadContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        downloadContext.setHandler(new TorrentDownloadHandler());
        //httpServer.setHandler(downloadContext);
        
        ContextHandlerCollection c = new ContextHandlerCollection();
        c.setHandlers(new ContextHandler [] { downloadContext, uploadContext });
        httpServer.setHandler(c);
        
        // Get the directory to store torrents. If it doesn't exist, it will be created.
        FileUtils.makeDirectory(TORRENT_DIR);
        
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

			LOGGER.info("POST - torrent upload request received from: " + request.getLocalAddr() + ":" + request.getLocalPort());
			
			response.setContentType("text/html;charset=utf-8");
			baseRequest.setHandled(true);
			
			String agent = request.getHeader("User-Agent");
			if (!agent.contains("cstream")) {
	        	LOGGER.warn("Cannot process GET from applications other than cstream");
	        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	        	return;
			}
			
			String filename = request.getHeader("filename");
			if (filename == null) {
				LOGGER.warn("Request header did not contain a valid filename");
	        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
	
	public class TorrentDownloadHandler extends HandlerWrapper {

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			
			LOGGER.info("GET - torrent download request received from: " + request.getLocalAddr() + ":" + request.getLocalPort());
			baseRequest.setHandled(true);
			
			// Ignore request from agents other than our own application
			String agent = request.getHeader("User-Agent");
			if (!agent.contains("cstream")) {
	        	LOGGER.warn("Cannot process GET from applications other than cstream");
	        	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	        	return;
			}
			
			response.setContentType("application/zip");
			
			try {
				
				if (!archiveTorrents()) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}
				
		        OutputStream outStream = response.getOutputStream();
		        try (FileInputStream stream = new FileInputStream(new File(TORRENT_DIR + "\\torrents.zip"))) {
		        	
		            int bytesRead;
		            byte[] buffer = new byte[BUFFER_SIZE];
		            
		            while( (bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) > 0 ) {
		                outStream.write(buffer, 0, bytesRead);
		                outStream.flush();
		            }
		            
		        } finally   {
		            outStream.close();
		            
		        }

				// Send an OK response
				response.setStatus(HttpServletResponse.SC_OK);
				return;
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			
	    }
		
		public boolean archiveTorrents() {
			
			try {
				
				// This will overwrite the current .zip file
				ZipFile zip = new ZipFile(TORRENT_DIR + "torrents.zip");
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
				
				ArrayList<File> torrentFiles = new ArrayList<File>(FileUtils.listFiles(TORRENT_DIR, new String[] {".torrent"}));
				
				LOGGER.info("Archiving " + torrentFiles.size() + " torrent files: ");
				for (int i=0; i<torrentFiles.size(); i++) {
					LOGGER.info("\t" + (i + 1) + ". " + torrentFiles.get(i).getName());					
				}
				
				zip.addFiles(torrentFiles, parameters);
				LOGGER.info("Archive created successfully: " + zip.getFile().getAbsolutePath());
				
				return true;
				
			} catch (ZipException e) {
				e.printStackTrace();
				
			}
			
			return false;
			
		}
		
	}

	public static void main(String[] args) {
		
		TorrentTracker tracker = new TorrentTracker();
		tracker.start();
		
	}

}
