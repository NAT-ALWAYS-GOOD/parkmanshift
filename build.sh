#!/usr/bin/env bash
set -e
echo "🏗️ [DOCKER] Construction de ParkManShift..."
docker compose build
echo "✅ Construction terminée avec succès."
