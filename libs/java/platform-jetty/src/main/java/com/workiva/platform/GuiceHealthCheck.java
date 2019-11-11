package com.workiva.platform;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
@Singleton
public class GuiceHealthCheck {

  @Path(Constants.readinessPath)
  @GET
  public Response getReadiness() {
    return Response.ok().build();
  }

  @Path(Constants.livenessPath)
  @GET
  public Response getLiveness() {
    return Response.ok().build();
  }
}
