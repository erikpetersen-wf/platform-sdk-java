package com.workiva.platform.core;

public class PlatformCore {

  private boolean isAlive;

  public static final String readinessPath = "/_wk/ready";

  public static final String livenessPath = "/_wk/alive";

  PlatformCore() {
    isAlive = true;
  }

  public void shutdown() {
    isAlive = false;
  }

  public int alive() {
    if (isAlive) {
      return 200;
    }
    return 418; // I'm as useful as a teapot.
  }

  public int ready() {
    return 200;
  }
}
