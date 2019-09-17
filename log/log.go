package log

import (
	"context"

	"github.com/Workiva/platform/internal"
)

// Field is a pre-defined name that can be shared throughout Workiva.
type Field string

// Common field declarations across Workiva.
const (
	FieldUserID         = Field(`userId`)
	FieldUserRID        = Field(`userResourceId`)
	FieldMembershipID   = Field(`membershipId`)
	FieldMembershipRID  = Field(`membershipResourceId`)
	FieldAccountID      = Field(`accountId`)
	FieldOrganizationID = Field(`organizationId`)
	FieldWorkspaceID    = Field(`workspaceId`)
	FieldWorkspaceRID   = Field(`workspaceResourceId`)
	FieldCMD            = Field(`cmd`)
	FieldPID            = Field(`pid`)
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

// Debugf formats its arguments according to the format, analogous to fmt.Printf,
// and records the text as a log message at Debug level. The message will be associated
// with the request linked with the provided context.
func Debugf(ctx context.Context, format string, args ...interface{}) {
	internal.Logf(ctx, 0, format, args...)
}

// Infof is like Debugf, but at Info level.
func Infof(ctx context.Context, format string, args ...interface{}) {
	internal.Logf(ctx, 1, format, args...)
}

// Warningf is like Debugf, but at Warning level.
func Warningf(ctx context.Context, format string, args ...interface{}) {
	internal.Logf(ctx, 2, format, args...)
}

// Errorf is like Debugf, but at Error level.
func Errorf(ctx context.Context, format string, args ...interface{}) {
	internal.Logf(ctx, 3, format, args...)
}

// Criticalf is like Debugf, but at Critical level.
func Criticalf(ctx context.Context, format string, args ...interface{}) {
	internal.Logf(ctx, 4, format, args...)
}
