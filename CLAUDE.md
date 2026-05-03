# AI-Apktool Skills

AI-native Android reverse engineering skills for Claude Code. Provides 5 skills covering APK analysis, security auditing, and code exploration.

## Quick Start

```bash
# Full analysis of an APK
java -jar apktool.jar analyze app.apk

# Quick info
java -jar apktool.jar info app.apk

# Security audit
java -jar apktool.jar security app.apk

# Compare two versions
java -jar apktool.jar diff app_v1.apk app_v2.apk

# Search for patterns
java -jar apktool.jar search app.apk "password" -t strings

# Generate AI prompts
java -jar apktool.jar ai app.apk -a security-review

# JSON help catalog
java -jar apktool.jar help --format json
```

## Skills

| Skill | Description | When to Use |
|-------|-------------|-------------|
| `quick-analysis` | Fast APK assessment | First encounter with an APK |
| `security-audit` | Comprehensive security audit | Vulnerability assessment, OWASP compliance |
| `compare` | Version comparison | Checking changes between app versions |
| `reverse` | Full reverse engineering | Deep analysis, modification, malware investigation |
| `reference` | CLI command reference | Looking up exact syntax or output format |

## CLI Command Reference

### Core Commands
| Command | Description |
|---------|-------------|
| `decode` / `d` | Decode APK to smali/resources |
| `build` / `b` | Build APK from decoded directory |
| `install-framework` / `if` | Install framework APK |
| `clean-frameworks` / `cf` | Clean framework files |
| `list-frameworks` / `lf` | List framework files |
| `publicize-resources` / `pr` | Make resources public in ARSC |

### Analysis Commands (JSON output)
| Command | Description | Key Fields |
|---------|-------------|------------|
| `info` | APK metadata summary | package, version, SDK, component counts |
| `manifest` | Full AndroidManifest.xml | permissions, components, flags |
| `permissions` | Permission list | all declared permissions |
| `activities` | Activity components | name, exported, intentFilters |
| `services` | Service components | name, exported, intentFilters |
| `receivers` | BroadcastReceiver components | name, exported, intentFilters |
| `providers` | ContentProvider components | name, exported, intentFilters |
| `components` | All components in one | activities, services, receivers, providers |
| `sdk-info` | SDK version requirements | minSdk, targetSdk, maxSdk |
| `resources` | Resource table summary | typeCounts, locales, totalEntries |
| `security` | Security report + risk score | dangerousPermissions, riskScore(0-100) |
| `api-surface` | Exported components + intent filters | totalExportedComponents |
| `signing` | APK signing certificate | subject, fingerprints, signing schemes |
| `strings` | Extract strings from APK | all strings with pattern filter |
| `dex-list` | List DEX files | dexCount, dexFiles |
| `locales` | Supported locales | locale strings from resource table |
| `native-libs` | Native libraries | architectures, libsByArch |
| `dex-info` | Per-DEX statistics | classes, methods, fields per DEX |
| `analyze` | Comprehensive one-shot analysis | all of the above combined |

### Search Commands
| Command | Description |
|---------|-------------|
| `search` | Search strings/classes/methods by regex (`-t strings|classes|methods`) |
| `strings` | Extract strings with optional pattern filter (`-p pattern`) |

### Diff & Structure Commands
| Command | Description |
|---------|-------------|
| `diff` | Compare two APKs (permissions, components, version changes) |
| `structure` | Code structure overview (classes, methods, fields, packages) |

### AI & Service Commands
| Command | Description |
|---------|-------------|
| `ai` | Generate LLM-ready prompts (`-a explain|security-review|summarize`) |
| `serve` | Start HTTP API server (`-p port`, default 8080) |

## Output Format

All analysis commands output JSON to stdout. Use `jq` or similar tools to parse:

```bash
java -jar apktool.jar info app.apk | jq '.packageName'
java -jar apktool.jar security app.apk | jq '.riskScore'
java -jar apktool.jar api-surface app.apk | jq '.exportedActivities[].name'
java -jar apktool.jar ai app.apk -a context | jq '.'
```

## HTTP API Endpoints (serve command)

### Analysis Endpoints (GET)
- `/api/v1/health` — Health check
- `/api/v1/info?apk=<path>` — APK metadata
- `/api/v1/manifest?apk=<path>` — AndroidManifest.xml
- `/api/v1/permissions?apk=<path>` — Permissions list
- `/api/v1/activities?apk=<path>` — Activities
- `/api/v1/services?apk=<path>` — Services
- `/api/v1/receivers?apk=<path>` — Broadcast receivers
- `/api/v1/providers?apk=<path>` — Content providers
- `/api/v1/components?apk=<path>` — All components
- `/api/v1/sdk-info?apk=<path>` — SDK versions
- `/api/v1/resources?apk=<path>` — Resource summary
- `/api/v1/security?apk=<path>` — Security report
- `/api/v1/api-surface?apk=<path>` — Exported components
- `/api/v1/signing?apk=<path>` — Signing info
- `/api/v1/structure?apk=<path>` — Code structure
- `/api/v1/analyze?apk=<path>` — Full analysis
- `/api/v1/ai?apk=<path>&action=<explain|security-review|summarize|context>` — AI prompts/context
- `/api/v1/search?apk=<path>&type=<type>&pattern=<pattern>` — Search
- `/api/v1/strings?apk=<path>&pattern=<pattern>` — String extraction
- `/api/v1/dex-list?apk=<path>` — DEX file list
- `/api/v1/locales?apk=<path>` — Supported locales
- `/api/v1/native-libs?apk=<path>` — Native libraries
- `/api/v1/dex-info?apk=<path>` — Per-DEX statistics
- `/api/v1/diff?apk1=<path>&apk2=<path>` — APK comparison
- `/api/v1/list-frameworks` — List frameworks

### Operational Endpoints (POST)
- `/api/v1/decode?apk=<path>&output=<dir>` — Decode APK
- `/api/v1/build?dir=<path>&output=<apk>` — Build APK
- `/api/v1/install-framework?apk=<path>` — Install framework
- `/api/v1/clean-frameworks` — Clean frameworks
- `/api/v1/publicize-resources?arsc=<path>` — Publicize resources

## Build

```bash
./gradlew build
# CLI jar at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```
