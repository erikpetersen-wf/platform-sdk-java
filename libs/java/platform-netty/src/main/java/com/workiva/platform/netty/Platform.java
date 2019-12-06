package com.workiva.platform.netty;

import com.workiva.platform.core.PlatformCore;

public class Platform extends PlatformCore {

  HealthCheckHandler healthHandler;

  HealthCheckHandler getHandler() {
    return this.healthHandler = new HealthCheckHandler(this::ready, this::alive, this::status);
  }
}
