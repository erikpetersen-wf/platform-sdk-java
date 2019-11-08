package check

import (
	"encoding/json"
	"log"
	"net/http"
	"runtime"
	"sync"
	"time"
)

// Note: data is in a whaky format here!
var acceptableIPListURLs = []string{
	"https://s3.amazonaws.com/nr-synthetics-assets/nat-ip-dnsname/production/ip.json",
	"https://s3.amazonaws.com/nr-synthetics-assets/nat-ip-dnsname/eu/ip.json",
}

var (
	exposeRequestQueue = make(chan chan<- map[string]struct{}, 16) // buffer up to 16 requestss
	exposeRequestOncer sync.Once
)

func canExposeMeta(r *http.Request) bool {
	exposeRequestOncer.Do(runUpdater)
	requestChan := make(chan map[string]struct{}, 1)

	// Attempt to enqueue expose request
	select {
	case exposeRequestQueue <- requestChan:
		runtime.Gosched() // let the other process handle the request really quick
	default:
		log.Printf(`check: backed up exposeRequestQueue (failing secure)`)
		return false
	}

	// Pull that data out... give it a timeout
	var allowedIPs map[string]struct{}
	select {
	case allowedIPs = <-requestChan:
	case <-time.After(10 * time.Millisecond):
		log.Printf(`check: requesting queue data timed out (failing secure)`)
		return false
	}

	ip := r.Header.Get("x-forwarded-for")
	_, ok := allowedIPs[ip]
	return ok
}

var updaterWrappedForTesting = updater

func runUpdater() {
	go updaterWrappedForTesting()
}

func updater() {
	ticker := time.NewTicker(time.Hour)
	var allowedIPs map[string]struct{}
	for {
		select {
		case <-ticker.C:
			ips := fetchLists()
			allowedIPs = make(map[string]struct{}, len(ips))
			for _, ip := range ips {
				allowedIPs[ip] = struct{}{}
			}
		case request := <-exposeRequestQueue:
			request <- allowedIPs
		}
	}
}

func fetchLists() (ips []string) {
	// TODO: setup well-behaved HTTP client
	for _, url := range acceptableIPListURLs {
		res, err := http.Get(url)
		if err != nil {
			log.Printf("check(fetch): Unable to load %q: %v", url, err)
			return nil
		}
		// TODO: handle non-200 response codes
		defer res.Body.Close() // TODO: don't defer
		var data map[string][]string
		if err := json.NewDecoder(res.Body).Decode(&data); err != nil {
			log.Printf("check(fetch): Unable to decode %q: %v", url, err)
			return nil
		}
		for _, list := range data {
			for _, ip := range list {
				ips = append(ips, ip)
			}
		}
	}
	return ips
}
