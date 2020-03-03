package com.workiva.platform.undertow;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformStatus;
import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

public class PlatformTest {

  private static PlatformMock platform;

  final CloseableHttpClient httpClient = HttpClients.createDefault();

  private HttpResponse makeHttpRequest(Platform platform, String path) {
    HttpResponse httpFrugalResp = null;
    try (Platform closeablePlatform = platform) {
      HttpGet httpGet = new HttpGet(path);
      httpGet.addHeader(PlatformCore.FORWARDED_FOR, "0.0.0.0");
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

  @Before
  public void setup() {
    platform = new PlatformMock();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus();
          }
        });
  }

  @Test
  public void TestReadiness() {
    platform.start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, StatusCodes.OK);
  }

  @Test
  public void TestLiveness() {
    platform.start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/alive");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);
  }

  @Test
  public void TestStatus() {
    platform.start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/status");

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
    platform.start();
    platform.setAllowedIPs("0.0.0.0");
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/status");

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

  @Test
  public void TestCustomPort() {
    Platform platform = new Platform(8889).start();
    HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8889/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(statusCode, HttpStatus.SC_OK);
  }
}
