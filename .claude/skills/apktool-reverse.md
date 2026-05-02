---
description: Full reverse engineering workflow for Android APK files. Use when you need to deeply analyze, modify, or understand an Android application's internals.
---

# APK Reverse Engineering

Full reverse engineering workflow using AI-Apktool's decode, analysis, and search capabilities.

## When to Use

- Reverse engineering an unknown Android app
- Understanding app architecture and behavior
- Modifying or repackaging an APK
- Investigating malware or suspicious apps
- Learning how an app implements specific features

## Workflow

### Step 1: Reconnaissance — Gather Intelligence

```bash
# Full automated analysis
java -jar apktool.jar analyze <apk-file>

# Or run individual commands:
java -jar apktool.jar info <apk-file>
java -jar apktool.jar manifest <apk-file>
java -jar apktool.jar structure <apk-file>
```

Focus on:
- What does the app do? (package name, version, permissions)
- How big is it? (DEX count, native libs, architectures)
- What's the code structure? (class count, method count, top packages)

### Step 2: Decode — Extract Source Code

```bash
# Full decode (smali + resources)
java -jar apktool.jar decode <apk-file> -o <output-dir>

# Decode resources only (faster)
java -jar apktool.jar decode <apk-file> -o <output-dir> -s

# Decode manifest only (fastest)
java -jar apktool.jar decode <apk-file> -o <output-dir> --only-manifest
```

After decoding, explore:
- `<output-dir>/AndroidManifest.xml` — full manifest in readable XML
- `<output-dir>/smali*/` — Dalvik bytecode in smali format
- `<output-dir>/res/` — decoded resources (layouts, strings, drawables)
- `<output-dir>/assets/` — raw assets
- `<output-dir>/apktool.yml` — decode metadata

### Step 3: Search — Find What Matters

Without decoding, use the search command:

```bash
# Find specific classes
java -jar apktool.jar search <apk-file> "LoginActivity|MainActivity|SplashActivity" -t classes

# Find URL endpoints
java -jar apktool.jar search <apk-file> "https?://.*" -t strings

# Find specific method implementations
java -jar apktool.jar search <apk-file> "onCreate|onClick|doLogin|encrypt" -t methods

# Find all strings (get a sense of the app)
java -jar apktool.jar search <apk-file> ".*" -t strings
```

### Step 4: Analyze — Deep Security Review

```bash
# Security report
java -jar apktool.jar security <apk-file>

# API surface (what the app exposes)
java -jar apktool.jar api-surface <apk-file>

# Signing information
java -jar apktool.jar signing <apk-file>
```

### Step 5: AI-Powered Analysis

Generate prompts for deeper LLM analysis:

```bash
# Comprehensive explanation
java -jar apktool.jar ai <apk-file> -a explain

# Security-focused review
java -jar apktool.jar ai <apk-file> -a security-review

# Quick summary
java -jar apktool.jar ai <apk-file> -a summarize
```

The generated prompts contain structured context about the APK that you can feed directly into an LLM.

### Step 6: Rebuild (if modifications needed)

```bash
# After making changes to decoded files:
java -jar apktool.jar build <decoded-dir> -o <output.apk>

# Build with debug flag
java -jar apktool.jar build <decoded-dir> -o <output.apk> --debuggable
```

## Common Patterns

### Pattern: Find Hardcoded Secrets
```bash
java -jar apktool.jar search <apk> "password|secret|api.?key|Bearer|token" -t strings
```

### Pattern: Find All Network Endpoints
```bash
java -jar apktool.jar search <apk> "https?://" -t strings
java -jar apktool.jar search <apk> "websocket|wss?://" -t strings
```

### Pattern: Find Encryption Implementation
```bash
java -jar apktool.jar search <apk> "Cipher|AES|RSA|DES|SecretKey" -t classes
java -jar apktool.jar search <apk> "encrypt|decrypt|initCipher" -t methods
```

### Pattern: Find Authentication Logic
```bash
java -jar apktool.jar search <apk> "Login|Auth|Session|Token|OAuth" -t classes
java -jar apktool.jar search <apk> "authenticate|login|signIn|getSession" -t methods
```

### Pattern: Analyze Malware Indicators
```bash
# Suspicious permissions
java -jar apktool.jar permissions <apk>

# Suspicious classes
java -jar apktool.jar search <apk> "Runtime|exec|ProcessBuilder|su|root" -t classes

# Command execution methods
java -jar apktool.jar search <apk> "exec|loadLibrary|DexClassLoader" -t methods

# C2 server URLs
java -jar apktool.jar search <apk> "http://" -t strings
```
