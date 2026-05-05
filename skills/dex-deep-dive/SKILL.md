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

AI-Apktool CLI must be available.

## Workflow

### Step 1: Get DEX Overview

```bash
# List all DEX files
java -jar apktool.jar dex-list app.apk

# Per-DEX statistics (classes, methods, fields)
java -jar apktool.jar dex-info app.apk

# Code structure overview
java -jar apktool.jar structure app.apk

# List all class names
java -jar apktool.jar class-list app.apk
```

### Step 2: Explore Specific Classes

```bash
# Get detailed class info (methods, fields, superclass, interfaces)
java -jar apktool.jar class-info app.apk com.example.MyActivity

# Get class inheritance chain
java -jar apktool.jar inheritance app.apk com.example.MyActivity
```

### Step 3: Search Methods and Fields

```bash
# Search methods by regex pattern
java -jar apktool.jar method-search app.apk -p 'onCreate'
java -jar apktool.jar method-search app.apk -p 'encrypt|decrypt|cipher'

# Search fields by regex pattern
java -jar apktool.jar field-search app.apk -p 'mContext'
java -jar apktool.jar field-search app.apk -p 'apiKey|secret|token'
```

### Step 4: Extract Strings

```bash
# All DEX strings
java -jar apktool.jar dex-strings app.apk

# All strings (DEX + resources)
java -jar apktool.jar strings app.apk

# Filter strings by pattern
java -jar apktool.jar strings app.apk -p 'http.*'
```

## Common Patterns

```bash
# Find all crypto-related code
java -jar apktool.jar method-search app.apk -p 'Cipher|SecretKey|MessageDigest|AES|RSA'

# Find all network-related classes
java -jar apktool.jar class-list app.apk | grep -i 'http\|url\|request\|api'

# Trace Activity lifecycle
java -jar apktool.jar method-search app.apk -p 'onCreate|onStart|onResume|onPause|onStop|onDestroy'

# Find hardcoded credentials
java -jar apktool.jar field-search app.apk -p 'password|secret|key|token|credential'
java -jar apktool.jar dex-strings app.apk | grep -i 'password\|api.key\|bearer'
```
