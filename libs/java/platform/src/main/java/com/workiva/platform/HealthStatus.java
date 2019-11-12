package com.workiva.platform;

public class HealthStatus {
  public static final Boolean OK = true;
  private Boolean status;
  public String failingReason;

  public HealthStatus(Boolean status) {
    this.status = status;
    this.failingReason = "";
  }

  public HealthStatus() {
    this(true);
  }

  public void ok() {
    failingReason = "";
    status = true;
  }

  public void notOk(String reason) {
    failingReason = reason;
    status = false;
  }

  public Boolean isOk() {
    return this.status == OK;
  }
}
