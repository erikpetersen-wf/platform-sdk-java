####
# Docker Commands
####
# Assumes ARTIFACTORY_PRO_USER and ARTIFACTORY_PRO_PASS are exported in your shell environment
# ARTIFACTORY_PRO_USER is your github username
# ARTIFACTORY_PRO_PASS can be found at https://workivaeast.jfrog.io/workivaeast/webapp/#/profile
gen-docker:
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		-t drydock.workiva.net/workiva/platform:latest-release .

gen-docker-no-tests:
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		--build-arg SKIP_TESTS=true \
		-t drydock.workiva.net/workiva/platform:latest-release .

test:
	docker build -t package:test .
	SKYNET_APPLICATION_PLATFORM=platform:test ./test/package/run
.PHONY: test
