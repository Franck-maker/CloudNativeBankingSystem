# Cloud-Native Banking System

A 15-Factor compliant microservices banking application built with **Hexagonal Architecture**, featuring a Spring Boot backend, Angular frontend, and a Node.js gRPC notification service, all orchestrated on Kubernetes with an Ingress API Gateway.

---

## Architecture Overview

```
                          ┌─────────────────────────────────────────────────────────────┐
                          │                    Kubernetes Cluster (Minikube)             │
                          │                                                             │
   ┌────────┐             │  ┌──────────────────────────────────────────────┐            │
   │Browser │─────────────┼─▶│         Ingress Gateway (banking.local)     │            │
   └────────┘             │  │  nginx.ingress.kubernetes.io                │            │
        │                 │  └──────────┬───────────────────┬──────────────┘            │
        │                 │             │                   │                           │
        │                 │    /api/*   │                   │  /*                       │
        │                 │             ▼                   ▼                           │
        │                 │  ┌──────────────────┐  ┌──────────────────┐                 │
        │                 │  │  Backend Service  │  │ Frontend Service │                 │
        │                 │  │  (Spring Boot)    │  │  (Angular/nginx) │                 │
        │                 │  │  Port: 8080       │  │  Port: 8080      │                 │
        │                 │  │  NodePort: 30080  │  │  NodePort: 30081 │                 │
        │                 │  └────────┬─────────┘  └──────────────────┘                 │
        │                 │           │         │                                       │
        │                 │    JPA    │         │  gRPC (:50051)                        │
        │                 │           ▼         ▼                                       │
        │                 │  ┌──────────────┐  ┌─────────────────────┐                  │
        │                 │  │  PostgreSQL   │  │ Notification Service│                  │
        │                 │  │  Port: 5432   │  │  (Node.js gRPC)    │                  │
        │                 │  │  PVC: 1Gi     │  │  Port: 50051       │                  │
        │                 │  └──────────────┘  └─────────────────────┘                  │
        │                 │                                                             │
        │                 └─────────────────────────────────────────────────────────────┘
        │
        │  Local Dev Mode (no K8s)
        │
        │    localhost:4200 ──proxy──▶ localhost:8082 ──gRPC──▶ localhost:50051
        │         Angular                Spring Boot           Node.js gRPC
        │                                    │
        │                                    ▼
        │                               localhost:5432
        │                                PostgreSQL
```

### Backend — Hexagonal (Ports & Adapters) Architecture

```
com.bank.banking_system/
├── domain/
│   └── model/               # Entities: Account, Transaction, TransactionType
├── application/
│   ├── ports/
│   │   ├── in/              # Input port:  AccountUseCase (interface)
│   │   └── out/             # Output port: AccountRepositoryPort (interface)
│   └── service/             # AccountService (implements AccountUseCase)
└── infrastructure/
    ├── web/                 # REST Controller, DTOs (AccountResponse, TransferRequest…)
    ├── persistence/         # JPA adapter (AccountJpaEntity, AccountRepositoryAdapter)
    ├── notification/        # gRPC client (NotificationGrpcClient)
    └── security/            # Spring Security, RBAC, DatabaseSeeder
```

### Communication Flow

1. **Browser** → Ingress Gateway (`banking.local`) → routes `/api/*` to Backend, `/*` to Frontend
2. **Frontend (nginx)** → proxies `/api/` requests to `banking-backend-service:8080` inside the cluster
3. **Backend** → reads/writes to PostgreSQL via JPA over `postgres-service:5432`
4. **Backend** → sends gRPC transfer alerts to `notification-service:50051` after each successful transfer

---

## Tech Stack

| Layer                | Technology                                | Version   |
|----------------------|-------------------------------------------|-----------|
| **Frontend**         | Angular (Standalone Components, Zoneless) | 21.2      |
| **Frontend Styling** | Tailwind CSS                              | 3.4       |
| **Backend**          | Spring Boot (Java)                        | 3.5       |
| **Language**         | Java                                      | 21        |
| **Database**         | PostgreSQL                                | 15        |
| **gRPC Framework**   | grpc-java (backend) / @grpc/grpc-js (notification) | 1.58 / 1.14 |
| **Protobuf**         | protoc                                    | 3.24      |
| **Containers**       | Docker (multi-stage builds)               | —         |
| **Orchestration**    | Kubernetes (Minikube)                     | —         |
| **API Gateway**      | Kubernetes Ingress (nginx controller)     | —         |
| **API Docs**         | springdoc-openapi (Swagger UI)            | 2.8       |
| **Security**         | Spring Security (Basic Auth + RBAC)       | —         |

---

## Prerequisites

### For Kubernetes Deployment

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)
- `kubectl` CLI

### For Local Development (additional)

