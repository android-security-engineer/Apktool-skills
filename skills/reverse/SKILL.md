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
java -jar apktool.jar analyze <apk-file>
```

### Step 2: Decode

```bash
java -jar apktool.jar decode <apk-file> -o <output-dir>
java -jar apktool.jar decode <apk-file> -o <output-dir> -s       # resources only
java -jar apktool.jar decode <apk-file> -o <output-dir> --only-manifest
```

### Step 3: Search

```bash
java -jar apktool.jar search <apk-file> "LoginActivity|MainActivity" -t classes
java -jar apktool.jar search <apk-file> "https?://.*" -t strings
java -jar apktool.jar search <apk-file> "onCreate|onClick|doLogin|encrypt" -t methods
```

### Step 4: Security Review

```bash
java -jar apktool.jar security <apk-file>
java -jar apktool.jar api-surface <apk-file>
java -jar apktool.jar signing <apk-file>
```

### Step 5: AI-Powered Analysis

```bash
java -jar apktool.jar ai <apk-file> -a explain
java -jar apktool.jar ai <apk-file> -a security-review
java -jar apktool.jar ai <apk-file> -a summarize
```

### Step 6: Rebuild (if modifications needed)

```bash
java -jar apktool.jar build <decoded-dir> -o <output.apk>
```

## Common Patterns

```bash
# Find hardcoded secrets
java -jar apktool.jar search <apk> "password|secret|api.?key|Bearer|token" -t strings

# Find network endpoints
java -jar apktool.jar search <apk> "https?://" -t strings

# Find encryption implementation
java -jar apktool.jar search <apk> "Cipher|AES|RSA|DES|SecretKey" -t classes

# Find auth logic
java -jar apktool.jar search <apk> "Login|Auth|Session|Token|OAuth" -t classes

# Malware indicators
java -jar apktool.jar search <apk> "Runtime|exec|ProcessBuilder|su|root" -t classes
java -jar apktool.jar search <apk> "exec|loadLibrary|DexClassLoader" -t methods

# Search fields
java -jar apktool.jar field-search <apk-file> -p 'mContext|mView|mHandler'

# Get class inheritance
java -jar apktool.jar inheritance <apk-file> com.example.SuspiciousClass

# Get detailed class info
java -jar apktool.jar class-info <apk-file> com.example.TargetClass
```
