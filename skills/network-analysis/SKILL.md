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
java -jar apktool.jar strings app.apk -p 'https?://.*'

# Search DEX strings for URLs
java -jar apktool.jar dex-strings app.apk | grep -i 'http'

# Search for specific API patterns
java -jar apktool.jar strings app.apk -p 'api\.|/v[0-9]/|graphql|websocket'
```

### Step 2: Find Network-Related Classes

```bash
# Search for HTTP client classes
java -jar apktool.jar method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection|Volley|HttpClient'

# Search for network-related methods
java -jar apktool.jar method-search app.apk -p 'execute|enqueue|doRequest|sendRequest|fetch'

# Search for SSL/TLS classes
java -jar apktool.jar method-search app.apk -p 'SSLSocket|TrustManager|HostnameVerifier|SSLContext'
```

### Step 3: Check Network Security

```bash
# Check manifest flags for cleartext traffic
java -jar apktool.jar manifest-flags app.apk

# Check for network security config
java -jar apktool.jar manifest app.apk | jq '.networkSecurityConfig'

# Full security report
java -jar apktool.jar security app.apk
```

### Step 4: Find Intent-Based Communication

```bash
# Check exported components (attack surface)
java -jar apktool.jar api-surface app.apk

# Check all components
java -jar apktool.jar components app.apk
```

## Common Patterns

```bash
# Complete network audit
java -jar apktool.jar strings app.apk -p 'https?://.*'
java -jar apktool.jar manifest-flags app.apk
java -jar apktool.jar method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection'
java -jar apktool.jar method-search app.apk -p 'TrustManager|HostnameVerifier'

# Find all REST API endpoints
java -jar apktool.jar strings app.apk -p '/api/.*'
java -jar apktool.jar method-search app.apk -p '@GET|@POST|@PUT|@DELETE'
```
