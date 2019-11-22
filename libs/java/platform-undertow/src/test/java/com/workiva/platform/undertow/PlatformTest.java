package com.workiva.platform.undertow;

import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;

public class PlatformTest {

    final CloseableHttpClient httpClient = HttpClients.createDefault();

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
    public void TestReadiness() {
      Platform platform = new Platform().start();
      HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/ready");

      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    }

    @Test
    public void TestLiveness() {
      Platform platform = new Platform().start();
      HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/alive");

      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, HttpStatus.SC_OK);
    }

	@Test
	public void TestStatus() {
		Platform platform = new Platform().start();
		HttpResponse httpFrugalResp = makeHttpRequest(platform, "http://localhost:8888/_wk/status");

		int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
		Assert.assertEquals(statusCode, HttpStatus.SC_OK);
	}
}
