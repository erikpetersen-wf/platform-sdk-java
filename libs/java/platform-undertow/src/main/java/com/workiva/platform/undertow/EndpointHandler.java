package com.workiva.platform.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformResponse;
import com.workiva.platform.core.StatusHandler;

import java.util.concurrent.Callable;

public class EndpointHandler implements HttpHandler {
  Callable callable;

  EndpointHandler(Callable c) {
    this.callable = c;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

    if (callable.getClass().equals(StatusHandler.class)) {
      String forwardedFor = exchange.getRequestHeaders().getFirst(PlatformCore.FORWARDED_FOR);
      ((StatusHandler) callable).setForwardedFor(forwardedFor);
    }

    PlatformResponse result = (PlatformResponse) callable.call();

    exchange.setStatusCode(result.getCode());
    exchange.getResponseSender().send(result.getBody());
  }
}
