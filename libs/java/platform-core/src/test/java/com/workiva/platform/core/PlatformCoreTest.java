package com.workiva.platform.core;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class PlatformCoreTest {

  @Test
  public void TestReady() {
    PlatformCore platform = new PlatformCore();

    int statusCode = platform.ready();
    Assert.assertEquals(statusCode, 200);
  }

  @Test
  public void TestReadyCheckFail() {
    PlatformCore platform = new PlatformCore();
    platform.register("don't care!", new PlatformStatus(false), PlatformCheckType.READY);
    Assert.assertEquals(platform.ready(), 500);
  }

  @Test
  public void TestReadyCheckPass() {
    PlatformCore platform = new PlatformCore();
    platform.register("don't care!", new PlatformStatus(true), PlatformCheckType.READY);
    Assert.assertEquals(platform.ready(), 200);
  }

  @Test
  public void TestAlive() {
    PlatformCore platform = new PlatformCore();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 200);
  }

  @Test
  public void TestAliveCheckFail() {
    PlatformCore platform = new PlatformCore();
    platform.register("don't care!", new PlatformStatus(false), PlatformCheckType.ALIVE);
    Assert.assertEquals(platform.alive(), 500);
  }

  @Test
  public void TestAliveCheckPass() {
    PlatformCore platform = new PlatformCore();
    platform.register("don't care!", new PlatformStatus(true), PlatformCheckType.ALIVE);
    Assert.assertEquals(platform.alive(), 200);
  }

  @Test
  public void TestShutdownHook() {
    PlatformCore platform = new PlatformCore();
    platform.shutdown();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 418);
  }

  @Test
  public void TestStatus() {
    PlatformCore platform = new PlatformCore();

    String expected = "{\n";
    expected += "\t\"data\": {\n";
    expected += "\t\t\"type\": \"status\",\n";
    expected += "\t\t\"id\": \"TODO\",\n";
    expected += "\t\t\"attributes\": {\n";
    expected += "\t\t\t\"status\": \"PASSED\"\n";
    expected += "\t\t},\n";
    expected += "\t\t\"meta\": {\n";
    expected += "\t\t}\n";
    expected += "\t}\n";
    expected += "}\n";

    PlatformResponse res = platform.status();
    Assert.assertEquals(res.code, 200);
    JSONObject wrapper = (JSONObject) JSONValue.parse(new String(res.body));
    Assert.assertTrue(wrapper != null);
    Assert.assertEquals(
        new String(res.body),
        "{\"data\":{\"attributes\":{\"status\":\"PASSED\"},\"id\":\"TODO\"}}");
  }

  @Test
  public void TestParseWhitelist() {
    Set<String> set = PlatformCore.parseWhitelist("WyIxLjEuMS4xIiwiMS4wLjAuMSJd");
    Assert.assertTrue(set.contains("1.1.1.1"));
    Assert.assertTrue(set.contains("1.0.0.1"));
    Assert.assertTrue(!set.contains("8.8.8.8"));
  }
}
