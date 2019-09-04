package com.workiva.platform;

import io.undertow.Handlers;
import io.undertow.Undertow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Platform {

  public static void main(String[] args) {
    builder().port(8090).readiness(() -> someCheckFunction()).start();
  }

  public static Builder builder() {
    return new Builder();
  }

  static Map someCheckFunction() throws Exception {
    Map map = new HashMap();
    map.put("status", "bad");
    return map;
  }

  private static class Builder {

    private int port;
    private Callable function;

    Builder() {}

    Builder port(int port) {
      this.port = port;
      return this;
    }

    Builder readiness(Callable function) {
      this.function = function;
      return this;
    }

    void start() {
      Undertow.builder()
          .addHttpListener(port, "localhost")
          .setHandler(
              Handlers.path()
                  .addExactPath("_wk/ready", new EndpointHandler(() -> function.call())))
          .build()
          .start();
    }
  }
}
