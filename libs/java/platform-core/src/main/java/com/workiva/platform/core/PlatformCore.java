package com.workiva.platform.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformCore {

  private boolean isAlive;
  private Map<String, PlatformStatus> aliveChecks;
  private Map<String, PlatformStatus> readyChecks;
  private Map<String, PlatformStatus> statusChecks;

  Set<String> allowedIPs;

  public static final String PATH_ALIVE = "/_wk/alive";
  public static final String PATH_READY = "/_wk/ready";
  public static final String PATH_STATUS = "/_wk/status";

  // old and deprecated
  public static final String livenessPath = PATH_ALIVE;
  public static final String readinessPath = PATH_READY;
  public static final String statusPath = PATH_STATUS;

  public PlatformCore() {
    isAlive = true;
    aliveChecks = new ConcurrentHashMap<>();
    readyChecks = new ConcurrentHashMap<>();
    statusChecks = new ConcurrentHashMap<>();

    String whitelist = System.getenv("NEW_RELIC_SYNTHETICS_IP_WHITELIST");
    allowedIPs = parseWhitelist(whitelist);
  }

  public void shutdown() {
    isAlive = false;
  }

  public int alive() {
    if (!isAlive) {
      return 418; // I'm as useful as a teapot.
    }
    for (PlatformStatus check : aliveChecks.values()) {
      if (!check.isOK()) {
        return 500;
      }
    }
    return 200;
  }

  public int ready() {
    for (PlatformStatus check : readyChecks.values()) {
      if (!check.isOK()) {
        return 500;
      }
    }
    return 200;
  }

  public PlatformResponse status(String forwardedFor) {
    JSONObject meta = new JSONObject();
    String status = PlatformStatus.PASSED;
    for (Map.Entry<String, PlatformStatus> entry : statusChecks.entrySet()) {
      String key = entry.getKey();
      PlatformStatus value = entry.getValue();

      if (value.isOK()) {
        meta.put(key, PlatformStatus.PASSED);
      } else {
        meta.put(key, value.status);
        status = PlatformStatus.FAILED;
      }
    }

    // Convert to JSON:API format JSON
    JSONObject wrapper = new JSONObject();
    JSONObject data = new JSONObject();
    JSONObject attrs = new JSONObject();
    wrapper.put("data", data);
    attrs.put("status", status);
    data.put("id", "TODO"); // TODO: generate ID: 2019-08-12T15:50:13-05:00
    data.put("attributes", attrs);

    // TODO: verify against NEW_RELIC_SYNTHETICS_IP_WHITELIST
    // TODO: add machine meta block (if meta is allowed)
    if (!meta.isEmpty() && allowedIPs.contains(forwardedFor)) {
      data.put("meta", meta);
    }

    return new PlatformResponse(200, wrapper.toJSONString().getBytes());
  }

  public PlatformResponse status() {
    return status("0.0.0.0");
  }

  public void register(String name, PlatformStatus status, PlatformCheckType type)
      throws Exception {
    if (status == null) {
      return; // NOOP for null pointers
    }
    switch (type) {
      case ALIVE:
        if (aliveChecks.containsKey(name)) {
          throw new Exception("duplicate entry key");
        }
        aliveChecks.put(name, status);
        break;
      case READY:
        if (readyChecks.containsKey(name)) {
          throw new Exception("duplicate entry key");
        }
        readyChecks.put(name, status);
        break;
      default: // everything else is a status check
        if (statusChecks.containsKey(name)) {
          throw new Exception("duplicate entry key");
        }
        statusChecks.put(name, status);
        break;
    }
  }

  public void register(String name, PlatformStatus callback) throws Exception {
    register(name, callback, PlatformCheckType.STATUS);
  }

  static Set<String> parseWhitelist(String whitelist) {
    Set<String> ips = new HashSet<>();
    if (whitelist == null) {
      return ips;
    }
    String jsonString = new String(Base64.getDecoder().decode(whitelist));
    JSONArray jsonArray = (JSONArray) JSONValue.parse(jsonString);

    for (Object ip : jsonArray) {
      ips.add(ip.toString());
    }
    return ips;
  }
}
