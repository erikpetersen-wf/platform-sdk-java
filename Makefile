test:
	docker build -t package:test .
	SKYNET_APPLICATION_PLATFORM=platform:test ./test/package/run
.PHONY: test
