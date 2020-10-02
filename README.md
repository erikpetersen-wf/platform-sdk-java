
# Platform SDK for Java

Welcome to the Workiva Software Development Platform!

![Software Development Lifecycle](docs/sdlc.png)

We provide the tools to help you write, test, package, provision, deploy, and monitor software at Workiva.

## Using the SDK

The recommended way to use the Platform SDK for Java in your project is to consume it from Maven. 

### Importing the BOM ####

To automatically manage module versions we recommend you use the Bill of Materials (bom) import as follows:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.workiva.sdk</groupId>
      <artifactId>bom</artifactId>
      <version>TBD</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Then individual models may omit the `version` from their dependency statement:

```xml
<dependencies>
  <dependency>
    <groupId>com.workiva.sdk</groupId>
    <artifactId>rdb</artifactId>
  </dependency>
  <dependency>
    <groupId>com.workiva.sdk</groupId>
    <artifactId>blob</artifactId>
  </dependency>
  <dependency>
    <groupId>com.workiva.sdk</groupId>
    <artifactId>kv</artifactId>
  </dependency>
</dependencies>
```
#### Individual Services ####

Alternatively you can add dependencies for the specific services you use only:

```xml
<dependency>
  <groupId>com.workiva.sdk</groupId>
  <artifactId>rdb</artifactId>
  <version>TBD</version>
</dependency>
<dependency>
  <groupId>com.workiva.sdk</groupId>
  <artifactId>blob</artifactId>
  <version>TBD</version>
</dependency>
```

#### Whole SDK ####

You can import the whole SDK into your project (includes *ALL* services). Please note that it is recommended to only import the modules you need.

```xml
<dependency>
  <groupId>com.workiva.sdk</groupId>
  <artifactId>sdk-all</artifactId>
  <version>TBD</version>
</dependency>
```

### Prerequisites

Java 8 or later is required.

## Available packages

All available packages can be found in the `/sdk` directory.

## How to reach us!

* Slack: `#support-wk-tools`
* Stakeholders: `Service Platform Stakeholder` meeting (every other Tuesday)

## Contributing

For details on contributing to this repository, see the [contributing guide](docs/contributing/README.md).

This project welcomes contributions and suggestions.