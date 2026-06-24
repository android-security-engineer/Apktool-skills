---
name: dex-deep-dive
description: Deep DEX analysis workflow for class, method, and field exploration. Use when you need to understand code structure, trace class hierarchies, or search for specific implementations.
autoInvoke: true
---

# DEX Deep Dive

Workflow for deep analysis of DEX bytecode — classes, methods, fields, and inheritance.

## When to Use

- Understanding app code architecture
- Tracing class inheritance hierarchies
- Searching for specific method implementations
- Finding field usage patterns
- Analyzing multi-DEX applications
- Reverse engineering specific functionality

## Prerequisites

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format=json` for the full machine-readable command catalog.

## Workflow

### Step 1: Get DEX Overview

```bash
# List all DEX files
apktool dex-list app.apk

# Per-DEX statistics (classes, methods, fields)
apktool dex-info app.apk

# Code structure overview
apktool structure app.apk

# List all class names
apktool class-list app.apk
```

### Step 2: Explore Specific Classes

```bash
# Get detailed class info (methods, fields, superclass, interfaces)
apktool class-info app.apk com.example.MyActivity

# Get class inheritance chain
apktool inheritance app.apk com.example.MyActivity
```

### Step 3: Search Methods and Fields

```bash
# Search methods by regex pattern
apktool method-search app.apk -p 'onCreate'
apktool method-search app.apk -p 'encrypt|decrypt|cipher'

# Search fields by regex pattern
apktool field-search app.apk -p 'mContext'
apktool field-search app.apk -p 'apiKey|secret|token'
```

### Step 4: Extract Strings

```bash
# All DEX strings
apktool dex-strings app.apk

# All strings (DEX + resources)
apktool strings app.apk

# Filter strings by pattern
apktool strings app.apk -p 'http.*'
```

## Common Patterns

```bash
# Find all crypto-related code
apktool method-search app.apk -p 'Cipher|SecretKey|MessageDigest|AES|RSA'

# Find all network-related classes
apktool class-list app.apk | grep -i 'http\|url\|request\|api'

# Trace Activity lifecycle
apktool method-search app.apk -p 'onCreate|onStart|onResume|onPause|onStop|onDestroy'

# Find hardcoded credentials
apktool field-search app.apk -p 'password|secret|key|token|credential'
apktool dex-strings app.apk | grep -i 'password\|api.key\|bearer'
```

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
