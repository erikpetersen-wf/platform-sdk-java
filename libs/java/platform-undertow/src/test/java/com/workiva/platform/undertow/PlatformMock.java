package com.workiva.platform.undertow;

class PlatformMock extends Platform {
  boolean setAllowedIPs(String allowedIP) {
    return allowedIPs.add(allowedIP);
  }
}