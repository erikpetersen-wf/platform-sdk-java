# What's with the `platform.Main` context?
Status: proposed

## Context
<what is the issue that we're seeing that is motivating this decision or change.>

Longterm, there should be no required parameters for the `platform.Main`.
This is because of the `workiva.yml` idea (TODO: link ADR) where these parameters come from a central configuration file.
But, to ease the transition, teams need a way to provide a PORT to host the server on.

There should be no required parameters for `platform.Main` (longterm).
There is no way to deduce required values at runtime (currently).

Seeing a few comments like this come through the support chat:

> Is `WithPort` a naming convention typical around the use of go contexts?
> It reads a bit clunkuly when passed into `platform.Main(...)` like https://github.com/Workiva/blob-storage/pull/311/files#diff-7ddfb3e035b42cd70649cc33393fe32cR203 for example, the construction of the bare context would make things obvious, but once it’s wrapped in WithPort is wasn’t obvious to me what was being passed into platform.Main

## Decision
<what is the change that we're actually proposing or doing.>

We will use `context.Context`, a method to pass optional parameters to `platform.Main`.
These parameters are optional, as long term, we want to ignore them.
These parameters can be ignored in the future as the ServicePlatform looks for alternative ways to populate these values.
Inspecting existing HELM charts or reading a repo level `workiva.yml` file should be able to provide these values in the future.
Additionally, any added parameters to the context will use the `WithXXX` naming convention to match go `context.Context` standards.

## Consequences
<what becomes easier or more difficult to do because of this change.>

Consumers must provide a `context.Context` into `platform.Main`.
While this appears clunky at first, it is standard practice in Go for any RPC-style call.
While this doesn't trigger an RPC specifically, sets a good precedent for all other RPC-style platform invocations.

```go
# Desired
platform.Main()

# Current
ctx := context.Background()
ctx = platform.WithPort(ctx, 9999)
platform.Main(ctx)
```

This also allows us explicit cancelation of the service to trigger liveness (restart the container).
The `context` package provides a `WithCancel` method, giving `Main` consumers the ability to gracefully shutdown their services.

```go
# Server shuts itself down after 1m
ctx, cancel := context.WithCancel(context.Background())
go func() {
  time.Sleep(time.Minute)
  cancel()
}()
platform.Main(ctx)
```

Inside `platform.Main`, this opens the options for ignoring the desired port when running multiple platform services locally.
Similar to how the AppEngine SDK used a `PORT` environment variable to determine which port to serve on, this precedent for "ignored parameters" should allow us to do something similar.

Finally, by accepting a context into platform.Main, we can add optional parameters without needing to do major API changes as time goes on.
