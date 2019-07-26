package internal

import (
	"log"
	"net/http"
	"net/url"
	"os"
)

// Main ...
func Main() {
	installHealthChecker(http.DefaultServeMux)

	port := "8080"
	if s := os.Getenv("PORT"); s != "" {
		port = s
	}

	if err := http.ListenAndServe(":"+port, http.DefaultServeMux); err != nil {
		log.Fatalf("http.ListenAndServe: %v", err)
	}
}

func installHealthChecker(mux *http.ServeMux) {
	// If no health check handler has been installed by this point, add a trivial one.
	const healthPath = "/_wk/ready"
	hreq := &http.Request{
		Method: "GET",
		URL: &url.URL{
			Path: healthPath,
		},
	}
	if _, pat := mux.Handler(hreq); pat != healthPath {
		mux.HandleFunc(healthPath, func(w http.ResponseWriter, r *http.Request) {
			http.Error(w, "ok", http.StatusOK)
		})
	}
}
