Begin by adding a new directory in the directory group for the module, such as `sdk/<group>/<new module>`.

After the directory has been added there are a few required files that must be added, they are the following:

* `pom.xml` that uses `TBD` as its parent POM.
* `README.md` that uses the [README template](README-EXAMPLE.md).
* `src` directory containing the Java code for the module.

Add your new package into the modules configuration section of the `pom.xml` found in the root of the repository, this should be added based on the alphabetical sorting of your new package.

Please adhere to the [design principles](DESIGN.md) when adding new modules.

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
