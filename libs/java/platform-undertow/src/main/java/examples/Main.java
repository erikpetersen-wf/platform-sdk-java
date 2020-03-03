package examples;

import com.workiva.platform.core.PlatformStatus;
import com.workiva.platform.undertow.Platform;

public class Main {

  public static void main(String[] args) {
    // Show it running with defaults.
    Platform platform = new Platform();
    platform.register("db", Main::myDbHealthCheck);
    platform.register("sqs", Main::mySqsHealthCheck);
    platform.start();
  }

  private static PlatformStatus myDbHealthCheck() {
    return new PlatformStatus("DB down.");
  }

  private static PlatformStatus mySqsHealthCheck() {
    return new PlatformStatus(true);
  }
}