- [Java 21 (Temurin)](https://adoptium.net/)
- [Node.js 18+](https://nodejs.org/)
- [PostgreSQL](https://www.postgresql.org/download/) (or use the K8s instance via port-forward)

---

## Kubernetes Deployment (Minikube)

### 1. Start the Cluster & Enable Ingress

```bash
minikube start --driver=docker
minikube addons enable ingress
```

### 2. Configure Local DNS (Windows)

1. Open **Notepad as Administrator**
2. Edit `C:\Windows\System32\drivers\etc\hosts`
3. Add this line at the bottom:
   ```
   127.0.0.1 banking.local
   ```

### 3. Build the Docker Images

Point your shell to Minikube's Docker daemon so that images are built directly inside the cluster:

```powershell
# PowerShell
minikube docker-env --shell powershell | Invoke-Expression
```

```bash
# Bash / Git Bash
eval $(minikube docker-env)
```

Then build all three images:

```bash
# Backend
cd backend
docker build -t franckfozie2023/banking-backend:v5 .

# Frontend
cd ../frontend
docker build -t franckfozie2023/banking-frontend:v4 .

# Notification Service
cd ../notification-service
docker build -t franckfozie2023/notification-service:v1 .
```

> **Tip:** If you also want images on Docker Hub for reuse, run `docker push <image>` after each build.

### 4. Deploy to the Cluster

Apply the manifests in order (database first, then services, then gateway):

```bash
cd ..
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/notification-service.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml
```

Wait for all pods to be ready:

```bash
kubectl get pods -w
```

Expected output (all `1/1 Running`):

```
NAME                                    READY   STATUS    AGE
postgres-db-xxx                         1/1     Running   ...
notification-service-xxx                1/1     Running   ...
banking-backend-xxx                     1/1     Running   ...
banking-frontend-xxx                    1/1     Running   ...
```

### 5. Open the Network Tunnel

In a **separate terminal** (keep it running), start the Minikube tunnel so that the Ingress binds to `127.0.0.1`:

```bash
minikube tunnel
```

> On Windows, this terminal may need **Administrator** privileges.

### 6. Access the Application

| What                 | URL                                          |
|----------------------|----------------------------------------------|
| **Web Portal**       | http://banking.local                         |
| **REST API**         | http://banking.local/api/v1/accounts         |
| **Swagger UI**       | http://banking.local/api/swagger-ui/index.html |

**Default Credentials (seeded on first startup):**

| Role    | Username             | Password      |
|---------|----------------------|---------------|
| ADMIN   | `admin@bank.local`   | `admin123`    |
| USER    | `user@bank.local`    | `password123` |

---

## Local Development (Dev Mode)

Run all three services on your machine for a fast development loop.

### 1. Start PostgreSQL

**Option A — Use your local PostgreSQL:**

Create the database and user if they don't exist:

```sql
CREATE USER dbadmin WITH PASSWORD 'securepass123';
CREATE DATABASE banking_db OWNER dbadmin;
```

**Option B — Port-forward the K8s instance:**

```bash
kubectl port-forward svc/postgres-service 5432:5432
```

### 2. Start the Notification Service (gRPC)

```bash
cd notification-service
npm install
node server.js
```

You should see:

```
gRPC Notification Service running on 0.0.0.0:50051
```

### 3. Start the Backend

The backend listens on port **8082** in dev mode:

```bash
cd backend
./mvnw spring-boot:run          # Linux / macOS / Git Bash
.\mvnw.cmd spring-boot:run      # Windows PowerShell
```

Verify it's running:

```bash
curl http://localhost:8082/api/v1/accounts -u admin@bank.local:admin123
```

Swagger UI is available at: http://localhost:8082/swagger-ui/index.html

### 4. Start the Frontend

The Angular dev server runs on port **4200** and proxies `/api/v1` requests to `localhost:8082`:

```bash
cd frontend
npm install
npx ng serve
```

Open http://localhost:4200 in your browser.

---

## Viewing Logs

### Kubernetes

```bash
# All pods
kubectl get pods

# Backend logs
kubectl logs -l app=banking-backend -f

# Frontend (nginx) logs
kubectl logs -l app=banking-frontend -f

# Notification service logs (gRPC)
kubectl logs -l app=notification-service -f

# PostgreSQL logs
kubectl logs -l app=postgres -f
```

### Seeing gRPC Notifications in Action

After performing a **transfer** in the web portal, check the notification service logs to see the gRPC response:

```bash
kubectl logs -l app=notification-service -f
```

You will see output like:

```
gRPC Notification Service running on 0.0.0.0:50051
Transfer Alert Received:
  Account: a1b2c3d4-...
  Amount: 100.00
  Message: Transfer of 100.00 completed successfully
```

In **local development**, the same output appears directly in the terminal where `node server.js` is running.

### Backend-side gRPC Confirmation

Check the backend logs for the notification round-trip:

```bash
kubectl logs -l app=banking-backend -f
```

---

## Redeploying After Code Changes

After modifying code and rebuilding a Docker image, Kubernetes needs to pick up the new version.

### 1. Rebuild the Image (inside Minikube's Docker)

```powershell
# Make sure you're using Minikube's Docker
minikube docker-env --shell powershell | Invoke-Expression

# Rebuild whichever service changed, e.g. backend
cd backend
docker build -t franckfozie2023/banking-backend:v5 .
```

### 2. Restart the Deployment

```bash
kubectl rollout restart deployment banking-backend
kubectl rollout status deployment banking-backend
```

Repeat for `banking-frontend` or `notification-service` as needed.

---

## Useful kubectl Commands

```bash
# Cluster status
minikube status

# List all resources
kubectl get all

# Describe a pod (events, env vars, errors)
kubectl describe pod <pod-name>

# Exec into a running container
kubectl exec -it <pod-name> -- /bin/sh

# Port-forward a service for local access
kubectl port-forward svc/banking-backend-service 8082:8080

# Delete and re-apply a specific manifest
kubectl delete -f k8s/backend.yaml
kubectl apply -f k8s/backend.yaml

# View Ingress routing rules
kubectl describe ingress banking-gateway
```

---

## Project Structure

```
banking-system/
├── backend/                        # Spring Boot 3.5 — Java 21
│   ├── Dockerfile                  # Multi-stage: temurin:21-jdk → jre-alpine
│   ├── pom.xml                     # Maven config (gRPC, JPA, Security, OpenAPI)
│   └── src/main/
│       ├── java/com/bank/...       # Hexagonal architecture (see above)
│       ├── proto/notification.proto # gRPC service definition
│       └── resources/application.properties
│
├── frontend/                       # Angular 21 — Tailwind CSS
│   ├── Dockerfile                  # Multi-stage: node:24-alpine → nginx-unprivileged
│   ├── nginx.conf                  # Production config (SPA routing + API proxy)
│   ├── proxy.conf.json             # Dev proxy → localhost:8082
│   ├── angular.json
│   └── src/app/
│       ├── app.component.ts        # Main component (account list + transfer form)
│       ├── account.service.ts      # HTTP service (GET accounts, POST transfer)
│       └── core/interceptors/
│           └── auth.interceptor.ts  # Attaches Basic Auth header
│
├── notification-service/           # Node.js gRPC server
│   ├── Dockerfile                  # node:24-alpine, non-root user
│   ├── server.js                   # gRPC server on :50051
│   ├── notification.proto          # Shared proto definition
│   └── package.json
│
├── proto/
│   └── notification.proto          # Source-of-truth proto definition
│
├── k8s/                            # Kubernetes manifests
│   ├── postgres.yaml               # PVC + Deployment + ClusterIP Service
│   ├── backend.yaml                # Deployment + NodePort Service (:30080)
│   ├── frontend.yaml               # Deployment + NodePort Service (:30081)
│   ├── notification-service.yaml   # Deployment + ClusterIP Service (:50051)
│   └── ingress.yaml                # Ingress Gateway (banking.local)
│
└── README.md
```

---

## Environment Variables

### Backend

| Variable                  | Default (Dev)   | K8s Value              | Description                       |
|---------------------------|-----------------|------------------------|-----------------------------------|
| `SERVER_PORT`             | `8082`          | `8080`                 | HTTP server port                  |
| `DB_HOST`                 | `localhost`     | `postgres-service`     | PostgreSQL host                   |
| `DB_PORT`                 | `5432`          | `5432`                 | PostgreSQL port                   |
| `DB_NAME`                 | `banking_db`    | `banking_db`           | Database name                     |
| `DB_USER`                 | `dbadmin`       | `dbadmin`              | Database username                 |
| `DB_PASS`                 | `securepass123` | `securepass123`        | Database password                 |
| `GRPC_NOTIFICATION_HOST`  | `localhost`     | `notification-service` | gRPC notification service host    |
| `GRPC_NOTIFICATION_PORT`  | `50051`         | `50051`                | gRPC notification service port    |

---

## Troubleshooting

### Pods stuck in `ImagePullBackOff`

The image isn't found on Docker Hub or locally.

1. Ensure you built images inside Minikube's Docker daemon:
   ```powershell
   minikube docker-env --shell powershell | Invoke-Expression
   docker images | Select-String "banking"
   ```
2. Verify the image name/tag in the K8s manifest matches exactly what you built.
3. If images are only local (not pushed to Docker Hub), set `imagePullPolicy: Never` in the manifest.

### Pods in `CrashLoopBackOff`

```bash
kubectl logs <pod-name>
kubectl describe pod <pod-name>
```

Common causes: database not ready yet (backend starts before postgres), wrong env vars, port conflicts.

### Minikube cannot reach `registry.k8s.io`

1. `minikube delete --all --purge`
2. `minikube start --driver=docker`
3. Configure proxy if on corporate network/VPN.

### Blank Page / Cannot reach `banking.local`

1. Verify your hosts file has `127.0.0.1 banking.local` (no `.txt` extension on the file).
2. Run `ipconfig /flushdns`.
3. Ensure `minikube tunnel` is running in a separate **Administrator** terminal.
4. Check the Ingress is created: `kubectl describe ingress banking-gateway`.

### Backend build fails in Docker (`protoc-gen-grpc-java not executable`)

The builder stage must use a **glibc-based** image (not Alpine). The Dockerfile uses `eclipse-temurin:21-jdk` (Debian) for the build stage and `eclipse-temurin:21-jre-alpine` for the runtime stage.
