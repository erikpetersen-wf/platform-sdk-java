# Software Development Platform

Welcome to the Workiva Software Development Platform!

![Software Development Lifecycle](docs/sdlc.png)

We provide the tools to help you write, test, package, provision, deploy and monitor software at Workiva.


## Platform Components

As with any Software Development Platform, not all things can be in one location or managed together in a single repo.
As such, this repo serves as a "jumping off point" for engineers at Workiva looking to build their software with the highest quality standards.
Below, is a list of various components necessary to build software at Workiva.

* Write
  * Setup Environment: https://dev.webfilings.org/
  * [Builder](#builder)
  * Language SDKs
    * [Go](platform.go) (TODO: link to the developer docs)
    * [Java](libs/java) ([documentation](#java))
    * Dart (TODO)
    * Python (TODO)
  * Security Guidelines (TODO)
  * StyleGuides (TODO)
* Test
  * Test Eng Stuff (TODO)
  * Skynet Stuff (TODO)
  * Signals Stuff (TODO)
* Package
  * [Packager](package)
  * [Aviary](https://dev.workiva.net/docs/teams/information-security/aviary)
* Provision
  * CloudFormation Stuff (TODO)
  * MARV Stuff (TODO)
* Deploy
  * Release Pipelines (TODO)
* Monitor
  * [New Relic](https://insights.newrelic.com/accounts/2361833/dashboards/949872)

## Builder

The platform now has a builder to help with various Helm (future: and cloudformation) asset generation.

To use this builder, add the following to the top of your `Dockerfile`:

```
FROM drydock-prod.workiva.net/workiva/platform:v0
```

If you already have the platform builder in your repo, you can remove any `ADD`, `RUN` and `ARG` commands after it, and leave it for the platform `ONBUILD` commands to handle.

Pinning to `v0` allows us to push full platform level changes across all Workiva during the bi-weekly security rebuild updates now required by FEDRAMP Moderate without needing to make PRs into all platform repos.  This also gives us the flexibility to change how this works completely and bump to `v1`.

This build phase will execute the code in [platform](package).  It will look for a `livenessProbe` and `readinessProbe` definition in your Helm chart.  If it does not find one it will append it to your Helm chart as a build artifact.  The appended probes will use default paths of `_wk/ready` and `_wk/alive`.  The build phase will search your `Dockerfile` for an `EXPOSE` command and use that as the port.  If it does not find an `EXPOSE` command the port will default to `8888`.

*Note*: This assumes your `helm` folder exists at the root of your repository and that your `Dockerfile` is named as such (ie: not `workivabuild.Dockerfile` or anything non-standard).

## Java
The Java SDK comes in the following varieties:
* [platform-core](#platform-core)
* [platform-undertow](#platform-undertow)
* [platform-jetty-servlet](#platform-jetty-servlet)
* [platform-netty](#platform-netty)
* [platform-spring](#platform-spring)

Each of these libraries will register three publicly exposed endpoints: `_wk/ready`, `_wk/alive`, and `_wk/status` used for your readiness probe, liveness probe, and availability checks respectively.

### Platform Core
This library contains the core logic from which all other libraries inherit.

Included within the core library are the [constants](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCore.java#L27) for the `readiness`, `liveness`, and `status` endpoints.  These endpoints are consumed by the other libraries.

The core library also implements the logic for each of those checks.

#### Alive / Ready
The [alive](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCore.java#L47) and [ready](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCore.java#L63) functions are used for your [liveness/readiness](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-a-liveness-command) probes.  These functions simply returns a `200` response if your container is reachable and can return a response.

#### Status
The [status](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCore.java#L76) function is used to check the availability of third-party dependencies of your service.

Invididual checks can be registered with the [`register`](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCore.java#L117) function like:
```java
platform.register("status", testFunction);

PlatformStatus testFunction() {
    return new PlatformStatus(false);
}
```

As you can see, the function must return type [PlatformStatus](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformStatus.java).

An overloaded `register` function exists that allows you to specify the [PlatformCheckType](https://github.com/Workiva/platform/blob/master/libs/java/platform-core/src/main/java/com/workiva/platform/core/PlatformCheckType.java). This enables you to register individual checks for the readiness/liveness probes.  It is normally [not recommended](https://w-dev-blog.appspot.com/posts/2019/07/15/kubernetes-liveness-readiness-best-practices/index.html) to register individual checks for anything but the `status` endpoint.

*Note: The core library is not meant to be consumed directly unless none of the other libraries satisfy your use case (ie: [here](https://github.com/Workiva/sa-tools-changeset-service/blob/master/src/main/java/com/workiva/satools/Main.java#L70)).*

### Platform Undertow
This library spins up the health checks using an independent HTTP server called Undertow.  Use this library if your service does not already have its own HTTP server.

#### Example Usage
```java
Platform platform = new Platform();
platform.register("check", ...);
platform.start();
```

### Platform Netty
This library spins up the health checks using an existing Netty HTTP server.

#### Example Usage
```java
Platform platform = new Platform();
platform.register("check", ...);
EventLoopGroup bossGroup = new NioEventLoopGroup();
EventLoopGroup workerGroup = new NioEventLoopGroup();

 ServerBootstrap b = new ServerBootstrap();
 b.group(bossGroup, workerGroup)
  .channel(NioServerSocketChannel.class)
  .childHandler(
      new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) {
          // codec+aggregator needed to make a FullHttpRequest.
          ch.pipeline().addLast("codec", new HttpServerCodec());
          ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024 * 10));
          ch.pipeline().addLast(platform.getHandler());
        }
      });
```

### Platform Jetty Servlet
This library spins up the health checks using an existing Jetty HTTP handler.  There are *two* ways this library can be used.

#### Example Usage
```java
Server server = new Server(8080);
Platform platform = new Platform();
platform.register("check", ...);
ServletContextHandler handler = platform.registerEndpoints();
server.setHandler(handler);
server.start();
```

#### Example Usage (passing in your own `ServletContextHandler`)
```java
Server server = new Server(8080);
ServletContextHandler handler = new ServletContextHandler(null, "/_wk/");
Platform platform = new Platform();
platform.register("check", ...);
platform.registerEndpoints(handler);
server.setHandler(handler);
server.start();
```

### Platform Spring
This library spins up the health checks using an existing Spring configuration.

#### Example Usage (Spring Boot 2.x)
```java
@EnablePlatform
public class Application {
  public static void main(String[] args) throws Exception {
    ApplicationContext context = SpringApplication.run(Application.class, args);
    Platform platform = context.getBean(Platform.class);
    platform.register("check", ...);
  }
}
```

#### Example Usage (Spring Boot 1.x)
Uses the same setup as 2.x with the exception you won't be able to access the bean after making the call to `SpringApplication.run(...)`.  Instead what you need is another class that does this:
```java
@Component
public class PlatformStartup
    implements ApplicationListener<ContextRefreshedEvent> {
    
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Platform platform = applicationContext.getBean(Platform.class);
        platform.register("check", ...);
    }
```

## Architecture Decision Records

* [Whats an ADR?](docs/adr/readme.md)
* [Whats with the `platform.Main` context?](docs/adr/platform_main_context.md)
* [Why does `check.Register` update a `status`?](docs/adr/check_naming.md)

If you still have questions, below is a list of other ways to reach us.


## How to reach us!

* Slack: `#support-sapi-platform`
* Guild: `Services Guild` (every other Monday)
* Stakeholders: `Service & API Platform Stakeholders` (every other Wednesday)

<!-- ## [Start Here](https://dev.webfilings.org/)

Platform
================

Services
------------
[Linking](https://github.com/workiva/linking)

[Identity](https://github.com/Workiva/Identity)

[Permissions](https://github.com/Workiva/OmniCorp/)

[Audit](https://github.com/Workiva/OmniCorp/)

[Config](https://github.com/Workiva/OmniCorp/)

[Email](https://github.com/Workiva/OmniCorp/)

[Server Scaling](https://github.com/Workiva/bolt)

Application Platforms and Frameworks
-------------
[H5 Application Platform](https://github.com/workiva/H5ClientPlatform)

[UI Platform](https://github.com/Workiva/w-ui-platform)

Business Platforms
---------------------
[Rich Content Platform](https://github.com/workiva/content)

Platform Specs
---
[platform-specs](https://drive.google.com/drive/folders/0B5BHXAruc8vBNDJlX29XajEwNGc) folder

[Application Logging Spec](https://github.com/Workiva/platform/blob/master/specs/app/logging/1.0.0/spec.md)

[Application Telemetry Spec](https://github.com/Workiva/platform/blob/master/specs/app/telemetry/0.0.1/spec.md)

[Gen2 Telemetry Specification](https://drive.google.com/open?id=1g0QUF0kRRKRzODOP0-W2feL_6QjQxSLl-r5cnZXaUTc)

[Harbour Logging](https://docs.google.com/a/webfilings.com/document/d/1TbPq5Erb1J26BltBNBlpUj53Zt4Lr_ZyJGaMxjmL9UI/edit?usp=sharing)

[Vessel Pub/Sub Channel Spec](https://docs.google.com/a/webfilings.com/document/d/1YyI14WkxdBvuQPx1_rHco7rSb_Qs7rzAuHiVGM2jmmU/edit?usp=sharing)

Teams:
=================

[OmniCorp](https://github.com/Workiva/OmniCorp/)

[Messaging](https://github.com/Workiva/messaging) -->
