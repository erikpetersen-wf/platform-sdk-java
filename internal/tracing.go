package internal

import (
	"log"
	"net/http"
	"os"
	"strings"
	"sync"

	newrelic "github.com/newrelic/go-agent"
)

func addTracing(hand http.Handler) http.Handler {
	app, err := setUpAPM()
	if err != nil {
		log.Printf("Error starting New Relic application.")
		return hand
	}
	_, hand = newrelic.WrapHandle(app, `/`, hand)
	return hand
}

var (
	newRelicApp newrelic.Application
	newRelicMux sync.Mutex
)

func setUpAPM() (newrelic.Application, error) {
	newRelicMux.Lock()
	defer newRelicMux.Unlock()
	if newRelicApp != nil {
		return newRelicApp, nil
	}

	appKey := os.Getenv("NEW_RELIC_APP_NAME")
	licenseKey := os.Getenv("NEW_RELIC_LICENSE_KEY")
	config := newrelic.NewConfig(appKey, licenseKey)
	config.DistributedTracer.Enabled = true
	config.Logger = newrelic.NewLogger(os.Stdout)

	if relicLabels := os.Getenv("NEW_RELIC_LABELS"); relicLabels != "" {
		labelList := strings.Split(relicLabels, ";")
		for _, pair := range labelList {
			l := strings.Split(pair, ":")
			if len(l) == 2 {
				config.Labels[l[0]] = l[1]
			}
		}
	}

	app, err := newrelic.NewApplication(config)
	if err != nil {
		return nil, err
	}
	newRelicApp = app
	return newRelicApp, nil
}
