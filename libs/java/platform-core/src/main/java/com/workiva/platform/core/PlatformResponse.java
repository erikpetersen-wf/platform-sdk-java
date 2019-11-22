package com.workiva.platform.core;

public class PlatformResponse {
  private int code;
  private String body;

  public PlatformResponse(int code, String body) {
    this.code = code;
    this.body = body;
  }

  public PlatformResponse(int code) {
    this(code, null);
  }

  public int getCode() {
    return code;
  }

  public String getBody() {
    return body;
  }
}
