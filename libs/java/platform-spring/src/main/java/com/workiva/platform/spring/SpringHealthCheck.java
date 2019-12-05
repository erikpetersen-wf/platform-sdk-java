package com.workiva.platform.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.workiva.platform.core.PlatformCore;

@Configuration
public class SpringHealthCheck {

  @RestController
  public static class Controller {

    @RequestMapping(value = PlatformCore.PATH_READY, method = RequestMethod.GET)
    public void getReadiness() {}

    @RequestMapping(value = PlatformCore.PATH_ALIVE, method = RequestMethod.GET)
    public void getLiveness() {}

    @RequestMapping(value = PlatformCore.PATH_STATUS, method = RequestMethod.GET)
    public void getStatus() {}
  }
}
