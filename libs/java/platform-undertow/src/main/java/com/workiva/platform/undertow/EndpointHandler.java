package com.workiva.platform.undertow;

import com.google.gson.Gson;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import com.workiva.platform.core.PlatformResponse;

import java.util.concurrent.Callable;

public class EndpointHandler implements HttpHandler {
  Callable callable;

  EndpointHandler(Callable c) {
    this.callable = c;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

    PlatformResponse result = null;
    try {
      result = (PlatformResponse) callable.call();
    } catch (Exception e) {
      result = new PlatformResponse(500, e.getMessage());

      exchange.setStatusCode(result.getCode());
      exchange.getResponseSender().send(result.getBody());
      return;
    }

    exchange.setStatusCode(result.getCode());
    exchange.getResponseSender().send(result.getBody());
  }
}
