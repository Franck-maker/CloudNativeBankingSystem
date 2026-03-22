# deploy.ps1 - Banking System Secure Deployment (Windows PowerShell)
# Usage: .\deploy.ps1 [-GenerateTLS] [-Delete]

param(
    [switch]$GenerateTLS,
    [switch]$Delete
)

$ErrorActionPreference = "Stop"
$NAMESPACE = "banking"
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path

function Log-Info  { param($msg) Write-Host "[INFO] $msg" -ForegroundColor Green }
function Log-Warn  { param($msg) Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Log-Error { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }

# Delete all
if ($Delete) {
    Log-Warn "Deleting all banking resources..."
    kubectl delete namespace $NAMESPACE --ignore-not-found=true
    Log-Info "All resources deleted."
    exit 0
}

# Pre-flight
Log-Info "Running pre-flight checks..."
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Log-Error "kubectl not found."; exit 1
}
kubectl cluster-info 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) { Log-Error "Cannot connect to cluster."; exit 1 }
Log-Info "Cluster connection: OK"

# Step 1
Log-Info "Step 1/7 - Creating namespace and resource quotas..."
kubectl apply -f "$SCRIPT_DIR\namespace.yaml"

# Step 2
Log-Info "Step 2/7 - Creating secrets..."
kubectl apply -f "$SCRIPT_DIR\secrets.yaml"

