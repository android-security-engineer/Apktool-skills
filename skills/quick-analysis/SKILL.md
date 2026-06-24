---
name: quick-analysis
description: Quick APK analysis workflow for initial assessment. Use when you need a fast overview of an unknown APK file.
autoInvoke: true
---

# APK Quick Analysis

Quick analysis workflow for getting a rapid overview of any APK file.

## When to Use

- First time encountering an APK
- Need a quick security assessment
- Want to understand what an app does
- Evaluating an app before deeper analysis

## Prerequisites

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format=json` for the full machine-readable command catalog.

## Workflow

### Step 1: Get APK Summary

Run a full analysis to get all data at once:

```bash
apktool analyze <apk-file>
```

This returns a comprehensive JSON object with: summary, manifest, security report, API surface, resources, signing info, and code structure.

### Step 2: Quick Assessment Checklist

Based on the `analyze` output, check these fields:

1. **Package Identity** — `summary.packageName`, `summary.versionName`, `summary.versionCode`
2. **Security Risk** — `security.riskScore` (0-100, higher = more risky)
   - 0-20: Low risk
   - 21-50: Medium risk
   - 51-100: High risk
3. **Dangerous Permissions** — `security.dangerousPermissions` (empty = good)
4. **Exported Components** — `apiSurface.totalExportedComponents` (more = larger attack surface)
5. **Signing** — `signing.certificates[0].subject` (who signed it?)
6. **Debug Flag** — `security.debuggable` (true = dangerous in production)

### Step 3: Targeted Deep Dive (if needed)

Based on findings, run targeted commands:

```bash
# Focus on security issues
apktool security <apk-file>

# Check what's exposed to other apps
apktool api-surface <apk-file>

# Search for sensitive strings
apktool search <apk-file> "password|secret|key|token" -t strings

# Search for specific classes
apktool search <apk-file> "LoginActivity|AuthHelper|Encryption" -t classes

# Check DEX structure
apktool dex-info <apk-file>

# Get file hashes for integrity
apktool file-hash <apk-file>

# Detailed permission analysis
apktool permission-detail <apk-file>
```

## Output Interpretation

### riskScore Breakdown
- `debuggable=true`: +30 points
- `allowBackup=true`: +10 points
- Each dangerous permission: +2 points (max 20)
- Each unprotected exported component: +5 points (max 30)

### Common Findings
- `"HIGH: Application is debuggable"` — app can be attached to a debugger
- `"MEDIUM: Application allows backup"` — app data can be extracted via adb
- `"HIGH: N exported components without permission protection"` — attack surface

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
