package rdb

import (
	"database/sql"
	"database/sql/driver"
	"errors"
	"os"
	"testing"
)

func fakeENV(tb testing.TB) func() {
	getenv = func(name string) string {
		switch name {
		case "WORKIVA_DEPLOY_MODE":
			return "modded"
		case "WORKIVA_SERVICE_NAME":
			return "service"
		case "RDS_USER":
			return "user"
		case "RDS_HOST":
			return "host"
		case "RDS_PORT":
			return "port"
		case "RDS_PASSWORD":
			return "pass"
		default:
			tb.Fatalf("unknown getenv call: %v", name)
			return "wtf-mate"
		}
	}
	return func() {
		getenv = os.Getenv
	}
}

func TestDSN(t *testing.T) {
	defer fakeENV(t)()
	str := dsn("")
	exp := "user:pass@tcp(host:port)/service?parseTime=true&timeout=90s&writeTimeout=90s&"
	exp += "readTimeout=90s&tls=skip-verify&maxAllowedPacket=1000000000&rejectReadOnly=true"
	if str != exp {
		t.Errorf("Did not retrieve expected DSN\nGOT : %q\nWANT: %q", str, exp)
	}
}

type fakeSQL struct {
	openErr error
	closed  bool
}

func (d *fakeSQL) Open(name string) (driver.Conn, error)     { return d, d.openErr }
func (d *fakeSQL) Prepare(query string) (driver.Stmt, error) { return nil, nil }
func (d *fakeSQL) Begin() (driver.Tx, error)                 { return nil, nil }
func (d *fakeSQL) Close() error {
	d.closed = true
	return nil
}

func TestConnect(t *testing.T) {
	defer fakeENV(t)()
	defer func() {
		driverName = "nrmysql"
	}()
	driverName = "fake"
	d := &fakeSQL{}
	sql.Register("fake", d)

	// Case 0: noop everything
	_, err := Connect(nil, "")
	if err != nil {
		t.Fatalf("Couldn't connect: %v", err)
	}

	// Case 1: ping failure
	db = nil
	d.openErr = errors.New("bad ping")
	_, err = Connect(nil, "")
	if err == nil || err.Error() != "bad ping" {
		t.Fatalf("Unexpected ping error: %v", err)
	}

	// Case 2: open failure
	db = nil
	driverName = "other"
	_, err = Connect(nil, "")
	if err == nil || err.Error() != "sql: unknown driver \"other\" (forgotten import?)" {
		t.Fatalf("Unexpected open error: %v", err)
	}
}

func TestBadConnect(t *testing.T) {
	_, err := Connect(nil, "")
	if err == nil || err.Error() != "rdb: Not deployed with workiva/platform" {
		t.Fatalf("Unexpected connect error: %v", err)
	}
}
