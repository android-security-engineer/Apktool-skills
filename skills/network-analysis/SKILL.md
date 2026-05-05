---
name: network-analysis
description: Network and communication analysis workflow. Use when you need to find API endpoints, URLs, network security issues, or cleartext traffic in an APK.
autoInvoke: true
---

# Network & Communication Analysis

Workflow for discovering and analyzing network communication in Android apps.

## When to Use

- Finding API endpoints and server URLs
- Checking for cleartext (HTTP) traffic
- Analyzing network security configuration
- Finding WebSocket or gRPC connections
- Security audit of network communication

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Find Network Endpoints

```bash
# Search for URLs in strings
apktool strings app.apk -p 'https?://.*'

# Search DEX strings for URLs
apktool dex-strings app.apk | grep -i 'http'

# Search for specific API patterns
apktool strings app.apk -p 'api\.|/v[0-9]/|graphql|websocket'
```

### Step 2: Find Network-Related Classes

```bash
# Search for HTTP client classes
apktool method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection|Volley|HttpClient'

# Search for network-related methods
apktool method-search app.apk -p 'execute|enqueue|doRequest|sendRequest|fetch'

# Search for SSL/TLS classes
apktool method-search app.apk -p 'SSLSocket|TrustManager|HostnameVerifier|SSLContext'
```

### Step 3: Check Network Security

```bash
# Check manifest flags for cleartext traffic
apktool manifest-flags app.apk

# Check for network security config
apktool manifest app.apk | jq '.networkSecurityConfig'

# Full security report
apktool security app.apk
```

### Step 4: Find Intent-Based Communication

```bash
# Check exported components (attack surface)
apktool api-surface app.apk

# Check all components
apktool components app.apk
```

## Common Patterns

```bash
# Complete network audit
apktool strings app.apk -p 'https?://.*'
apktool manifest-flags app.apk
apktool method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection'
apktool method-search app.apk -p 'TrustManager|HostnameVerifier'

# Find all REST API endpoints
apktool strings app.apk -p '/api/.*'
apktool method-search app.apk -p '@GET|@POST|@PUT|@DELETE'
```
