FROM debian:stretch-slim as helm_artifact

# get updates for security requirements
RUN apt update && \
    apt full-upgrade -y && \
    apt autoremove -y && \
    apt clean all

# Install and verify HELM
RUN apt-get install -y wget && \
    wget -q https://storage.googleapis.com/kubernetes-helm/helm-v2.9.1-linux-amd64.tar.gz && \
    echo "56ae2d5d08c68d6e7400d462d6ed10c929effac929fedce18d2636a9b4e166ba helm-v2.9.1-linux-amd64.tar.gz" | sha256sum -c && \
    tar xf helm-v2.9.1-linux-amd64.tar.gz && \
    cp linux-amd64/helm /usr/local/bin && \
    rm -rf helm-v2.9.1-linux-amd64.tar.gz linux-amd64

WORKDIR /build/
RUN helm init --client-only
ADD package /usr/bin/local

# # USAGE
# FROM drydock.workiva.com/Workiva/platform:0.0.1 as builder
# ADD helm /build/
# RUN package
# ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz
