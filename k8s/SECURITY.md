# Kubernetes Cluster Security Documentation

## Architecture Overview

All banking resources are deployed in a dedicated `banking` namespace with comprehensive security controls applied at every layer.

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                                │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │               Namespace: banking                              │  │
│  │          Pod Security Standards: RESTRICTED                    │  │
│  │          ResourceQuota + LimitRange enforced                   │  │
│  │  ┌──────────────────────────────────────────────────────────┐  │  │
│  │  │  Ingress (TLS termination + security headers)           │  │  │
│  │  │  Rate limiting: 20 req/s, 300 req/min                   │  │  │
│  │  └──────────┬─────────────────────┬────────────────────────┘  │  │
│  │             │                     │                            │  │
│  │     ┌───────▼───────┐     ┌───────▼───────┐                   │  │
│  │     │   Frontend    │     │   Backend     │                   │  │
│  │     │  (2 replicas) │     │  (2 replicas) │                   │  │
│  │     │  non-root     │     │  non-root     │                   │  │
│  │     │  read-only FS │     │  read-only FS │                   │  │
│  │     └───────────────┘     └────┬─────┬────┘                   │  │
│  │                                │     │                        │  │
│  │  ┌──── Network Policies ───────┼─────┼────────────────────┐   │  │
│  │  │  deny-all default           │     │                    │   │  │
│  │  │  + explicit allow-list      │     │                    │   │  │
│  │  └─────────────────────────────┼─────┼────────────────────┘   │  │
│  │                                │     │                        │  │
│  │                     ┌──────────▼──┐  │  ┌──────────────────┐  │  │
│  │                     │ PostgreSQL  │  └──▶ Notification Svc │  │  │
│  │                     │ (1 replica) │     │  (gRPC, 1 rep.)  │  │  │
│  │                     │ PVC backed  │     │  non-root        │  │  │
│  │                     └─────────────┘     └──────────────────┘  │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

## Security Controls Applied

### 1. Namespace Isolation & Pod Security Standards
- **File:** `namespace.yaml`
- All resources deployed in dedicated `banking` namespace
- **Pod Security Standards** enforced at `restricted` level:
  - `pod-security.kubernetes.io/enforce: restricted`
  - `pod-security.kubernetes.io/audit: restricted`
  - `pod-security.kubernetes.io/warn: restricted`
- **ResourceQuota** limits total CPU/memory/pods in the namespace
- **LimitRange** sets default resource limits for all containers

### 2. Secrets Management
- **File:** `secrets.yaml`
- All sensitive data (DB passwords, credentials) stored in **Kubernetes Secrets**
- No hardcoded passwords in deployment manifests
- Secrets referenced via `secretKeyRef` in deployments
- **Production recommendation:** Use SealedSecrets, external-secrets-operator, or HashiCorp Vault

### 3. Network Policies (Zero-Trust Model)
- **File:** `network-policies.yaml`
- **Default deny-all** for both ingress and egress traffic
- Explicit allow-list rules:
  | Source | Destination | Port | Protocol |
  |--------|-------------|------|----------|
  | Ingress Controller | Frontend | 8080 | TCP |
  | Ingress Controller | Backend | 8080 | TCP |
  | Backend | PostgreSQL | 5432 | TCP |
  | Backend | Notification Svc | 50051 | TCP (gRPC) |
  | All pods | kube-dns | 53 | UDP/TCP |
- PostgreSQL & Notification services are **not accessible** from outside the backend

### 4. RBAC & Service Accounts
- **File:** `rbac.yaml`
- **Dedicated ServiceAccount** per microservice (least privilege)
- `automountServiceAccountToken: false` on all service accounts
- Custom **Roles** and **RoleBindings**:
  - Backend SA: read configmaps, read own secret
  - Postgres SA: read own secret
  - Frontend/Notification SA: no API permissions

### 5. Pod Security (Defense in Depth)
Applied to **all** deployments:
- `runAsNonRoot: true` — containers never run as root
- `readOnlyRootFilesystem: true` — immutable container filesystem
- `allowPrivilegeEscalation: false` — no privilege escalation
- `capabilities.drop: [ALL]` — all Linux capabilities dropped
- `seccompProfile: RuntimeDefault` — system call filtering
- `emptyDir` volumes for writable temp directories (size-limited)

### 6. TLS & Ingress Security
- **Files:** `ingress.yaml`, `tls-secret.yaml`
- **TLS termination** at Ingress with HTTPS redirect
- **Security headers** via nginx annotations:
  - `Strict-Transport-Security` (HSTS with preload)
  - `Content-Security-Policy`
  - `X-Frame-Options: DENY`
  - `X-Content-Type-Options: nosniff`
  - `X-XSS-Protection`
  - `Referrer-Policy`
  - `Permissions-Policy`
- **Rate limiting**: 20 req/s, 10 concurrent connections, 300 req/min
- **Request size limit**: 10MB max body
- **Timeouts**: connection 10s, read/send 30s

### 7. Availability & Resilience
- **Liveness probes**: detect and restart unresponsive pods
- **Readiness probes**: prevent traffic to pods not yet ready
- **PodDisruptionBudgets**: guarantee minimum availability during updates
- **RollingUpdate strategy** with `maxUnavailable: 0` — zero downtime deployments
- **Multiple replicas**: frontend (2), backend (2) for high availability
- Services use **ClusterIP** (not NodePort) — traffic only through Ingress

## Deployment Order

```
1. namespace.yaml     → Namespace, ResourceQuota, LimitRange
2. secrets.yaml       → Kubernetes Secrets
3. tls-secret.yaml    → TLS certificate (or generate with deploy script)
4. rbac.yaml          → ServiceAccounts, Roles, RoleBindings
5. network-policies.yaml → Network Policies
6. postgres.yaml      → PostgreSQL (wait for ready)
7. notification-service.yaml
8. backend.yaml
9. frontend.yaml
10. pod-disruption-budgets.yaml
11. ingress.yaml      → Ingress with TLS + security headers
```

Or use the deployment script:
```bash
# Linux/macOS
chmod +x deploy.sh
./deploy.sh --generate-tls

# Windows PowerShell
.\deploy.ps1 -GenerateTLS
```

## Verification Commands

```bash
# Check all resources in banking namespace
kubectl get all -n banking

# Verify network policies
kubectl get networkpolicies -n banking

# Verify RBAC
kubectl get serviceaccounts -n banking
kubectl get roles,rolebindings -n banking

# Verify secrets (metadata only)
kubectl get secrets -n banking

# Verify Pod Security Standards
kubectl get namespace banking --show-labels

# Check pod security contexts
kubectl get pods -n banking -o jsonpath='{range .items[*]}{.metadata.name}: runAsNonRoot={.spec.securityContext.runAsNonRoot}{"\n"}{end}'

# Test network policy (should be blocked)
kubectl run test-pod --rm -it --image=busybox -n banking -- wget -qO- http://postgres-service:5432

# View Ingress with TLS
kubectl describe ingress banking-gateway -n banking
```

## Production Recommendations
1. **Use cert-manager** with Let's Encrypt for automatic TLS certificate management
2. **Use SealedSecrets** or **external-secrets-operator** for secrets management
3. **Enable audit logging** at the cluster level
4. **Implement Falco** or similar runtime security monitoring
5. **Scan images** with Trivy/Snyk before deployment
6. **Use OPA/Gatekeeper** for additional policy enforcement
7. **Enable Pod Security Admission** at cluster level
8. **Configure backup** for PostgreSQL PVC
