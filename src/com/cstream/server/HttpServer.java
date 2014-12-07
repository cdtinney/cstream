package com.cstream.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class HttpServer {
	
	private static final int BUFFER_SIZE = 65536;
	private static final int PORT = 8080;
	
	private Server server;
	
	public HttpServer() {
		
		server = new Server(PORT);
 
        ContextHandler context = new ContextHandler();
        context.setContextPath("/*");
        context.setResourceBase(".");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        context.setHandler(new TransferHandler());
        
        server.setHandler(context);
        
        start();
		
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
			
			response.getWriter().println("test response");
			
			// TODO - Ignore based on user-agent? How?
//			System.out.println("request for: " + request.getHeader("User-Agent"));
//			
//			response.setBufferSize(BUFFER_SIZE);
//	        OutputStream outStream = response.getOutputStream();
//
//	        try (FileInputStream stream = new FileInputStream(new File("C:\\output.mp3"))) {
//	        	
//	            int bytesRead;
//	            byte[] buffer = new byte[BUFFER_SIZE];
//	            
//	            while( (bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) > 0 ) {
//	                outStream.write(buffer, 0, bytesRead);
//	                outStream.flush();
//	            }
//	            
//	        } finally   {
//	            outStream.close();
//	            
//	        }
	        
	    }
		
	}

}
