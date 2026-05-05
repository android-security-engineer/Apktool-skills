---
name: resource-explorer
description: Resource and file exploration workflow. Use when you need to understand app resources, locales, assets, or file structure.
autoInvoke: true
---

# Resource & File Explorer

Workflow for exploring Android app resources, assets, and file structure.

## When to Use

- Understanding app resource organization
- Finding specific resources (strings, drawables, layouts)
- Checking localization support
- Exploring assets directory
- Analyzing APK file structure

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Resource Overview

```bash
# Resource table summary
java -jar apktool.jar resources app.apk

# Resource package groups
java -jar apktool.jar resource-packages app.apk

# Library and framework package IDs
java -jar apktool.jar lib-frame-packages app.apk

# Supported locales
java -jar apktool.jar locales app.apk
```

### Step 2: File Exploration

```bash
# List all files in APK
java -jar apktool.jar file-list app.apk

# APK file hashes (integrity check)
java -jar apktool.jar file-hash app.apk

# Assets directory
java -jar apktool.jar asset-list app.apk

# Native libraries per architecture
java -jar apktool.jar native-libs app.apk
```

### Step 3: String Resources

```bash
# All strings (DEX + resources)
java -jar apktool.jar strings app.apk

# DEX-only strings
java -jar apktool.jar dex-strings app.apk

# Search strings by pattern
java -jar apktool.jar strings app.apk -p 'app_name|version|url'
```

### Step 4: Decoded APK Info

```bash
# Read apktool.yml from decoded directory
java -jar apktool.jar apk-info decoded_app_dir

# Shared libraries used
java -jar apktool.jar uses-libs app.apk
```

## Common Patterns

```bash
# Full resource audit
java -jar apktool.jar resources app.apk
java -jar apktool.jar locales app.apk
java -jar apktool.jar asset-list app.apk
java -jar apktool.jar native-libs app.apk

# Find all configuration files
java -jar apktool.jar file-list app.apk | grep -i '\.xml\|\.json\|\.properties\|\.conf'

# Check for tracking/analytics resources
java -jar apktool.jar strings app.apk -p 'google-analytics|firebase|mixpanel|flurry|amplitude'
```
