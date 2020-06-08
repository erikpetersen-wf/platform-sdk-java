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

// NewHTTPClient creates a unique HTTP client to be used on a per client basis.
//
// There is a known issue in the golang base http client where if a connection to a
// remote host fails in an unexpected way, if the request set a custom http header, the
// caller not only blocks, but it leaves in place a mutex preventing any other new
// connections on the same http client from reaching that same remote host.
//
// See https://github.com/golang/go/issues/33006 for more details.
//
// This client is designed to not share any of the global resources with go's builtin
// http.DefaultClient so that clients can be isolated from this shared failure state.
// This should help with issues as seen in https://jira.atl.workiva.net/browse/OI-1338.
//
// By default, this client also sets a Timeout for all connections running through it.
// If you feel this timeout does not satisfy your needs, feel free to override it using:
//
//   client := platform.NewHTTPClient()
//   client.Timeout = 60 * time.Second
//
// Keep in mind that this timeout should be the MAXIMUM timeout for this client.
// If you would like a lower, per-request timeout, please set request's Context
// using req.WithContext(ctx): https://golang.org/pkg/net/http/#Request.WithContext
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
		ForceAttemptHTTP2:     true, // added in go 1.13, if your build breaks here.. update
		MaxIdleConns:          100,
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
	return &http.Client{
		Transport: transport,

		// Default timeout for all requests going through this client.
		// Should be "similar" to the linux TCP client timout
		// See tcp_retries2 in https://www.kernel.org/doc/Documentation/networking/ip-sysctl.txt
		Timeout: 15 * 60 * time.Second,
	}
}
