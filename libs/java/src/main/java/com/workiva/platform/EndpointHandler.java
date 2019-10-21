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

    Gson gson = new Gson();
    HealthStatus result = null;
    try {
      result = (HealthStatus) callable.call();
    } catch (Exception e) {
      result = new HealthStatus(false);
      result.notOk(e.getMessage());
      String json = gson.toJson(result);

      exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
      exchange.getResponseSender().send(json);
      return;
    }

    String json = gson.toJson(result);
    boolean pass = result.isOk();

    exchange.setStatusCode(pass ? StatusCodes.OK : StatusCodes.SERVICE_UNAVAILABLE);
    exchange.getResponseSender().send(json);
  }
}