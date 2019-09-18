package internal

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	logpb "github.com/Workiva/platform/internal/log"
)

// Log ...
func Log(ctx context.Context, level int, msg string, meta map[string]string) {
	logInternal(fromContext(ctx), int64(level), msg, meta)
}

var logLevelName = map[int64]string{
	0: "DEBUG",
	1: "INFO",
	2: "WARNING",
	3: "ERROR",
	4: "CRITICAL",
}

func logInternal(c *internalContext, level int64, msg string, meta map[string]string) {
	if c == nil {
		panic("not a Workiva context")
	}
	msg = strings.TrimRight(msg, "\n") // Remove any trailing newline characters.
	c.addLogLine(&logpb.UserAppLogLine{
		TimestampUsec: int64(time.Now().UnixNano() / 1e3),
		Level:         level,
		Message:       msg,
		Meta:          meta,
	})
	// Only duplicate log to stderr if not running on App Engine second generation
	// if !IsSecondGen() {
	// TODO: print with meta TOO
	// log.Print(logLevelName[level] + ": " + s)
	// }
}

func (c *internalContext) addLogLine(ll *logpb.UserAppLogLine) {
	// Truncate long log lines.
	const lim = 8 << 10
	if len(ll.Message) > lim {
		suffix := fmt.Sprintf("...(length %d)", len(ll.Message))
		ll.Message = ll.Message[:lim-len(suffix)] + suffix
	}

	c.pendingLogs.Lock()
	c.pendingLogs.lines = append(c.pendingLogs.lines, ll)
	c.pendingLogs.Unlock()
}

var contextKey = "holds a *internalContext"

// fromContext returns the App Engine context or nil if ctx is not
// derived from an App Engine context.
func fromContext(ctx context.Context) *internalContext {
	c, _ := ctx.Value(&contextKey).(*internalContext)
	return c
}

// context represents the context of an in-flight HTTP request.
// It implements the appengine.Context and http.ResponseWriter interfaces.
type internalContext struct {
	req *http.Request

	outCode   int
	outHeader http.Header
	outBody   []byte

	pendingLogs struct {
		sync.Mutex
		lines   []*logpb.UserAppLogLine
		flushes int
	}

	apiURL *url.URL
}
