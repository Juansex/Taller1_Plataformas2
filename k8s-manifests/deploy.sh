#!/bin/bash

set -e

echo "Iniciando deployment Kubernetes..."
echo ""

echo "1. Creando ConfigMaps..."
kubectl apply -f k8s-manifests/configmaps/configmap.yaml
echo "   ConfigMap creado correctamente"
echo ""

echo "2. Creando Secrets..."
kubectl apply -f k8s-manifests/secrets/secret.yaml
echo "   Secret creado correctamente"
echo ""

echo "3. Creando Services..."
kubectl apply -f k8s-manifests/services/services.yaml
echo "   Services creados correctamente"
echo ""

echo "4. Creando Deployments..."
kubectl apply -f k8s-manifests/deployments/
echo "   Deployments creados correctamente"
echo ""

echo "5. Esperando a que los pods estén listos..."
sleep 5

echo "6. Creando HPAs..."
kubectl apply -f k8s-manifests/hpa/hpa.yaml
echo "   HPAs creados correctamente"
echo ""

echo "7. Verificando estado de los pods..."
kubectl get pods -o wide
echo ""

echo "8. Listando HPAs..."
kubectl get hpa
echo ""

echo "Deployment completado exitosamente!"
echo ""
echo "URLs de acceso:"
echo "  Frontend: http://localhost:30080 (NodePort)"
echo "  Auth API: http://auth-api:8000 (interno)"
echo "  Users API: http://users-api:8083 (interno)"
echo "  Todos API: http://todos-api:8082 (interno)"
echo ""
echo "Para ver los HPA en acción:"
echo "  kubectl get hpa -w"
echo "  kubectl top pods"
echo "  kubectl describe hpa users-api-hpa"
