package platform

import "github.com/Workiva/platform/internal"

// Main ...
func Main() { internal.Main() }

// Restart tells the controlling infrastructure to restart this container.
func Restart() { internal.Restart() }
