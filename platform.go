package platform

import (
	"context"
	"net"
	"net/http"
	"time"

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

// NewHTTPClient ... TODO: GOOD DOCSTRING
func NewHTTPClient() *http.Client {
	// borrowed from net/http::DefaultTransport to ensure we don't have shared globals
	dialer := &net.Dialer{
		Timeout:   30 * time.Second,
		KeepAlive: 30 * time.Second,
		DualStack: true,
	}
	transport := &http.Transport{
		Proxy:                 http.ProxyFromEnvironment,
		DialContext:           dialer.DialContext,
		ForceAttemptHTTP2:     true,
		MaxIdleConns:          100,
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
	return &http.Client{
		Transport: transport,
		Timeout:   15 * 60 * time.Second, // THE REAL ADDIITON HERE
	}
}
