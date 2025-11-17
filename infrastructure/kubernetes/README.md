# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the Intelligent Message Processor to a Kubernetes cluster.

## Prerequisites

- Kubernetes cluster (v1.25+)
- kubectl configured
- Helm (optional, for dependencies)

## Infrastructure Setup

### 1. Deploy PostgreSQL

```bash
# Using Helm
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install postgres bitnami/postgresql \
  --set auth.username=imp_user \
  --set auth.password=secure_password \
  --set auth.database=imp_extraction
```

### 2. Deploy Redis

```bash
helm install redis bitnami/redis \
  --set auth.enabled=false
```

### 3. Deploy Kafka

```bash
helm repo add strimzi https://strimzi.io/charts/
helm install kafka strimzi/strimzi-kafka-operator
# Apply Kafka custom resource
kubectl apply -f kafka-cluster.yaml
```

## Application Deployment

### 1. Create Secrets

```bash
kubectl create secret generic postgres-secret \
  --from-literal=username=imp_user \
  --from-literal=password=secure_password
```

### 2. Deploy Services

```bash
# Deploy all services
kubectl apply -f extraction-service-deployment.yaml
kubectl apply -f classification-service-deployment.yaml
kubectl apply -f job-search-processor-deployment.yaml
```

### 3. Verify Deployment

```bash
kubectl get pods
kubectl get services
kubectl logs -f deployment/extraction-service
```

## Monitoring

Prometheus and Grafana can be deployed using:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack
```

## Scaling

Services are configured with HorizontalPodAutoscaler:
- Extraction Service: 2-10 replicas
- Classification Service: 3-15 replicas
- Job Search Processor: 1-5 replicas
