package internal

import (
	"context"
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
	ctx, restart := context.WithCancel(context.Background())
	go Main(ctx)
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
	restart()
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "I'm a teapot", http.StatusText(w.Code))
	assertEqual(t, "context canceled\n", w.Body.String())
}

func TestMainCustom(t *testing.T) {
	http.DefaultServeMux = &http.ServeMux{}
	custom := func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "what?", http.StatusGone)
	}
	http.HandleFunc("/_wk/ready", custom)
	http.HandleFunc("/_wk/alive", custom)
	os.Setenv("PORT", "9999")
	ctx, restart := context.WithCancel(context.Background())

	go Main(ctx)
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
	restart()
	w = httptest.NewRecorder()
	s.Handler.ServeHTTP(w, r)
	assertEqual(t, "Gone", http.StatusText(w.Code))
	assertEqual(t, "what?\n", w.Body.String())
}

func TestCoercePort(t *testing.T) {
	os.Setenv("PORT", "")
	cases := []struct {
		in  interface{}
		out string
	}{
		{nil, `8888`},
		{int(1), `1`},
		{int8(2), `2`},
		{int16(3), `3`},
		{int32(4), `4`},
		{int64(5), `5`},
		{uint(6), `6`},
		{uint8(7), `7`},
		{uint16(8), `8`},
		{uint32(9), `9`},
		{uint64(10), `10`},
		{`11`, `11`},
	}
	for _, test := range cases {
		t.Run(test.out, func(in interface{}, out string) func(*testing.T) {
			return func(ts *testing.T) {
				ctx := context.WithValue(context.Background(), `port`, in)
				assertEqual(t, out, coercePort(ctx))
			}
		}(test.in, test.out))
	}
}
