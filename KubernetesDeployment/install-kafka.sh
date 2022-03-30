#!/bin/bash
helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/
helm repo update
helm install confluent confluentinc/cp-helm-charts --namespace final --values confluent-values.yaml
