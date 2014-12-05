package com.cstream.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

public class FileUtils {
	
	private static Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
	
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
