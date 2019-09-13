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
COPY ./libs/java/pom.xml ./libs/java/pom.xml
RUN mvn -B dependency:go-offline -q -f ./libs/java/pom.xml

#! STAGE - Client Library - Java - Produce Library
FROM java_lib_dependencies as build_java_lib

WORKDIR /build
COPY ./libs/java ./libs/java

WORKDIR /build/libs/java
# Linter Steps
RUN mvn -B fmt:check -q
RUN mvn -B checkstyle:checkstyle -q
# Run Unit-Tests & Build
RUN mvn -B clean && mvn -B verify

# Publish Artifacts
# ARG BUILD_ARTIFACTS_AUDIT=/audit/**/*
ARG BUILD_ARTIFACTS_JAVA=/build/libs/java/target/platform-*.jar
# ARG BUILD_ARTIFACTS_TEST_REPORTS=/build/libs/java/target/surefire-reports/TEST-*.xml

FROM debian:stretch-slim

# get updates for security requirements
RUN apt update && \
    apt full-upgrade -y && \
    apt autoremove -y && \
    apt clean all

# Install and verify HELM
RUN apt-get install -y wget python3 && \
    wget -q https://storage.googleapis.com/kubernetes-helm/helm-v2.9.1-linux-amd64.tar.gz && \
    echo "56ae2d5d08c68d6e7400d462d6ed10c929effac929fedce18d2636a9b4e166ba helm-v2.9.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v2.9.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v2.9.1-linux-amd64.tar.gz linux-amd64

WORKDIR /build/
RUN helm init --client-only
ADD package /usr/local/bin

# # USAGE
# FROM drydock.workiva.com/Workiva/platform:latest-release as builder
# ADD helm /build/
# ADD Dockerfile /build/
# RUN package
# ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz
