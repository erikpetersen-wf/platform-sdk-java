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

deps: ## Install dependencies
	pip install -r requirements_dev.txt

test: ## Run integration tests
	docker build -t package:test .
	SKYNET_APPLICATION_PLATFORM=platform:test ./test/package/run
.PHONY: test

unit: ## Run unit tests
	yapf --recursive --parallel --diff skynet
	flake8
	pydocstyle
	mypy skynet
	py.test -s -v
.PHONY: unit
