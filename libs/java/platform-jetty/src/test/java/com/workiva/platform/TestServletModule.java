package com.workiva.platform;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import javax.inject.Singleton;

public class TestServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bind(GuiceHealthCheck.class).in(Singleton.class);
    //    bind(DefaultServlet.class).in(Singleton.class);

    // Make the servlet use a special Guice container for the web app.
    serve("/*").with(GuiceContainer.class);
  }
}
