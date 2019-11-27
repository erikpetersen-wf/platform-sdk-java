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
COPY ./libs/java ./libs/java
RUN mvn -B dependency:go-offline -q -f ./libs/java/pom.xml

WORKDIR /build/libs/java
# Linter Steps
# TODO: move to skynet ;)
RUN mvn -B clean install

RUN mkdir -p /artifacts/java && \
    mv target/platform-*.jar \
    platform/target/platform-*.jar \
    platform-jetty/target/platform-jetty-*.jar \
    platform-core/target/platform-core-*.jar \
    platform-undertow/target/platform-undertow-*.jar \
    /artifacts/java

# Publish Artifacts
ARG BUILD_ARTIFACTS_JAVA=/artifacts/java/*.jar


#! STAGE - Platform Builder - Python 3 - Help customers package their application
FROM python:3.7-alpine
WORKDIR /build/

# Get latest package updates (security requirement)
RUN apk update && apk upgrade

# Install and Verify HELM
RUN wget -q https://storage.googleapis.com/kubernetes-helm/helm-v2.16.1-linux-amd64.tar.gz && \
    echo "7eebaaa2da4734242bbcdced62cc32ba8c7164a18792c8acdf16c77abffce202  helm-v2.16.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v2.16.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v2.16.1-linux-amd64.tar.gz linux-amd64 && \
    helm init --client-only

# Add wk tool (with requirements based layer caching!)
COPY tools/wk/ /root/wk/
RUN pip install /root/wk/

# Add package (backwards compatibility for folks directly referencing `package`)
ADD tools/package /usr/local/bin

# steps for consuming builds to use
ONBUILD ADD helm /build/helm/
ONBUILD ADD Dockerfile /build/
ONBUILD RUN wk package
ONBUILD ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz

# # USAGE
# FROM drydock-prod.workiva.net/workiva/platform:v0
