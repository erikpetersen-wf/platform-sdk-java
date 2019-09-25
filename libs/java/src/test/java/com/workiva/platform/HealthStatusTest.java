package com.workiva.platform;

import org.junit.Assert;
import org.junit.Test;

public class HealthStatusTest {

  @Test
  public void ctor() {
    HealthStatus status = new HealthStatus();
    Assert.assertEquals(true, status.isOk());
  }

  @Test
  public void ok() {
    HealthStatus status = new HealthStatus();
    status.notOk("Failed");
    Assert.assertEquals(false, status.isOk());
    status.ok();
    Assert.assertEquals(true, status.isOk());
  }

  @Test
  public void notOk() {
    HealthStatus status = new HealthStatus();
    status.notOk("Failed");
    Assert.assertEquals(false, status.isOk());
    Assert.assertEquals("Failed", status.failingReason);
  }
}
