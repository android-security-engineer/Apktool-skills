---
name: compare
description: APK version comparison workflow. Use when comparing two versions of the same app to identify changes, new features, or regressions.
autoInvoke: true
---

# APK Version Comparison

Workflow for comparing two versions of an Android APK to identify differences.

## When to Use

- Checking what changed between app versions
- Verifying security fixes were applied
- Tracking new permissions or components
- Pre-release regression testing

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Quick Diff

```bash
apktool diff <old-apk> <new-apk>
```

Review: `addedPermissions[]`, `removedPermissions[]`, `addedActivities[]`, `removedActivities[]`, `addedServices[]`, `addedDexFiles[]`, `addedNativeLibs[]`, `versionCodeChange`, `versionNameChange`, `targetSdkChange`

### Step 2: Detailed Analysis

```bash
apktool security <old-apk> > old_security.json
apktool security <new-apk> > new_security.json
apktool api-surface <old-apk> > old_surface.json
apktool api-surface <new-apk> > new_surface.json
apktool structure <old-apk> > old_structure.json
apktool structure <new-apk> > new_structure.json
```

### Step 3: Identify Key Changes

Focus on: New Permissions, New Exported Components, Native Library Changes, DEX Changes, Target SDK Change, Risk Score Delta.

### Step 4: Search for Specific Changes

```bash
apktool search <new-apk> "changelog|whats.?new|updated" -t strings
apktool search <new-apk> "NewFeature|UpdatedActivity" -t classes

# Compare signing certificates
apktool signing <old-apk> > old_signing.json
apktool signing <new-apk> > new_signing.json

# Compare file hashes
apktool file-hash <old-apk> > old_hash.json
apktool file-hash <new-apk> > new_hash.json
```
