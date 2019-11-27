package com.workiva.platform;

import com.workiva.platform.core.PlatformCore;

public class Platform extends PlatformCore {

	private ServletContextHandler servletContext;

	public Platform(ServletContextHandler servletContext) {
		Platform.servletContext = servletContext;
	}
	
}
