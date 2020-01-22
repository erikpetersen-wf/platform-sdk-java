package com.workiva.platform.jetty.servlet;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformStatus;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

public class PlatformTest {

  private static PlatformMock platform;
  private final CloseableHttpClient httpClient = HttpClients.createDefault();
  private Server server;

  private HttpResponse makeHttpRequest(String path) {
    HttpResponse httpFrugalResp = null;

    HttpGet httpGet = new HttpGet(path);
    httpGet.addHeader(PlatformCore.FORWARDED_FOR, "0.0.0.0");
    try {
      httpFrugalResp = httpClient.execute(httpGet);
    } catch (IOException e) {
      Assert.fail();
    }

    return httpFrugalResp;
  }

  private String getBodyFromResponse(HttpResponse response) {
    String responseString = null;
    try {
      responseString = EntityUtils.toString(response.getEntity());
    } catch (IOException ex) {
      Assert.fail();
    }
    return responseString;
  }

  @Before
  public void startJetty() throws Exception {
    platform = new PlatformMock();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus();
          }
        });
    // Create Server
    server = new Server(8080);
    ServletContextHandler servletContextHandler = platform.registerEndpoints();
    server.setHandler(servletContextHandler);

    // Start Server
    server.start();
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

    JSONObject wrapper = (JSONObject) JSONValue.parse(getBodyFromResponse(httpFrugalResp));
    Assert.assertTrue(wrapper != null);

    JSONObject data = (JSONObject) wrapper.get("data");
    Assert.assertTrue(data != null);
    Assert.assertTrue(data.get("meta") == null);
    Assert.assertTrue(data.get("id") instanceof String);

    JSONObject attrs = (JSONObject) data.get("attributes");
    Assert.assertEquals(attrs.get("status"), PlatformStatus.PASSED);
    Assert.assertTrue(attrs.get("meta") == null);
  }

  /** Ensure if request originates from allowed ip, meta data included in response. */
  @Test
  public void TestStatusWithMeta() {
    platform.setAllowedIPs("0.0.0.0");
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8080/_wk/status");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);

    JSONObject wrapper = (JSONObject) JSONValue.parse(getBodyFromResponse(httpFrugalResp));
    Assert.assertTrue(wrapper != null);

    JSONObject data = (JSONObject) wrapper.get("data");
    Assert.assertTrue(data != null);
    Assert.assertTrue(data.get("meta") != null);
    Assert.assertTrue(data.get("id") instanceof String);

    JSONObject attrs = (JSONObject) data.get("attributes");
    Assert.assertEquals(attrs.get("status"), PlatformStatus.PASSED);
    Assert.assertTrue(attrs.get("meta") == null);
  }
}
