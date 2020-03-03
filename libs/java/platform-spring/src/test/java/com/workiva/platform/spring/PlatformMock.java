package com.workiva.platform.spring;

class PlatformMock extends Platform {
  boolean setAllowedIPs(String allowedIP) {
    return allowedIPs.add(allowedIP);
  }
}
