package internal

import (
	"crypto/tls"
	"net"
	"net/http"
	"time"
)

// NewHTTPTransport creates a unique http transport to be used in an http client.
//
// The returned transport has all the same settings as the default global
// http transport, with the following exceptions:
//    MaxIdleConnsPerHost is set to 100,
//    ForceAttemptHTTP2 is set to false,
//    TLSNextProto is set to a non-nil, empty map to disable HTTP/2 (Please see https://github.com/golang/go/blob/95d4e6158b4199e1eee957e2c8c934d2cb86c35e/src/net/http/doc.go#L81-L95).
//
// Since Go 1.13, HTTP clients will attempt HTTP/2 connection first, which is controlled by ForceAttemptHTTP2.
// However, it is possible for a single stuck HTTP/2 connection to block all outbound HTTP connections (including
// both HTTP/1 and HTTP/2) from the HTTP client for up to 15 minutes, regardless of destination host.
// Notably, see:
// 1. https://github.com/golang/go/issues/33006
// 2. https://github.com/golang/go/issues/36026
// 3. https://github.com/golang/go/issues/39337
//
// For this reason we temporarily set ForceAttemptHTTP2 to false here.
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
		ForceAttemptHTTP2:     false, // different from default, added in go 1.13, if your build breaks here.. update
		TLSNextProto:          make(map[string]func(authority string, c *tls.Conn) http.RoundTripper), // set to non-nil, empty map to disable HTTP/2
		MaxIdleConns:          100,
		MaxIdleConnsPerHost:   100, // note: different from default global transport
		IdleConnTimeout:       90 * time.Second,
		TLSHandshakeTimeout:   10 * time.Second,
		ExpectContinueTimeout: 1 * time.Second,
	}
}
