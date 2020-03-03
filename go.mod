module github.com/Workiva/platform

go 1.11

require (
	git.apache.org/thrift.git v0.0.0-20161221203622-b2a4d4ae21c7 // indirect
	github.com/Sirupsen/logrus v1.4.0
	github.com/Workiva/app_intelligence_go v0.0.0-20190822210737-3ede751e55e5
	github.com/Workiva/frugal v0.0.2-0.20190516182251-cca32f57ec1c // indirect
	github.com/Workiva/go-auth v0.0.0-20180606144156-041929d19d91 // indirect
	github.com/Workiva/messaging-sdk v0.0.0-20190422221936-cee83480243c
	github.com/Workiva/vessel v0.0.0-20190911220016-54cb050d26ac // indirect
	github.com/go-sql-driver/mysql v1.5.0
	github.com/go-stomp/stomp v2.0.3+incompatible // indirect
	github.com/golang/protobuf v1.3.2 // indirect
	github.com/google/jsonapi v0.0.0-20181016150055-d0428f63eb51
	github.com/kr/pretty v0.1.0 // indirect
	github.com/mattrobenolt/gocql v0.0.0-20130828033103-56c5a46b65ee // indirect
	github.com/nats-io/gnatsd v1.4.1 // indirect
	github.com/nats-io/go-nats v1.7.2 // indirect
	github.com/nats-io/nuid v1.0.1 // indirect
	github.com/newrelic/go-agent v3.3.0+incompatible
	github.com/pborman/uuid v1.2.0 // indirect
	github.com/sirupsen/logrus v1.4.2 // indirect
	github.com/stretchr/testify v1.4.0 // indirect
	golang.org/x/crypto v0.0.0-20190701094942-4def268fd1a4 // indirect
	golang.org/x/net v0.0.0-20190514140710-3ec191127204 // indirect
	golang.org/x/oauth2 v0.0.0-20150813224026-397fe7649477 // indirect
	golang.org/x/sync v0.0.0-20190911185100-cd5d95a43a6e // indirect
	golang.org/x/sys v0.0.0-20190813064441-fde4db37ae7a // indirect
	google.golang.org/appengine v1.6.2 // indirect
	gopkg.in/check.v1 v1.0.0-20180628173108-788fd7840127 // indirect
)

replace (
	git.apache.org/thrift.git => github.com/apache/thrift v0.0.0-20161221203622-b2a4d4ae21c7
	github.com/Sirupsen/logrus => github.com/sirupsen/logrus v1.4.0
	github.com/golang/protobuf => github.com/golang/protobuf v1.2.0
	github.com/nats-io/go-nats => github.com/nats-io/go-nats v0.0.0-20170202190301-e6bb81b5a5f3
	golang.org/x/tools => golang.org/x/tools v0.0.0-20181024171208-a2dc47679d30
	google.golang.org/appengine => google.golang.org/appengine v1.2.0
)
