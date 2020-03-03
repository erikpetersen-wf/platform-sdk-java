package com.workiva.platform.spring;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformStatus;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.*;

import java.util.concurrent.Callable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HealthCheckTest {

  private static PlatformMock platform;

  @BeforeClass
  public static void setup() {
    platform = new PlatformMock();
    platform.register(
        "don't care!",
        new Callable<PlatformStatus>() {
          @Override
          public PlatformStatus call() throws Exception {
            return new PlatformStatus();
          }
        });
  }

  @Test
  public void TestReadiness() throws Exception {
    HealthCheck.Controller controller = new HealthCheck.Controller(platform);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    mockMvc
        .perform(get("/_wk/ready").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void TestLiveness() throws Exception {
    HealthCheck.Controller controller = new HealthCheck.Controller(platform);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    mockMvc
        .perform(get("/_wk/alive").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void TestStatus() throws Exception {
    HealthCheck.Controller controller = new HealthCheck.Controller(platform);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    MvcResult result =
        mockMvc
            .perform(get("/_wk/status").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    String content = result.getResponse().getContentAsString();

    JSONObject wrapper = (JSONObject) JSONValue.parse(content);
    Assert.assertTrue(wrapper != null);

    JSONObject data = (JSONObject) wrapper.get("data");
    Assert.assertTrue(data != null);
    Assert.assertTrue(data.get("meta") == null);
    Assert.assertTrue(data.get("id") instanceof String);

    JSONObject attrs = (JSONObject) data.get("attributes");
    Assert.assertEquals(attrs.get("status"), PlatformStatus.PASSED);
    Assert.assertTrue(attrs.get("meta") == null);
  }

  /** Ensure if request originates from allowed ip, meta data included in response. */
  @Test
  public void TestStatusWithMeta() throws Exception {
    platform.setAllowedIPs("0.0.0.0");
    HealthCheck.Controller controller = new HealthCheck.Controller(platform);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    MvcResult result =
        mockMvc
            .perform(
                get("/_wk/status")
                    .header(PlatformCore.FORWARDED_FOR, "0.0.0.0")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    String content = result.getResponse().getContentAsString();

    JSONObject wrapper = (JSONObject) JSONValue.parse(content);
    Assert.assertTrue(wrapper != null);

    JSONObject data = (JSONObject) wrapper.get("data");
    Assert.assertTrue(data != null);
    Assert.assertTrue(data.get("meta") != null);
    Assert.assertTrue(data.get("id") instanceof String);

    JSONObject attrs = (JSONObject) data.get("attributes");
    Assert.assertEquals(attrs.get("status"), PlatformStatus.PASSED);
    Assert.assertTrue(attrs.get("meta") == null);
  }
}
