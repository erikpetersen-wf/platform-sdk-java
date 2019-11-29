package com.workiva.platform.servlet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.workiva.platform.core.PlatformCore;

import java.util.List;

public class Platform extends PlatformCore {

  static final String PATH_PREFIX = "/_wk";

  /**
   * Takes a servlet context handler and registers health check endpoints.
   *
   * @param servletContext Context from the Jetty server.
   */
  public void registerEndpoints(ServletContextHandler servletContext) {
    List<String> endpointPaths = Utils.stripPrefix(servletContext.getContextPath());

    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::ready)), endpointPaths.get(0));
    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::alive)), endpointPaths.get(1));
    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::status)), endpointPaths.get(2));
  }

  /**
   * Creates a servlet context and registers health check endpoints.
   *
   * @return Servlet context handler with healthcheck servlets.
   */
  public ServletContextHandler registerEndpoints() {
    ServletContextHandler servletContext = new ServletContextHandler(null, PATH_PREFIX);
    this.registerEndpoints(servletContext);
    return servletContext;
  }
}
