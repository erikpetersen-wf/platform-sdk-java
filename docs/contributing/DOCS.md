Begin by adding a new directory in the directory group for the module, such as `sdk/<group>/<new module>`.

After the directory has been added there are a few required files that must be added, they are the following:

* `pom.xml` that uses `TBD` as its parent POM.
* `README.md` that uses the [README template](README-EXAMPLE.md).
* `src` directory containing the Java code for the module.

Add your new package into the modules configuration section of the `pom.xml` found in the root of the repository, this should be added based on the alphabetical sorting of your new package.

Please follow the [design principles](DESIGN.md) when adding new modules.

More specific guidelines follow below:

*The following guidelines were ported from the [Azure SDK Java guidelines](https://azure.github.io/azure-sdk/java_design.html).  Strict adherence to these guidelines is not currently required.  Over time, as these guidelines are massaged to fit Workiva's use case, adherence may become required.*

- [API Guidelines](#api-guidelines)
  * [Namespaces](#namespaces)
  * [Maven](#maven)
  * [Client interface](#client-interface)
  * [Model types](#model-types)
  * [Network requests](#network-requests)
  * [Authentication](#authentication)
  * [Response formats](#response-formats)
  * [Pagination](#pagination)
  * [Long running operations](#long-running-operations)
  * [Async API vs Sync API](#async-api-vs-sync-api)
  * [Service clients](#service-clients)
  * [Common service client patterns](#common-service-client-patterns)
  * [Service method parameters](#service-method-parameters)
  * [Model classes](#model-classes)
  * [Naming Patterns](#naming-patterns)
  * [Other Java API Guidance](#other-java-api-guidance)
  * [Versioning](#versioning)
- [Implementation](#implementation)
  * [Client configuration](#client-configuration)
  * [Service-specific environment variables](#service-specific-environment-variables)
  * [Parameter validation](#parameter-validation)
  * [Network requests](#network-requests-1)
  * [Authentication](#authentication-1)
  * [Native code](#native-code)
  * [Error handling](#error-handling)
  * [Logging](#logging)
  * [Distributed Tracing](#distributed-tracing)
  * [Testing](#testing)
- [Documentation](#documentation)
  * [Code samples](#code-samples)
  * [JavaDoc](#javadoc)

# API Guidelines

The API surface of your client library must have the most thought as it is the primary interaction that the consumer has with your service. 

## Namespaces

Java uses packages to group related types. Grouping services within a cloud infrastructure is common since it aids discoverability and provides structure to the reference documentation.

In Java, the namespace should be named `com.workiva.sdk.<group>.<service>[.<feature>]`. All consumer-facing APIs that are commonly used should exist within this package structure. Here:

* `<group>` is the group for the service
* `<service>` is the service name represented as a single word
* `<feature>` is an optional subpackage to break services into separate components (for example, storage may have .blob, .files, and .queues)

Other guidelines are as follows:

* Start the package with `com.workiva.sdk` to indicate an Workiva client library.
* Construct the package name with all lowercase letters (no camel case is allowed), without spaces, hyphens, or underscores.
* Choose the `<group>` from the following list:

| Namespace Group |                                    Functional Area |
|-----------------|---------------------------------------------------:|
| core            |                    Core functionality, such as TBD |
| data            | Dealing with structured data stores like databases |
| iam             |                   Authentication and authorization |

## Maven

* Each client library must have a `pom.xml`.
* Specify the `groupId` as `com.workiva.sdk`.
* Specify the `artifactId` to be of the form `<group>-<service>`, for example, `data-rdb`. In cases where the client library has multiple children modules, set the root POM `artifactId` to be of the form `<group>-<service>-parent`.

## Client interface

Your API surface consists of one or more service clients that the consumer will instantiate to connect to your service, plus a set of supporting types.

* Name service client types with the `Client` suffix.

* Place service client types that the consumer is most likely to interact with in the root package of the client library.

* Allow the consumer to construct a service client with the minimal information needed to connect and authenticate to the service.
   
## Model types
Client libraries represent entities transferred to and from Workiva services as model types. Certain types are used for round-trips to the service. They can be sent to the service (as an addition or update operation) and retrieved from the service (as a get operation). These should be named according to the type.

Data within the model type can generally be split into two parts - data used to support one of the champion scenarios for the service, and less important data. Given a type `Foo`, the less important details can be gathered in a type called `FooDetails` and attached to `Foo` as the details property.

For example:

```java
public class ConfigurationSettingDetails {
    private OffsetDateTime lastModifiedDate;
    private OffsetDateTime receivedDate;
    private ETag eTag;
}

public class ConfigurationSetting {
    private String key;
    private String value;
    private ConfigurationSettingDetails details;
}
```

Optional parameters and settings to an operation should be collected into an options bag named `<operation>Options`. For example, the `GetConfigurationSetting` method might take a `GetConfigurationSettingOptions` class for specifying optional parameters.

Results should use the model type (e.g. `ConfigurationSetting`) where the return value is a complete set of data for the model. However, in cases where a partial schema is returned, use the following types:

* `<model>Item` for each item in an enumeration if the enumeration returns a partial schema for the model. For example, `GetBlobs()` return an enumeration of `BlobItem`, which contains the blob name and metadata, but not the content of the blob.                                                             
* `<operation>Result` for the result of an operation. The `<operation>` is tied to a specific service operation. If the same result can be used for multiple operations, use a suitable noun-verb phrase instead. For example, use `UploadBlobResult` for the result from `UploadBlob`, but `ContainerChangeResult` for results from the various methods that change a blob container.

The following table enumerates the various models you might create:

| Model                 | Example              | Usage                                                                  |
|-----------------------|----------------------|------------------------------------------------------------------------|
| `<model>`             | `Secret`             | The full data for a resource                                           |
| `<model>Details`      | `SecretDetails`      | Less important details about a resource. Attached to `<model>.details` |
| `<model>Item`         | `SecretItem`         | A partial set of data returned for enumeration                         |
| `<operation>Options`  | `AddSecretOptions`   | Optional parameters to a single operation                              |
| `<operation>Result`   | `AddSecretResult`    | A partial or different set of data for a single operation              |
| `<model><verb>Result` | `SecretChangeResult` | A partial or different set of data for multiple operations on a model  |

## Network requests

?

## Authentication

?

## Response formats

Requests to the service fall into two basic groups: methods that make a single logical request, and methods that make a deterministic sequence of requests. An example of a single logical request is a request that may be retried inside the operation. An example of a deterministic sequence of requests is a paged operation.

The logical entity is a protocol neutral representation of a response. The logical entity may combine data from headers, body, and the status line. `Response<T>` is the 'complete response'. It contains HTTP headers, status code, and the `T` object (a deserialized object created from the response body). The `T` object would be the 'logical entity'.

* Return the logical entity for the normal form of a service method. The logical entity represents the information needed in the 99%+ case.

* Make it possible for a developer to access the complete response, including the status line, headers, and body.

* Return `Response<T>` on the maximal overload for a service method with `WithResponse` appended to the name. For example:

```java
Foo foo = client.getFoo(a);
Foo foo = client.getFoo(a, b);
Foo foo = client.getFoo(a, b, c, context); // This is the maximal overload
Response<Foo> response = client.getFooWithResponse(a, b, c, context);
```

* Provide examples on how to access the raw and streamed response for a request, where exposed by the client library. We don’t expect all methods to expose a streamed response.

* Provide a Java-idiomatic way to enumerate all logical entities for a paged operation, automatically fetching new pages as needed. For example:

```java
// Yes:
client.listSettings().forEach(this::print);

// No - don't force the caller of the library to do paging:
String nextPage = null;
while (!done) {
    Page<ConfigurationSetting> pageOfSettings = client.listSettings(nextPage);
    for (ConfigurationSetting setting : pageOfSettings) {
        print(setting);
    }
    nextPage = pageOfSettings.getNextPage();
    done = nextPage == null;
}    
```

## Pagination

?

## Long running operations

?

## Async API vs Sync API

?

## Service clients

* Name service client types with the Client suffix (for example, `ConfigurationClient`).

* Place service clients in the root package of their corresponding client library (for example, `com.workiva.sdk.blob.BlobClient`).

* Ensure that all service client classes are immutable upon instantiation.

* Use standard JavaBean naming prefixes for all getters and setters that are not service methods.

* Refrain from providing public or protected constructors in the service client, except where necessary to support mock testing. Keep visibility to a minimum.

## Common service client patterns

Prefer the use of the following terms for CRUD operations:

| Verb            | Parameters        | Returns                 | Comments                                                                                                               |
|-----------------|-------------------|-------------------------|------------------------------------------------------------------------------------------------------------------------|
| `upsert<noun>`  | key, item         | Updated or created item | Create new item or update existing item. Verb is primarily used in database-like services.                             |
| `set<noun>`     | key, item         | Updated or created item | Create new item or update existing item. Verb is primarily used for dictionary-like properties of a service.           |
| `create<noun>`  | key, item         | Created item            | Create new item. Fails if item already exists.                                                                         |
| `update<noun>`  | key, partial item | Updated item            | Fails if item doesn’t exist.                                                                                           |
| `replace<noun>` | key, item         | Replace existing item   | Completely replaces an existing item. Fails if the item doesn’t exist.                                                 |
| `delete<noun>`  | key               | Deleted item, or `null` | Delete an existing item. Will succeed even if item didn’t exist. Deleted item may be returned, if service supports it. |
| `add<noun>`     | index, item       | Added item              | Add item to a collection. Item will be added last, or into the index position specified.                               |
| `get<noun>`     | key               | Item                    | Will return null if item doesn’t exist.                                                                                |
| `list<noun>`    |                   | Items                   | Return list of items. Returns empty list if no items exist.                                                            |
| `<noun>Exists`  | key               | `boolean`               | Return `true` if the item exists.

*Note: Remain flexible and use names best suited for developer experience. Don’t let the naming rules result in non-idiomatic naming patterns.*

## Service method parameters
Service methods fall into two main groups when it comes to the number and complexity of parameters they accept:

* Service Methods with simple inputs, *simple* methods for short
* Service Methods with complex inputs, *complex* methods for short

`Simple methods` are methods that take up to six parameters, with most of the parameters being simple primitive types. Complex methods are methods that take a larger number of parameters and typically correspond to REST APIs with complex request payloads.

`Simple methods` should follow standard Java best practices for parameter list and overload design.

`Complex methods` should introduce an `option parameter` to represent the request payload. Consideration can subsequently be made for providing simpler convenience overloads for the most common scenarios.

```java
public class BlobContainerClient {

    // simple service methods
    public BlobInfo uploadBlob(String blobName, byte[] content);
    public Response<BlobInfo> uploadBlobWithResponse(String blobName, byte[] content, Context context);

    // complex service methods
    public BlobInfo createBlob(CreateBlobOptions options);
    public Response<BlobInfo> createBlobWithResponse(CreateBlobOptions options, Context context);

    // convenience overload[s]
    public BlobInfo createBlob(String blobName);
}

public class CreateBlobOptions {
    private String blobName;
    private PublicAccessType access;
    private Map<String, String> metadata;

    // Constructor enforces the requirement that blobName is always set
    public CreateBlobOptions(String blobName) {
        this.blobName = blobName;
    }

    public String getBlobName() {
        return blobName;
    }

    public CreateBlobOptions setAccess(PublicAccessType access) {
        this.access = access;
        return this;
    }

    public PublicAccessType getAccess() {
        return access;
    }

    public CreateBlobOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}   
```

* Name the options type after the name of the service method it is used for, such that the type is named <operation>Options. For example, above the method was createBlob, and so the options type was named CreateBlobOptions.

* Use the options parameter pattern for complex service methods.

If in common scenarios, users are likely to pass just a small subset of what the options parameter represents, consider adding an overload with a parameter list representing just this subset.

* Use the options parameter type, if it exists, for all `*WithResponse` methods. If no options parameter type exists, do not create one solely for the `*WithResponse` method.

* Place all options types in a root-level `options` package, to make options types distinct from service clients and model types.

* Design options types with the same design guidance as given below for model class types, namely fluent setters for optional arguments, using the standard JavaBean naming convention of `get*`, `set*`, and `is*`. Additionally, there may be constructor overloads for each combination of required arguments.

* Refrain from introducing method overloads that take a subset of the parameters as well as the options parameter. Option parameters must be the only argument to a method, apart from the Context type, which must continue to be outside the options type.

## Model classes

Model classes are classes that consumers use to provide required information into client library methods, or to receive information from Workiva services from client library methods. These classes typically represent the domain model.

* Provide public constructors for all model classes that a user is allowed to instantiate. Model classes that are not instantiable by the user, for example if they are model types returned from the service, should not have any publicly visible constructors.


* Refrain from offering builder classes for model classes.

* Put model classes that are intended as service return types only, and which have undesirable public API (which is not intended for consumers of the client library) into the `.implementation.models` package. In its place, an interface should be put into the public-facing `.models` package, and it should be this type that is returned through the public API to end users. Examples of situations where this is applicable include when there are constructors or setters on a type which receive implementation types, or when a type should be immutable but needs to be mutable internally. The interface should have the model type name, and the implementation (within `.implementation.models`) should be named `<interfaceName>Impl`.

**Note:** Extra caution must be taken to ensure that the returned interface has had consideration given to any future evolution of the interface, as growing an interface presents its own set of challenges.

* Use the JavaBean naming convention of `get*`, `set*`, and `is*`.

## Naming Patterns

Using a consistent set of naming patterns across all client libraries will ensure a consistent and more intuitive developer experience. This section outlines good practices for naming that must be followed by all client libraries.

* Prefer succinctness over verbosity, except when readability is impacted. A few examples include:

  * A class may want to return an identifier to a user. There is no additional value in the fully-qualified `getIdentifier()` compared with the shorter and equally-descriptive `getId()`.
  
  * A method called `getName()` is short, but may leave some doubt in the users mind about which name is being represented. Instead, naming this method `getLinkName()` will remove all doubt from the users mind, and without substantial additional verbosity. Similarly, in the case of `getId()` above, always choose to specify the identifier name if there is any likelihood of confusion about which identifier is being referenced. For example, use `getTenantId()` rather than `getId()`, unless it is completely unambiguous as to which identifier is being referenced.


* Refrain from using fully uppercase acronyms. APIs must take the form of `getHttpConnection()` or `getUrlName()` rather than `getHTTPConnection()` or `getURLName()`.

* Refrain from naming interface types with an 'I' prefix, e.g. `ISearchClient`. Instead, do not have any prefix for an interface, preferring `SearchClient` as the name for the interface type in this case.

* Use all upper-case names for enum (and 'expandable' enum) values. `EnumType.FOO` and `EnumType.TWO_WORDS` are valid, whereas `EnumType.Foo` and `EnumType.twoWords` are not).

## Other Java API Guidance

* Refrain from creating APIs that expose the old Java date library (e.g. `java.util.Date`, `java.util.Calendar`, and `java.util.Timezone`). All API must use the new date / time APIs that shipped in JDK 8 in the `java.util.time` package.

* Refrain from creating APIs that expose the `java.net.URL` API. This API is difficult to work with, and more frequently gets in the users way rather than provide any real assistance. Instead, use the String type to represent the URL. When it is necessary to parse this String into a URL, and if it fails to be parsed (throwing a checked `MalformedURLException`), catch this internally and throw an unchecked `IllegalArgumentException` instead.

* Represent file paths using the Java `java.nio.file.Path` type. Do not use String or the older `java.io.File` type.

## Versioning
   
* Strive to be 100% backwards compatible with older versions of the same package.
   
* Call the highest supported service API version by default.
   
* Allow the consumer to explicitly select a supported service API version when instantiating the service client.
   
* Offer a `getLatest()` method on the `enum` that returns the latest service version. If a consumer doesn't specify a service version, the builder will call `getLatest()` to obtain the appropriate service version.
   
* Use the version naming used by the service itself in naming the version values in the `enum`. The standard approach takes the form `V<year>_<month>_<day>`, such as `V2019_05_09`. Being consistent with the service naming enables easier cross-referencing between service versions and the availability of features in the client library.

# Implementation

When configuring your client library, particular care must be taken to ensure that the consumer of your client library can properly configure the connectivity to your Workiva service both globally (along with other client libraries the consumer is using) and specifically with your client library.

## Client configuration

* Use relevant global configuration settings either by default or when explicitly requested to by the user, for example by passing in a configuration object to a client constructor.

* Allow different clients of the same type to use different configurations.

* Allow consumers of your service clients to opt out of all global configuration settings at once.

* Allow all global configuration settings to be overridden by client-provided options. The names of these options should align with any user-facing global configuration keys.

## Service-specific environment variables
   
* Use this syntax for environment variables specific to a particular Azure service:

  * `<ServiceName>_<ConfigurationKey>`
  
where *ServiceName* is the canonical shortname without spaces, and *ConfigurationKey* refers to an unnested configuration key for that client library.

* Refrain from using non-alpha-numeric characters in your environment variable names with the exception of underscore. This ensures broad interoperability.


## Parameter validation

The service client will have several methods that perform requests on the service. Service parameters are directly passed across the wire to an Azure service. Client parameters are not passed directly to the service, but used within the client library to fulfill the request. Examples of client parameters include values that are used to construct a URI, or a file that needs to be uploaded to storage.
   
* Validate client parameters.

* Validate the developer experience when the service parameters are invalid to ensure appropriate error messages are generated by the service. If the developer experience is compromised due to service-side error messages, work with the service team to correct prior to release.
   
* Refrain from validating service parameters. This includes null checks, empty strings, and other common validating conditions. Let the service validate any request parameters.
   
## Network requests

?

## Authentication

?  
   
## Native code

Native code plugins cause compatibility issues and require additional scrutiny. Certain languages compile to a machine-native format (for example, C or C++), whereas most modern languages opt to compile to an intermediary format to aid in cross-platform support.
   
* Refrain from writing platform-specific / native code.
   
## Error handling

Error handling is an important aspect of implementing a client library. It is the primary method by which problems are communicated to the consumer. There are two methods by which errors are reported to the consumer. Either the method throws an exception, or the method returns an error code (or value) as its return value, which the consumer must then check. In this section we refer to “producing an error” to mean returning an error value or throwing an exception, and “an error” to be the error value or exception object.

* Prefer the use of exceptions over returning an error value when producing an error.

* Produce an error when any HTTP request fails with an HTTP status code that is not defined by the service/Swagger as a successful status code. These errors should also be logged as errors.

* Use unchecked exceptions for HTTP requests. Java offers checked and unchecked exceptions, where checked exceptions force the user to introduce verbose `try .. catch` code blocks and handle each specified exception. Unchecked exceptions avoid verbosity and improve scalability issues inherent with checked exceptions in large apps.

* Ensure that the error produced contains the HTTP response (including status code and headers) and originating request (including URL, query parameters, and headers).

In the case of a higher-level method that produces multiple HTTP requests, either the last exception or an aggregate exception of all failures should be produced.

* Ensure that if the service returns rich error information (via the response headers or body), the rich information must be available via the error produced in service-specific properties/fields.

* Refrain from creating a new error type when a language-specific error type will suffice. Use system-provided error types for validation.

* Use the following standard Java exceptions for pre-condition checking:

| Exception                       | When to use                                                  |
|---------------------------------|--------------------------------------------------------------|
| `IllegalArgumentException`      | When a method argument is non-null, but inappropriate        |
| `IllegalStateException`         | When the object state means method invocation can’t continue |
| `NullPointerException`          | When a method argument is `null` and `null` is unexpected        |
| `UnsupportedOperationException` | When an object doesn’t support method invocation             |

* Document the errors that are produced by each method (with the exception of commonly thrown errors that are generally not documented in the target language).

* Specify all checked and unchecked exceptions thrown in a method within the JavaDoc documentation on the method as `@throws` statements.                

## Logging

?

Note that static loggers are shared among all client library instances running in a JVM instance. Static loggers should be used carefully and in short-lived cases only.

* Use one of the following log levels when emitting logs: `Verbose` (details), `Informational` (things happened), `Warning` (might be a problem or not), and `Error`.

* Use the `Error` logging level for failures that the application is unlikely to recover from (out of memory, etc.).

* Use the `Warning` logging level when a function fails to perform its intended task. This generally means that the function will raise an exception. Do not include occurrences of self-healing events (for example, when a request will be automatically retried).

* You may log the request and response (see below) at the `Warning` logging level when a request/response cycle (to the start of the response body) exceeds a service-defined threshold. The threshold should be chosen to minimize false-positives and identify service issues.

* Use the `Informational` logging level when a function operates normally.

* Use the `Verbose` logging level for detailed troubleshooting scenarios. This is primarily intended for developers or system administrators to diagnose specific failures.

* Only log headers and query parameters that are in a service-provided “allow-list” of approved headers and query parameters. All other headers and query parameters must have their values redacted.

* Log an `Informational` message if a service call is cancelled. The log should include:

  * The SDK provided request ID (?).
  * The reason for the cancellation (if available).

* Log exceptions thrown as a `Warning` level message. If the log level set to `Verbose`, append stack trace information to the message.

## Distributed Tracing

? 

## Testing

One of the key things we want to support is to allow consumers of the library to easily write repeatable unit-tests for their applications without activating a service. This allows them to reliable and quickly test their code without worrying about the vagaries of the underlying service implementation (including, for example, network conditions or service outages). Mocking is also helpful to simulate failures, edge cases, and hard to reproduce situations (for example: does code work on February 29th).
   
* Support mocking of network operations.
   
* Parameterize all applicable unit tests to make use of all available HTTP clients and service versions. Parameterized runs of all tests must occur as part of live tests. Shorter runs, consisting of just Netty and the latest service version, can be run whenever PR validation occurs.
 
# Documentation

There are several pieces of documentation that must be included with your client library. Beyond complete and helpful API documentation within the code itself (`JavaDoc`), you need a great README and other supporting documentation.   

## Code samples

Code samples are small applications that demonstrate a certain feature that is relevant to the client library. Samples allow developers to quickly understand the full usage requirements of your client library. Code samples shouldn’t be any more complex than necessary to demonstrate the feature. Don’t write full applications. Samples should have a high signal to noise ratio between useful code and boilerplate code for non-related reasons.

* Include code samples alongside your library’s code within the repository. The samples should clearly and succinctly demonstrate the code most developers need to write with your library. Include samples for all common operations. Pay attention to operations that are complex or might be difficult for new users of your library. Include samples for the champion scenarios you’ve identified for the library.

* Place code samples within the `/src/samples/java` directory within the client library root directory. The samples will be compiled, but not packaged into the resulting jar.

* Ensure that each sample file is executable by including a `public static void main(String[] args)` method.

* Use the latest coding conventions when creating samples. Make liberal use of modern Java syntax and APIs (for example, diamond operators) as they remove boilerplate from your samples.

* Compile sample code using the latest major release of the library. Review sample code for freshness.

* Ensure that code samples can be easily grafted from the documentation into a users own application. For example, don’t rely on variable declarations in other samples.

* Write code samples for ease of reading and comprehension over code compactness and efficiency.

* Build and test your code samples using the repository’s continuous integration (CI) to ensure they remain functional.

* Refrain from combining multiple operations in a code sample unless it’s required for demonstrating the type or member.

## JavaDoc

* Ensure that anybody can clone the repo containing the client library and execute `mvn javadoc:javadoc` to generate the full and complete JavaDoc output for the code, without any need for additional processing steps.

* Include code samples in all class-level JavaDoc, and in relevant method-level JavaDoc.

* Refrain from hard-coding the sample within the JavaDoc (where it may become stale). Put code samples in `/src/samples/java` and use the available tooling to reference them.
