# This target scrapes this Makefile for '##' and displays those messages
help: ## This list
	@echo 'Usage:'
	@echo '  make [target] ...'
	@echo
	@echo 'Targets:'
	@egrep '^.+\:.*\ ##\ .+' Makefile | sed 's/:.*##/:/' | column -t -c 2 -s ':' | sort | sed 's/^/  /'

####
# Docker Commands
####
# Assumes ARTIFACTORY_PRO_USER and ARTIFACTORY_PRO_PASS are exported in your shell environment
# ARTIFACTORY_PRO_USER is your github username
# ARTIFACTORY_PRO_PASS can be found at https://workivaeast.jfrog.io/workivaeast/webapp/#/profile
gen-docker: ## Build docker image
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		-t drydock.workiva.net/workiva/platform:latest-release .

gen-docker-no-tests: ## Build docker image w/o tests
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		--build-arg SKIP_TESTS=true \
		-t drydock.workiva.net/workiva/platform:latest-release .

init: init-py ## Install dependencies
.PHONY: init

check-full: test ## Run integration tests
	docker build -t platform:test .
	SKYNET_APPLICATION_PLATFORM=platform:test ./test/package/run
.PHONY: check-full

test: test-py test-go  ## Run unit tests
.PHONY: test

test-go:
	echo "TEST GO CODE"
.PHONY: test-go


# ------------------------- PYTHON -------------------------

deps-py: requirements_dev.txt
	# pip install -Ur requirements_dev.txt
.PHONY: deps-py

test-py: deps-py
	yapf --recursive --parallel --diff package
	flake8
	# pydocstyle
	# mypy package
	# py.test -s -v
.PHONY: test-py
