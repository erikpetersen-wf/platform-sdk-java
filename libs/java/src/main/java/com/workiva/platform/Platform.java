package com.workiva.platform;

import io.undertow.Handlers;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** Stands up an HTTP server for liveness/readiness probes. */
public class Platform implements AutoCloseable {

  Platform(Undertow httpServer) {
    this.httpServer = httpServer;
  }

  private final Undertow httpServer;

  @Override
  public void close() {
    this.httpServer.stop();
  }

  public static Builder builder() {
    return new Builder();
  }

  static HealthStatus defaultCheck() throws Exception {
    HealthStatus status = new HealthStatus();
    status.ok();

    return status;
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

    public Builder function(Callable function) {
      this.readinessFunction = function;
      this.livenessFunction = function;
      return this;
    }

    public Builder path(String path) {
      this.readinessPath = path;
      this.livenessPath = path;
      return this;
    }

    public Builder readinessPath(String path) {
      this.readinessPath = path;
      return this;
    }

    public Builder readinessFunction(Callable function) {
      this.readinessFunction = function;
      return this;
    }

    public Builder livenessPath(String path) {
      this.livenessPath = path;
      return this;
    }

    public Builder livenessFunction(Callable function) {
      this.livenessFunction = function;
      return this;
    }

    public Platform start() {
      final Logger log = LoggerFactory.getLogger(Platform.class);
      Undertow httpServer =
          Undertow.builder()
              .addHttpListener(port, "0.0.0.0")
              .setHandler(
                  Handlers.path()
                      .addExactPath(
                          livenessPath, new EndpointHandler(() -> livenessFunction.call()))
                      .addExactPath(
                          readinessPath, new EndpointHandler(() -> readinessFunction.call())))
              .build();
      httpServer.start();
      log.info("Started liveness/readiness probes.");
      log.info(
          "Liveness route: {}, Readiness route: {} on port: {}", livenessPath, readinessPath, port);

      return new Platform(httpServer);
    }
  }
}
