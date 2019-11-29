package com.workiva.platform.servlet;

import com.workiva.platform.core.PlatformCore;

import java.util.Arrays;
import java.util.List;

import static com.workiva.platform.servlet.Platform.PATH_PREFIX;

class Utils {

  /**
   * Removes `/_wk` from endpoint path if present in the context.
   *
   * @param contextPath Context path for the servlet context handler.
   * @return List of endpoint path strings.
   */
  static List<String> stripPrefix(String contextPath) {
    String readyPath = PlatformCore.PATH_READY;
    String alivePath = PlatformCore.PATH_ALIVE;
    String statusPath = PlatformCore.PATH_STATUS;
    if (contextPath.startsWith(PATH_PREFIX)) {
      readyPath = readyPath.replace(PATH_PREFIX, "");
      alivePath = alivePath.replace(PATH_PREFIX, "");
      statusPath = statusPath.replace(PATH_PREFIX, "");
    }

    return Arrays.asList(readyPath, alivePath, statusPath);
  }
}
