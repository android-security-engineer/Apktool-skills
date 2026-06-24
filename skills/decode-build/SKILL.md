---
name: decode-build
description: APK decode and build workflow. Use when you need to decode an APK to smali/resources, rebuild it, or manage framework dependencies.
autoInvoke: true
---

# APK Decode & Build

Workflow for decoding, modifying, and rebuilding Android APK files.

## When to Use

- Decoding an APK to inspect or modify smali code and resources
- Rebuilding an APK after modifications
- Managing framework APKs for proper resource decoding
- Converting binary AndroidManifest.xml to readable XML
- Extracting resources from an APK

## Prerequisites

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format json` for the full machine-readable command catalog.

## Workflow

### Step 1: Decode APK

```bash
# Full decode (smali + resources)
apktool decode app.apk -o decoded_app

# Decode resources only (no smali)
apktool decode app.apk -o decoded_app -s

# Decode manifest only
apktool decode app.apk -o decoded_app --only-manifest

# Force decode (overwrite existing directory)
apktool decode app.apk -o decoded_app -f
```

### Step 2: Inspect Decoded Content

```bash
# Read decoded APK metadata
apktool apk-info decoded_app

# View full manifest XML
apktool manifest-xml app.apk
```

### Step 3: Modify (if needed)

Edit files in the decoded directory:
- `smali/` — Dalvik bytecode in smali format
- `res/` — Android resources (layouts, strings, drawables)
- `AndroidManifest.xml` — App manifest
- `apktool.yml` — Apktool metadata

### Step 4: Rebuild APK

```bash
# Build from decoded directory
apktool build decoded_app -o modified.apk

# Build with custom output
apktool build decoded_app -o output/modified.apk
```

### Step 5: Framework Management

```bash
# Install framework APK (needed for proper resource decoding)
apktool install-framework framework-res.apk

# List installed frameworks
apktool list-frameworks

# Clean all frameworks
apktool clean-frameworks

# Publicize resources in ARSC
apktool publicize-resources resources.arsc
```

## Common Patterns

```bash
# Decode → Modify → Rebuild cycle
apktool decode app.apk -o app_dir
# ... edit files in app_dir ...
apktool build app_dir -o modified.apk

# Decode with framework for OEM ROMs
apktool install-framework framework-res.apk
apktool decode app.apk -o app_dir
```

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
