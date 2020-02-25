package com.workiva.platform.rdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provide basic functionality to work with database resources provisioned with
 * consolidated-rds service.  In the future this logic will likely be provided
 * through the Data SDK.
 */
public class RelationalDatabase {

  private String serviceName;
  private String hostName;
  private String username;
  private String password;

  public RelationalDatabase() {
    serviceName = System.getenv("WORKIVA_SERVICE_NAME");
    hostName = System.getenv("RDS_HOST");
    username = System.getenv("RDS_USER");
    password = System.getenv("RDS_PASSWORD");
  }

  public Connection getConnection() throws SQLException {
    Properties connectionProps = new Properties();
    connectionProps.setProperty("user", username);
    connectionProps.setProperty("password", password);
    connectionProps.setProperty("useSSL", "true");
    connectionProps.setProperty("requireSSL", "true");
    connectionProps.setProperty("enabledTLSProtocols", "TLSv1.2");

    String connectionUrl = String.format("jdbc:mysql://%s:3306/%s", hostName, serviceName);

    return DriverManager.getConnection(connectionUrl, connectionProps);
  }
}
