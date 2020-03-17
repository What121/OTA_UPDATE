package com.rockchip.ota.log;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.*;

public class Log4j extends HttpServlet {

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		String basePath = getServletContext().getRealPath("/");
		String confFile = getInitParameter("log4j");
		if (confFile!=null) {
			PropertyConfigurator.configure(basePath+confFile);
		}else{
			System.out.println("load log4j config file error!");
		}
	}

}
