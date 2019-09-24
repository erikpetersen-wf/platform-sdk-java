package com.workiva.platform;

import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;

public class PlatformTest {

  final CloseableHttpClient httpClient = HttpClients.createDefault();

  static HealthStatus customFunction() {
    HealthStatus status = new HealthStatus();
    status.notOk("some exception");

    return status;
  }

  @Test
  public void TestDefaultReadiness() {
    try (Platform platform = Platform.builder().start()) {
      HttpGet httpGet = new HttpGet("http://localhost:8888/_wk/ready");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    } catch (Exception ex) {
      Assert.fail();
    }
  }

  @Test
  public void TestDefaultLiveness() {
    try (Platform platform = Platform.builder().start()) {
      HttpGet httpGet = new HttpGet("http://localhost:8888/_wk/alive");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    } catch (Exception ex) {
      Assert.fail();
    }
  }

  @Test
  public void TestCustomFunctionAndPath() {
    try (Platform platform =
        Platform.builder().function(() -> customFunction()).path("_custom/path").start()) {
      HttpGet httpGet = new HttpGet("http://localhost:8888/_custom/path");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
    } catch (Exception ex) {
      Assert.fail();
    }
  }

  @Test
  public void TestCustomReadiness() throws Exception {
    try (Platform platform =
        Platform.builder()
            .port(8889)
            .readinessFunction(PlatformTest::customFunction)
            .readinessPath("_custom/ready")
            .start()) {
      HttpGet httpGet = new HttpGet("http://localhost:8889/_custom/ready");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
    }
  }

  @Test
  public void TestCustomLiveness() throws Exception {
    try (Platform platform =
        Platform.builder()
            .port(8887)
            .livenessFunction(PlatformTest::customFunction)
            .livenessPath("_custom/alive")
            .livenessPath("_custom/alive")
            .start()) {
      HttpGet httpGet = new HttpGet("http://localhost:8887/_custom/alive");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.SERVICE_UNAVAILABLE);
    }
  }
}
