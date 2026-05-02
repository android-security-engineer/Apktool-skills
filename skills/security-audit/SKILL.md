---
name: security-audit
description: Comprehensive security audit workflow for Android APK files. Use when performing security analysis, vulnerability assessment, or compliance checking.
autoInvoke: true
---

# APK Security Audit

Comprehensive security audit workflow for Android applications.

## When to Use

- Security review of an Android app
- Vulnerability assessment
- Pre-release security check
- Compliance audit (OWASP Mobile Top 10)
- Third-party library security review

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Automated Security Report

```bash
java -jar apktool.jar security <apk-file>
```

Review: `dangerousPermissions[]`, `highRiskComponents[]`, `debuggable`, `allowBackup`, `usesCleartextTraffic`, `findings[]`, `riskScore`

### Step 2: Attack Surface Analysis

```bash
java -jar apktool.jar api-surface <apk-file>
```

For each exported component, verify: Is `exported=true` intentional? Is there permission protection? What data can it receive/process?

### Step 3: Permission Deep Dive

```bash
java -jar apktool.jar permissions <apk-file>
```

Cross-reference with app functionality.

### Step 4: Sensitive Data Search

```bash
java -jar apktool.jar search <apk-file> "password|passwd|secret|api.?key|token|credential" -t strings
java -jar apktool.jar search <apk-file> "https?://" -t strings
java -jar apktool.jar search <apk-file> "Cipher|SecretKey|MessageDigest|SSLContext|TrustManager" -t classes
java -jar apktool.jar search <apk-file> "encrypt|decrypt|hash|sign|verify" -t methods
```

### Step 5: Signing Verification

```bash
java -jar apktool.jar signing <apk-file>
```

### Step 6: Generate AI Prompt for Deeper Analysis

```bash
java -jar apktool.jar ai <apk-file> -a security-review
```

## OWASP Mobile Top 10 Mapping

| OWASP Category | AI-Apktool Commands |
|---|---|
| M1: Platform Misuse | `security` |
| M2: Insecure Data Storage | `search` (find hardcoded keys) |
| M3: Insecure Communication | `search` (cleartext URLs), `manifest` |
| M4: Insecure Authentication | `search` (auth classes) |
| M5: Insufficient Cryptography | `search` (crypto classes), `signing` |
| M6: Insecure Authorization | `api-surface` |
| M7: Client Code Quality | `structure` |
| M8: Code Tampering | `signing`, `security` |
| M9: Reverse Engineering | `structure` |
| M10: Extraneous Functionality | `manifest`, `search` |
