package platform

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"time"

	"github.com/Sirupsen/logrus"
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
	return &http.Client{
		Transport: NewHTTPTransport(),

		// Default timeout for all requests going through this client.
		Timeout: 60 * time.Second,
	}
}

func NewHTTPTransport() *http.Transport {
	// borrowed from net/http::DefaultTransport to ensure we don't have shared globals
	dialer := &net.Dialer{
		Timeout:   30 * time.Second,
		KeepAlive: 30 * time.Second,
		DualStack: true,
	}

	return &http.Transport{
		Proxy:                 http.ProxyFromEnvironment,
		DialContext:           dialer.DialContext,
		ForceAttemptHTTP2:     true, // added in go 1.13, if your build breaks here.. update
		MaxIdleConns:          100,
		MaxIdleConnsPerHost:   100,
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
}

// Logs any differences between the current default transport and the transport
// the repo creates. Returns true if any changes were detected, false otherwise.
func CheckTransportDifferences() bool {
	defaultTransport := *http.DefaultTransport.(*http.Transport)

	// TODO get from whatever function
	customTransport := NewHTTPTransport()

	logFields := logrus.Fields{}

	if defaultTransport.ForceAttemptHTTP2 != customTransport.ForceAttemptHTTP2 {
		logFields["force_attempt_http2"] = fmt.Sprintf("old: %v, new %v", defaultTransport.ForceAttemptHTTP2, customTransport.ForceAttemptHTTP2)
	}
	if defaultTransport.ReadBufferSize != customTransport.ReadBufferSize {
		logFields["read_buffer_size"] = fmt.Sprintf("old: %v, new %v", defaultTransport.ReadBufferSize, customTransport.ReadBufferSize)
	}
	if defaultTransport.WriteBufferSize != customTransport.WriteBufferSize {
		logFields["write_buffer_size"] = fmt.Sprintf("old: %v, new %v", defaultTransport.WriteBufferSize, customTransport.WriteBufferSize)
	}
	if defaultTransport.MaxResponseHeaderBytes != customTransport.MaxResponseHeaderBytes {
		logFields["max_response_header_bytes"] = fmt.Sprintf("old: %v, new %v", defaultTransport.MaxResponseHeaderBytes, customTransport.MaxResponseHeaderBytes)
	}
	if defaultTransport.ExpectContinueTimeout != customTransport.ExpectContinueTimeout {
		logFields["expect_continue_timeout"] = fmt.Sprintf("old: %v, new %v", defaultTransport.ExpectContinueTimeout, customTransport.ExpectContinueTimeout)
	}
	if defaultTransport.ResponseHeaderTimeout != customTransport.ResponseHeaderTimeout {
		logFields["response_header_timeout"] = fmt.Sprintf("old: %v, new %v", defaultTransport.ResponseHeaderTimeout, customTransport.ResponseHeaderTimeout)
	}
	if defaultTransport.IdleConnTimeout != customTransport.IdleConnTimeout {
		logFields["idle_conn_timeout"] = fmt.Sprintf("old: %v, new %v", defaultTransport.IdleConnTimeout, customTransport.IdleConnTimeout)
	}
	if defaultTransport.MaxConnsPerHost != customTransport.MaxConnsPerHost {
		logFields["max_conns_per_host"] = fmt.Sprintf("old: %v, new %v", defaultTransport.MaxConnsPerHost, customTransport.MaxConnsPerHost)
	}
	if defaultTransport.MaxIdleConnsPerHost != customTransport.MaxIdleConnsPerHost {
		logFields["max_idle_conns_per_host"] = fmt.Sprintf("old: %v, new %v", defaultTransport.MaxIdleConnsPerHost, customTransport.MaxIdleConnsPerHost)
	}
	if defaultTransport.MaxIdleConns != customTransport.MaxIdleConns {
		logFields["max_idle_conns"] = fmt.Sprintf("old: %v, new %v", defaultTransport.MaxIdleConns, customTransport.MaxIdleConns)
	}
	if defaultTransport.DisableCompression != customTransport.DisableCompression {
		logFields["disable_compression"] = fmt.Sprintf("old: %v, new %v", defaultTransport.DisableCompression, customTransport.DisableCompression)
	}
	if defaultTransport.DisableKeepAlives != customTransport.DisableKeepAlives {
		logFields["disable_keep_alives"] = fmt.Sprintf("old: %v, new %v", defaultTransport.DisableKeepAlives, customTransport.DisableKeepAlives)
	}
	if defaultTransport.TLSHandshakeTimeout != customTransport.TLSHandshakeTimeout {
		logFields["tls_handshake_timeout"] = fmt.Sprintf("old: %v, new %v", defaultTransport.TLSHandshakeTimeout, customTransport.TLSHandshakeTimeout)
	}

	if len(logFields) > 0 {
		logrus.WithFields(logFields).Warn("changes from default client to custom client")
		return true
	}

	return false
}
