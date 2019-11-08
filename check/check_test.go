package check

import (
	"encoding/base64"
	"errors"
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
	whitelist.Store(map[string]struct{}{})
	is(t, !canExposeMeta(r), "Don't expose if not in list")
}

func TestCanExposeMetaInList(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, `/_wk/status`, nil)
	r.Header.Set("x-forwarded-for", "some-ip-addr")
	whitelist.Store(map[string]struct{}{
		"some-ip-addr": struct{}{},
	})
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
	list, ok := whitelist.Load().(map[string]struct{})
	is(t, ok, "type")
	is(t, len(list) == 3, "len")
	_, ok = list["ip-1"]
	is(t, ok, "ip-1")
	_, ok = list["ip-2"]
	is(t, ok, "ip-2")
	_, ok = list["ip-3"]
	is(t, ok, "ip-3")
}
