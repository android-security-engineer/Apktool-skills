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

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format json` for the full machine-readable command catalog.

## Workflow

### Step 1: Resource Overview

```bash
# Resource table summary
apktool resources app.apk

# Resource package groups
apktool resource-packages app.apk

# Library and framework package IDs
apktool lib-frame-packages app.apk

# Supported locales
apktool locales app.apk
```

### Step 2: File Exploration

```bash
# List all files in APK
apktool file-list app.apk

# APK file hashes (integrity check)
apktool file-hash app.apk

# Assets directory
apktool asset-list app.apk

# Native libraries per architecture
apktool native-libs app.apk
```

### Step 3: String Resources

```bash
# All strings (DEX + resources)
apktool strings app.apk

# DEX-only strings
apktool dex-strings app.apk

# Search strings by pattern
apktool strings app.apk -p 'app_name|version|url'
```

### Step 4: Decoded APK Info

```bash
# Read apktool.yml from decoded directory
apktool apk-info decoded_app_dir

# Shared libraries used
apktool uses-libs app.apk
```

## Common Patterns

```bash
# Full resource audit
apktool resources app.apk
apktool locales app.apk
apktool asset-list app.apk
apktool native-libs app.apk

# Find all configuration files
apktool file-list app.apk | grep -i '\.xml\|\.json\|\.properties\|\.conf'

# Check for tracking/analytics resources
apktool strings app.apk -p 'google-analytics|firebase|mixpanel|flurry|amplitude'
```

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
