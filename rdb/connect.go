package rdb

import (
	"context"
	"database/sql"
	"errors"
	"os"
	"sync"
	"time"

	// Forcing mysql driver and new-relic integration
	_ "github.com/go-sql-driver/mysql"
	_ "github.com/newrelic/go-agent/_integrations/nrmysql"

	"github.com/Workiva/platform/check"
)

// used for unit tests (gross, I know)
var driverName = "nrmysql"

var (
	db *sql.DB
	mu sync.Mutex
)

// Connect pulls information from your environment and connects to a database.
func Connect(ctx context.Context, name string) (*sql.DB, error) {
	mu.Lock() // defer in go 1.11 and 1.12 takes a noticable amount of time when tracing
	if db != nil {
		mu.Unlock()
		return db, nil
	}
	// context.Context can be used in the future to get account level secure databases.
	if getenv("RDS_HOST") == "" {
		mu.Unlock()
		return nil, errors.New("rdb: No database resource provisioned")
	}

	idb, err := sql.Open(driverName, dsn(name))
	if err != nil {
		mu.Unlock()
		return nil, err
	}
	db = idb
	mu.Unlock()

	// By default, this is 0 which sets max number of open conns to infinity.
	// We need to monitor this to ensure we don't overwhelm the container if
	// we open too many TCP connections simultaneously. Use zero for "infinity"
	db.SetMaxOpenConns(0)
	// we have to watch idle conns when using aurora as described
	// here: https://github.com/go-sql-driver/mysql/issues/363
	db.SetMaxIdleConns(100)
	// by default, MySQL has a wait_timeout of 8 hours.  Just to be
	// safe, we set it to 30 minutes.  If we don't set this, queries
	// to mysql will fail with an ErrBadConn error.
	db.SetConnMaxLifetime(30 * time.Minute)

	// Verify connection to the database
	if err = db.Ping(); err != nil {
		return nil, err
	}
	check.Register("rdb", check.Debounce(db.Ping, time.Second))
	return db, nil
}

// used for unit tests (gross, I know)
var getenv = os.Getenv

func dsn(name string) string {
	if name == "" {
		name = getenv("WORKIVA_SERVICE_NAME")
	}
	user := getenv("RDS_USER")
	host := getenv("RDS_HOST")
	port := getenv("RDS_PORT")
	pass := getenv("RDS_PASSWORD")
	// Config stolen from skaardb (TODO: document each option)
	// https://github.com/Workiva/skaardb/search?q=dsn_params&unscoped_q=dsn_params
	// https://github.com/Workiva/skaardb/blob/master/cerberus/config/persister_config.go
	params := "parseTime=true&"
	params += "timeout=90s&"
	params += "writeTimeout=90s&"
	params += "readTimeout=90s&"
	params += "tls=skip-verify&" // https://github.com/go-sql-driver/mysql/issues/363
	params += "maxAllowedPacket=1000000000&"
	params += "rejectReadOnly=true"
	return user + ":" + pass + "@tcp(" + host + ":" + port + ")/" + name + "?" + params
}
