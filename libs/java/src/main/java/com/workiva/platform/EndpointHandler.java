package com.workiva.platform;

import com.google.gson.Gson;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.concurrent.Callable;

public class EndpointHandler implements HttpHandler {
  Callable callable;

  private EndpointHandler() {}

  EndpointHandler(Callable c) {
    this.callable = c;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

    Object result = null;
    try {
      result = callable.call();
    } catch (Exception e) {
      exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
    }

    Gson gson = new Gson();
    String json = gson.toJson(result);
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
    exchange.putAttachment(Result.KEY, json);
    exchange.getResponseSender().send(json);
  }
}
