# Configuraciones del Proyecto

Carpeta centralizada con archivos de configuración para monitoreo y deployments.

## Contenido

- **prometheus.yml** - Configuración de Prometheus para recolección de métricas
- **grafana/** - Configuración de dashboards y datasources de Grafana

## Uso

Los archivos aquí se montan automáticamente en los contenedores mediante docker-compose.yml.

### Prometheus
- Endpoint: http://localhost:9090
- Recolecta métricas de: auth-api, users-api, todos-api

### Grafana  
- Endpoint: http://localhost:3000
- Credenciales: admin/admin
- Datasource: Prometheus (ya configurado automáticamente)
