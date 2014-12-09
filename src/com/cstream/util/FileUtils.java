package com.cstream.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.codec.digest.DigestUtils;

public class FileUtils {
	
	private static Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
	
	public static ZipFile unzip(InputStream is, File f) throws IOException, ZipException {
		
		BufferedInputStream input = new BufferedInputStream(is);
		BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(f));
		
		int inByte;
		while ((inByte = input.read()) != -1) { 
			output.write(inByte);
		}

		output.close(); 
		input.close();
		
		ZipFile zip = new ZipFile(f);
		return zip;
		
	}
	
	public static File getFile(String path) {
		
		File f = new File(path);
		makeDirectory(f.getParentFile().getAbsolutePath());
		return f;
		
	}
	
	public static File makeDirectory(String path) {
		
		File directory = new File(path);
        if (!directory.exists()) {
        	LOGGER.info("Creating directory: " + path);
        	directory.mkdir();
        }
        
        return directory;		
		
	}
	
	public static String generateMd5(String pathToFile) {
		
		try (FileInputStream fis = new FileInputStream(new File(pathToFile)) ){
			return DigestUtils.md5Hex(fis);
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
    public static List<File> listFiles(String directoryName, String[] extensions) {
    	
    	List<File> results = new ArrayList<File>();    	
    	
    	if (directoryName == null) {
        	LOGGER.warning("No directory name provided");
        	return results;    		
    	}
    	
        File directory = new File(directoryName);
        
        File[] fileList = directory.listFiles();
        if (fileList == null) {
        	LOGGER.warning("Cannot list files of a directory which does not exist - " + directoryName);
        	return results;
        }
        
        for (File file : fileList) {
        	
        	if (file.isDirectory()) {
        		
        		// Recursively add files within sub-directories
        		results.addAll(listFiles(file.getAbsolutePath(), extensions));
        		
        	} else {
        		
        		if (extensions == null) {
        			results.add(file);
        			continue;
        		}
        		
        		if (validExtension(file, extensions)) {
        			results.add(file);
        		}
        	}
        	
        }
        
        return results;
        
    }
    
    private static boolean validExtension(File file, String[] extensions) {
    	
    	boolean valid = false;
    	for (String extension : extensions) {
			
			if (file.getName().endsWith(extension) || file.getName().endsWith("." + extension)) {
				valid = true;
				break;
			}
			
		}
    	
    	return valid;
    	
    }

}
