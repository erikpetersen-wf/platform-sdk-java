package com.workiva.platform;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/** Intercepts an HTTP request when the path is equal to `Platform.healthPath` and returns a 200. */
public class HttpFrugalHealthHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    // TODO - this flow can probably be cleaned up a fair bit
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest request = (FullHttpRequest) msg;
      if (!request.uri().equalsIgnoreCase(Platform.readinessPath)
          && !request.uri().equalsIgnoreCase(Platform.livenessPath)
          && !request.uri().equalsIgnoreCase(Platform.statusPath)) {
        ctx.fireChannelRead(msg);
      } else {
        FullHttpResponse response =
            new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
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
