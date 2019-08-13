package check

import (
	"encoding/json"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/Workiva/platform/internal/api"
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
	meta := api.Meta{}
	data := availability{
		Status: `OK`,
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
		meta[name] = status
	}

	// set overall status
	if unavailable {
		data.Status = `UNAVAILABLE`
	}

	// construct jsonAPI object
	doc := api.Document{
		JSONAPI: api.Details,
		Data: api.Resource{
			ID:    timeNow().Format(time.RFC3339),
			Type:  `availability`,
			Attrs: data,
			Meta:  meta,
		},
		Meta: api.Meta{
			`name`: hostname,
		},
	}

	// serialize and write
	w.Header().Set(`content-type`, `application/vnd.api+json`)
	h := json.NewEncoder(w)
	h.SetIndent(``, "\t")
	if err := h.Encode(doc); err != nil {
		log.Printf(`check: could not serialize response: %v`, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}

// availibility is the defines the service availability resource.
type availability struct {
	Status string `json:"status"`
}
