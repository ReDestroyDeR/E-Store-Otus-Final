#!/bin/bash
./tear-down-postgres.sh
./tear-down-mongo.sh
./tear-down-kafka.sh

kubectl delete -f configmap.yaml
kubectl delete -f secret.yaml

kubectl delete -f deployment.yaml
kubectl delete -f svc.yaml

kubectl delete -f dr.yaml
kubectl delete -f vs.yaml
kubectl delete -f gw.yaml
