# Service Checks

The `check` package provides the ability for a service to "self report" its status.
Additionally, it enables service owners to report the status of their internal dependencies.
This package adds the handler `/_wk/available` to your service to check these values.

NO ACTION IS PERFORMED ON YOUR SERVICE BASED ON THIS DATA.

This information is used for informational and diagnosing purposes only.

## Example Usage

```go
package main

import (
  "github.com/Workiva/platform"
  "github.com/Workiva/platform/check"
  )

func main() {
  // setup critical stuff

  // Register given status checks.
  check.Register(`alive`, func() error {
    return nil // if I'm speaking... I'm alive
  })

  // Start the service.
  platform.Main(nil)
}
```
