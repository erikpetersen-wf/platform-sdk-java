package com.workiva.platform.netty;

import com.workiva.platform.core.PlatformCore;

public class Platform extends PlatformCore {

  HttpFrugalHealthHandler healthHandler;

  HttpFrugalHealthHandler getHandler() {
    return this.healthHandler = new HttpFrugalHealthHandler(this::ready, this::alive, this::status);
  }
}
