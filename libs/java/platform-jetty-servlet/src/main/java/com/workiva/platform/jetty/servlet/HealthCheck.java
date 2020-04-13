package com.workiva.platform.jetty.servlet;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformResponse;
import com.workiva.platform.core.StatusHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HealthCheck extends HttpServlet {

  private Callable callable;

  public HealthCheck(Callable c) {
    this.callable = c;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PlatformResponse result = null;
    try {
      if (callable.getClass().equals(StatusHandler.class)) {
        String forwardedFor = request.getHeader(PlatformCore.FORWARDED_FOR);
        ((StatusHandler) callable).setForwardedFor(forwardedFor);
      }
      result = (PlatformResponse) callable.call();
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    response.setStatus(result.getCode());

    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.print(result.getBody());
    out.flush();
  }
}
