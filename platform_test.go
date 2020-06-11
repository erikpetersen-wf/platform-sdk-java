package platform_test

import (
	"context"
	"fmt"
	"net/http"
	"testing"

	"github.com/Workiva/platform"
)

func ExampleMain() {
	ctx, die := context.WithCancel(context.Background())

	// bind all your HTTP Handlers!!!
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintln(w, "ok!")
	})
	http.HandleFunc("/die", func(w http.ResponseWriter, r *http.Request) {
		die()
	})

	platform.Main(platform.WithPort(ctx, 6070))
}

func TestNewHTTPClient(t *testing.T) {
	client := platform.NewHTTPClient()
	if client.Timeout == 0 {
		t.Fatalf("should have a reasonable default for timeout.")
	}
	if client.Transport == nil {
		t.Fatalf("don't fallback to http.DefaultTransport.")
	}
}
