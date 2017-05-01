#!/usr/bin/env bash
set -x

gsutil -m rm gs://$(cf env casey-storage-sample | ag bucket_name | perl -p -e's/^/{/;s/$/}/' | jq -r '.bucket_name')/*

cf delete -f casey-storage-sample
cf delete-service -f storage-service
cf delete-orphaned-routes -f

fly -t concourse destroy-pipeline -p storage-sample
fly -t concourse set-pipeline -p storage-sample -c ci/pipeline.yml -l ~/.gcp-config.yml
fly -t concourse unpause-pipeline -p storage-sample
