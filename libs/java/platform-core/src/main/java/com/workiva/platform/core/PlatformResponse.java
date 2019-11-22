package com.workiva.platform.core;

public class PlatformResponse {
  public int code;
  public byte[] body;

  public PlatformResponse(int code, byte[] body) {
    this.code = code;
    this.body = body;
  }

  public PlatformResponse(int code) {
    this(code, null);
  }
}
