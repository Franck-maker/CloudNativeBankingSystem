#!/bin/bash
# ==========================================
# Banking System - Secure Kubernetes Deployment Script
# ==========================================
# Usage: ./deploy.sh [--generate-tls] [--delete]
# ==========================================

set -euo pipefail

NAMESPACE="banking"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ======================
# Generate self-signed TLS certificate
# ======================
generate_tls() {
    log_info "Generating self-signed TLS certificate for banking.local..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout "${SCRIPT_DIR}/tls.key" \
        -out "${SCRIPT_DIR}/tls.crt" \
        -subj "/CN=banking.local/O=BankingSystem" \
        2>/dev/null

    # Create the TLS secret directly (instead of using the placeholder)
    kubectl create secret tls banking-tls-secret \
        --cert="${SCRIPT_DIR}/tls.crt" \
        --key="${SCRIPT_DIR}/tls.key" \
        -n "${NAMESPACE}" \
        --dry-run=client -o yaml | kubectl apply -f -

    log_info "TLS certificate generated and secret created."
    # Clean up local cert files
    rm -f "${SCRIPT_DIR}/tls.key" "${SCRIPT_DIR}/tls.crt"
}

# ======================
# Delete all resources
# ======================
delete_all() {
    log_warn "Deleting all banking resources..."
    kubectl delete namespace "${NAMESPACE}" --ignore-not-found=true
    log_info "All resources deleted."
    exit 0
}

# ======================
# Parse arguments
# ======================
GENERATE_TLS=false
for arg in "$@"; do
    case $arg in
        --generate-tls) GENERATE_TLS=true ;;
        --delete) delete_all ;;
        *) log_error "Unknown argument: $arg"; exit 1 ;;
    esac
done

# ======================
# Pre-flight checks
# ======================
log_info "Running pre-flight checks..."

if ! command -v kubectl &> /dev/null; then
    log_error "kubectl not found. Please install kubectl."
    exit 1
fi

if ! kubectl cluster-info &> /dev/null; then
    log_error "Cannot connect to Kubernetes cluster. Is your cluster running?"
    exit 1
fi

log_info "Cluster connection: OK"

# ======================
# Step 1: Create namespace with security labels
# ======================
log_info "Step 1/7 - Creating namespace and resource quotas..."
kubectl apply -f "${SCRIPT_DIR}/namespace.yaml"

# ======================
# Step 2: Create secrets
# ======================
log_info "Step 2/7 - Creating secrets..."
kubectl apply -f "${SCRIPT_DIR}/secrets.yaml"

# Generate TLS cert if requested
if [ "$GENERATE_TLS" = true ]; then
    generate_tls
else
    log_warn "Skipping TLS cert generation. Use --generate-tls to generate a self-signed cert."
    log_warn "Or create your own: kubectl create secret tls banking-tls-secret --cert=tls.crt --key=tls.key -n banking"
fi

# ======================
# Step 3: RBAC and Service Accounts
# ======================
log_info "Step 3/7 - Applying RBAC and service accounts..."
kubectl apply -f "${SCRIPT_DIR}/rbac.yaml"

# ======================
# Step 4: Network Policies (zero-trust)
# ======================
log_info "Step 4/7 - Applying network policies..."
kubectl apply -f "${SCRIPT_DIR}/network-policies.yaml"

# ======================
# Step 5: Deploy database
# ======================
log_info "Step 5/7 - Deploying PostgreSQL..."
kubectl apply -f "${SCRIPT_DIR}/postgres.yaml"
log_info "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=available --timeout=120s deployment/postgres-db -n "${NAMESPACE}" || \
    log_warn "PostgreSQL not ready yet, continuing..."

# ======================
# Step 6: Deploy application services
# ======================
log_info "Step 6/7 - Deploying application services..."
kubectl apply -f "${SCRIPT_DIR}/notification-service.yaml"
kubectl apply -f "${SCRIPT_DIR}/backend.yaml"
kubectl apply -f "${SCRIPT_DIR}/frontend.yaml"
kubectl apply -f "${SCRIPT_DIR}/pod-disruption-budgets.yaml"

# ======================
# Step 7: Configure Ingress (API Gateway)
# ======================
log_info "Step 7/7 - Configuring Ingress gateway with security headers..."
kubectl apply -f "${SCRIPT_DIR}/security-headers.yaml"
kubectl apply -f "${SCRIPT_DIR}/ingress.yaml"

# ======================
# Wait for all deployments
# ======================
log_info "Waiting for all deployments to be ready..."
kubectl wait --for=condition=available --timeout=180s \
    deployment/banking-backend \
    deployment/banking-frontend \
    deployment/notification-service \
    -n "${NAMESPACE}" || \
    log_warn "Some deployments may not be ready yet."

# ======================
# Summary
# ======================
echo ""
echo "============================================"
log_info "Banking System deployed to namespace: ${NAMESPACE}"
echo "============================================"
echo ""
log_info "Security features applied:"
echo "  ✓ Dedicated namespace with Pod Security Standards (restricted)"
echo "  ✓ Kubernetes Secrets for sensitive credentials"
echo "  ✓ Network Policies (zero-trust: deny-all + allow-list)"
echo "  ✓ RBAC with dedicated service accounts (least privilege)"
echo "  ✓ SecurityContext: non-root, read-only FS, drop ALL capabilities"
echo "  ✓ Seccomp profiles (RuntimeDefault)"
echo "  ✓ TLS termination at Ingress"
echo "  ✓ Security headers (HSTS, CSP, X-Frame-Options, etc.)"
echo "  ✓ Rate limiting on Ingress"
echo "  ✓ Resource quotas and limits"
echo "  ✓ Liveness/Readiness probes"
echo "  ✓ PodDisruptionBudgets"
echo "  ✓ Rolling update strategy (zero downtime)"
echo ""
log_info "Check status: kubectl get all -n ${NAMESPACE}"
log_info "View pods:    kubectl get pods -n ${NAMESPACE}"
log_info "View logs:    kubectl logs -f deployment/banking-backend -n ${NAMESPACE}"
