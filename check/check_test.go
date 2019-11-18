package check

import (
	"encoding/base64"
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
	"time"
)

func ExampleRegister() {
	Register(`customInternalServiceAttribute`, func() error {
		return errors.New("how do I test this?")
	})
}

// failed event
const fixture1 = `{
	"data": {
		"type": "status",
		"id": "2019-08-12T15:50:13-05:00",
		"attributes": {
			"status": "FAILED"
		},
		"meta": {
			"goodbye": "why?",
			"hello": "PASSED"
		}
	},
	"meta": {
		"name": "testbox"
	}
}
`

// failed can't expose meta
const fixture2 = `{
	"data": {
		"type": "status",
		"id": "2019-08-12T15:50:13-05:00",
		"attributes": {
			"status": "FAILED"
		}
	}
}
`

// passed event
const fixture3 = `{
	"data": {
		"type": "status",
		"id": "2019-08-12T15:50:13-05:00",
		"attributes": {
			"status": "PASSED"
		},
		"meta": {
			"goodbye": "PASSED",
			"hello": "PASSED"
		}
	},
	"meta": {
		"name": "testbox"
	}
}
`

// passed can't expose meta
const fixture4 = `{
	"data": {
		"type": "status",
		"id": "2019-08-12T15:50:13-05:00",
		"attributes": {
			"status": "PASSED"
		}
	}
}
`

func TestRegister(t *testing.T) {
	err := errors.New(`why?`)
	Register(`hello`, func() error { return nil })
	Register(`goodbye`, func() error { return err })

	// overrides
	hostname = `testbox`
	timeNow = func() time.Time {
		return time.Date(2019, 8, 12, 3+12, 50, 13, 0, time.FixedZone(`CDT`, -5*60*60))
	}
	r := httptest.NewRequest(http.MethodGet, `/_wk/status`, nil)
	var canExpose bool
	canExposeMetaWrappedForTesting = func(*http.Request) bool { return canExpose }

	// asserter!
	assert := func(t testing.TB, w *httptest.ResponseRecorder, name, want string) {
		if w.Body.String() != want {
			t.Errorf(`outp: %q`, w.Body.String())
			t.Errorf(`want: %q`, want)
			t.Fatalf(`missmatched responses (%s)`, name)
		}
		if w.Header().Get(`content-type`) != `application/vnd.api+json` {
			t.Fatalf(`producing invalid json:API (%s)`, name)
		}
	}

	// Test 1
	canExpose = true
	w := httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	assert(t, w, "!pass, canExpose", fixture1)

	// Test 2
	canExpose = false
	w = httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	assert(t, w, "!pass, !canExpose", fixture2)

	// Test 3
	canExpose = true
	err = nil
	w = httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	assert(t, w, "pass, canExpose", fixture3)

	// Test 4
	canExpose = false
	w = httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	assert(t, w, "pass, !canExpose", fixture4)
}

func TestRegisterPanic(t *testing.T) {
	defer func() {
		r := recover()
		if r == nil {
			t.Fatal(`double should have panic-ed`)
		}
		m, ok := r.(string)
		if !ok {
			t.Fatal(`expecting panic string`)
		}
		if m != `check.Dependency: "no" already registered` {
			t.Fatalf(`Unexpected panic string: %q`, m)
		}
	}()
	Register(`no`, nil)
	Register(`no`, nil)
	t.Fatal(`should have panic-ed`)
}

func TestDebounce(t *testing.T) {
	when := time.Now()
	timeNow = func() time.Time { return when }
	var calls int
	fn := Debounce(func() error {
		calls++
		return nil
	}, time.Hour)
	fn()
	fn()
	if calls != 1 {
		t.Fatalf(`Got some unexpected calls: %d`, calls)
	}
	when = when.Add(time.Hour * 2)
	fn()
	fn()
	if calls != 2 {
		t.Fatalf(`Got lots of unexpected calls: %d`, calls)
	}
}

func is(tb testing.TB, bit bool, args ...interface{}) {
	if !bit {
		tb.Fatal(args...)
	}
}

