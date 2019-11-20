package com.workiva.platform.core;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class PlatformCore {

  private boolean isAlive;
  private Map<String, Callable<Boolean>> aliveChecks;
  private Map<String, Callable<Boolean>> readyChecks;
  private Map<String, Callable<Boolean>> statusChecks;

  public static final String PATH_ALIVE = "/_wk/alive";
  public static final String PATH_READY = "/_wk/ready";
  public static final String PATH_STATUS = "/_wk/status";

  // old and deprecated
  public static final String livenessPath = PATH_ALIVE;
  public static final String readinessPath = PATH_READY;
  public static final String statusPath = PATH_STATUS;

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
    aliveChecks = new TreeMap<>();
    readyChecks = new TreeMap<>();
    statusChecks = new TreeMap<>();
  }

  public void shutdown() {
    isAlive = false;
  }

  public int alive() {
    if (!isAlive) {
      return 418; // I'm as useful as a teapot.
    }
    try {
      for (Callable<Boolean> check : aliveChecks.values()) {
        if (check.call()) {
          return 500;
        }
      }
    } catch (Exception e) {
      return 500; // something bad happened
    }
    return 200;
  }

  public int ready() {
    try {
      for (Callable<Boolean> check : readyChecks.values()) {
        if (check.call()) {
          return 500;
        }
      }
    } catch (Exception e) {
      return 500; // something bad happened
    }
    return 200;
  }

  public HttpResponse status() {
    String response = "\t\t\"meta\": {";
    for (Map.Entry<String, Callable<Boolean>> entry : statusChecks.entrySet()) {
      response += "\n\t\t\t\"" + entry.getKey() + "\": \"";
      try {
        if (entry.getValue().call()) {
          response += "PASSED";
        } else {
          response += "FAILED";
        }
      } catch (Exception e) {
        response += e.toString();
      }
      response += "\",";
    }
    response += "\n\t\t}";
    // TODO: statusChecks
    return new HttpResponse(418, response.getBytes());
  }

  public void register(String name, Callable<Boolean> callback, CheckType type) {
    switch (type) {
      case ALIVE:
        aliveChecks.put(name, callback);
        break;
      case READY:
        readyChecks.put(name, callback);
        break;
      case STATUS:
        statusChecks.put(name, callback);
        break;
      default:
        System.out.println("unsupported callback!");
    }
  }

  public void register(String name, Callable<Boolean> callback) {
    register(name, callback, CheckType.STATUS);
  }
}
