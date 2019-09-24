package com.workiva.platform;

import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;

public class PlatformTest {

  final CloseableHttpClient httpClient = HttpClients.createDefault();

  static HealthStatus customFunction() {
    HealthStatus status = new HealthStatus();
    status.notOk("some exception");

    return status;
  }

  private HttpResponse makeHttpRequest(Platform platform, String path) {
    HttpResponse httpFrugalResp = null;
    try (Platform closeablePlatform = platform) {
      HttpGet httpGet = new HttpGet(path);
      httpFrugalResp = httpClient.execute(httpGet);
    } catch (Exception ex) {
      Assert.fail();
    }

    return httpFrugalResp;
  }

  @Test
  public void TestDefaultReadiness() {
    Platform platform = Platform.builder().start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/ready");
    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.OK);
  }

  @Test
  public void TestDefaultLiveness() {
    Platform platform = Platform.builder().start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/alive");
    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.OK);
  }

  @Test(expected = ConnectException.class)
  public void TestHttpServerCloses() throws IOException {
    Platform platform = Platform.builder().start();
    makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

    HttpGet httpGet = new HttpGet("http://localhost:8888/_wk/ready");
    httpClient.execute(httpGet);
  }

  @Test
  public void TestCustomFunctionAndPath() {
    Platform platform =
        Platform.builder().function(PlatformTest::customFunction).path("_custom/path").start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_custom/path");
    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
  }

  @Test
  public void TestCustomReadiness() throws Exception {
    Platform platform =
        Platform.builder()
            .port(8889)
            .readinessFunction(PlatformTest::customFunction)
            .readinessPath("_custom/ready")
            .start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8889/_custom/ready");
    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
  }

  @Test
  public void TestCustomLiveness() throws Exception {
    Platform platform =
        Platform.builder()
            .port(8887)
            .livenessFunction(PlatformTest::customFunction)
            .livenessPath("_custom/alive")
            .livenessPath("_custom/alive")
            .start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8887/_custom/alive");
    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
  }
}
