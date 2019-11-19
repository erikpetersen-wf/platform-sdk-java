package com.workiva.platform.core;

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
  public void TestAlive() {
    PlatformCore platform = new PlatformCore();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 200);
  }

  @Test
  public void TestShutdownHook() {
    PlatformCore platform = new PlatformCore();
    platform.shutdown();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 418);
  }
}