func TestCanExposeMetaNotLoaded(t *testing.T) {
	is(t, !canExposeMeta(nil), "Don't expose w/o data")
}

func TestCanExposeMetaNotInList(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, `/_wk/status`, nil)
	whitelist = map[string]struct{}{}
	is(t, !canExposeMeta(r), "Don't expose if not in list")
}

func TestCanExposeMetaInList(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, `/_wk/status`, nil)
	r.Header.Set("x-forwarded-for", "some-ip-addr")
	whitelist = map[string]struct{}{
		"some-ip-addr": struct{}{},
	}
	is(t, canExposeMeta(r), "Expose with-data-in-list")
}

func TestInitWhitelistUnset(t *testing.T) {
	os.Setenv(`NEW_RELIC_SYNTHETICS_IP_WHITELIST`, ``)
	is(t, initWhitelist() == "unset", "unset")
}

func TestInitWhitelistDecode(t *testing.T) {
	os.Setenv(`NEW_RELIC_SYNTHETICS_IP_WHITELIST`, `garbage`)
	is(t, initWhitelist() == "decode", "decode")
}

func TestInitWhitelistUnmarshal(t *testing.T) {
	value := base64.StdEncoding.EncodeToString([]byte{'X'})
	os.Setenv(`NEW_RELIC_SYNTHETICS_IP_WHITELIST`, value)
	is(t, initWhitelist() == "unmarshal", "decode")
}

func TestInitWhitelistSuccess(t *testing.T) {
	value := base64.StdEncoding.EncodeToString([]byte(`[
		"ip-1",
		"ip-2",
		"ip-3"
	]`))
	os.Setenv(`NEW_RELIC_SYNTHETICS_IP_WHITELIST`, value)
	is(t, initWhitelist() == "", "success")
	is(t, len(whitelist) == 3, "len")
	_, ok := whitelist["ip-1"]
	is(t, ok, "ip-1")
	_, ok = whitelist["ip-2"]
	is(t, ok, "ip-2")
	_, ok = whitelist["ip-3"]
	is(t, ok, "ip-3")
}

