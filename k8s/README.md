# Kubernetes (K8s) 

Kubernetes is an open-source container orchestration platform that automates the deployment, scaling, and management of containerized applications. It was originally designed by Google and is now maintained by the Cloud Native Computing Foundation.

## Setup Infra and Application

1. Create configmap for the application
```bash
kubectl apply -f infra/config/
```

2. Create secret for the application
```bash
kubectl apply -f infra/secret/
```

3. Create volumn for the application
```bash
kubectl apply -f infra/volume/
```

4. Create `node_exporter`, `prometheus` and `grafana` for monitoring
```bash
kubectl apply -f infra/node_exporter.yml
kubectl apply -f infra/prometheus.yml
kubectl apply -f infra/grafana.yml
```
5. Access to minikube dashboard
```bash
minikube dashboard
```

6. Expose the application
```bash
minikube tunnel
```

