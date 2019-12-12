package com.workiva.platform.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.workiva.platform.core.PlatformCore;
import com.workiva.platform.core.PlatformResponse;

@Configuration
public class HealthCheck {

  @RestController
  public static class Controller {

    @Autowired private Platform platform;

    public Controller(Platform platform) {
      this.platform = platform;
    }

    @RequestMapping(value = PlatformCore.PATH_READY, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getReadiness() {
      PlatformResponse platformResponse = platform.ready();
      return ResponseEntity.status(platformResponse.getCode()).build();
    }

    @RequestMapping(value = PlatformCore.PATH_ALIVE, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getLiveness() {
      PlatformResponse platformResponse = platform.alive();
      return ResponseEntity.status(platformResponse.getCode()).build();
    }

    @RequestMapping(value = PlatformCore.PATH_STATUS, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getStatus() {
      PlatformResponse platformResponse = platform.status();
      return ResponseEntity.status(platformResponse.getCode()).body(platformResponse.getBody());
    }
  }
}
