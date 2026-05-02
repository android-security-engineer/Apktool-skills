---
description: Complete CLI command reference for AI-Apktool. Use as a lookup when you need the exact syntax, options, or output format of any command.
---

# AI-Apktool Command Reference

Complete reference for all AI-Apktool CLI commands.

## Universal Patterns

### All analysis commands output JSON to stdout
```bash
java -jar apktool.jar <command> <apk-file>
```

### JSON Help Catalog (for programmatic discovery)
```bash
java -jar apktool.jar help --format json
```

### Pipe to jq for field extraction
```bash
java -jar apktool.jar info app.apk | jq '.packageName'
```

---

## Core Commands

### decode / d — Decode APK to directory

```bash
java -jar apktool.jar d [options] <apk-file>
```

| Option | Description |
|--------|-------------|
| `-f, --force` | Force delete destination directory |
| `-s, --no-src` | Don't decode sources (keep DEX files) |
| `-r, --no-res` | Don't decode resources |
| `--only-manifest` | Only decode AndroidManifest.xml |
| `-o, --output <dir>` | Output directory name |
| `-p, --frame-path <dir>` | Framework directory path |
| `-t, --frame-tag <tag>` | Framework tag |
| `-j, --jobs <n>` | Parallel DEX decoding jobs |

**Output:** Directory with `smali/`, `res/`, `AndroidManifest.xml`, `apktool.yml`

### build / b — Build APK from decoded directory

```bash
java -jar apktool.jar b [options] <apk-dir>
```

| Option | Description |
|--------|-------------|
| `-f, --force` | Skip change detection |
| `--no-apk` | Don't repack into APK |
| `--no-crunch` | Disable resource crunching |
| `--copy-original` | Copy original manifest/META-INF |
| `--debuggable` | Set debuggable=true |
| `--net-sec-conf` | Add network security config |
| `-o, --output <file>` | Output APK file |

### install-framework / if — Install framework APK

```bash
java -jar apktool.jar if [options] <framework-apk>
```

| Option | Description |
|--------|-------------|
| `-p, --frame-path <dir>` | Framework directory |
| `-t, --frame-tag <tag>` | Framework tag |

### clean-frameworks / cf — Clean framework files

```bash
java -jar apktool.jar cf [options]
```

### list-frameworks / lf — List installed frameworks

```bash
java -jar apktool.jar lf [options]
```

### publicize-resources / pr — Make resources public

```bash
java -jar apktool.jar pr <arsc-file>
```

---

## Analysis Commands

All analysis commands take `<apk-file>` as argument and output JSON.

### info — APK metadata summary

```bash
java -jar apktool.jar info <apk-file>
```

**Output fields:** `fileName, fileSize, packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, dexCount, hasResources, hasAssets, hasNativeLibs, architectures, permissionCount, activityCount, serviceCount, receiverCount, providerCount`

### manifest — Full manifest data

```bash
java -jar apktool.jar manifest <apk-file>
```

**Output fields:** `packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, maxSdkVersion, permissions[], activities[{name, exported, intentFilters[], permissions[]}], services[], receivers[], providers[], usesLibraries[], debuggable, allowBackup, networkSecurityConfig`

### permissions — Permission list

```bash
java -jar apktool.jar permissions <apk-file>
```

**Output:** JSON array of permission strings

### activities / services / receivers / providers — Component lists

```bash
java -jar apktool.jar activities <apk-file>
java -jar apktool.jar services <apk-file>
java -jar apktool.jar receivers <apk-file>
java -jar apktool.jar providers <apk-file>
```

**Output:** JSON array of `{name, exported, intentFilters[], permissions[]}`

### components — All components combined

```bash
java -jar apktool.jar components <apk-file>
```

**Output:** `{activities[], services[], receivers[], providers[]}`

### sdk-info — SDK version requirements

```bash
java -jar apktool.jar sdk-info <apk-file>
```

**Output fields:** `minSdkVersion, targetSdkVersion, maxSdkVersion`

### resources — Resource table summary

```bash
java -jar apktool.jar resources <apk-file>
```

**Output fields:** `packageName, packageId, typeCounts{typeName->count}, locales[], totalEntries`

### security — Security report with risk score

```bash
java -jar apktool.jar security <apk-file>
```

**Output fields:** `dangerousPermissions[], highRiskComponents[], debuggable, allowBackup, usesCleartextTraffic, findings[], riskScore(0-100)`

### api-surface — Exported components and intent filters

```bash
java -jar apktool.jar api-surface <apk-file>
```

**Output fields:** `exportedActivities[], exportedServices[], exportedReceivers[], exportedProviders[], intentFilters[{component, componentType, actions[], categories[], dataSchemes[]}], totalExportedComponents`

### signing — APK signing certificate info

```bash
java -jar apktool.jar signing <apk-file>
```

**Output fields:** `v1Scheme, v2Scheme, v3Scheme, certificates[{subject, issuer, serialNumber, notBefore, notAfter, signatureAlgorithm, sha256, sha1, md5}], signingCertificateDigest`

### analyze — Comprehensive one-shot analysis

```bash
java -jar apktool.jar analyze <apk-file>
```

**Output fields:** `summary, manifest, security, apiSurface, resources, signing, structure` (all of the above combined)

---

## Search Commands

### search — Search APK content by regex

```bash
java -jar apktool.jar search <apk-file> [pattern] -t <type>
```

| Option | Description |
|--------|-------------|
| `-t, --type` | Search type: `strings`, `classes`, `methods` (default: classes) |

**Pattern:** Java regex syntax. Default: `.*` (match all)

**Output fields:** `query, type, totalMatches, matches[{name, value, source}]`

**Examples:**
```bash
java -jar apktool.jar search app.apk "LoginActivity" -t classes
java -jar apktool.jar search app.apk "http.*" -t strings
java -jar apktool.jar search app.apk "onCreate" -t methods
```

---

## Diff & Structure Commands

### diff — Compare two APKs

```bash
java -jar apktool.jar diff <apk1> <apk2>
```

**Output fields:** `addedPermissions[], removedPermissions[], addedActivities[], removedActivities[], addedServices[], removedServices[], addedDexFiles[], removedDexFiles[], addedNativeLibs[], removedNativeLibs[], versionCodeChange, versionNameChange, targetSdkChange`

### structure — Code structure overview

```bash
java -jar apktool.jar structure <apk-file>
```

**Output fields:** `totalClasses, totalMethods, totalFields, packageCounts{}, topClasses[], dexCount, dexClassCounts{}`

---

## AI & Service Commands

### ai — Generate LLM analysis prompts

```bash
java -jar apktool.jar ai <apk-file> -a <action>
```

| Option | Description |
|--------|-------------|
| `-a, --action` | Action: `explain`, `security-review`, `summarize` (default: explain) |

**Output:** Plain text LLM prompt

### serve — Start HTTP API server

```bash
java -jar apktool.jar serve [-p <port>]
```

| Option | Description |
|--------|-------------|
| `-p, --port` | Port number (default: 8080) |

**API Endpoints:**
- `GET /api/v1/health` — Health check
- `GET /api/v1/info?apk=<path>` — APK summary
- `GET /api/v1/manifest?apk=<path>` — Manifest info
- `GET /api/v1/permissions?apk=<path>` — Permissions
- `GET /api/v1/security?apk=<path>` — Security report
- `GET /api/v1/search?apk=<path>&type=<type>&pattern=<pattern>` — Search
- `GET /api/v1/diff?apk1=<path>&apk2=<path>` — Diff
- `GET /api/v1/resources?apk=<path>` — Resource summary
