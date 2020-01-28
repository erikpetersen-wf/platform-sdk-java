#! /usr/bin/env bash

# Setup "safe" bash script, releative to scripts location
set -euxo pipefail
cd "$(dirname "$0")"

# https://helm.sh/docs/topics/chart_repository/

echo "TODO: update helm charts' version if there are changes!"

# While in --check mode, we need to prune out `created` and `generated` lines
# because they include the exact timestamp of generation.  This value is used
# below with `grep -v` to create a clean file that can be verified.
export DIRTY="ated:"

# If we are verifying, create a clean copy of the original index.yaml
if [[ $* == *--check* ]]
then
  grep -v $DIRTY index.yaml > orig.yaml
  trap "rm orig.yaml" EXIT
fi

helm package service
helm repo index .

# If we are verifying, create a clean copy of the generated index.yaml and diff
if [[ $* == *--check* ]]
then
  grep -v $DIRTY index.yaml > next.yaml
  trap "rm next.yaml orig.yaml" EXIT # traps overrite, so delete both files here
  diff orig.yaml next.yaml
fi
