package log

import (
	"context"

	"github.com/sirupsen/logrus"
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
func (hook *logrusHook) Fire(log *logrus.Entry) error {
	panic(`TODO`)
	return nil
}
