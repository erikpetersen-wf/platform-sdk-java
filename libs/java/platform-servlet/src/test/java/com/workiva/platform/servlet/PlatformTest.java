package com.workiva.platform.servlet;

import com.workiva.platform.core.PlatformStatus;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PlatformTest {

  private final CloseableHttpClient httpClient = HttpClients.createDefault();
  private Server server;

  private HttpResponse makeHttpRequest(String path) {
    HttpResponse httpFrugalResp = null;

    HttpGet httpGet = new HttpGet(path);
    try {
      httpFrugalResp = httpClient.execute(httpGet);
    } catch (IOException e) {
      Assert.fail();
    }

    return httpFrugalResp;
  }

  @Before
  public void startJetty() throws Exception {
    // Create Server
    server = new Server(8080);
    ServletContextHandler servletContextHandler =
        new ServletContextHandler(
            ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
    server.setHandler(servletContextHandler);
    Platform platform = new Platform();
    platform.register("db", this::myDbHealthCheck);
    platform.registerEndpoints(servletContextHandler);

    // Start Server
    server.start();
  }

  private PlatformStatus myDbHealthCheck() {
    return new PlatformStatus("DB down.");
  }

  @After
  public void stopJetty() {
    try {
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void TestReadiness() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8080/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);
  }

  @Test
  public void TestLiveness() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8080/_wk/alive");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);
  }

  @Test
  public void TestStatus() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8080/_wk/status");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);
  }
}
