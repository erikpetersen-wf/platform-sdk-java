package com.workiva.platform;

import com.google.gson.Gson;
import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;

public class PlatformTest {

  final CloseableHttpClient httpClient = HttpClients.createDefault();

  private static HealthStatus customFunction() {
    HealthStatus status = new HealthStatus();
    status.notOk("some exception");

    return status;
  }

  private static HealthStatus throwingFunction() throws Exception {
    throw new Exception("uncaught exception");
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

  private String getBodyFromResponse(HttpResponse response) {
    String responseString = null;
    try {
      responseString = EntityUtils.toString(response.getEntity());
    } catch (IOException ex) {
      Assert.fail();
    }
    return responseString;
  }

  private String createExpectedStatus(boolean ok, String reason) {
    HealthStatus expectedStatus = new HealthStatus(ok);
    if (!ok) {
      expectedStatus.notOk(reason);
    }
    Gson gson = new Gson();
    return gson.toJson(expectedStatus);
  }

  @Test
  public void TestDefaultReadiness() {
    Platform platform = Platform.builder().start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.OK);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(true, "");
    Assert.assertEquals(jsonStatus, responseStatus);
  }

  @Test
  public void TestDefaultLiveness() {
    Platform platform = Platform.builder().start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/alive");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.OK);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(true, "");
    Assert.assertEquals(jsonStatus, responseStatus);
  }

  @Test(expected = ConnectException.class)
  public void TestHttpServerCloses() throws IOException {
    Platform platform = Platform.builder().start();
    makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

    // Attempting another request at this point should fail because the
    // HTTP server auto-closes as part of `makeHttpRequest`.
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

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(false, "some exception");
    Assert.assertEquals(jsonStatus, responseStatus);
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

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(false, "some exception");
    Assert.assertEquals(jsonStatus, responseStatus);
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

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(false, "some exception");
    Assert.assertEquals(jsonStatus, responseStatus);
  }

  @Test
  public void TestFunctionException() throws Exception {
    Platform platform = Platform.builder().function(PlatformTest::throwingFunction).start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    String jsonStatus = createExpectedStatus(false, "uncaught exception");
    Assert.assertEquals(jsonStatus, responseStatus);
  }
}
