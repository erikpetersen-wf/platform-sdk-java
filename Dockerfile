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
RUN wget -q https://storage.googleapis.com/kubernetes-helm/helm-v2.16.1-linux-amd64.tar.gz && \
    echo "7eebaaa2da4734242bbcdced62cc32ba8c7164a18792c8acdf16c77abffce202  helm-v2.16.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v2.16.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v2.16.1-linux-amd64.tar.gz linux-amd64


#! STAGE - Shared python builder (security approved python:3.8 image)
# https://tecadmin.net/install-python-3-8-centos/
FROM amazonlinux:2 as python38
WORKDIR /build/
ENV VERSION=3.8.3
RUN yum update -y && \
    yum upgrade -y && \
    yum install -y gcc openssl-devel bzip2-devel libffi-devel tar gzip make && \
    yum autoremove -y && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    cd /opt && \
    curl -o python.tgz https://www.python.org/ftp/python/${VERSION}/Python-${VERSION}.tgz && \
    tar xzf python.tgz && \
    cd Python-${VERSION}/ && \
    ./configure --enable-optimizations && \
    make install && \
    rm -rf /opt/Python* /opt/python.tgz

RUN python3 --version
RUN pip3 install --upgrade pip


#! STAGE - Python deps - download tool dependencies
FROM python38 as python-deps

# Add wk tool (with requirements based layer caching!)
ARG PIP_INDEX_URL
COPY tools/wk/ /root/wk/
RUN mkdir -p /wheels && \
    pip3 install wheel && \
    pip3 wheel -w /wheels -r /root/wk/requirements.txt


#! STAGE - Platform Builder - Python 3 - Help customers package their application
FROM python38

# Verify HELM
COPY --from=helm /usr/local/bin/helm /usr/local/bin/helm
RUN helm init --client-only

# Add package (backwards compatibility for folks directly referencing `package`)
ADD tools/package /usr/local/bin

# Copy in WK command
COPY --from=python-deps /root/wk/ /root/wk/
COPY --from=python-deps /wheels /wheels
RUN pip3 install --no-index --find-links=/wheels /root/wk/
RUN wk --version

# steps for consuming builds to use
ONBUILD ADD helm /build/helm/
ONBUILD ADD Dockerfile /build/
ONBUILD RUN wk package
ONBUILD ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz

# # USAGE
# FROM drydock-prod.workiva.net/workiva/platform:v0
