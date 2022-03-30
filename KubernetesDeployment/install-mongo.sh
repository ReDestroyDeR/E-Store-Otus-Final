#!/bin/bash
helm install mongodb bitnami/mongodb --namespace final --values mongo-values.yaml
