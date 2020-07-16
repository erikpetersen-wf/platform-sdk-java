package log

import (
	"context"
	"io/ioutil"
	"os"

	"github.com/Sirupsen/logrus"

	appintel "github.com/Workiva/app_intelligence_go/v7"
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

// New constructs a new base logger object.
// currently based on logrus' logger, but subject to change in the future.
func New(ctx context.Context) logrus.FieldLogger {
	log := logrus.StandardLogger()

	// future: update to ship logs across Call
	// https://github.com/Workiva/platform/pull/26

	// Add app-intelligence if we have are considered "safe".
	if os.Getenv("WORKIVA_DEPLOY_MODE") != "" {
		log.AddHook(appintel.NewHook()) // Add hook from app intelligence
		log.Out = ioutil.Discard        // Don't duplicate logs to stderr
	}
	return log
}
