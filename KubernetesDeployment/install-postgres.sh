#!/bin/bash
helm install postgres bitnami/postgresql --namespace stream-processing --values postgres-values.yaml
