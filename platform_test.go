package platform_test

import (
	"context"
	"fmt"
	"net/http"

	"github.com/Workiva/platform"
)

func ExampleMain() {
	ctx, die := context.WithCancel(context.Background())
	ctx = context.WithValue(ctx, `port`, 6070)

	// bind all your HTTP Handlers!!!
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintln(w, "ok!")
	})
	http.HandleFunc("/die", func(w http.ResponseWriter, r *http.Request) {
		die()
	})

	platform.Main(ctx)
}
