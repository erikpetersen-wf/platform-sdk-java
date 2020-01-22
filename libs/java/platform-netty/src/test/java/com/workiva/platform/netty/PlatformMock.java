package com.workiva.platform.netty;

class PlatformMock extends Platform {
  boolean setAllowedIPs(String allowedIP) {
    return allowedIPs.add(allowedIP);
  }
}
