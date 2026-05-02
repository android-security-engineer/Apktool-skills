# AI-Apktool

AI-Apktool is an AI-native Android reverse engineering platform built on Apktool. It provides a comprehensive CLI and HTTP API for APK analysis, security auditing, and code exploration.

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
java -jar apktool.jar search app.apk "LoginActivity" -t classes

# Generate AI prompts
java -jar apktool.jar ai app.apk -a explain
java -jar apktool.jar ai app.apk -a security-review

# JSON help catalog
java -jar apktool.jar help --format json
```

## Command Reference

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
| `analyze` | Comprehensive one-shot analysis | all of the above combined |

### Search Commands
| Command | Description |
|---------|-------------|
| `search` | Search strings/classes/methods by regex (`-t strings|classes|methods`) |

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
# Get package name only
java -jar apktool.jar info app.apk | jq '.packageName'

# Get risk score
java -jar apktool.jar security app.apk | jq '.riskScore'

# Find exported activities
java -jar apktool.jar api-surface app.apk | jq '.exportedActivities[].name'
```

## Architecture

```
apktool-cli/     → CLI entry point (Main.java)
apktool-lib/     → Core library
  analyze/       → ApkAnalyzer, data models (ApkSummary, SecurityReport, etc.)
  search/        → ApkSearcher (strings, classes, methods)
  ai/            → AiPromptBuilder, AiContext
  output/        → JsonOutput, CommandRegistry
apktool-serve/   → HTTP API server (Javalin)
```

## Build

```bash
./gradlew build
# CLI jar at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```

## Skills

See `.claude/skills/` directory for AI agent workflow guides:
- `apktool-quick-analysis.md` — Quick APK assessment workflow
- `apktool-security-audit.md` — Comprehensive security audit
- `apktool-compare.md` — Version comparison workflow
- `apktool-reverse.md` — Full reverse engineering workflow
- `apktool-reference.md` — Complete command reference for AI agents
