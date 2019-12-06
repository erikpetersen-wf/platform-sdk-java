package com.workiva.platform.netty;

import java.io.IOException;

import com.workiva.platform.core.PlatformCore;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HealthCheckHandlerTest {

  private static final int frugalPort = 8000;

  private final CloseableHttpClient httpClient = HttpClients.createDefault();

  private HttpResponse makeHttpRequest(String path) {
    HttpResponse httpFrugalResp = null;
    try {
      HttpGet httpGet = new HttpGet(path);
      httpFrugalResp = httpClient.execute(httpGet);
    } catch (Exception ex) {
      Assert.fail(ex.getMessage());
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

  @BeforeClass
  public static void setup() {
    Platform platform = new Platform();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                  // codec+aggregator needed to make a FullHttpRequest.
                  ch.pipeline().addLast("codec", new HttpServerCodec());
                  ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024 * 10));
                  ch.pipeline().addLast(platform.getHandler());
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
                  String.format("http://localhost:%d%s", frugalPort, PlatformCore.PATH_READY));

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
  public void TestReadiness() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8000/_wk/ready");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(200, statusCode);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    Assert.assertEquals("", responseStatus);
  }

  @Test
  public void TestLiveness() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8000/_wk/alive");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(200, statusCode);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    Assert.assertEquals("", responseStatus);
  }

  @Test
  public void TestStatus() {
    HttpResponse httpFrugalResp = makeHttpRequest("http://localhost:8000/_wk/status");

    int statusCode = httpFrugalResp.getStatusLine().getStatusCode();
    Assert.assertEquals(200, statusCode);

    String responseStatus = getBodyFromResponse(httpFrugalResp);
    Assert.assertEquals("", responseStatus);
  }
}
