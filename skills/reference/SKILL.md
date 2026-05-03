---
name: reference
description: Complete CLI command reference for AI-Apktool. Use as a lookup when you need the exact syntax, options, or output format of any command.
autoInvoke: true
---

# AI-Apktool Command Reference

Complete reference for all AI-Apktool CLI commands.

## Universal Patterns

All analysis commands output JSON to stdout:
```bash
java -jar apktool.jar <command> <apk-file>
```

JSON Help Catalog:
```bash
java -jar apktool.jar help --format json
```

## Core Commands

| Command | Description |
|---------|-------------|
| `decode` / `d` | Decode APK to smali/resources |
| `build` / `b` | Build APK from decoded directory |
| `install-framework` / `if` | Install framework APK |
| `clean-frameworks` / `cf` | Clean framework files |
| `list-frameworks` / `lf` | List framework files |
| `publicize-resources` / `pr` | Make resources public in ARSC |

## Analysis Commands (JSON output)

| Command | Key Fields |
|---------|------------|
| `info` | package, version, SDK, component counts |
| `manifest` | permissions, components, flags |
| `permissions` | all declared permissions |
| `activities` / `services` / `receivers` / `providers` | name, exported, intentFilters |
| `components` | activities, services, receivers, providers |
| `sdk-info` | minSdk, targetSdk, maxSdk |
| `resources` | typeCounts, locales, totalEntries |
| `security` | dangerousPermissions, riskScore(0-100) |
| `api-surface` | totalExportedComponents, intentFilters |
| `signing` | subject, fingerprints, signing schemes |
| `strings` | all strings from DEX and resources with pattern filter |
| `dex-list` | dexCount, dexFiles |
| `locales` | locale strings from resource table |
| `native-libs` | architectures, libsByArch |
| `dex-info` | classes, methods, fields per DEX |
| `apk-info` | version, sdkInfo, usesFramework, featureFlags from decoded dir |
| `resource-packages` | packageGroups, packageGroupCount |
| `lib-frame-packages` | libPackageIds, framePackageIds |
| `analyze` | all of the above combined |

## Search Commands

```bash
java -jar apktool.jar search <apk-file> [pattern] -t <type>
# type: strings, classes, methods (default: classes)
# pattern: Java regex (default: .*)

java -jar apktool.jar strings <apk-file> [-p <pattern>]
# shortcut for search -t strings
```

## Diff & Structure

```bash
java -jar apktool.jar diff <apk1> <apk2>
java -jar apktool.jar structure <apk-file>
```

## AI & Service

```bash
java -jar apktool.jar ai <apk-file> -a <action>
# action: explain, security-review, summarize

java -jar apktool.jar serve [-p <port>]
# default port: 8080
```

## HTTP API Endpoints (serve command)

- `GET /api/v1/health`
- `GET /api/v1/info?apk=<path>`
- `GET /api/v1/manifest?apk=<path>`
- `GET /api/v1/permissions?apk=<path>`
- `GET /api/v1/activities?apk=<path>`
- `GET /api/v1/services?apk=<path>`
- `GET /api/v1/receivers?apk=<path>`
- `GET /api/v1/providers?apk=<path>`
- `GET /api/v1/components?apk=<path>`
- `GET /api/v1/sdk-info?apk=<path>`
- `GET /api/v1/resources?apk=<path>`
- `GET /api/v1/security?apk=<path>`
- `GET /api/v1/api-surface?apk=<path>`
- `GET /api/v1/signing?apk=<path>`
- `GET /api/v1/structure?apk=<path>`
- `GET /api/v1/analyze?apk=<path>`
- `GET /api/v1/ai?apk=<path>&action=<explain|security-review|summarize>`
- `GET /api/v1/search?apk=<path>&type=<type>&pattern=<pattern>`
- `GET /api/v1/diff?apk1=<path>&apk2=<path>`
- `GET /api/v1/strings?apk=<path>&pattern=<pattern>`
- `GET /api/v1/dex-list?apk=<path>`
- `GET /api/v1/locales?apk=<path>`
- `GET /api/v1/native-libs?apk=<path>`
- `GET /api/v1/dex-info?apk=<path>`
- `GET /api/v1/apk-info?dir=<path>`
- `GET /api/v1/resource-packages?apk=<path>`
- `GET /api/v1/lib-frame-packages?apk=<path>`
- `POST /api/v1/decode?apk=<path>&output=<dir>`
- `POST /api/v1/build?dir=<path>&output=<apk>`
- `POST /api/v1/install-framework?apk=<path>`
- `POST /api/v1/clean-frameworks`
- `GET /api/v1/list-frameworks`
- `POST /api/v1/publicize-resources?arsc=<path>`
