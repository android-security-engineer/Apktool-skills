---
name: reverse
description: Full reverse engineering workflow for Android APK files. Use when you need to deeply analyze, modify, or understand an Android application's internals.
autoInvoke: true
---

# APK Reverse Engineering

Full reverse engineering workflow using AI-Apktool's decode, analysis, and search capabilities.

## When to Use

- Reverse engineering an unknown Android app
- Understanding app architecture and behavior
- Modifying or repackaging an APK
- Investigating malware or suspicious apps

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Reconnaissance

```bash
apktool analyze <apk-file>
```

### Step 2: Decode

```bash
apktool decode <apk-file> -o <output-dir>
apktool decode <apk-file> -o <output-dir> -s       # resources only
apktool decode <apk-file> -o <output-dir> --only-manifest
```

### Step 3: Search

```bash
apktool search <apk-file> "LoginActivity|MainActivity" -t classes
apktool search <apk-file> "https?://.*" -t strings
apktool search <apk-file> "onCreate|onClick|doLogin|encrypt" -t methods
```

### Step 4: Security Review

```bash
apktool security <apk-file>
apktool api-surface <apk-file>
apktool signing <apk-file>
```

### Step 5: AI-Powered Analysis

```bash
apktool ai <apk-file> -a explain
apktool ai <apk-file> -a security-review
apktool ai <apk-file> -a summarize
```

### Step 6: Rebuild (if modifications needed)

```bash
apktool build <decoded-dir> -o <output.apk>
```

## Common Patterns

```bash
# Find hardcoded secrets
apktool search <apk> "password|secret|api.?key|Bearer|token" -t strings

# Find network endpoints
apktool search <apk> "https?://" -t strings

# Find encryption implementation
apktool search <apk> "Cipher|AES|RSA|DES|SecretKey" -t classes

# Find auth logic
apktool search <apk> "Login|Auth|Session|Token|OAuth" -t classes

# Malware indicators
apktool search <apk> "Runtime|exec|ProcessBuilder|su|root" -t classes
apktool search <apk> "exec|loadLibrary|DexClassLoader" -t methods

# Search fields
apktool field-search <apk-file> -p 'mContext|mView|mHandler'

# Get class inheritance
apktool inheritance <apk-file> com.example.SuspiciousClass

# Get detailed class info
apktool class-info <apk-file> com.example.TargetClass
```
