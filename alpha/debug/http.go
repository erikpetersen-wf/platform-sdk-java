package debug

import (
	"fmt"
	"net/http"

	"github.com/Sirupsen/logrus"
	"github.com/Workiva/platform"
)

// Logs any differences between the current default transport and the transport
// the repo creates. Returns true if any changes were detected, false otherwise.
func CheckTransportDifferences() bool {
	defaultTransport := *http.DefaultTransport.(*http.Transport)

	// TODO get from whatever function
	customTransport := platform.NewHTTPTransport()

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

