package com.workiva.platform.core;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class PlatformCore {

  private volatile boolean isAlive;
  private Map<String, Callable<PlatformStatus>> aliveChecks;
  private Map<String, Callable<PlatformStatus>> readyChecks;
  private Map<String, Callable<PlatformStatus>> statusChecks;

  protected Set<String> allowedIPs;
  final DateTimeFormatter formatter;

  public static final String PATH_ALIVE = "/_wk/alive";
  public static final String PATH_READY = "/_wk/ready";
  public static final String PATH_STATUS = "/_wk/status";
  public static final String FORWARDED_FOR = "X-Forwarded-For";

  public PlatformCore() {
    isAlive = true;
    aliveChecks = new HashMap<>();
    readyChecks = new HashMap<>();
    statusChecks = new HashMap<>();

    String whitelist = System.getenv("NEW_RELIC_SYNTHETICS_IP_WHITELIST");
    allowedIPs = parseWhitelist(whitelist);
    formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
  }

  public void shutdown() {
    isAlive = false;
  }

  public PlatformResponse alive() {
    if (!isAlive) {
      return new PlatformResponse(418); // I'm as useful as a teapot.
    }
    try {
      for (Callable<PlatformStatus> check : aliveChecks.values()) {
        if (!check.call().isOK()) {
          return new PlatformResponse(500);
        }
      }
    } catch (Exception e) {
      return new PlatformResponse(500);
    }
    return new PlatformResponse(200);
  }

  public PlatformResponse ready() {
    try {
      for (Callable<PlatformStatus> check : readyChecks.values()) {
        if (!check.call().isOK()) {
          return new PlatformResponse(500);
        }
      }
    } catch (Exception e) {
      return new PlatformResponse(500);
    }
    return new PlatformResponse(200);
  }

  public PlatformResponse status(String forwardedFor) {
    JSONObject meta = new JSONObject();
    String status = PlatformStatus.PASSED;
    for (Map.Entry<String, Callable<PlatformStatus>> entry : statusChecks.entrySet()) {
      String key = entry.getKey();
      try {
        PlatformStatus value = entry.getValue().call();
        if (value.isOK()) {
          meta.put(key, PlatformStatus.PASSED);
        } else {
          meta.put(key, value.toString());
          status = PlatformStatus.FAILED;
        }
      } catch (Exception e) {
        meta.put(key, e.toString());
        status = PlatformStatus.FAILED;
      }
    }

    // Convert to JSON:API format JSON
    JSONObject wrapper = new JSONObject();
    JSONObject data = new JSONObject();
    JSONObject attrs = new JSONObject();
    wrapper.put("data", data);
    attrs.put("status", status);
    long ms = System.currentTimeMillis();
    data.put("id", formatter.format(new Date(ms).toInstant()));
    data.put("attributes", attrs);

    // Add protected metadata if we are an allowed IP address
    if (!meta.isEmpty() && allowedIPs.contains(forwardedFor)) {
      data.put("meta", meta);
    }

    return new PlatformResponse(200, wrapper.toJSONString());
  }

  public PlatformResponse status() {
    return status("0.0.0.0");
  }

  public void register(String name, Callable<PlatformStatus> status, PlatformCheckType type) {
    if (status == null) {
      return; // NOOP for null pointers
    }
    switch (type) {
      case ALIVE:
        if (aliveChecks.containsKey(name)) {
          throw new IllegalStateException("duplicate entry key");
        }
        aliveChecks.put(name, status);
        break;
      case READY:
        if (readyChecks.containsKey(name)) {
          throw new IllegalStateException("duplicate entry key");
        }
        readyChecks.put(name, status);
        break;
      default: // everything else is a status check
        if (statusChecks.containsKey(name)) {
          throw new IllegalStateException("duplicate entry key");
        }
        statusChecks.put(name, status);
        break;
    }
  }

  public void register(String name, Callable<PlatformStatus> callback) {
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
