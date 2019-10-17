package com.workiva.platform;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.undertow.util.StatusCodes;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;

public class PlatformTest {

  static final int frugalPort = 8000;

  final CloseableHttpClient httpClient = HttpClients.createDefault();

  private static HealthStatus notOkHealthCheck() {
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

  @BeforeClass
  public static void setup() {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  // codec+aggregator needed to make a FullHttpRequest.
                  ch.pipeline().addLast("codec", new HttpServerCodec());
                  ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024 * 10));
                  ch.pipeline().addLast(new HttpFrugalHealthHandler());
                }
              });

      new Thread(
              () -> {
                try {
                  ChannelFuture f = b.bind(frugalPort).sync();
                  // Blocking
                  f.channel().closeFuture().sync();
                } catch (Exception e) {
                  Assert.fail(
                      "Error encountered when attempting to standup HTTP-Messaging service");
                }
              })
          .start();

      while (true) {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
          HttpGet httpGet =
              new HttpGet(
                  String.format("http://localhost:%d%s", frugalPort, Platform.readinessPath));
          HttpResponse httpFrugalResp = httpClient.execute(httpGet);
          int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
          if (statusCode != 503) {
            break;
          }
          Thread.sleep(30);
        } catch (Exception e) {
        }
      }
    } catch (Exception e) {
      Assert.fail("Unable to start test service");
    }
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
        Platform.builder().function(PlatformTest::notOkHealthCheck).path("_custom/path").start();
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
            .readinessFunction(PlatformTest::notOkHealthCheck)
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
            .livenessFunction(PlatformTest::notOkHealthCheck)
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

  @Test
  public void TestCheckFrugalHealth() throws Exception {
    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet httpGet = new HttpGet("http://localhost:8000/_wk/ready");
      HttpResponse httpFrugalResp = httpClient.execute(httpGet);
      int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
      Assert.assertEquals(statusCode, StatusCodes.OK);
    }
  }
}
