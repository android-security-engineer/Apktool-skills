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

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format=json` for the full machine-readable command catalog.

## Workflow

### Step 1: Automated Security Report

```bash
apktool security <apk-file>
```

Review: `dangerousPermissions[]`, `highRiskComponents[]`, `debuggable`, `allowBackup`, `usesCleartextTraffic`, `findings[]`, `riskScore`

### Step 2: Attack Surface Analysis

```bash
apktool api-surface <apk-file>
```

For each exported component, verify: Is `exported=true` intentional? Is there permission protection? What data can it receive/process?

### Step 3: Permission Deep Dive

```bash
apktool permissions <apk-file>
```

Cross-reference with app functionality.

### Step 4: Sensitive Data Search

```bash
apktool search <apk-file> "password|passwd|secret|api.?key|token|credential" -t strings
apktool search <apk-file> "https?://" -t strings
apktool search <apk-file> "Cipher|SecretKey|MessageDigest|SSLContext|TrustManager" -t classes
apktool search <apk-file> "encrypt|decrypt|hash|sign|verify" -t methods

# Check manifest security flags
apktool manifest-flags <apk-file>

# Detailed permission analysis
apktool permission-detail <apk-file>

# Check for dynamic code loading
apktool method-search <apk-file> -p 'DexClassLoader|PathClassLoader'
```

### Step 5: Signing Verification

```bash
apktool signing <apk-file>
```

### Step 6: Generate AI Prompt for Deeper Analysis

```bash
apktool ai <apk-file> -a security-review   # LLM prompt for a deeper review
apktool ai <apk-file> -a context           # structured facts (JSON) to feed an agent
```

## Run the Whole Audit in One Pass

Steps 1-5 hit the same APK repeatedly — batch them with `apktool run` for a single shared parse and per-command error isolation. A ready-to-run script is in [`references/batch-audit.md`](references/batch-audit.md).

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

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
