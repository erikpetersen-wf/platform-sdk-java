package com.workiva.platform.core;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

public class PlatformCoreTest {

  @Test
  public void TestReady() {
    PlatformCore platform = new PlatformCore();
    Assert.assertEquals(platform.ready().getCode(), 200);
  }

  @Test
  public void TestReadyCheckFail() {
    PlatformCore platform = new PlatformCore();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus(false);
          }
        },
        PlatformCheckType.READY);
    Assert.assertEquals(platform.ready().getCode(), 500);
  }

  @Test
  public void TestReadyCheckPass() {
    PlatformCore platform = new PlatformCore();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus();
          }
        },
        PlatformCheckType.READY);
    Assert.assertEquals(platform.ready().getCode(), 200);
  }

  @Test
  public void TestAlive() {
    PlatformCore platform = new PlatformCore();
    Assert.assertEquals(platform.alive().getCode(), 200);
  }

  @Test
  public void TestAliveCheckFail() {
    PlatformCore platform = new PlatformCore();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus(false);
          }
        },
        PlatformCheckType.ALIVE);
    Assert.assertEquals(platform.alive().getCode(), 500);
  }

  @Test
  public void TestAliveCheckPass() {
    PlatformCore platform = new PlatformCore();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus();
          }
        },
        PlatformCheckType.ALIVE);
    Assert.assertEquals(platform.alive().getCode(), 200);
  }

  @Test
  public void TestShutdownHook() {
    PlatformCore platform = new PlatformCore();
    platform.shutdown();
    Assert.assertEquals(platform.alive().getCode(), 418);
  }

  @Test
  public void TestStatus() {
    PlatformCore platform = new PlatformCore();
    PlatformResponse res = platform.status();
    Assert.assertEquals(res.getCode(), 200);

    JSONObject wrapper = (JSONObject) JSONValue.parse(res.getBody());
    Assert.assertTrue(wrapper != null);
    Assert.assertTrue(wrapper.get("meta") == null);

    JSONObject data = (JSONObject) wrapper.get("data");
    Assert.assertTrue(data != null);
    Assert.assertTrue(data.get("id") instanceof String);

    JSONObject attrs = (JSONObject) data.get("attributes");
    Assert.assertEquals(attrs.get("status"), PlatformStatus.PASSED);
    Assert.assertTrue(attrs.get("meta") == null);
  }

  @Test
  public void TestParseWhitelist() {
    Set<String> set = PlatformCore.parseWhitelist("WyIxLjEuMS4xIiwiMS4wLjAuMSJd");
    Assert.assertTrue(set.contains("1.1.1.1"));
    Assert.assertTrue(set.contains("1.0.0.1"));
    Assert.assertTrue(!set.contains("8.8.8.8"));
  }
}
