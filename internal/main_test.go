package internal

import (
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
)

func assertEqual(t *testing.T, a, b string) {
	if a != b {
		t.Fatalf("%q != %q", a, b)
	}
}

func TestMain(t *testing.T) {
	http.DefaultServeMux = &http.ServeMux{}
	go Main()
	s := <-testServerReference
	defer s.Close()

	// default readiness
	r := httptest.NewRequest("GET", "/_wk/ready", nil)
	w := httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "OK", http.StatusText(w.Code))
	assertEqual(t, "ok\n", w.Body.String())

	// default liveness
	r.URL.Path = "/_wk/alive"
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "OK", http.StatusText(w.Code))
	assertEqual(t, "ok\n", w.Body.String())

	// default port
	assertEqual(t, ":8888", s.Addr)

	// Restart invoked!
	Restart()
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "I'm a teapot", http.StatusText(w.Code))
	assertEqual(t, "restart me plz\n", w.Body.String())
}

func TestMainCustom(t *testing.T) {
	http.DefaultServeMux = &http.ServeMux{}
	custom := func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "what?", http.StatusGone)
	}
	http.HandleFunc("/_wk/ready", custom)
	http.HandleFunc("/_wk/alive", custom)
	os.Setenv("PORT", "9999")

	go Main()
	s := <-testServerReference
	defer s.Close()

	// default readiness
	r := httptest.NewRequest("GET", "/_wk/ready", nil)
	w := httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "Gone", http.StatusText(w.Code))
	assertEqual(t, "what?\n", w.Body.String())

	// default liveness
	r.URL.Path = "/_wk/alive"
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "Gone", http.StatusText(w.Code))
	assertEqual(t, "what?\n", w.Body.String())

	// default port
	assertEqual(t, ":9999", s.Addr)

	// Restart invoked!
	Restart()
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "Gone", http.StatusText(w.Code))
	assertEqual(t, "what?\n", w.Body.String())
}
