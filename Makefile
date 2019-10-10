# This target scrapes this Makefile for '##' and displays those messages
help: ## Show this message!
	@echo 'Usage:'
	@echo '  make [target] ...'
	@echo
	@echo 'Targets:'
	@egrep '^.+\:.*\ ##\ .+' Makefile | sed 's/:.*##/:/' | column -t -c 2 -s ':' | sort | sed 's/^/  /'

init: init-py ## Initialize environment
.PHONY:=init

check: check-py check-go ## Run all unit tests
.PHONY:=check

check-full: check env-notify ## Run all tests (unit + integration)
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		-t drydock.workiva.net/workiva/platform:latest-release .
	SKYNET_APPLICATION_PLATFORM=drydock.workiva.net/workiva/platform:latest-release ./test/package/run
.PHONY:=check-full

build: env-notify ## Build (docker image)
	docker build \
		--build-arg GIT_SSH_KEY \
		--build-arg ARTIFACTORY_PRO_USER \
		--build-arg ARTIFACTORY_PRO_PASS \
		--build-arg SKIP_TESTS=true \
		-t drydock.workiva.net/workiva/platform:latest-release .
.PHONY:=build

env-notify:
	@echo "Assumes ARTIFACTORY_PRO_USER and ARTIFACTORY_PRO_PASS are exported in your shell environment"
	@echo "\tARTIFACTORY_PRO_USER is your github username"
	@echo "\tARTIFACTORY_PRO_PASS can be found at https://workivaeast.jfrog.io/workivaeast/webapp/#/profile"
.PHONY:=env-notify


# ------------------------- Python -------------------------

init-py: requirements_dev.txt ## Initialize Python environment
	pip install -Ur requirements_dev.txt
.PHONY:=init-py

check-py: init-py ## Run Python unit tests
	yapf --recursive --parallel --diff package
	flake8 package
	# pydocstyle
	# mypy package
	# py.test -s -v
.PHONY:=check-py


# ------------------------- Go -------------------------

check-go: ## Run Go unit tests
	echo "TEST GO CODE"
.PHONY:=check-go
