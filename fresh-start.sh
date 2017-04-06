#!/usr/bin/env bash
set -x

gsutil rm gs://$(cf env casey-storage-sample | ag bucket_name | perl -p -e's/^/{/;s/$/}/' | jq -r '.bucket_name')/*

cf delete -f casey-storage-sample
cf delete-service -f storage-service
cf delete-orphaned-routes -f

fly -t lite destroy-pipeline -p storage-demo
fly -t lite set-pipeline -p storage-demo -c ci/pipeline.yml -l ~/.gcp-config.yml
fly -t lite unpause-pipeline -p storage-demo
