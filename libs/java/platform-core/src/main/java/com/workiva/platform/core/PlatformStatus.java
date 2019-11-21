package com.workiva.platform.core;

public class PlatformStatus {
  public static final String FAILED = "FAILED";
  public static final String PASSED = "PASSED";

  public String status;

  public PlatformStatus(String status) {
    this.status = status;
  }

  public PlatformStatus() {
    this.status = null;
  }

  public PlatformStatus(Boolean isOK) {
    if (isOK) {
      this.status = null;
    } else {
      this.status = FAILED;
    }
  }

  public Boolean isOK() {
    return status == null || status.length() == 0;
  }

  public void pass() {
    status = null;
  }

  public void fail(String reason) {
    if (reason == null || reason.length() == 0) {
      status = FAILED;
    } else {
      status = reason;
    }
  }

  public void fail() {
    fail(null);
  }
}
