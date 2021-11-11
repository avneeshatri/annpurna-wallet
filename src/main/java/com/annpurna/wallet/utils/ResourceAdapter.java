package com.annpurna.wallet.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceAdapter {
	private Properties props ;

	private ResourceAdapter(String filename) {
		loadProperties(filename);
	}
	public static ResourceAdapter getInstance(String filename) {
		return new ResourceAdapter(filename);
	}
	private void loadProperties(String filename) {

		try (InputStream in = ResourceAdapter.class.getClassLoader().getResourceAsStream(filename)) {
			if(props == null) {
				props = new Properties();
			}
			props.load(in);
		} catch (IOException e) {
			throw new IllegalArgumentException(e) ;
		}
		
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
}
