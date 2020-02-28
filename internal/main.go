package internal

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
)

var testServerReference = make(chan *http.Server, 32) // max servers per container

type contextValues uint8

// Various context variables
const (
	PORT = contextValues(iota)
)

// Main performs the main content loop for serving traffic.
func Main(ctx context.Context) {

	// Attach default handlers if they don't already exist
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/ready", func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "ok", http.StatusOK)
	})
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/alive", func(w http.ResponseWriter, r *http.Request) {
		select {
		case <-ctx.Done():
			http.Error(w, ctx.Err().Error(), http.StatusTeapot)
		default:
			http.Error(w, `ok`, http.StatusOK)
		}
	})

	// Create the server (FUTURE: wrap default MUX)
	s := &http.Server{
		Handler: addTracing(http.DefaultServeMux),
		Addr:    ":" + coercePort(ctx),
	}

	// Monitor the outer context for shutdown events and shed connections.
	go func() {
		<-ctx.Done()
		s.Shutdown(context.Background()) // try to shutdown until k8s kills us.
	}()

	// ListenAndServe forever!
	testServerReference <- s
	if err := s.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatalf("http.ListenAndServe: %v", err)
	}
}

// installDefaultEndpoint adds a default handler if one has not already been added.
func installDefaultEndpoint(mux *http.ServeMux, path string, handler http.HandlerFunc) {
	if _, pat := mux.Handler(&http.Request{URL: &url.URL{Path: path}}); pat != path {
		mux.HandleFunc(path, handler)
	}
}

// coercePort attempts to parse the HTTP port from the envionrment or context.
func coercePort(ctx context.Context) string {
	port := `8888`
	if s := os.Getenv(`PORT`); s != `` {
		port = s
	}
	switch p := ctx.Value(PORT).(type) {
	case int, int8, int16, int32, int64, uint, uint8, uint16, uint32, uint64:
		port = fmt.Sprintf(`%d`, p)
	case string:
		port = p
	case nil:
		// noop
	default:
		log.Fatalf(`invalid port context value: %v`, ctx.Value(`port`))
	}
	return port
}
