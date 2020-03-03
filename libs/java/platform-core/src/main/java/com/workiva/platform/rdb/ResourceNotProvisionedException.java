package com.workiva.platform.rdb;

public class ResourceNotProvisionedException extends Exception {
  public ResourceNotProvisionedException(String errorMessage) {
    super(errorMessage);
  }
}
