package com.workiva.platform.netty;

import io.netty.buffer.Unpooled;
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
import com.workiva.platform.core.PlatformResponse;

/** Intercepts an HTTP request when the path is equal to health paths and returns a 200. */
@ChannelHandler.Sharable
public class HealthCheckHandler extends ChannelInboundHandlerAdapter {

  private Platform platform;

  HealthCheckHandler(Platform platform) {
    this.platform = platform;
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
      switch (request.uri()) {
        case PlatformCore.PATH_READY:
          result = platform.ready();
          break;
        case PlatformCore.PATH_ALIVE:
          result = platform.alive();
          break;
        case PlatformCore.PATH_STATUS:
          result = platform.status(request.headers().get(PlatformCore.FORWARDED_FOR));
          break;
        default:
          ctx.fireChannelRead(msg);
      }

      if (result != null) {
        FullHttpResponse response =
            new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(result.getCode()),
                Unpooled.copiedBuffer(result.getBody().getBytes("utf-8")));
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
