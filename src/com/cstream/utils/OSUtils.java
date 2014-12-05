package com.cstream.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * OSUtils contains a collection of functions related to OS functionality.
 *
 * @author Ben Sweett
 * @version 1.0
 * @since 2014-09-30
 */
public class OSUtils {
	
	private static String OS = System.getProperty("os.name").toLowerCase().trim().replace(" ", "");

	public static String getLocalIp() {
		
		try {
			InetAddress ip = InetAddress.getLocalHost();
			return ip.getHostAddress();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	public static String generateUserId() {

		String name = System.getProperty("user.name");

//		String os = "unknown";
//		if (isMac()) {
//			os = "mac";
//		} else if (isWindows()) {
//			os = "windows";
//		} else if (isUnix()) {
//			os = "unix";
//		}
//		
		return name + "_" + OS;
		
	}
	
	public static boolean isWindows() {		 
		return (OS.indexOf("win") >= 0);
	}
	
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
	
	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}
	
}
