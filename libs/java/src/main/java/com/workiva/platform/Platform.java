package com.workiva.platform;

import io.undertow.Handlers;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Platform {

  public static Builder builder() {
    return new Builder();
  }

  static Map defaultCheck() throws Exception {
    Map map = new HashMap();
    map.put("status", "OK");
    return map;
  }

  public static class Builder {

    private int port = 8888;
    private Callable readinessFunction = Platform::defaultCheck;
    private String readinessPath = "_wk/ready";
    private Callable livenessFunction = Platform::defaultCheck;
    private String livenessPath = "_wk/alive";

    Builder() {}

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder readiness(Callable function, String path) {
      this.readinessFunction = function;
      this.readinessPath = path;
      return this;
    }

    public Builder liveness(Callable function, String path) {
      this.livenessFunction = function;
      this.livenessPath = path;
      return this;
    }

    public void start() {
      final Logger log = LoggerFactory.getLogger(Platform.class);
      Undertow.builder()
          .addHttpListener(port, "0.0.0.0")
          .setHandler(
              Handlers.path()
                  .addExactPath(livenessPath, new EndpointHandler(() -> livenessFunction.call()))
                  .addExactPath(readinessPath, new EndpointHandler(() -> readinessFunction.call())))
          .build()
          .start();
      log.info("Started liveness/readiness probes.");
      log.info(
          "Liveness route: {}, Readiness route: {} on port: {}", livenessPath, readinessPath, port);
    }
  }
}
