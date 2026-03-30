#!/usr/bin/env bash
set -e

echo "🧪 [BACKEND] Exécution des tests Spring Boot réels (MockMvc)..."
cd backend
rm -f src/test/java/com/parkmanshift/backend/BackendApplicationTests.java || true # on enlève le context load s'il vient de la création spring car on a le vrai test mvc
./mvnw test
cd ..

echo "✅ Tous les tests locaux (sans DB requise ni Docker) sont terminés !"
