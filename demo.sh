#!/bin/bash

# 🎬 Script para ejecutar demostración completa
# Uso: chmod +x demo.sh && ./demo.sh

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║     🚀 MICROSERVICIOS CON KUBERNETES, PROMETHEUS Y GRAFANA    ║"
echo "║           Taller 1 - Plataformas 2                            ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verificar prerequisitos
echo -e "${BLUE}[1/5]${NC} Verificando requisitos..."
echo ""

echo -n "  Docker: "
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✓${NC} $DOCKER_VERSION"
else
    echo -e "${RED}✗${NC} Docker no está instalado"
    exit 1
fi

echo -n "  Docker Compose: "
if command -v docker-compose &> /dev/null; then
    DC_VERSION=$(docker-compose --version)
    echo -e "${GREEN}✓${NC} $DC_VERSION"
else
    echo -e "${RED}✗${NC} Docker Compose no está instalado"
    exit 1
fi

echo ""
echo -e "${BLUE}[2/5]${NC} Limpiando contenedores antiguos..."
docker-compose down -v 2>/dev/null || true
echo -e "${GREEN}✓${NC} Limpieza completada"
echo ""

echo -e "${BLUE}[3/5]${NC} Construyendo imágenes Docker..."
echo "  Esto puede tomar 10-15 minutos la primera vez..."
echo ""

docker-compose build

echo ""
echo -e "${GREEN}✓${NC} Imágenes construidas exitosamente"
echo ""

echo -e "${BLUE}[4/5]${NC} Iniciando servicios..."
echo "  Esperando a que todos los servicios estén saludables..."
echo ""

docker-compose up -d

# Esperar a que los servicios estén saludables
echo "  Esperando servicios..."
sleep 10

# Verificar servicios
HEALTH_CHECK_PASSED=0
for i in {1..30}; do
    if docker-compose ps | grep -q "Up.*healthy"; then
        echo -e "${GREEN}✓${NC} Servicios iniciados"
        HEALTH_CHECK_PASSED=1
        break
    fi
    echo -n "."
    sleep 2
done

if [ $HEALTH_CHECK_PASSED -eq 0 ]; then
    echo -e "${RED}✗${NC} Servicios no respondieron"
    docker-compose logs
    exit 1
fi

echo ""
echo -e "${BLUE}[5/5]${NC} Mostrando información de acceso..."
echo ""

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                    🎉 ¡TODO LISTO!                           ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

echo -e "${YELLOW}📱 APLICACIONES DISPONIBLES:${NC}"
echo ""
echo "  Frontend (Vue.js)"
echo -e "    ${GREEN}http://localhost:8080${NC}"
echo "    Usuario: admin"
echo "    Contraseña: admin"
echo ""

echo "  Prometheus (Métricas)"
echo -e "    ${GREEN}http://localhost:9090${NC}"
echo "    Query ejemplo: rate(http_requests_total[1m])"
echo ""

echo "  Grafana (Monitoreo)"
echo -e "    ${GREEN}http://localhost:3000${NC}"
echo "    Usuario: admin"
echo "    Contraseña: admin"
echo ""

echo -e "${YELLOW}🔧 APIS DISPONIBLES:${NC}"
echo ""
echo "  Auth API (Go)"
echo "    http://localhost:8000"
echo ""
echo "  Users API (Java)"
echo "    http://localhost:8083"
echo ""
echo "  TODOs API (Node.js)"
echo "    http://localhost:8082"
echo ""

echo -e "${YELLOW}📊 ESTADO DE CONTENEDORES:${NC}"
echo ""
docker-compose ps
echo ""

echo -e "${YELLOW}🔍 VER LOGS EN TIEMPO REAL:${NC}"
echo ""
echo "  Todos los servicios:"
echo "    ${BLUE}docker-compose logs -f${NC}"
echo ""
echo "  Solo Log Processor (operaciones):"
echo "    ${BLUE}docker-compose logs -f log-processor${NC}"
echo ""
echo "  Solo APIs:"
echo "    ${BLUE}docker-compose logs -f auth-api users-api todos-api${NC}"
echo ""

echo -e "${YELLOW}⏹️  PARA DETENER:${NC}"
echo ""
echo "    ${BLUE}docker-compose down${NC}"
echo ""

echo -e "${YELLOW}📚 DOCUMENTACIÓN:${NC}"
echo ""
echo "  Guía Docker Compose"
echo "    GUIA_DOCKER_COMPOSE.md"
echo ""
echo "  Guía Kubernetes"
echo "    INTEGRACION_KUBERNETES_PROMETHEUS_GRAFANA.md"
echo ""
echo "  Pruebas sin Docker"
echo "    GUIA_PRUEBAS.md"
echo ""

echo -e "${YELLOW}🎬 PRÓXIMOS PASOS PARA EL VIDEO:${NC}"
echo ""
echo "  1. Abre http://localhost:8080 en tu navegador"
echo "  2. Login con admin/admin"
echo "  3. Crea algunos TODOs (completa/elimina algunos)"
echo "  4. En otra terminal: docker-compose logs -f log-processor"
echo "  5. Abre http://localhost:9090 (Prometheus)"
echo "  6. Abre http://localhost:3000 (Grafana)"
echo "  7. Ve cómo cambian las métricas cuando haces acciones"
echo ""

echo -e "${GREEN}¡Listo para grabar!${NC} 🎥"
echo ""
