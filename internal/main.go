package internal

import (
	"log"
	"net/http"
	"net/url"
	"os"
)

// Main ...
func Main() {
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/ready", func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "ok", http.StatusOK)
	})
	installDefaultEndpoint(http.DefaultServeMux, "/_wk/alive", livenessCheck)

	port := "8888"
	if s := os.Getenv("PORT"); s != "" {
		port = s
	}

	if err := http.ListenAndServe(":"+port, http.DefaultServeMux); err != nil {
		log.Fatalf("http.ListenAndServe: %v", err)
	}
}

// installDefaultEndpoint adds a default handler if one has not already been added.
func installDefaultEndpoint(mux *http.ServeMux, path string, handler http.HandlerFunc) {
	hreq := &http.Request{
		Method: "GET",
		URL: &url.URL{
			Path: path,
		},
	}
	if _, pat := mux.Handler(hreq); pat != path {
		mux.HandleFunc(path, handler)
	}
}
