# Otus Final Project

Otus Final Project...

***Description TBD***
To be honest, codebases requires refactoring due to problems in core functionality and architecture
If you're going to review this distributed ball of mud, I sincerily apologize, It has been made in 8 days from Pre-Existing Services that I had written as part of previous homeworks in the Otus Microservice CoUrse

### Kubernetes Deployment

#### Prerequisites:

1. Kubernetes Cluster
2. Helm

#### Setup:

1. Install istiod

```bash
helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update
kubectl create namespace istio-system
helm install istio-base istio/base -n istio-system
helm install istiod istio/istiod -n istio-system --wait
```

2. Install istio-ingress

```bash
kubectl create namespace istio-ingress
kubectl label namespace istio-ingress istio-injection=enabled
helm install istio-ingress istio/gateway -n istio-ingress --wait
```

3. Run `bootstrap.sh` in KubernetesDeployment folder. It will install everything needed into newly created
   namespace `stream-processing`

#### Uninstall:

1. Run `tear-down.sh` in KubernetesDeployment folder.
2. `kubectl delete ns stream-processing`

**Total:** 20 Gi of Storage and ~4 Gi of RAM

### IntelliJ Idea

0. `git clone https://github.com/ReDestroyDeR/StreamProcessing`
1. Start all the dependencies via `docker-compose up -d`
2. Open `StreamProcessing` in IntelliJ as Project
3. (Optional) If modules haven't been detected automatically you need to:
   1. **File** -> **Project Structure** -> **+** -> **Import module** -> **(
      NotificationService/OrderService/BillingService/...)** -> **Maven**
   2. Wait until they don't appear in Project Structure Menu
4. Generate Avro schemas for each service `mvn avro:schema`
5. Mark `target/generated-sources` as generated sources folder in **Project Structure**
   1. **... Service** -> **Sources** -> **Select target/generated-sources** -> **Alt + S** -> **(On the right panel)
      Source Folders target/generated-sources Edit properties (Pencil Symbol)** -> **Check For generated sources**
6. You have ready development deployment!

