package com.workiva.platform;

import com.google.inject.Guice;
import com.google.inject.servlet.GuiceFilter;

import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class GuiceHealthCheckTest {

  @BeforeClass
  public static void setup() throws Exception {
    TestServletModule testServletModule = new TestServletModule();
    Guice.createInjector(testServletModule);

    Server server = new Server(8080);

    ServletContextHandler servletContextHandler =
        new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
    servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

    servletContextHandler.addServlet(DefaultServlet.class, "/");

    server.start();
  }

  @Test
  public void TestGuiceReadinessCheck() throws Exception {
    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet("http://localhost:8080/_wk/ready");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    }
  }

  @Test
  public void TestGuiceLivenessCheck() throws Exception {
    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet("http://localhost:8080/_wk/alive");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    }
  }
}
