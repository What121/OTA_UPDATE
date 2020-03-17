package com.rockchip.ota.log;

import org.apache.log4j.*;

public class MyLog {
	private static Logger logger;
	private static boolean DBUG = true;
	
	public static void log(String tag, String content) {
		if(DBUG) {
			getLogger().info(tag + ": " + content);
		}
	}
	
	private static Logger getLogger() {
		if(logger == null) {
			logger = Logger.getLogger("OTA");
		}
		
		return logger;
	}
}
