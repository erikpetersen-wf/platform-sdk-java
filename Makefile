.PHONY: help init check check-full build env-notify clean

# This target scrapes this Makefile for '##' and displays those messages
help: ## Show this message!
	@echo 'Usage:'
	@echo '  make [target] ...'
	@echo
	@echo 'Targets:'
	@egrep '^.+\:.*\ ##\ .+' Makefile | sed 's/:.*##/:/' | column -t -c 2 -s ':' | sort | sed 's/^/  /'

init: init-py ## Initialize environment

check: check-py check-go ## Run all unit tests

check-full: check env-notify ## Run all tests (unit + integration)
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		-t drydock.workiva.net/workiva/platform:latest-release .
	SKYNET_APPLICATION_PLATFORM=drydock.workiva.net/workiva/platform:latest-release ./test/package/run

build: env-notify ## Build (docker image)
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		--build-arg SKIP_TESTS=true \
		-t drydock.workiva.net/workiva/platform:latest-release .

env-notify:
	@echo "Assumes ARTIFACTORY_PRO_USER and ARTIFACTORY_PRO_PASS are exported in your shell environment"
	@echo "\tARTIFACTORY_PRO_USER is your github username"
	@echo "\tARTIFACTORY_PRO_PASS can be found at https://workivaeast.jfrog.io/workivaeast/webapp/#/profile"

clean:
	rm -rf test/__pycache__/installed
	go clean -cache -modcache

# ------------------------- Python -------------------------

.PHONY: init-py check-py

init-py: test/__pycache__/installed ## Initialize Python environment

# use a marker file to denote if it's been run before
# NOTE: this is how Makefiles are supposed to run, not be script runners
test/__pycache__/installed: requirements_dev.txt requirements.txt
	pip install -Ur requirements_dev.txt
	mkdir -p test/__pycache__
	touch test/__pycache__/installed

check-py: init-py ## Run Python unit tests
	yapf --recursive --parallel --diff package test
	flake8 package test
	# pydocstyle
	# mypy package
	py.test -s -v test

# ------------------------- Go -------------------------

.PHONY: init-go check-go

init-go: ## Initialize Go enviornment
	GO111MODULE=on go mod download

check-go: init-go ## Run Go unit tests
	GO111MODULE=on go test -v ./...
