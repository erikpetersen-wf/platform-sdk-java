package com.workiva.platform.netty;

import com.workiva.platform.core.PlatformResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import com.workiva.platform.core.PlatformCore;

import java.util.concurrent.Callable;

/** Intercepts an HTTP request when the path is equal to health paths and returns a 200. */
@ChannelHandler.Sharable
public class HealthCheckHandler extends ChannelInboundHandlerAdapter {

  private Callable ready;
  private Callable alive;
  private Callable status;

  HealthCheckHandler(Callable ready, Callable alive, Callable status) {
    this.ready = ready;
    this.alive = alive;
    this.status = status;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest request = (FullHttpRequest) msg;
      PlatformResponse result = null;
      if (request.uri().equalsIgnoreCase(PlatformCore.PATH_READY)) {
        result = (PlatformResponse) ready.call();
      } else if (request.uri().equalsIgnoreCase(PlatformCore.PATH_ALIVE)) {
        result = (PlatformResponse) alive.call();
      } else if (request.uri().equalsIgnoreCase(PlatformCore.PATH_STATUS)) {
        result = (PlatformResponse) status.call();
      } else {
        ctx.fireChannelRead(msg);
      }

      if (result != null) {
        FullHttpResponse response =
            new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, new HttpResponseStatus(result.getCode(), result.getBody()));
        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        request.release();
      }
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
