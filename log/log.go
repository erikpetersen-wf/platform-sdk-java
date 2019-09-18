package log

import (
	"context"

	"github.com/Workiva/platform/internal"
)

// Common field declarations across Workiva.
const (
	FieldUserID         = `userId`
	FieldUserRID        = `userResourceId`
	FieldMembershipID   = `membershipId`
	FieldMembershipRID  = `membershipResourceId`
	FieldAccountID      = `accountId`
	FieldOrganizationID = `organizationId`
	FieldWorkspaceID    = `workspaceId`
	FieldWorkspaceRID   = `workspaceResourceId`
	FieldCMD            = `cmd`
	FieldPID            = `pid`
)

// these field keys are restricted and are reserved for Splunk to populate.
var restricted = map[string]bool{
	`host`:          true,
	`index`:         true,
	`linecount`:     true,
	`punct`:         true,
	`source`:        true,
	`sorucetype`:    true,
	`splunk_server`: true,
	`timestamp`:     true,
}

// Logger ...
type Logger func(level int, msg string, meta map[string]string)

// New ...
func New(ctx context.Context) Logger {
	return func(level int, msg string, meta map[string]string) {
		// TODO: do we validate restricted fields here?
		internal.Log(ctx, level, msg, meta)
	}
}

// Debug is fine level logging information. For example technical details,
// information that someone will use to debug a technical issue.
func (l Logger) Debug(msg string) { l(0, msg, nil) }

// Info is high level logging information. For example, basic information about
// a process.
func (l Logger) Info(msg string) { l(1, msg, nil) }

// Warning conveys information about potential issues, something may not be
// right, but is easily recoverable.
func (l Logger) Warning(msg string) { l(2, msg, nil) }

// Error logs emitted when something with the application goes wrong. This is
// usually for exceptions.
func (l Logger) Error(msg string) { l(3, msg, nil) }

// Critical error, an issue that has wider impact outside the application. This
// usually useful for indicating problems with structural integrity of the
// infrastructure and application.
func (l Logger) Critical(msg string) { l(4, msg, nil) }

// WithMeta ...
func (l Logger) WithMeta(meta map[string]string) Logger {
	c := make(map[string]string, len(meta))
	for k, v := range meta {
		c[k] = v
	}
	return func(level int, msg string, meta map[string]string) {
		scope := c
		if meta != nil {
			// have to make a new map to merge old meta with new meta
			scope = make(map[string]string, len(meta)+len(c))
			for k, v := range c {
				scope[k] = v
			}
			for k, v := range meta {
				scope[k] = v
			}
		}
		l(level, msg, scope)
	}
}

// Debug is fine level logging information. For example technical details,
// information that someone will use to debug a technical issue.
func Debug(ctx context.Context, msg string, meta map[string]string) {
	internal.Log(ctx, 0, msg, meta)
}

// Info is high level logging information. For example, basic information about
// a process.
func Info(ctx context.Context, msg string, meta map[string]string) {
	internal.Log(ctx, 1, msg, meta)
}

// Warning conveys information about potential issues, something may not be
// right, but is easily recoverable.
func Warning(ctx context.Context, msg string, meta map[string]string) {
	internal.Log(ctx, 2, msg, meta)
}

// Error logs emitted when something with the application goes wrong. This is
// usually for exceptions.
func Error(ctx context.Context, msg string, meta map[string]string) {
	internal.Log(ctx, 3, msg, meta)
}

// Critical error, an issue that has wider impact outside the application. This
// usually useful for indicating problems with structural integrity of the
// infrastructure and application.
func Critical(ctx context.Context, msg string, meta map[string]string) {
	internal.Log(ctx, 4, msg, meta)
}
