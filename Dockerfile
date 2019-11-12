#! STAGE - Client Library - Java - Cache Depencencies
FROM maven:3.6-jdk-8-alpine as java_lib_dependencies

WORKDIR /build
ENV MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
ENV CODECOV_TOKEN='bQ4MgjJ0G2Y73v8JNX6L7yMK9679nbYB'
RUN apk add --update bash git perl wget

# Setup Maven Authentication
RUN mkdir -p /root/.m2
ARG ARTIFACTORY_PRO_USER
ARG ARTIFACTORY_PRO_PASS
COPY ./workivabuild.settings.xml /root/.m2/settings.xml

# Cache Wrapper Dependencies
COPY ./libs/java/platform/pom.xml ./libs/java/platform/pom.xml
RUN mvn -B dependency:go-offline -q -f ./libs/java/platform/pom.xml

COPY ./libs/java/platform-jetty/pom.xml ./libs/java/platform-jetty/pom.xml
RUN mvn -B dependency:go-offline -q -f ./libs/java/platform-jetty/pom.xml

#! STAGE - Client Library - Java - Produce Library
WORKDIR /build
RUN mkdir -p /artifacts/java
COPY ./libs/java ./libs/java

WORKDIR /build/libs/java/platform
# Linter Steps
# TODO: move to skynet ;)
RUN mvn -B fmt:check -q
RUN mvn -B checkstyle:checkstyle -q
# Run Unit-Tests & Build
RUN mvn -B clean && mvn -B verify
RUN mv /build/libs/java/platform/target/platform-*.jar /artifacts/java

WORKDIR /build/libs/java/platform-jetty
RUN mvn -B fmt:check -q
RUN mvn -B checkstyle:checkstyle -q
RUN mvn -B clean && mvn -B verify
RUN mv /build/libs/java/platform-jetty/target/platform-jetty-*.jar /artifacts/java

# Publish Artifacts
ARG BUILD_ARTIFACTS_JAVA=/artifacts/java/*.jar

#! STAGE - Platform Python Tests - Python 3 - Verify the Python code
# TODO: move to skynet ;)
FROM python:3.7-alpine
RUN apk update && apk upgrade && apk add make
ADD requirements.txt requirements_dev.txt /
RUN pip install -r requirements_dev.txt
ADD package Makefile /
ADD test/ /test/
RUN make check-py


#! STAGE - Platform Builder - Python 3 - Help customers package their application
FROM python:3.7-alpine
WORKDIR /build/

# Get latest package updates (security requirement)
RUN apk update && apk upgrade

# Install and Verify HELM
RUN wget -q https://storage.googleapis.com/kubernetes-helm/helm-v2.9.1-linux-amd64.tar.gz && \
    echo "56ae2d5d08c68d6e7400d462d6ed10c929effac929fedce18d2636a9b4e166ba  helm-v2.9.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v2.9.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v2.9.1-linux-amd64.tar.gz linux-amd64 && \
    helm init --client-only

# Add Python dependencies (layer caching!)
ADD requirements.txt .
RUN pip install -r requirements.txt && rm requirements.txt

# Add the actual package script!
ADD package /usr/local/bin

# steps for consuming builds to use
ONBUILD ADD helm /build/helm/
ONBUILD ADD Dockerfile /build/
ONBUILD RUN package
ONBUILD ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz

# # USAGE
# FROM drydock-prod.workiva.net/workiva/platform:v0
