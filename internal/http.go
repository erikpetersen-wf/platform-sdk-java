package internal

import (
	"net"
	"net/http"
	"time"
)

// NewHTTPTransport creates a unique http transport to be used in an http client.
//
// The returned transport has all the same settings as the default global
// http transport, with the exception of MaxIdleConnsPerHost, which is set to 100.
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
		MaxIdleConnsPerHost:   100, // note: different from default global transport
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
}
