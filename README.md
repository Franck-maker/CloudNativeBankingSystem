# 🚀 Cloud-Native Banking Architecture

A modern, 15-Factor compliant microservices application built with Clean Architecture. This project is designed to fulfill a comprehensive Cloud-Native DevSecOps grading rubric.

## 🏗️ Architecture & Tech Stack

* **Frontend:** Angular 18+ (Standalone Components, Reactive Forms) with Tailwind CSS (Glassmorphism UI).
* **Backend:** Java 21, Spring Boot 3, Clean Architecture (Domain, Application, Infrastructure layers).
* **Database:** H2 In-Memory Database (Phase 1 MVP) -> Target: PostgreSQL.
* **DevSecOps:** Docker (Multi-stage builds), Kubernetes (Minikube), Ingress API Gateway.

---

## 🚦 Prerequisites
To run this project, your local machine must have:
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* [Minikube](https://minikube.sigs.k8s.io/docs/start/) & `kubectl`

And when running it locally:
* [Java 21](https://adoptium.net/)
* [Node.js (v18+)](https://nodejs.org/) & Angular CLI (`npm i -g @angular/cli`)

---

## Kubernetes Deployment (Minikube)

### 1) Start the Minikube Cluster & Enable Gateway

```bash
minikube start --driver=docker
minikube addons enable ingress
```

### 2) Configure Local DNS (Windows)

```bash
To use our custom domain, map it to your local loopback address:

1. Open Notepad as Administrator.

2. Edit C:\Windows\System32\drivers\etc\hosts.

3. Add this line at the bottom: 127.0.0.1 banking.local
```

### 3) Deploy the Infrastructure

Apply the manifests from the root directory:

```bash
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml
```

### 4) Open the Network Tunnel
For Windows users, Minikube requires an active tunnel to bind the Ingress
 to your physical network. Leave this running in a separate terminal:

```bash
minikube tunnel
```
### 5) Access the Application

```bash
WebPortal: Navigate to http://banking.local

APIEndpoints: Route via http://banking.local/api/v1/...

Default Admin Credentials: 
                Username: admin@bank.local
                Password: admin123
```
---

## 💻 Running the Project Locally (Dev Mode)

If you need to code on the Spring Boot backend locally but want to use the Kubernetes database:
### 1. Port-Forward the Database

Open a tunnel to the K8s PostgreSQL instance. We use local port 5433 to avoid conflicts with native Windows services:

```bash
kubectl port-forward svc/postgres-service 5433:5432
```
### 2. Start the Backend (Spring Boot)
The backend REST API runs on port `8080`.

```bash
cd backend
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
To see the documentation of the endpoints and test them
url : http://localhost:8080/swagger-ui/index.html

### 3. Run frontend

```bash
cd ../frontend
npm install
ng serve -o
```

---

## Troubleshooting 

### Minikube cannot reach `registry.k8s.io`

Symptoms: image pull/connectivity/addon failures.  
Fix path:

1. `minikube delete --all --purge`
2. restart with `minikube start --driver=docker`
3. configure proxy if on corporate network/VPN

### Blank Page / Cannot reach **banking.local**

Symptoms: The browser spins indefinitely.
Fix path:

1. Ensure your hosts file has no .txt extension.
2. Run ipconfig /flushdns.
3. Ensure minikube tunnel is actively running in an Administrator terminal.
