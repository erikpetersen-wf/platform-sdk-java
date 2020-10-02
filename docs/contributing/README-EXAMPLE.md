# Blob Storage client library for Java

Blob storage is an object storage solution for the cloud. Blob
storage is optimized for storing massive amounts of unstructured data.
Unstructured data is data that does not adhere to a particular data model or
definition, such as text or binary data.

[Source code](source) | [API reference documentation](docs) | [REST API documentation](rest_docs) | [Samples](samples)

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above

### Include the package

```xml
<dependency>
  <groupId>com.workiva.sdk</groupId>
  <artifactId>blob-storage</artifactId>
  <version>TBD</version>
</dependency>
```

## Key concepts

Blob storage is designed for:

- Serving images or documents directly to a browser.
- Storing files for distributed access.
- Streaming video and audio.
- Writing to log files.
- Storing data for backup and restore, disaster recovery, and archiving.
- Storing data for analysis by an on-premises or Azure-hosted service.

## Examples

The following sections provide several code snippets covering some of the most common Blob Storage tasks, including:

- [Creating BlobBatchClient](#creating-blobbatchclient)
- [Bulk Deleting Blobs](#bulk-deleting-blobs)
- [Bulk Setting AccessTier](#bulk-setting-accesstier)
- [Advanced Batching](#advanced-batching)

### Creating BlobBatchClient

Create a BlobBatchClient from a [BlobServiceClient][blob_service_client].

```java
BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
```

### Bulk Deleting Blobs

```java
blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
    System.out.printf("Deleting blob with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Bulk Setting AccessTier

```java
blobBatchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
    System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Advanced Batching

Deleting blobs in a batch that have different pre-requisites.

```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Delete a blob.
Response<Void> deleteResponse = blobBatch.deleteBlob(blobUrl);

// Delete a specific blob snapshot.
Response<Void> deleteSnapshotResponse =
    blobBatch.deleteBlob(blobUrlWithSnapshot, DeleteSnapshotsOptionType.ONLY, null);

// Delete a blob that has a lease.
Response<Void> deleteWithLeaseResponse =
    blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobRequestConditions()
        .setLeaseId("leaseId"));

blobBatchClient.submitBatch(blobBatch);
System.out.printf("Deleting blob completed with status code %d%n", deleteResponse.getStatusCode());
System.out.printf("Deleting blob snapshot completed with status code %d%n",
    deleteSnapshotResponse.getStatusCode());
System.out.printf("Deleting blob with lease completed with status code %d%n",
    deleteWithLeaseResponse.getStatusCode());
```

Setting `AccessTier` on blobs in batch that have different pre-requisites.

```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Set AccessTier on a blob.
Response<Void> setTierResponse = blobBatch.setBlobAccessTier(blobUrl, AccessTier.COOL);

// Set AccessTier on another blob.
Response<Void> setTierResponse2 = blobBatch.setBlobAccessTier(blobUrl2, AccessTier.ARCHIVE);

// Set AccessTier on a blob that has a lease.
Response<Void> setTierWithLeaseResponse = blobBatch.setBlobAccessTier(blobUrlWithLease, AccessTier.HOT,
    "leaseId");

blobBatchClient.submitBatch(blobBatch);
System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse.getStatusCode());
System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse2.getStatusCode());
System.out.printf("Set AccessTier on  blob with lease completed with status code %d%n",
    setTierWithLeaseResponse.getStatusCode());
```

## Troubleshooting

When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client.\

## Next steps

Get started with our Blob Storage [samples](src/samples).

## Contributing

This project welcomes contributions and suggestions.