package internal

import (
	"net/http"
	"sync/atomic"
)

var shouldRestart atomic.Value

func init() {
	shouldRestart.Store(false)
}

// Restart ...
func Restart() {
	shouldRestart.Store(true)
}

func livenessCheck(w http.ResponseWriter, r *http.Request) {
	if shouldRestart.Load().(bool) {
		http.Error(w, "I am as usefull as a coffeepot, please restart me!", http.StatusTeapot)
	} else {
		http.Error(w, "ok", http.StatusOK)
	}
}
