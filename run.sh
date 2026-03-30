#!/usr/bin/env bash
set -e

echo "🚀 Démarrage de ParkManShift (Detached mode)..."
docker compose up -d

echo ""
echo "=========================================================="
echo "✅ ParkManShift (Skeleton) est LANCE avec ses conteneurs !"
echo "   - 🌐 Web Interface:      http://localhost"
echo "   - 🔌 API:                http://localhost:8080/api/hello"
echo "   - 🐰 RabbitMQ Dashboard: http://localhost:15672 (guest/guest)"
echo "   - 🐘 PostgreSQL:         localhost:5432 (parkmanshift/password)"
echo "=========================================================="
