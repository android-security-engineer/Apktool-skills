---
name: signing-verify
description: APK signing and certificate verification workflow. Use when you need to verify APK signing, check certificate details, or assess signing security.
autoInvoke: true
---

# APK Signing & Verification

Workflow for verifying and analyzing APK signing certificates.

## When to Use

- Verifying APK authenticity
- Checking signing certificate details
- Assessing signing scheme security (v1/v2/v3/v4)
- Comparing certificates between app versions
- Detecting self-signed or debug certificates

## Prerequisites

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format json` for the full machine-readable command catalog.

## Workflow

### Step 1: Get Signing Information

```bash
# Full signing details
apktool signing app.apk
```

Review: `v1Signing`, `v2Signing`, `v3Signing`, `certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}]`

### Step 2: Analyze Certificate

Key checks:
- **Subject/Issuer**: Is it self-signed? (subject == issuer)
- **Validity**: Is the certificate expired? (notAfter < now)
- **Fingerprints**: Compare with known good certificates
- **Signing schemes**: v1 only = vulnerable to Janus attack, v2+ recommended

### Step 3: Compare with Another APK

```bash
# Compare signing between versions
apktool signing app_v1.apk > signing_v1.json
apktool signing app_v2.apk > signing_v2.json

# Full diff including signing changes
apktool diff app_v1.apk app_v2.apk
```

### Step 4: Check for Debug Signing

```bash
# Check manifest flags (debuggable)
apktool manifest-flags app.apk

# Full security report
apktool security app.apk
```

## Signing Scheme Security

| Scheme | Android Version | Security |
|--------|---------------|----------|
| v1 (JAR) | 7.0+ | Vulnerable to Janus attack, allows APK modification |
| v2 (APK Signature Scheme v2) | 7.0+ | Protects entire APK, prevents modification |
| v3 (APK Signature Scheme v3) | 9.0+ | Adds key rotation support |
| v4 | 11.0+ | Adds streaming verification |

## Common Patterns

```bash
# Complete signing audit
apktool signing app.apk
apktool manifest-flags app.apk
apktool file-hash app.apk
```

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
