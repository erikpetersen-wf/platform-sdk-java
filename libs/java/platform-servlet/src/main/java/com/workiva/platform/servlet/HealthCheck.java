package com.workiva.platform.servlet;

import com.workiva.platform.core.PlatformResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HealthCheck extends HttpServlet {

  private static Callable callable;

  public HealthCheck(Callable c) {
    HealthCheck.callable = c;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PlatformResponse result = null;
    try {
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
