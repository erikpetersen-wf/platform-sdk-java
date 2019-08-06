package platform

import "github.com/Workiva/platform/internal"

// Main tells the platform that your container is ready to serve traffic.
// It starts serving HTTP traffic on :PORT using http.ListenAndServe.
// If PORT is not provided, it defaults to hosting on :8888.
func Main() { internal.Main() }

// Restart tells the controlling infrastructure to restart this container.
func Restart() { internal.Restart() }
