package com.workiva.platform;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
@Singleton
public class GuiceHealthCheck {

  @Path(Platform.readinessPath)
  @GET
  public Response getReadiness() {
    return Response.ok().build();
  }

  @Path(Platform.livenessPath)
  @GET
  public Response getLiveness() {
    return Response.ok().build();
  }
}
