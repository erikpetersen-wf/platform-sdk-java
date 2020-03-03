package com.workiva.platform.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.StatusHandler;

/** Stands up an HTTP server for liveness/readiness probes. */
public class Platform extends PlatformCore implements AutoCloseable {

  private static int port = 8888;

  private Undertow httpServer;

  public Platform(int port) {
    Platform.port = port;
  }

  public Platform() {}

  @Override
  public void close() {
    this.httpServer.stop();
  }

  public Platform start() {
    final Logger log = LoggerFactory.getLogger(Platform.class);
    Undertow httpServer =
        Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(
                Handlers.path()
                    .addExactPath(PATH_ALIVE, new EndpointHandler(this::alive))
                    .addExactPath(PATH_READY, new EndpointHandler(this::ready))
                    .addExactPath(PATH_STATUS, new EndpointHandler(new StatusHandler(this))))
            .build();
    this.httpServer = httpServer;
    httpServer.start();
    log.info("Started liveness/readiness probes.");
    log.info(
        "Liveness route: {}, Readiness route: {}, Status route: {} on port: {}",
        PATH_ALIVE,
        PATH_READY,
        PATH_STATUS,
        port);

    return this;
  }
}
