package com.workiva.platform.core;

public class PlatformStatus {
  public static final String FAILED = "FAILED";
  public static final String PASSED = "PASSED";

  private String status;

  // NOTE: failureReason is logged to our infrastructure.
  // WARNING: DO NOT INCLUDE SENSITIVE DATA IN FAILURE_REASON!
  public PlatformStatus(String failureReason) {
    this.status = failureReason;
  }

  public PlatformStatus() {
    this(null);
  }

  public PlatformStatus(boolean isOK) {
    if (isOK) {
      this.status = null;
    } else {
      this.status = FAILED;
    }
  }

  public boolean isOK() {
    return status == null || status.length() == 0;
  }

  public String toString() {
    if (isOK()) {
      return PASSED;
    } else {
      return status;
    }
  }
}
