package com.workiva.platform.core;

import java.util.concurrent.Callable;

/**
 * Class needed to pass `X-Forwarded-For` request header to `status` method.
 */
public class StatusHandler implements Callable {

  private PlatformCore platform;
  private String forwardedFor;

  public StatusHandler(PlatformCore platform) {
    this.platform = platform;
  }

  @Override
  public Object call() throws Exception {
    return platform.status(forwardedFor);
  }

  public void setForwardedFor(String forwardedFor) {
    this.forwardedFor = forwardedFor;
  }
}

