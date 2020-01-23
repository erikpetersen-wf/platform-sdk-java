#! /bin/bash

# Setup "safe" bash script, releative to scripts location
set -euxo pipefail
cd "$(dirname "$0")"

# https://helm.sh/docs/topics/chart_repository/

echo "TODO: update monochart version if there are changes!"

helm package monochart
helm repo index .

echo "TODO: run this script in check mode during CI to make sure assets are committed"
