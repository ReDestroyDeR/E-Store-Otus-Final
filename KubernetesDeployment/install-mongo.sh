#!/bin/bash
helm install mongodb bitnami/mongodb --namespace stream-processing --values mongo-values.yaml
