# MongoDB Quarkus Kitchen Sink

![CI](https://github.com/your-username/mongodb-quarkus-kitchensink/workflows/CI/badge.svg)

A full-stack web application demonstrating modern development practices with Quarkus backend, React frontend, and containerized development environment.

## Project Structure

```
mongodb-quarkus-kitchensink/
├── backend/                 # Quarkus Java backend
│   ├── src/                # Java source code
│   ├── pom.xml            # Maven dependencies
│   └── README.md          # Backend-specific documentation
├── frontend/               # React TypeScript frontend
│   ├── src/               # React components and services
│   ├── package.json       # Node.js dependencies
│   └── public/            # Static assets
├── .devcontainer/         # Development container configuration
│   ├── docker-compose.yml # Services (Postgres, Keycloak, MongoDB)
│   ├── secrets/           # Secret files (create manually)
│   └── devcontainer.json  # VS Code dev container config
└── README.md              # This file
```

## Technology Stack

- **Backend**: Quarkus (Java), MongoDB, Keycloak Authentication
- **Frontend**: React, TypeScript, Material-UI, Vite
- **Infrastructure**: Docker, PostgreSQL, Keycloak
- **Development**: VS Code Dev Containers

## Prerequisites

- Docker Desktop
- VS Code with Dev Containers extension
- Git

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd mongodb-quarkus-kitchensink
```

### 2. Create Secrets Directory

Create the secrets directory and required secret files:

```bash
mkdir -p .devcontainer/secrets
```

Create the following secret files in `.devcontainer/secrets/`:

```bash
# Database credentials
echo "postgres" > .devcontainer/secrets/postgres-username.txt
echo "your-postgres-password" > .devcontainer/secrets/postgres-password.txt

# MongoDB credentials
echo "mongodb" > .devcontainer/secrets/mongo-db-username.txt
echo "your-mongodb-password" > .devcontainer/secrets/mongo-db-password.txt

# Keycloak admin credentials
echo "admin" > .devcontainer/secrets/keycloak-admin-username.txt
echo "your-keycloak-admin-password" > .devcontainer/secrets/keycloak-admin-password.txt

# OIDC client secret
echo "your-oidc-client-secret" > .devcontainer/secrets/oidc-client-secret.txt
```

### 3. Open in Dev Container

1. Open the project in VS Code
2. When prompted, click "Reopen in Container" or use Command Palette: `Dev Containers: Reopen in Container`
3. Wait for the container to build and services to start

### 4. Start the Applications

#### Backend (Quarkus)
```bash
cd backend
./mvnw quarkus:dev
```
Backend will be available at: http://localhost:8080

#### Frontend (React)
```bash
cd frontend
npm install
npm run dev
```
Frontend will be available at: http://localhost:5173

## Testing

Run tests with coverage:

```bash
cd backend
./mvnw clean test jacoco:report
```

View coverage report at: `backend/target/site/jacoco/index.html`

## Services

The dev container includes the following services:

- **PostgreSQL**: Database for Keycloak (port 5432)
- **Keycloak**: Authentication server (port 8081)
- **MongoDB**: Application database (configured in backend)

## Development Workflow

1. **Backend Development**: Make changes in `backend/src/`, Quarkus hot reload is enabled
2. **Frontend Development**: Make changes in `frontend/src/`, Vite provides hot module replacement
3. **Database**: Access PostgreSQL at `localhost:5432`, MongoDB connection configured in backend
4. **Authentication**: Keycloak admin console at http://localhost:8081

## Key Features

- User registration and authentication via Keycloak
- Member management (CRUD operations)
- Role-based access control (Admin/User roles)
- Responsive Material-UI frontend
- RESTful API with Quarkus
- MongoDB integration
- Containerized development environment

## Troubleshooting

### Container Issues
- Ensure Docker Desktop is running
- Check that all secret files exist in `.devcontainer/secrets/`
- Rebuild container: Command Palette → `Dev Containers: Rebuild Container`

### Service Connection Issues
- Verify services are healthy: `docker-compose ps`
- Check logs: `docker-compose logs <service-name>`
- Restart services: `docker-compose restart`

### Port Conflicts
- Ensure ports 5173, 8080, 8081, 5432 are available
- Modify port mappings in `docker-compose.yml` if needed

## Contributing

1. Create a feature branch
2. Make your changes
3. Test in the dev container environment
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.