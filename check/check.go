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
	NameS3   = `s3`
	NameNats = `nats`
	NameSQS  = `sqs`
)

// Register provides the ability for consumers to register status check function.
func Register(name string, fn func() error) {
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
	data := &availability{
		ID:     timeNow().Format(time.RFC3339),
		Status: `OK`,
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
		status := `PASSED`
		if err := fn(); err != nil {
			status = err.Error()
			unavailable = true
		}
		data.meta[name] = status
	}

	// set overall status
	if unavailable {
		data.Status = `UNAVAILABLE`
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

// availibility is the defines the service availability resource.
type availability struct {
	ID     string `jsonapi:"primary,availability"`
	Status string `jsonapi:"attr,status"`
	meta   jsonapi.Meta
}

// Implements jsonapi.Metable
func (a *availability) JSONAPIMeta() *jsonapi.Meta { return &a.meta }
