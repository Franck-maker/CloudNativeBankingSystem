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
* [Java 21](https://adoptium.net/)
* [Node.js (v18+)](https://nodejs.org/) & Angular CLI (`npm i -g @angular/cli`)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* [Minikube](https://minikube.sigs.k8s.io/docs/start/) & `kubectl`

---

## 💻 Running the Project Locally (Dev Mode)

### 1. Start the Backend (Spring Boot)
The backend REST API runs on port `8080`.
```bash
cd backend
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

#Swagger API Docs: 
## To see the documentation of the endpoints and test them
url : http://localhost:8080/swagger-ui/index.html

### 2) Run frontend

```bash
cd ../frontend
npm install
ng serve -o
```

---

## Kubernetes Deployment (Minikube)

### 1) Start Minikube

```powershell
minikube start --driver=docker
```

### 2) Enable the Ingress Gateway

```powershell
minikube addon enable ingress
```

### 3) Apply manifests

```powershell
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
```

### 3) Verify resources

```powershell
kubectl get pods -A
kubectl get svc -A
kubectl get ingress -A
```

### 4) Access the backend services

If using ingress addon:

```powershell
minikube service banking-backend-service --url
```
(Append "/swagger-ui/index.html" to the provided URL to view the API).

---

## Resource Limits Recommendation (Kubernetes)

To avoid noisy-neighbor warnings, define container resources in deployments:

```yaml
resources:
  requests:
    cpu: "100m"
    memory: "256Mi"
  limits:
    cpu: "500m"
    memory: "512Mi"
```

---


### 2) Minikube cannot reach `registry.k8s.io`

Symptoms: image pull/connectivity/addon failures.  
Fix path:

1. `minikube delete --all --purge`
2. restart with `minikube start --driver=docker`
3. configure proxy if on corporate network/VPN



---

## License

Add your project license here (e.g., MIT, Apache-2.0, proprietary).
