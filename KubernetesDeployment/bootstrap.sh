#!/bin/bash
kubectl create -f ns.yaml

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
./install-postgres.sh
./install-mongo.sh
./install-kafka.sh

kubectl create -f configmap.yaml
kubectl create -f secret.yaml

kubectl create -f deployment.yaml
kubectl create -f svc.yaml

kubectl create -f dr.yaml
kubectl create -f vs.yaml
kubectl create -f gw.yaml