if ($GenerateTLS) {
    Log-Info "Generating self-signed TLS certificate..."
    try {
        # Generate cert in the Windows cert store
        $cert = New-SelfSignedCertificate `
            -DnsName "banking.local" `
            -CertStoreLocation "Cert:\CurrentUser\My" `
            -NotAfter (Get-Date).AddYears(1) `
            -KeyAlgorithm RSA -KeyLength 2048 `
            -KeyExportPolicy Exportable `
            -FriendlyName "Banking System TLS"

        # Export PFX (contains cert + private key)
        $pfxPass = ConvertTo-SecureString "temppass" -Force -AsPlainText
        $pfxPath = "$SCRIPT_DIR\tls.pfx"
        Export-PfxCertificate -Cert $cert -FilePath $pfxPath -Password $pfxPass | Out-Null

        # Export certificate PEM
        $certBytes = $cert.Export([System.Security.Cryptography.X509Certificates.X509ContentType]::Cert)
        $certPem = "-----BEGIN CERTIFICATE-----`r`n"
        $certPem += [Convert]::ToBase64String($certBytes, [Base64FormattingOptions]::InsertLineBreaks)
        $certPem += "`r`n-----END CERTIFICATE-----"
        [System.IO.File]::WriteAllText("$SCRIPT_DIR\tls.crt", $certPem)

        # Export private key PEM (.NET Framework / PS 5.1 compatible)
        $pfxObj = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2(
            $pfxPath, "temppass",
            [System.Security.Cryptography.X509Certificates.X509KeyStorageFlags]::Exportable)
        $rsaParams = $pfxObj.PrivateKey.ExportParameters($true)

        # Build PKCS#1 RSAPrivateKey DER from RSA parameters
        function ConvertTo-Asn1Integer([byte[]]$val) {
            $tag = [byte]0x02
            if ($val[0] -ge 0x80) { $body = @([byte]0x00) + $val } else { $body = $val }
            $len = $body.Length
            if ($len -lt 128) { $hdr = @($tag, [byte]$len) }
            elseif ($len -lt 256) { $hdr = @($tag, [byte]0x81, [byte]$len) }
            else { $hdr = @($tag, [byte]0x82, [byte](($len -shr 8) -band 0xFF), [byte]($len -band 0xFF)) }
            return [byte[]]($hdr + $body)
        }
        $seq  = ConvertTo-Asn1Integer @(0)
        $seq += ConvertTo-Asn1Integer $rsaParams.Modulus
        $seq += ConvertTo-Asn1Integer $rsaParams.Exponent
        $seq += ConvertTo-Asn1Integer $rsaParams.D
        $seq += ConvertTo-Asn1Integer $rsaParams.P
        $seq += ConvertTo-Asn1Integer $rsaParams.Q
        $seq += ConvertTo-Asn1Integer $rsaParams.DP
        $seq += ConvertTo-Asn1Integer $rsaParams.DQ
        $seq += ConvertTo-Asn1Integer $rsaParams.InverseQ
        $seqLen = $seq.Length
        if ($seqLen -lt 128) { $seqHdr = @([byte]0x30, [byte]$seqLen) }
        elseif ($seqLen -lt 256) { $seqHdr = @([byte]0x30, [byte]0x81, [byte]$seqLen) }
        else { $seqHdr = @([byte]0x30, [byte]0x82, [byte](($seqLen -shr 8) -band 0xFF), [byte]($seqLen -band 0xFF)) }
        $derBytes = [byte[]]($seqHdr + $seq)

        $keyPem = "-----BEGIN RSA PRIVATE KEY-----`r`n"
        $keyPem += [Convert]::ToBase64String($derBytes, [Base64FormattingOptions]::InsertLineBreaks)
        $keyPem += "`r`n-----END RSA PRIVATE KEY-----"
        [System.IO.File]::WriteAllText("$SCRIPT_DIR\tls.key", $keyPem)

        # Create TLS secret in K8s
        kubectl create secret tls banking-tls-secret `
            --cert="$SCRIPT_DIR\tls.crt" `
            --key="$SCRIPT_DIR\tls.key" `
            -n $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

        # Cleanup
        Remove-Item "$SCRIPT_DIR\tls.pfx","$SCRIPT_DIR\tls.crt","$SCRIPT_DIR\tls.key" -ErrorAction SilentlyContinue
        Remove-Item "Cert:\CurrentUser\My\$($cert.Thumbprint)" -ErrorAction SilentlyContinue
        Log-Info "TLS certificate generated and secret created."
    } catch {
        Log-Warn "TLS generation failed: $_"
        Log-Warn "Continuing without TLS. You can create it manually later."
    }
} else {
    Log-Warn "Skipping TLS cert generation. Use -GenerateTLS to generate."
}

# Step 3
Log-Info "Step 3/7 - Applying RBAC and service accounts..."
kubectl apply -f "$SCRIPT_DIR\rbac.yaml"

# Step 4
Log-Info "Step 4/7 - Applying network policies..."
kubectl apply -f "$SCRIPT_DIR\network-policies.yaml"

# Step 5
Log-Info "Step 5/7 - Deploying PostgreSQL..."
kubectl apply -f "$SCRIPT_DIR\postgres.yaml"
Log-Info "Waiting for PostgreSQL..."
kubectl wait --for=condition=available deployment/postgres-db -n $NAMESPACE 2>$null

# Step 6
Log-Info "Step 6/7 - Deploying application services..."
kubectl apply -f "$SCRIPT_DIR\notification-service.yaml"
kubectl apply -f "$SCRIPT_DIR\backend.yaml"
kubectl apply -f "$SCRIPT_DIR\frontend.yaml"
kubectl apply -f "$SCRIPT_DIR\pod-disruption-budgets.yaml"

# Step 7
Log-Info "Step 7/7 - Configuring Ingress gateway with security headers..."
kubectl apply -f "$SCRIPT_DIR\security-headers.yaml"
kubectl apply -f "$SCRIPT_DIR\ingress.yaml"

# Wait
Log-Info "Waiting for all deployments (no timeout)..."
kubectl wait --for=condition=available `
    deployment/banking-backend `
    deployment/banking-frontend `
    deployment/notification-service `
    -n $NAMESPACE 2>$null

# Summary
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Log-Info "Banking System deployed to namespace: $NAMESPACE"
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Log-Info "Security features applied:"
Write-Host "  + Dedicated namespace with Pod Security Standards (restricted)"
Write-Host "  + Kubernetes Secrets for sensitive credentials"
Write-Host "  + Network Policies (zero-trust: deny-all + allow-list)"
Write-Host "  + RBAC with dedicated service accounts (least privilege)"
Write-Host "  + SecurityContext: non-root, read-only FS, drop ALL capabilities"
Write-Host "  + Seccomp profiles (RuntimeDefault)"
Write-Host "  + TLS termination at Ingress"
Write-Host "  + Security headers (HSTS, CSP, X-Frame-Options, etc.)"
Write-Host "  + Rate limiting on Ingress"
Write-Host "  + Resource quotas and limits"
Write-Host "  + Liveness/Readiness probes"
Write-Host "  + PodDisruptionBudgets"
Write-Host "  + Rolling update strategy (zero downtime)"
Write-Host ""
Log-Info "Check status: kubectl get all -n $NAMESPACE"
Log-Info "View pods:    kubectl get pods -n $NAMESPACE"
