package log

// UserAppLogLine ...
type UserAppLogLine struct {
	TimestampUsec int64
	Level         int64
	Message       string
	Meta          map[string]string
}
