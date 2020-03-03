package com.workiva.platform.jetty.servlet;

class PlatformMock extends Platform {
  boolean setAllowedIPs(String allowedIP) {
    return allowedIPs.add(allowedIP);
  }
}
