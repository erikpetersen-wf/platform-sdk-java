package platform

import (
	"context"

	"github.com/Workiva/platform/internal"
)

// Main tells the platform that your container is ready to serve traffic.
//
// Main starts serving HTTP traffic on a derived port using http.ListenAndServe.
// If a context is provided with a `port` value, it will be used.
// Otherwise, the `PORT` enviornment variable will be used.
// Otherwise, the logic defaults to :8888.
//
// The lifecycle of this container depends on the liveness of the ctx.
// If your container enters an un-recoverable state, cancel the context.
// This will cause the container to restart.
func Main(ctx context.Context) { internal.Main(ctx) }

// WithPort allows you to attach a default serving port to a given context.
func WithPort(ctx context.Context, port int) context.Context {
	return context.WithValue(ctx, internal.PORT, port)
}
