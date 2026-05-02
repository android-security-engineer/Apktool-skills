---
description: Comprehensive security audit workflow for Android APK files. Use when performing security analysis, vulnerability assessment, or compliance checking.
---

# APK Security Audit

Comprehensive security audit workflow for Android applications.

## When to Use

- Security review of an Android app
- Vulnerability assessment
- Pre-release security check
- Compliance audit (OWASP Mobile Top 10)
- Third-party library security review

## Workflow

### Step 1: Automated Security Report

```bash
java -jar apktool.jar security <apk-file>
```

Review the output fields:
- `dangerousPermissions[]` — permissions with high privacy impact
- `highRiskComponents[]` — exported components without protection
- `debuggable` — can the app be debugged?
- `allowBackup` — can app data be extracted?
- `usesCleartextTraffic` — does it send unencrypted data?
- `findings[]` — list of identified security issues
- `riskScore` — overall risk score (0-100)

### Step 2: Attack Surface Analysis

```bash
java -jar apktool.jar api-surface <apk-file>
```

Check:
- `exportedActivities` — activities accessible to other apps
- `exportedServices` — services that can be bound/started externally
- `exportedReceivers` — broadcast receivers listening for external intents
- `exportedProviders` — content providers exposing data
- `intentFilters` — what actions/data each component accepts

For each exported component, verify:
1. Is `exported=true` intentional?
2. Is there a permission protection on it?
3. What data can it receive/process?

### Step 3: Permission Deep Dive

```bash
java -jar apktool.jar permissions <apk-file>
```

Cross-reference with app functionality:
- Does a flashlight app need `READ_CONTACTS`?
- Does a calculator need `ACCESS_FINE_LOCATION`?
- Are there more permissions than the app's features justify?

### Step 4: Sensitive Data Search

```bash
# Search for hardcoded credentials
java -jar apktool.jar search <apk-file> "password|passwd|secret|api.?key|token|credential" -t strings

# Search for URLs (potential API endpoints)
java -jar apktool.jar search <apk-file> "https?://" -t strings

# Search for crypto-related classes
java -jar apktool.jar search <apk-file> "Cipher|SecretKey|MessageDigest|SSLContext|TrustManager" -t classes

# Search for sensitive methods
java -jar apktool.jar search <apk-file> "encrypt|decrypt|hash|sign|verify" -t methods
```

### Step 5: Signing Verification

```bash
java -jar apktool.jar signing <apk-file>
```

Verify:
- Who is the signer? (`certificates[0].subject`)
- Is the SHA-256 fingerprint expected?
- Which signing schemes are used? (v1 only = less secure than v2/v3)
- Is the certificate still valid? (`notAfter` should be in the future)

### Step 6: Generate AI Prompt for Deeper Analysis

```bash
java -jar apktool.jar ai <apk-file> -a security-review
```

This generates a structured prompt you can feed to an LLM for in-depth security analysis.

## OWASP Mobile Top 10 Mapping

| OWASP Category | AI-Apktool Commands |
|---|---|
| M1: Platform Misuse | `security` (check exported components, permissions) |
| M2: Insecure Data Storage | `search` (find hardcoded keys, tokens) |
| M3: Insecure Communication | `search` (find cleartext URLs), `manifest` (networkSecurityConfig) |
| M4: Insecure Authentication | `search` (find auth-related classes) |
| M5: Insufficient Cryptography | `search` (find crypto classes), `signing` (check schemes) |
| M6: Insecure Authorization | `api-surface` (check exported components without permissions) |
| M7: Client Code Quality | `structure` (code complexity metrics) |
| M8: Code Tampering | `signing` (verify certificate), `security` (check debuggable) |
| M9: Reverse Engineering | `structure` (obfuscation indicators) |
| M10: Extraneous Functionality | `manifest` (unused components), `search` (debug/test code) |

## Report Template

After completing the audit, summarize findings:

```
## Security Audit Report

**App:** {packageName} v{versionName}
**Risk Score:** {riskScore}/100

### Critical Findings
- [List HIGH severity issues]

### Warnings
- [List MEDIUM severity issues]

### Informational
- [List LOW severity issues]

### Recommendations
- [Suggested fixes for each finding]
```
