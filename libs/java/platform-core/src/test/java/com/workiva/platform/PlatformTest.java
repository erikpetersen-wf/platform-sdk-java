package com.workiva.platform;

import org.junit.Assert;
import org.junit.Test;

public class PlatformTest {

  @Test
  public void TestReady() {
    Platform platform = new Platform();

    int statusCode = platform.ready();
    Assert.assertEquals(statusCode, 200);
  }

  @Test
  public void TestAlive() {
    Platform platform = new Platform();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 200);
  }

  @Test
  public void TestShutdownHook() {
    Platform platform = new Platform();
    platform.shutdown();

    int statusCode = platform.alive();
    Assert.assertEquals(statusCode, 418);
  }
}
