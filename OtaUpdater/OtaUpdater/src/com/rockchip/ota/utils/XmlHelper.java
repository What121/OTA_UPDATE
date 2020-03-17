package com.rockchip.ota.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.rockchip.ota.log.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XmlHelper {
	private static String TAG = "XmlHelper";
	
	public static HashMap<String, VersionInfoItem> getVersionList(String manifastPath, String product, String sn) {
		FileInputStream fin = null;
		File f;
		
		try {
			f = new File(manifastPath);
			fin = new FileInputStream(f);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = factory.newPullParser();    
			xmlPullParser.setInput(fin, "UTF-8"); 
			HashMap<String, VersionInfoItem> versionItemList = new HashMap<String, VersionInfoItem>();
			
			int eventType = xmlPullParser.getEventType(); 
			boolean start = false;
			while (eventType != XmlPullParser.END_DOCUMENT) { 
				String tag = xmlPullParser.getName();
				switch (eventType) {    
	            case XmlPullParser.START_DOCUMENT:    
	                break;    
	            case XmlPullParser.START_TAG:
	            	if(tag.equals("product")) {
	            		String productAttr = xmlPullParser.getAttributeValue(0);
	            		String target; 
	            		if(productAttr.contains("/") && sn != null) {
	            			target = product + "/" + sn.substring(sn.length()-1, sn.length());
	            		}else {
	            			target = product;
	            		}
	            
	            		if(productAttr.equals(target)) {
	            			start = true;
	            		}
	            	}else if(tag.equals("version")) {
	            		if(start) {
	            			VersionInfoItem versionItem = new VersionInfoItem();
	            			versionItem.setPackagePath(xmlPullParser.getAttributeValue(1));
	            			if(xmlPullParser.getAttributeCount() == 3) {
	            				if(xmlPullParser.getAttributeName(2).equals("package_size")) {
	            					versionItem.setPackageLength(xmlPullParser.getAttributeValue(2));
	            					versionItem.setDescriptionPath(null);
	            				}else {
	            					versionItem.setDescriptionPath(xmlPullParser.getAttributeValue(2));
	            					versionItem.setPackageLength(null);
	            				}
	            				
	            			}else if(xmlPullParser.getAttributeCount() == 4){
	            				versionItem.setDescriptionPath(xmlPullParser.getAttributeValue(3));
	            				versionItem.setPackageLength(xmlPullParser.getAttributeValue(2));
	            			}else {
	            				versionItem.setDescriptionPath(null);
	            				versionItem.setPackageLength(null);
	            			}
	            			versionItemList.put(xmlPullParser.getAttributeValue(0), versionItem);
	            		}
	            	}
	            	break;    
	            case XmlPullParser.END_TAG:    
	            	if(tag.equals("product") && start) {
	            		return versionItemList;
	            	}
	                break;    
	            }    
	            eventType = xmlPullParser.next();
			}
		}catch (XmlPullParserException e) {
			MyLog.log(TAG, "catch XmlPullParserException error!!");
			return null;
		} catch (IOException e) {
			MyLog.log(TAG, "catch IOException error!!");
			return null;
		}finally {
			try {
				fin.close();
			} catch (IOException e) {
				MyLog.log(TAG, "close FileInputStream error!!");
			}
		}
		
		return null;
	}
	
	public static String getRkImagePath(String manifastPath, String product) {
		FileInputStream fin = null;
		File f;
		
		try {
			f = new File(manifastPath);
			fin = new FileInputStream(f);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = factory.newPullParser();    
			xmlPullParser.setInput(fin, "UTF-8"); 
			String rkImagePath = null;
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) { 
				String tag = xmlPullParser.getName();
				switch (eventType) {    
	            case XmlPullParser.START_DOCUMENT:    
	                break;    
	            case XmlPullParser.START_TAG:
	            	if(tag.equals("product")) {
	            		if(xmlPullParser.getAttributeValue(0).equals(product)) {
	            			for(int i = 0; i < xmlPullParser.getAttributeCount(); i++) {
	            				if(xmlPullParser.getAttributeName(i).equals("rkimage_path")) {
	            					rkImagePath = xmlPullParser.getAttributeValue(i);
	            					MyLog.log(TAG, "find rkImagePath = " + rkImagePath);
	            					return rkImagePath;
	            				}
	            			}
	            		}
	            	}
	            	break;    
	            case XmlPullParser.END_TAG:    
	                break;    
	            }    
	            eventType = xmlPullParser.next();
			}
		}catch (XmlPullParserException e) {
			MyLog.log(TAG, "catch XmlPullParserException error!!");
			return null;
		} catch (IOException e) {
			MyLog.log(TAG, "catch IOException error!!");
			return null;
		}finally {
			try {
				fin.close();
			} catch (IOException e) {
				MyLog.log(TAG, "close FileInputStream error!!");
			}
		}
		
		return null;
	}
	
	public static String getFullPackagePath(String manifastPath, String product) {
		FileInputStream fin = null;
		File f;
		
		try {
			f = new File(manifastPath);
			fin = new FileInputStream(f);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = factory.newPullParser();    
			xmlPullParser.setInput(fin, "UTF-8"); 
			String fullPackagePath = null;
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) { 
				String tag = xmlPullParser.getName();
				switch (eventType) {    
	            case XmlPullParser.START_DOCUMENT:    
	                break;    
	            case XmlPullParser.START_TAG:
	            	if(tag.equals("product")) {
	            		if(xmlPullParser.getAttributeValue(0).equals(product)) {
	            			for(int i = 0; i < xmlPullParser.getAttributeCount(); i++) {
	            				if(xmlPullParser.getAttributeName(i).equals("full_package_path")) {
	            					fullPackagePath = xmlPullParser.getAttributeValue(i);
	            					MyLog.log(TAG, "find fullpackagepath = " + fullPackagePath);
	            					return fullPackagePath;
	            				}
	            			}
	            		}
	            	}
	            	break;    
	            case XmlPullParser.END_TAG:    
	                break;    
	            }    
	            eventType = xmlPullParser.next();
			}
		}catch (XmlPullParserException e) {
			MyLog.log(TAG, "catch XmlPullParserException error!!");
			return null;
		} catch (IOException e) {
			MyLog.log(TAG, "catch IOException error!!");
			return null;
		}finally {
			try {
				fin.close();
			} catch (IOException e) {
				MyLog.log(TAG, "close FileInputStream error!!");
			}
		}
		
		return null;
	}
	
	public static boolean checkManifast(String manifastPath) {
		FileInputStream fin = null;
		File f;
		
		try {
			f = new File(manifastPath);
			fin = new FileInputStream(f);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = factory.newPullParser();    
			xmlPullParser.setInput(fin, "UTF-8");
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) { 
				String tag = xmlPullParser.getName();
				//MyLog.log(TAG, "tag => " + tag);
				switch (eventType) {    
	            case XmlPullParser.START_DOCUMENT:    
	                break;    
	            case XmlPullParser.START_TAG:
	            	if(tag.equals("product")) {
	            		if(!xmlPullParser.getAttributeName(0).equals("name") ||
	            				!xmlPullParser.getAttributeName(1).equals("full_package_path") ||
	            				!xmlPullParser.getAttributeName(2).equals("rkimage_path")) {
	            			MyLog.log(TAG, "manifast product tag format is not correct!");
	            			return false;
	            		}
	            	}
	            	if(tag.equals("version")) {
	            		if(!xmlPullParser.getAttributeName(0).equals("name") ||
	            				!xmlPullParser.getAttributeName(1).equals("package_path")) {
	            			MyLog.log(TAG, "manifast version tag format is not correct!");
	            			return false;
	            		}
	            	}
	            	break;    
	            case XmlPullParser.END_TAG:    
	                break;    
	            }    
	            eventType = xmlPullParser.next();
			}
		}catch (XmlPullParserException e) {
			MyLog.log(TAG, "catch XmlPullParserException error!!");
			return false;
		} catch (IOException e) {
			MyLog.log(TAG, "catch IOException error!!");
			return false;
		}finally {
			try {
				fin.close();
			} catch (IOException e) {
				MyLog.log(TAG, "close FileInputStream error!!");
			}
		}
		
		return true;
	}
	
	public static String getDescription(String descriptionPath, String country, String language) {
		FileInputStream fin = null;
		String result = null;
		File f;
		
		try {
			f = new File(descriptionPath);
			fin = new FileInputStream(f);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xmlPullParser = factory.newPullParser();    
			xmlPullParser.setInput(fin, "UTF-8"); 
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) { 
				String tag = xmlPullParser.getName();
				switch (eventType) {    
	            case XmlPullParser.START_DOCUMENT:    
	                break;    
	            case XmlPullParser.START_TAG:
	            	if(tag.equals("Local")) {
	            		if((xmlPullParser.getAttributeValue(0).equals(country) 
	            				&& xmlPullParser.getAttributeValue(1).equals(language)) 
	            				|| xmlPullParser.getAttributeValue(0).equals("others")) {
	            			result = xmlPullParser.nextText();
	            			return result;
	            		}
	            	}
	            	break;    
	            case XmlPullParser.END_TAG:    
	                break;    
	            }    
	            eventType = xmlPullParser.next();
			}
		}catch (XmlPullParserException e) {
			MyLog.log(TAG, "catch XmlPullParserException error!!");
			result = null;
		} catch (IOException e) {
			MyLog.log(TAG, "catch IOException error!!");
			result = null;
		}finally {
			try {
				fin.close();
			} catch (IOException e) {
				MyLog.log(TAG, "close FileInputStream error!!");
			}
		}
			
		return result;
	}
}
