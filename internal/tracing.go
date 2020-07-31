package internal

import (
	"log"
	"net/http"
	"os"

	"github.com/newrelic/go-agent/v3/newrelic"
)

func addTracing(hand http.Handler) http.Handler {
	if os.Getenv("NEW_RELIC_APP_NAME") == "" {
		return hand
	}
	app, err := newrelic.NewApplication(
		newrelic.ConfigDistributedTracerEnabled(true),
		newrelic.ConfigInfoLogger(os.Stdout),
		newrelic.ConfigFromEnvironment(),
	)
	if err != nil {
		log.Printf("Error starting New Relic application.")
		return hand
	}
	_, hand = newrelic.WrapHandle(app, `/`, hand)
	return hand
}