func TestInitWhitelistRealData(t *testing.T) {
	os.Setenv(`NEW_RELIC_SYNTHETICS_IP_WHITELIST`, `WyIzNC4yMjQuMjU1LjE2OSIsICIzNC4yMDEuODkuMTE1IiwgIjUyLjQ0LjcxLjI0NyIsICIzNS4xNjguMTg1LjE4NSIsICIzNS4xNjguMTQxLjkiLCAiNTIuMjEuMjIuNDMiLCAiMTguMjE3Ljg4LjQ5IiwgIjE4LjIyMS4yMzEuMjMiLCAiMTguMjE3LjE1OS4xNzQiLCAiMTMuNTYuMTM3LjE4MCIsICI1NC4yNDEuNTIuMTU4IiwgIjUyLjM2LjI1MS4xMTgiLCAiNTQuMjAwLjE4Ny4xODkiLCAiMzQuMjE2LjIwMS4xMzEiLCAiMzUuMTgyLjEwNC4xOTgiLCAiNTIuNjAuODMuNDgiLCAiNTQuNzYuMTM3LjgzIiwgIjM0LjI0MS4xOTguMTI3IiwgIjM0LjI0Mi4yNTIuMjQ5IiwgIjM1LjE3OC4yMi4xMDIiLCAiMzUuMTc3LjE3NS4xMDYiLCAiMzUuMTc3LjMxLjkzIiwgIjUyLjQ3LjE4My4xIiwgIjUyLjQ3LjE1MS41NiIsICI1Mi40Ny4xMzguMjA3IiwgIjE4LjE5NC43Ny4xMzYiLCAiMTguMTk1LjE2My43MSIsICIzNS4xNTguMjI1LjE2NyIsICIxMy40OC45LjI0IiwgIjEzLjQ4LjExMC4xMzYiLCAiMTMuNTMuMTk1LjIyMSIsICIxMy4xMTQuMjQ4LjE5NyIsICI1Mi42OC4yMjMuMTc4IiwgIjUyLjc5LjIxMC44MyIsICI1Mi43OS4xMjguMTM1IiwgIjEzLjIyOC4zNS4yMTAiLCAiMTMuMjI4LjM5LjE0NiIsICI1NC43OS4xMjcuMjAiLCAiNTQuMTUzLjE1OS4yNiIsICIxMy41NS43Mi4xMTUiLCAiMTMuMTI3Ljk3LjE0MCIsICIxMy4xMjcuNDguMTcwIiwgIjE4LjE2Mi4xNDAuNDYiLCAiMTguMTYyLjM3LjU4IiwgIjE4LjE2Mi4zNy44NCIsICIxOC4yMzEuNTYuMTg1IiwgIjUyLjY3LjExNC4xMTAiLCAiMTU3LjE3NS4xMTguNzciLCAiMTU3LjE3NS4yMS4yNTQiLCAiMTU3LjE3NS4xMTYuOTAiLCAiNTIuNTUuNS45NSIsICIzLjIyNi4xMzAuMjA3IiwgIjMuMjI2LjE2Ni4yOSIsICIzLjIyMS4xNjIuMTkwIiwgIjMuMjA5LjIzMS4xMzEiLCAiMzQuMjMxLjQyLjIzOCIsICIzLjEzMC4xNTkuMjUyIiwgIjMuMTMuNy4xMSIsICIzLjEzMC4xNTUuMjQyIiwgIjU0LjI0MS4yMjUuMTMiLCAiMTMuNTIuODIuMTkwIiwgIjU0LjIwMy4zNS4xNTQiLCAiNTIuNDEuMTc2LjE0NiIsICI1NC43MC42Ny41NyIsICI1Mi4zNi4xMzcuMTA0IiwgIjk5Ljc5LjE3MS4yMDkiLCAiMzUuMTgyLjYyLjEwMCIsICI1Mi40OS4xMzYuMjUyIiwgIjU0LjE5NC4yNDkuNCIsICIzNC4yNDYuMTI2LjE0MSIsICIzNS4xNzcuMjI1LjI3IiwgIjMuMTAuMy42MiIsICIzNS4xNzYuMTgyLjI0MyIsICIxNS4xODguMC45MyIsICIxNS4xODguMjQuMjE2IiwgIjM1LjE4MC4yMjIuNzkiLCAiMTguMTk2LjIwNC4yMzEiLCAiMTguMTk0LjE5MC43NyIsICI1Mi41OC4xOTAuMzYiLCAiMTMuNDguOTMuMjMwIiwgIjEzLjQ4LjExOS4yNDkiLCAiMTMuNDguMTIyLjEzMSIsICIzLjExMy4xNjguMjA3IiwgIjMuMTE0Ljk2LjE3NyIsICIxMy4xMjQuMjEwLjc0IiwgIjUyLjc4LjEwNC4xNSIsICIxOC4xMzguMTI1LjQzIiwgIjE4LjEzOS4yNDkuNTEiLCAiMTMuMjM3LjI1LjUwIiwgIjUyLjY0LjM0LjI5IiwgIjMuMTA0LjI3LjIzIiwgIjEzLjIzNS4xMTIuMjA4IiwgIjEzLjIzNC4xOTYuMTc5IiwgIjE4LjE2Mi44NC4xODYiLCAiMTguMTYyLjE1OS4xNTMiLCAiMTguMTYyLjI0MC4xNDMiLCAiMTguMjI5LjEwNC45NyIsICIxOC4yMjkuMTIxLjIwOSIsICIxNTcuMTc1LjI3LjE3MiIsICIxNTcuMTc1LjEwNi4yMzIiLCAiMTU3LjE3NS4xMTUuMjUyIl0=`)
	is(t, initWhitelist() == "", "success")
	is(t, len(whitelist) == 101, fmt.Sprintf("len=%d", len(whitelist)))
}
