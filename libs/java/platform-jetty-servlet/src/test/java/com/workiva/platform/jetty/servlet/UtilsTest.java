package com.workiva.platform.jetty.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UtilsTest {

  static String PATH_READY_STRIPPED = Platform.PATH_READY.substring(4);
  static String PATH_ALIVE_STRIPPED = Platform.PATH_ALIVE.substring(4);
  static String PATH_STATUS_STRIPPED = Platform.PATH_STATUS.substring(4);

  @Test
  public void TestStripPrefixDoesNothing() {
    List<String> result = Utils.stripPrefix("/*");

    Assert.assertEquals(result.get(0), Platform.PATH_READY);
    Assert.assertEquals(result.get(1), Platform.PATH_ALIVE);
    Assert.assertEquals(result.get(2), Platform.PATH_STATUS);
  }

  @Test
  public void TestStripPrefixReplacesWk() {
    List<String> result = Utils.stripPrefix("/_wk");

    Assert.assertEquals(result.get(0), PATH_READY_STRIPPED);
    Assert.assertEquals(result.get(1), PATH_ALIVE_STRIPPED);
    Assert.assertEquals(result.get(2), PATH_STATUS_STRIPPED);

    result = Utils.stripPrefix("/_wk/");

    Assert.assertEquals(result.get(0), PATH_READY_STRIPPED);
    Assert.assertEquals(result.get(1), PATH_ALIVE_STRIPPED);
    Assert.assertEquals(result.get(2), PATH_STATUS_STRIPPED);

    result = Utils.stripPrefix("/_wk/*");

    Assert.assertEquals(result.get(0), PATH_READY_STRIPPED);
    Assert.assertEquals(result.get(1), PATH_ALIVE_STRIPPED);
    Assert.assertEquals(result.get(2), PATH_STATUS_STRIPPED);
  }
}
