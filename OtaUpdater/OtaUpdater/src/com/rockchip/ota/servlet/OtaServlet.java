package com.rockchip.ota.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xmlpull.v1.XmlPullParserException;

import com.rockchip.ota.log.MyLog;
import com.rockchip.ota.utils.VersionInfoItem;
import com.rockchip.ota.utils.XmlHelper;

public class OtaServlet extends HttpServlet {
	private static final String TAG = "OtaServlet";
	private static final String Version = "1.41";
	private static final String ManifastFileName = "manifast.xml";
	private static int DownLoadBufferSize = 128 * 1024;
	private static String ConfigFilePath;
	/**
	 * Constructor of the object.
	 */
	public OtaServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			
		long RangeStart = 0;
		long RangeEnd = 0;
		
		String version = request.getParameter("version");
		String product = request.getParameter("product");
		String sn = request.getParameter("sn");
		
		printReqInfo(request);
		
		if(version == null || product == null) {
			MyLog.log(TAG, "invalid request because not content system product and version!");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		MyLog.log(TAG, "product = " + product);
		MyLog.log(TAG, " version = " + version);
		MyLog.log(TAG, "sn = " + sn);
		
		/*
		String redirect = "http://200.169.104.23:2300/OtaUpdater/android?product=" + product +
				"&version=" + version;
		if(sn != null) {
			redirect = redirect + "&sn=" + sn;
		}
		response.sendRedirect(redirect);
		MyLog.log(TAG, "send redirect: " + redirect);
		return;
		*/
		
		String packagePath;
		VersionInfoItem item = findItemByVersion(product, version, sn);
		if(item == null) {
			MyLog.log(TAG, "not found update package!");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		packagePath = item.getPackagePath();
		
		String finalPath = getServletContext().getRealPath("/WEB-INF/" + packagePath);
		MyLog.log(TAG, "package file real path = " + finalPath);
		File packageFile = new File(finalPath);
		if(!packageFile.exists()) {
			MyLog.log(TAG, "PackagFile is not exists!");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String packageName = packageFile.getName();
		String packageLength = null;
		String packageVersion = null;
		RandomAccessFile srcPackageFile = null;
		String rangeSet;
		
		try {
			srcPackageFile = new RandomAccessFile(packageFile, "r");
			packageLength = String.valueOf(srcPackageFile.length());
			packageVersion = packageName.substring(0, packageName.lastIndexOf('.'));
		
			MyLog.log(TAG, "find update package! packageName = " + packageName + 
					" packageLength = " + packageLength + " packageVersion = " + packageVersion);
			
			if(isRange(request)) {
				MyLog.log(TAG, "request is contain range head!");
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				response.setHeader("Accept-Ranges", "bytes");
				rangeSet = request.getHeader("Range");
				RangeStart = getRangStart(rangeSet);
				RangeEnd = getRangEnd(rangeSet);
				MyLog.log(TAG, "this request is content range! start = " + RangeStart + " end = " + RangeEnd);
				response.setHeader("Content-Length", String.valueOf(RangeEnd - RangeStart + 1));
			}else {
				response.setStatus(HttpServletResponse.SC_OK);
				response.setHeader("Content-Length", String.valueOf(packageLength));
				RangeStart = 0;
				RangeEnd = srcPackageFile.length() - 1;
			}
			
			response.setHeader("Content-Disposition", "attachment; filename=\"" +  
	                new String(packageName.getBytes(),"ISO8859_1")+ "\"");
			response.addHeader("OtaPackageUri", request.getRequestURI() + "?product=" + product + "&version=" + version);
			response.addHeader("OtaPackageName", packageName);
			response.addHeader("OtaPackageLength", packageLength);
			response.addHeader("OtaPackageVersion", packageVersion);
			
			//send file content to client
			byte[] buffer=new byte[DownLoadBufferSize];
			long writedLength = 0;
			srcPackageFile.seek(RangeStart);
			while(writedLength < (RangeEnd - RangeStart +1)) {
				int len = srcPackageFile.read(buffer, 0, buffer.length);
				response.getOutputStream().write(buffer, 0, len);
				writedLength += len;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			MyLog.log(TAG, "PackagFile is not exists!");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			throw e; 
		}finally {
			if(srcPackageFile != null) {
				srcPackageFile.close();
			}
		}	
		
		response.getOutputStream().flush();
		response.getOutputStream().close();
	
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MyLog.log(TAG, "do post request, nothing to do.");
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String version = req.getParameter("version");
		String product = req.getParameter("product");
		String country = req.getParameter("country");
		String language = req.getParameter("language");
		String sn = req.getParameter("sn");
		
		printReqInfo(req);
		
		if(version == null || product == null) {
			MyLog.log(TAG, "invalid request because not content system product and version!");
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		MyLog.log(TAG, "product = " + product);
		MyLog.log(TAG, " version = " + version);
		MyLog.log(TAG, "sn = " + sn);
		
		/*
		String redirect = "http://200.169.104.23:2300/OtaUpdater/android?product=" + product +
				"&version=" + version;
		if(sn != null) {
			redirect = redirect + "&sn=" + sn;
		}
		
		if(country != null) {
			redirect = redirect + "&country=" + country;
		}
		
		if(language != null) {
			redirect = redirect + "&language=" + language;
		}
		
		resp.sendRedirect(redirect);
		MyLog.log(TAG, "send redirect: " + redirect);
		
		*/
		String packagePath;
		VersionInfoItem item = findItemByVersion(product, version, sn);
		if(item == null) {
			MyLog.log(TAG, "not found update package!");
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		packagePath = item.getPackagePath();
		String packageName = packagePath.substring(packagePath.lastIndexOf("/") + 1);
		String packageLength = null;
		String packageVersion = null;
		String description = null;
		RandomAccessFile srcPackageFile = null;
		boolean ifPackagePathRemote = false;
		
		if(packagePath.startsWith("http://") || packagePath.startsWith("ftp://") || packagePath.startsWith("https://")) {
			packageLength = item.getPackageLength();
			packageVersion = packageName.substring(0, packageName.lastIndexOf('.'));
			ifPackagePathRemote = true;
		}else {
			String finalPath = getServletContext().getRealPath("/WEB-INF/" + packagePath);
			MyLog.log(TAG, "package file real path = " + finalPath);
			File packageFile = new File(finalPath);
			if(!packageFile.exists()) {
				MyLog.log(TAG, "PackagFile is not exists!");
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			try {
				srcPackageFile = new RandomAccessFile(packageFile, "r");
				packageLength = String.valueOf(srcPackageFile.length());
				packageVersion = packageName.substring(0, packageName.lastIndexOf('.'));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				MyLog.log(TAG, "PackagFile is not exists!");
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} finally {
				srcPackageFile.close();
			}
		}
	
		
		try {	
			//handle the package description, according to country and language
			String descriptionPath = item.getDescriptionPath();
			if(descriptionPath != null && country != null && language != null) {
				descriptionPath = getServletContext().getRealPath("/WEB-INF/" + descriptionPath);
				description = XmlHelper.getDescription(descriptionPath, country, language);
			}
			
			MyLog.log(TAG, "find update package! packageName = " + packageName + 
					" packageLength = " + packageLength + " packageVersion = " + packageVersion 
					+ "description = " + description);
			
			if(isRange(req)) {
				MyLog.log(TAG, "request is contain range head");
				resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				resp.setHeader("Accept-Ranges", "bytes");
			}else {
				resp.setStatus(HttpServletResponse.SC_OK);
			}
			
			resp.setHeader("Content-Disposition", "attachment; filename=\"" +  
	                new String(packageName.getBytes(),"ISO8859_1")+ "\"");
			if(ifPackagePathRemote) {
				resp.addHeader("OtaPackageUri", packagePath);
			}else {
				resp.addHeader("OtaPackageUri", req.getRequestURI() + "?product=" + product + (sn != null ? ("&sn=" + sn + "&version=" + version) : ("&version=" + version)));
			}		
			resp.addHeader("OtaPackageName", packageName);
			resp.addHeader("OtaPackageLength", packageLength);
			resp.addHeader("OtaPackageVersion", packageVersion);
			
			if(description != null) {
				resp.addHeader("description", new String(description.getBytes(), "ISO8859_1"));
			}
		} catch (IOException e) {
			throw e;
		}
		
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		MyLog.log(TAG, "servlet init! version is " + Version);
		ConfigFilePath = getServletContext().getRealPath("/WEB-INF/" + ManifastFileName);
		
		if(!checkManifastXml(ConfigFilePath)) {
			MyLog.log(TAG, "have some error in manifast.xml, so can't start service!");
			throw new ServletException();
		}
	}
	
	private boolean checkManifastXml(String manifastPath) {
		File f = new File(manifastPath);
		if(!f.exists()) {
			return false;
		}
		
		boolean bl = false;
		bl = XmlHelper.checkManifast(manifastPath);
		return bl;
	}
	
	private boolean isRange(HttpServletRequest req) {
		String rangeHead = req.getHeader("Range");
		if(rangeHead == null) {
			return false;
		}
		
		if(rangeHead.contains("bytes=")) {
			return true;
		}
		
		return false;
	}

	private VersionInfoItem findItemByVersion(String product, String systemVersion, String sn) {
		HashMap<String, VersionInfoItem> versionList;
		versionList = XmlHelper.getVersionList(ConfigFilePath, product, sn);
		
		if(versionList != null) {
			return versionList.get(systemVersion);
		}
		
		return null;
	}
	
	private long getRangStart(String input) {
		return Long.valueOf(input.substring(input.indexOf('=') + 1, input.indexOf('-')));
	}
	
	private long getRangEnd(String input) {
		return Long.valueOf(input.substring(input.indexOf('-') + 1));
	}
	
	private void printReqInfo(HttpServletRequest req) {
		MyLog.log(TAG, "request method: " + req.getMethod());
		MyLog.log(TAG, "remote ip addr: " + req.getRemoteAddr());
		MyLog.log(TAG, "remote real addr: " + getIpAddr(req));
		MyLog.log(TAG, "request uri: " + req.getRequestURI());
	}
	
	public String getIpAddr(HttpServletRequest request) {   
		String ip = request.getHeader("x-forwarded-for");   
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("Proxy-Client-IP");  
		}  
		
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getHeader("WL-Proxy-Client-IP");  
		}   
		
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {   
			ip = request.getRemoteAddr();  
		}  
		
		return ip;  
	}   
}
