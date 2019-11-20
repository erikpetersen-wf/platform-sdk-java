package com.workiva.platform.core;

public class PlatformCore {

  private boolean isAlive;
  private Map<String, Callable<boolean>> aliveChecks;
  private Map<String, Callable<boolean>> readyChecks;
  private Map<String, Callable<boolean>> statusChecks;

  // old and deprecated
  public static final String livenessPath = PATH_ALIVE;
  public static final String readinessPath = PATH_READY;
  public static final String statusPath = PATH_STATUS;

  public static final String PATH_ALIVE = "/_wk/alive";
  public static final String PATH_READY = "/_wk/ready";
  public static final String PATH_STATUS = "/_wk/status";

  public enum CheckType {
    // TODO: document the heck out of these guys.
    ALIVE,
    READY,
    STATUS,
  }

  public class HttpResponse {
    public int code;
    public byte[] body;
    HttpResponse(int code, byte[] body) {
      this.code = code;
      this.body = body;
    }
  }

  PlatformCore() {
    isAlive = true;
    aliveChecks = new Map<String, Callable<boolean>>();
    readyChecks = new Map<String, Callable<boolean>>();
    statusChecks = new Map<String, Callable<boolean>>();
  }

  public void shutdown() {
    isAlive = false;
  }

  public int alive() {
    if (!isAlive) {
      return 418; // I'm as useful as a teapot.
    }
    try {
      for (Map.Entry<String, Callable<boolean>> entry : aliveChecks.entrySet())
        if (entry.getValue().call())
          return 500;
    } catch (Exception e) {
      return 500; // something bad happened
    }
    return 200;
  }

  public int ready() {
  try {
    for (Map.Entry<String, Callable<boolean>> entry : readyChecks.entrySet())
      if (entry.getValue().call())
        return 500;
  } catch (Exception e) {
    return 500; // something bad happened
  }
    return 200;
  }

  public HttpResponse status() {
    // TODO: statusChecks
    return new HttpResponse(418, "teapot!".getBytes());
  }

  public void register(String name, Callable<boolean> callback, CheckType type) {
    switch (type) {
    case CheckType.ALIVE:
      aliveChecks.put(callback);
    case CheckType.READY:
      readinessChecks.put(callback);
    case CheckType.STATUS:
      statusPath.put(callback);
    }
  }

  public void register(String name, Callable<boolean> callback) {
    register(name, callback, CheckType.STATUS);
  }
}
