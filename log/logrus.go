package log

import (
	"context"
	"fmt"

	"github.com/sirupsen/logrus"

	"github.com/Workiva/platform/internal"
)

// New2 ... trying out some logrus ideas (should replace New)
func New2(ctx context.Context) logrus.FieldLogger {
	log := logrus.New()
	log.AddHook(NewHook(ctx))
	// TODO: all the app inteligence stuff (maybe move to sidecar?)
	return log
}

// NewHook ... general purpose logging hook for logrus
func NewHook(ctx context.Context) logrus.Hook {
	return &logrusHook{
		ctx: ctx,
	}
}

type logrusHook struct {
	ctx context.Context
}

func (hook *logrusHook) Levels() []logrus.Level { return logrus.AllLevels }

// Internal standard logging levels as defined by RFD-0249
var levelMap = map[logrus.Level]int{
	logrus.TraceLevel: 0,
	logrus.DebugLevel: 0,
	logrus.InfoLevel:  1,
	logrus.WarnLevel:  2,
	logrus.ErrorLevel: 3,
	logrus.FatalLevel: 4,
	logrus.PanicLevel: 4,
}

func (hook *logrusHook) Fire(entry *logrus.Entry) error {

	// normalize level to internal standard (RFD-0249)
	level, ok := levelMap[entry.Level]
	if !ok {
		level = 2 // default to warn :shrug:
	}

	// normalize data to stringly typed info (it's going to JSON folks!)
	meta := make(map[string]string, len(entry.Data))
	for k, v := range entry.Data {
		meta[k] = fmt.Sprint(v)
	}

	// Actually log the message to the platform
	internal.Log(hook.ctx, level, entry.Message, meta)
	return nil
}
