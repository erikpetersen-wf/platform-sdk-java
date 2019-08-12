package check

import (
	"encoding/json"
	"log"
	"net/http"
	"os"
	"sync"
	"time"
)

var (
	checks   = map[string]func() error{}
	lock     = &sync.RWMutex{}
	timeNow  = time.Now // for testing
	hostname = `unknown`
)

// Dependency provides the ability for consumers to register a service that it depends on
func Dependency(name string, fn func() error) {
	lock.Lock()
	defer lock.Unlock()
	if _, ok := checks[name]; ok {
		panic(`check.Dependency: "` + name + `" already registered`)
	}
	checks[name] = fn
}

// Debounce wraps a checker and debounce it's invocations.
func Debounce(fn func() error, dt time.Duration) func() error {
	next := timeNow().Add(dt)
	value := fn()
	return func() error {
		now := timeNow()
		if now.After(next) {
			value = fn()
			next = now.Add(dt)
		}
		return value
	}
}

func init() {
	http.HandleFunc(`/_wk/availability`, serviceCheckHandler)
	if name, err := os.Hostname(); err == nil {
		hostname = name
	}
}

func serviceCheckHandler(w http.ResponseWriter, r *http.Request) {
	data := wrap{
		Type: `availability`,
		ID:   hostname + `-` + timeNow().Format(`2006-01-02-15-04-05-MST`),
		Attrs: internal{
			Status:  `OK`,
			Name:    `TODO`,
			Details: make(map[string]string),
		},
	}

	// create a duplicate map of status (faster than calling the fns)
	lock.RLock()
	dupe := make(map[string]func() error, len(checks))
	for n, fn := range checks {
		dupe[n] = fn
	}
	lock.RUnlock()

	// ask each status whats up
	var unavailable bool
	for name, fn := range dupe {
		status := `PASSED`
		if err := fn(); err != nil {
			status = err.Error()
			unavailable = true
		}
		data.Attrs.Details[name] = status
	}

	// set overall status
	if unavailable {
		data.Attrs.Status = `UNAVAILABLE`
	}

	// serialize and write
	w.Header().Set(`content-type`, `application/vnd.api+json`)
	h := json.NewEncoder(w)
	h.SetIndent(``, "\t")
	if err := h.Encode(jsonapiWrapper{Data: data}); err != nil {
		log.Printf(`check: could not serialize response: %v`, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

type jsonapiWrapper struct {
	Data interface{} `json:"data,omitempty"`
}

type wrap struct {
	Type  string   `json:"type"`
	ID    string   `json:"id"`
	Attrs internal `json:"attributes"`
}

type internal struct {
	Status  string            `json:"status"`
	Name    string            `json:"name"`
	Details map[string]string `json:"details"`
}
