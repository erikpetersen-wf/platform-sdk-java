package com.workiva.platform.spring;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HealthCheckTest {

  @Test
  public void TestReadiness() throws Exception {
    Platform core = new Platform();
    HealthCheck.Controller controller = new HealthCheck.Controller(core);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    mockMvc
        .perform(get("/_wk/ready").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void TestLiveness() throws Exception {
    Platform core = new Platform();
    HealthCheck.Controller controller = new HealthCheck.Controller(core);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    mockMvc
        .perform(get("/_wk/alive").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void TestStatus() throws Exception {
    Platform core = new Platform();
    HealthCheck.Controller controller = new HealthCheck.Controller(core);
    StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
    MockMvc mockMvc = mockMvcBuilder.build();
    mockMvc
        .perform(get("/_wk/status").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
