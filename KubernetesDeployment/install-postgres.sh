#!/bin/bash
helm install postgres bitnami/postgresql --namespace final --values postgres-values.yaml
