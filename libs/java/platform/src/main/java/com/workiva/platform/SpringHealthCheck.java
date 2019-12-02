package com.workiva.platform;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Configuration
public class SpringHealthCheck {

  @RestController
  public static class Controller {

    @RequestMapping(value = Platform.readinessPath, method = RequestMethod.GET)
    public void getReadiness() {}

    @RequestMapping(value = Platform.livenessPath, method = RequestMethod.GET)
    public void getLiveness() {}

    @RequestMapping(value = Platform.statusPath, method = RequestMethod.GET)
    public void getStatus() {}
  }
}
