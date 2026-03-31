# Parkmanshift 🅿️⚡

Parkmanshift est une application de gestion de réservation de places de parking conçue pour répondre aux défis du travail hybride. Elle permet aux employés de réserver des places de parking à la demande tout en gérant les spécificités liées aux véhicules électriques et aux profils utilisateurs (Employé, Secrétaire, Manager).

## 📌 Contexte du Projet

Le projet a été initié pour automatiser un processus manuel basé sur des emails et des fichiers Excel. Parkmanshift offre une solution moderne, équitable et efficace pour la gestion des 60 places disponibles, réparties en 6 rangées (A à F).

Pour plus de détails, consultez les [spécifications métiers](https://gist.github.com/rhwy/3d44893f3919c6ebdc5bdffad67b44eb).

## ✨ Fonctionnalités Clés

*   **Réservation Flexible** : Créneaux d'une demi-journée, jusqu'à 5 jours ouvrés consécutifs (30 jours pour les managers).
*   **Zones Dédiées (Électrique/Hybride)** : Les rangées A et F sont réservées aux véhicules nécessitant une recharge électrique.
*   **Système de Check-in** : Validation obligatoire de l'occupation avant 11h. Passé ce délai, la place est libérée.
*   **Gestion des Rôles** :
    *   **Employé** : Réservation en toute autonomie.
    *   **Secrétaire (Admin)** : Gestion complète des utilisateurs et des réservations.
    *   **Manager** : Accès à un tableau de bord analytique (taux d'occupation, statistiques).
*   **Notification par Email** : Envoi automatique d'une confirmation de réservation via une file de messages (RabbitMQ).

## 🛠️ Stack Technique

### Backend
*   **Langage** : Java 21+
*   **Framework** : Spring Boot 3.4+
*   **Architecture** : Hexagonale (domain-driven design)
*   **Base de Données** : PostgreSQL
*   **Migrations** : Flyway
*   **File de Messages** : RabbitMQ

### Frontend
*   **Framework** : Vue.js 3
*   **Build Tool** : Vite
*   **Langage** : TypeScript
*   **Design** : Vanilla CSS 
*   **Client API** : Généré via OpenAPI Generator (contract-first)

### Infrastructure & DevOps
*   **Containerisation** : Docker & Docker Compose
*   **Documentation API** : OpenAPI 3.0 (`openapi.yaml`)
*   **Scripts d'Automatisation** : Bash scripts (`build.sh`, `run.sh`, `test.sh`)

## 🚀 Démarrage Rapide

### Prérequis
*   Docker & Docker Compose
*   Java 21 (si exécution locale hors Docker)
*   Node.js & npm (si exécution locale hors Docker)

### Installation avec Docker (Recommandé)

1.  **Cloner le projet** :
    ```bash
    git clone https://github.com/NAT-ALWAYS-GOOD/parkmanshift.git
    cd parkmanshift
    ```

2.  **Configurer l'environnement** :
    Copiez le fichier d'exemple et adaptez les variables si nécessaire :
    ```bash
    cp .env.example .env
    ```

3.  **Lancer l'application** :
    ```bash
    ./run.sh
    ```
    Cette commande construit les images et lance les conteneurs (API, Web, DB, MQ).

### Accès aux Services
*   **Frontend** : [http://localhost:80](http://localhost:80)
*   **Backend API** : [http://localhost:8080](http://localhost:8080)
*   **Swagger UI** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (ou endpoint configuré)
*   **Management RabbitMQ** : [http://localhost:15672](http://localhost:15672)

## 📂 Structure du Projet

```text
.
├── backend/            # Code source Java/SpringBoot (Hexagonal)
├── web/                # Code source Vue.js/Vite
├── docs/               # Architecture (ADRs, Diag. C4)
├── openapi.yaml        # Spécification de l'API (Source de vérité)
├── docker-compose.yml  # Orchestration des services
├── build.sh            # Script de compilation globale
├── run.sh              # Script de lancement (Docker)
└── test.sh             # Script d'exécution des tests
```

## 🏗️ Architecture & Design Decisions

Le projet suit les principes de la **Software Craftsmanship** :
- **Architecture Hexagonale** : Isolation du domaine métier des détails d'infrastructure.
- **Contract-First** : Toute modification de l'API est d'abord définie dans `openapi.yaml`.
- **Tests** : Mise en place de tests unitaires et d'intégration significatifs.

Les diagrammes C4 et les ADR (Architecture Decision Records) sont disponibles dans le dossier `/docs`.

## 🧪 Tests

Pour lancer l'ensemble des tests (Frontend & Backend) :
```bash
./test.sh
```

---
*Projet réalisé dans le cadre du cours "Software Architecture : Heuristiques et Compromis", dispensé par Rui CARVALHO - ESGI 2026.* 🚀
