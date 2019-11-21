package com.workiva.platform.core;

public class PlatformResponse {
  public int code;
  public byte[] body;

  PlatformResponse(int code, byte[] body) {
    this.code = code;
    this.body = body;
  }

  PlatformResponse(int code) {
    this(code, null);
  }
}
