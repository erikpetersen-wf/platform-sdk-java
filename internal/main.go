package internal

import (
	"log"
	"net/http"
	"net/url"
	"os"
	"sync/atomic"
)

var (
	shouldRestart       atomic.Value
	testServerReference = make(chan *http.Server, 1)
)

// Main performs the main content loop for serving traffic.
func Main() {
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/ready", func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "ok", http.StatusOK)
	})
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/alive", livenessCheck)

	port := "8888"
	if s := os.Getenv("PORT"); s != "" {
		port = s
	}

	s := &http.Server{
		Handler: http.DefaultServeMux,
		Addr:    ":" + port,
	}
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

// Restart triggers a restart on the controlling infrastructure.
func Restart() {
	shouldRestart.Store(true)
}

func livenessCheck(w http.ResponseWriter, r *http.Request) {
	if restart, ok := shouldRestart.Load().(bool); ok && restart {
		http.Error(w, "restart me plz", http.StatusTeapot)
	} else {
		http.Error(w, "ok", http.StatusOK)
	}
}
