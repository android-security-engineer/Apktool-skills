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

AI-Apktool CLI must be available. Build from source or download:
```bash
./gradlew build
# JAR at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```

## Workflow

### Step 1: Get APK Summary

Run a full analysis to get all data at once:

```bash
java -jar apktool.jar analyze <apk-file>
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
java -jar apktool.jar security <apk-file>

# Check what's exposed to other apps
java -jar apktool.jar api-surface <apk-file>

# Search for sensitive strings
java -jar apktool.jar search <apk-file> "password|secret|key|token" -t strings

# Search for specific classes
java -jar apktool.jar search <apk-file> "LoginActivity|AuthHelper|Encryption" -t classes

# Check DEX structure
java -jar apktool.jar dex-info <apk-file>

# Get file hashes for integrity
java -jar apktool.jar file-hash <apk-file>

# Detailed permission analysis
java -jar apktool.jar permission-detail <apk-file>
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
