package com.workiva.platform.servlet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.workiva.platform.core.PlatformCore;

public class Platform extends PlatformCore {

  /**
   * Takes a servlet context handler and registers the readiness, liveness, and status endpoints.
   *
   * @param servletContext Context from the Jetty server.
   */
  public void registerEndpoints(ServletContextHandler servletContext) {
    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::ready)), PlatformCore.PATH_READY + "/*");
    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::alive)), PlatformCore.PATH_ALIVE);
    servletContext.addServlet(
        new ServletHolder(new HealthCheck(this::status)), PlatformCore.PATH_STATUS);
  }
}