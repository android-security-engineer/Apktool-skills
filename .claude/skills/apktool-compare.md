---
description: APK version comparison workflow. Use when comparing two versions of the same app to identify changes, new features, or regressions.
---

# APK Version Comparison

Workflow for comparing two versions of an Android APK to identify differences.

## When to Use

- Checking what changed between app versions
- Verifying security fixes were applied
- Tracking new permissions or components
- Understanding app evolution
- Pre-release regression testing

## Workflow

### Step 1: Quick Diff

```bash
java -jar apktool.jar diff <old-apk> <new-apk>
```

Review the output:
- `addedPermissions[]` — new permissions requested
- `removedPermissions[]` — permissions removed
- `addedActivities[]` / `removedActivities[]` — new/removed Activities
- `addedServices[]` / `removedServices[]` — new/removed Services
- `addedDexFiles[]` / `removedDexFiles[]` — DEX file changes
- `addedNativeLibs[]` / `removedNativeLibs[]` — native library changes
- `versionCodeChange` — "X → Y"
- `versionNameChange` — "X → Y"
- `targetSdkChange` — "X → Y"

### Step 2: Detailed Analysis

Run individual commands on both APKs for detailed comparison:

```bash
# Security comparison
java -jar apktool.jar security <old-apk> > old_security.json
java -jar apktool.jar security <new-apk> > new_security.json

# Compare risk scores
# Old: old_security.json → riskScore
# New: new_security.json → riskScore

# API surface comparison
java -jar apktool.jar api-surface <old-apk> > old_surface.json
java -jar apktool.jar api-surface <new-apk> > new_surface.json

# Code structure comparison
java -jar apktool.jar structure <old-apk> > old_structure.json
java -jar apktool.jar structure <new-apk> > new_structure.json
```

### Step 3: Identify Key Changes

Focus on these high-impact changes:

1. **New Permissions** — Why was each permission added? Is it justified?
2. **New Exported Components** — New attack surface? Properly protected?
3. **Native Library Changes** — New architectures? New native code?
4. **DEX Changes** — New DEX files = significant code additions
5. **Target SDK Change** — Higher target SDK = stricter security policies
6. **Risk Score Delta** — Did security improve or degrade?

### Step 4: Search for Specific Changes

```bash
# Search for new strings in the new version
java -jar apktool.jar search <new-apk> "changelog|whats.?new|updated" -t strings

# Search for new classes
java -jar apktool.jar search <new-apk> "NewFeature|UpdatedActivity" -t classes
```

## Change Report Template

```
## Version Comparison Report

**Old:** v{oldVersion} (versionCode {oldCode})
**New:** v{newVersion} (versionCode {newCode})

### Permissions Changes
- Added: {addedPermissions}
- Removed: {removedPermissions}

### Component Changes
- New Activities: {addedActivities}
- Removed Activities: {removedActivities}
- New Services: {addedServices}
- New Native Libraries: {addedNativeLibs}

### Security Impact
- Risk Score: {oldScore} → {newScore}
- New exported components: {count}
- Key security changes: {summary}

### Recommendations
- [Review each new permission]
- [Audit each new exported component]
```
