package check

import (
	"encoding/json"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/google/jsonapi"
)

var (
	checks   = map[string]func() error{}
	lock     = &sync.RWMutex{}
	timeNow  = time.Now // for testing
	hostname = `unknown`
)

// Standard names shared between services.
// Just because the name exists, does not mean you need to register a check for it.
const (
	NameDB   = `db`
	NameHTTP = `http`
	NameNats = `nats`
	NameS3   = `s3`
	NameSQS  = `sqs`
)

// Register provides the ability for consumers to register status check function.
// Checkers that are registered are immediatly called when a status check is requested.
// If your checker is unable to handle that kind of load, wrap it in a Debounce call.
func Register(name string, checker func() error) {
	lock.Lock()
	defer lock.Unlock()
	if _, ok := checks[name]; ok {
		panic(`check.Dependency: "` + name + `" already registered`)
	}
	checks[name] = checker
}

// Debounce wraps a checker and debounce it's invocations.
//
// Usage: check.Debounce(funcToDebounce, time.Second)
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
	http.HandleFunc(`/_wk/status`, serviceCheckHandler)
	if name, err := os.Hostname(); err == nil {
		hostname = name
	}
}

func serviceCheckHandler(w http.ResponseWriter, r *http.Request) {
	data := &availability{
		ID:     timeNow().Format(time.RFC3339),
		Status: statusPassed,
		meta:   jsonapi.Meta{},
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
		status := statusPassed
		if err := fn(); err != nil {
			status = err.Error()
			unavailable = true
		}
		data.meta[name] = status
	}

	// set overall status
	if unavailable {
		data.Status = statusFailed
	}

	// serialize and write
	w.Header().Set(`content-type`, jsonapi.MediaType)
	w.WriteHeader(http.StatusOK)
	if err := marshal(w, data); err != nil {
		log.Printf(`check: could not serialize response: %v`, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}

// duplicate of jsonapi.MarshalPayload with indentation and meta injection.
func marshal(w http.ResponseWriter, models interface{}) error {
	payload, err := jsonapi.Marshal(models)
	if err != nil {
		return err
	}
	payload.(*jsonapi.OnePayload).Meta = &jsonapi.Meta{
		`name`: hostname,
	}
	m := json.NewEncoder(w)
	m.SetIndent(``, "\t")
	return m.Encode(payload)
}

// Possible top level status responses
const (
	statusPassed = `PASSED`
	statusFailed = `FAILED`
)

// availibility is the defines the service availability resource.
type availability struct {

	// ID is the timestamp of the given event.
	// This is NON-Standard JSON:API.
	// But these documents are not designed to be re-requested by identifier.
	ID string `jsonapi:"primary,status"`

	// The resulting status of all the registered checks.
	// If any one check fails, this reports a `FAILED`.
	// Otherwise, this reports `PASSED`.
	Status string `jsonapi:"attr,status"`

	// Registered checks will be different as teams implement different checks.
	// Keeping with JSON:API, they should not be defined as attributes of a document.
	// Instead they are represented as metadata of a particular document.
	// https://jsonapi.org/format/1.1/#document-resource-objects
	meta jsonapi.Meta
}

// Implements jsonapi.Metable
func (a *availability) JSONAPIMeta() *jsonapi.Meta { return &a.meta }
