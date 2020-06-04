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
    platform-core/target/platform-core-*.jar \
    platform-undertow/target/platform-undertow-*.jar \
    platform-jetty-servlet/target/platform-jetty-servlet-*.jar \
    platform-netty/target/platform-netty-*.jar \
    platform-spring/target/platform-spring-*.jar \
    /artifacts/java

# Publish Artifacts
ARG BUILD_ARTIFACTS_JAVA=/artifacts/java/*.jar


#! STAGE - Helm Download - Helm - download helm for install in base image.
FROM bash:5 as helm
RUN wget -q https://get.helm.sh/helm-v3.2.1-linux-amd64.tar.gz && \
    echo "018f9908cb950701a5d59e757653a790c66d8eda288625dbb185354ca6f41f6b  helm-v3.2.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v3.2.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v3.2.1-linux-amd64.tar.gz linux-amd64


#! STAGE - Shared python builder (security approved python:3.8 image)
# https://tecadmin.net/install-python-3-8-centos/
FROM amazonlinux:2 as python-base
WORKDIR /build/

# Get latest package updates (security requirement)
RUN yum update -y && \
    yum upgrade -y && \
    yum install -y python3 && \
    yum autoremove -y && \
    yum clean all && \
    rm -rf /var/cache/yum

RUN python3 --version
RUN pip3 install --upgrade pip


#! STAGE - Platform Builder - Python 3 - Help customers package their application
FROM python-base

# Verify HELM
COPY --from=helm /usr/local/bin/helm /usr/local/bin/helm

# Add package (backwards compatibility for folks directly referencing `package`)
ADD tools/package /usr/local/bin
RUN mkdir /root/.wk
COPY tools/config.yml /root/.wk/config.yml


# steps for consuming builds to use
ONBUILD ARG GITHUB_USER
ONBUILD ARG GITHUB_PASS
ONBUILD ARG PIP_INDEX_URL
# public pip registry has a version 1.0 for some reason :cry:
ONBUILD RUN pip install "wk<1.0"
ONBUILD RUN wk --version
ONBUILD ADD ./ /build/
ONBUILD RUN wk package
ONBUILD ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz

# # USAGE
# FROM drydock-prod.workiva.net/workiva/platform:v0
