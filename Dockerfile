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
ENV HELM_HOME=/build/
RUN helm init --client-only
ADD package /usr/local/bin
RUN chown -R nobody:nogroup /build/
USER nobody

# steps for consuming builds to use
ONBUILD ADD helm /build/helm/
ONBUILD ADD Dockerfile /build/
ONBUILD RUN package

# # USAGE
# FROM drydock-prod.workiva.net/workiva/platform:v0 as platform
# ARG BUILD_ARTIFACTS_HELM_CHARTS=/build/*.tgz
