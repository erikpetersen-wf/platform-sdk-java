package check

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"
)

func ExampleRegister() {
	Register(`customInternalServiceAttribute`, func() error {
		return errors.New("how do I test this?")
	})
}

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

const fixture2 = `{
	"data": {
		"type": "status",
		"id": "2019-08-12T15:50:13-05:00",
		"attributes": {
			"status": "OK"
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

	// Test 0
	w := httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	if w.Body.String() != fixture1 {
		t.Errorf(`outp: %q`, w.Body.String())
		t.Errorf(`want: %q`, fixture1)
		t.Fatalf(`missmatched responses`)
	}
	if w.Header().Get(`content-type`) != `application/vnd.api+json` {
		t.Fatalf(`producing invalid json:API`)
	}

	// Test 1
	err = nil
	w = httptest.NewRecorder()
	http.DefaultServeMux.ServeHTTP(w, r)
	if w.Body.String() != fixture2 {
		t.Errorf(`outp: %q`, w.Body.String())
		t.Errorf(`want: %q`, fixture2)
		t.Fatalf(`missmatched responses`)
	}
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
