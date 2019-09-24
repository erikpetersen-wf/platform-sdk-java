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
  * [Build Phase](#build)
  * Language SDKs
    * [Go](platform.go) (TODO: link to the developer docs)
    * [Java](lib/src/java) ([documentation](#java))
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

## Build
The build phase is integrated through your service by adding these lines to your `Dockerfile`.

```Dockerfile
FROM drydock-prod.workiva.net/workiva/platform:v0 as platform
ADD ./server/helm/ /build/
ADD workivabuild.Dockerfile /build/Dockerfile
RUN package
```

This build phase will execute the code in [platform](package).  It will look for a `livenessProbe` and `readinessProbe` definition in your Helm chart.  If it does not find one it will append it to your Helm chart as a build artifact.  If the probes are appended to your chart via as part of the build, the paths will be `_wk/ready` and `_wk/alive`.  The build phase will search your `Dockerfile` for an `EXPOSE` command and use that as the port.  If it does not find an `EXPOSE` command we will default to port `8888`.

## Java
### Adding the dependency
```xml
<dependency>
    <groupId>com.workiva.platform</groupId>
    <artifactId>platform</artifactId>
    <version>0.0.10</version>
</dependency>
```
### Running the platform
Running `Platform.builder().start()` will setup an HTTP server with the following routes for your service:
* Readiness probe running at `_wk/ready` on port `8888`.
* Liveness probe running at `_wk/alive` on port `8888`.

### Override defaults
The port and the functions that are run can all be overridden if necessary.

#### Port
Global Override
```java
Platform.builder().port(9999).start();
```

Individual Override
```java
Platform.builder().livenessPort(9999).start();
```
```java
Platform.builder().readinessPort(9999).start();
```

#### Function
Global Override
```java
Platform.builder().function(() -> myFunction())).start();
```

Individual Override
```java
Platform.builder().livenessFunction(() -> myLivenessFunction()).start();
```
```java
Platform.builder().readinessFunction(() -> myReadinessFunction()).start();
```

If your function returns successfully, the probe will return a `200`.  If your function returns an exception, the probe will return a `503`. 

## Architecture Decision Records

* [Whats an ADR?](docs/adr/readme.md)
* [Whats with the `platform.Main` context?](docs/adr/platform_main_context.md)
* [Why does `check.Register` update a `status`?](docs/adr/check_naming.md)

If you still have questions, below is a list of other ways to reach us.


## How to reach us!

* Slack: `#support-sapi-platform`
* Guild: `Services Guild` (every other Monday)
* Stakeholders: `Service & API Platform Stakeholders` (every other Wednesday)


## Builder

The platform now has a builder to help with various HELM (future: and cloudformation) asset generation.

To use this builder, add the following to the top of your Dockerfile:

```
FROM drydock-prod.workiva.net/workiva/platform:v0 as platform
```

If you already have the platform builder in your repo, you can remove any `ADD`, `RUN` and `ARG` commands after it, and leave it for the platform `ONBUILD` commands to handle.

Pinning to `v0` allows us to push full platform level changes across all Workiva during the bi-weekly security rebuild updates now required by FEDRAMP Moderate without needing to make PRs into all platform repos.  This also gives us the flex-ability to change how this works completely and bump to `v1`.

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
