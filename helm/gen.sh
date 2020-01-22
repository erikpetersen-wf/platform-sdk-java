#! /bin/bash

# Setup "safe" bash script, releative to scripts location
set -euxo pipefail
cd "$(dirname "$0")"

# https://helm.sh/docs/topics/chart_repository/

echo "TODO: update monochart version if there are changes!"

helm package monochart
helm repo index .
