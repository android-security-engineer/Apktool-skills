# AI-Apktool Skills

> AI-native Android reverse engineering for Claude Code — 11 skills and 50+ CLI commands for APK analysis, security auditing, and code exploration.

**English** | [简体中文](README.zh-CN.md)

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE.md)
[![Skills](https://img.shields.io/badge/skills-11-green.svg)](#skills)
[![CLI Commands](https://img.shields.io/badge/CLI%20commands-51-orange.svg)](#cli-command-reference)

AI-Apktool turns [Apktool](https://apktool.org) into an AI-native reverse engineering platform. Every analysis capability emits structured **JSON**, so Claude Code (or any LLM agent) can reason over an APK without scraping human-readable logs. It ships as a set of Claude Code **Skills** plus a unified `apktool` CLI and an optional HTTP API.

---

## Table of Contents

- [Highlights](#highlights)
- [Quick Start](#quick-start)
- [Skills](#skills)
- [Installation](#installation)
- [Usage](#usage)
- [CLI Command Reference](#cli-command-reference)
- [HTTP API](#http-api)
- [Architecture](#architecture)
- [Building](#building)
- [Acknowledgements](#acknowledgements)
- [License](#license)

---

## Highlights

- **11 Skills** covering the full APK workflow — from a 5-second triage to deep DEX inheritance tracing and malware hunting.
- **51 CLI commands** across 7 categories — all analysis commands output JSON, ready for `jq` or an LLM.
- **Batch scripting** — run dozens of analysis commands in one pass over a single decoded APK with `run` / `pipe`.
- **HTTP API** — expose the same capabilities over REST with `apktool serve`.
- **Zero log scraping** — structured output everywhere, designed for agents.

---

## Quick Start

```bash
# Build the unified CLI
./gradlew build shadowJar

# Full one-shot analysis of an APK
apktool analyze app.apk

# Quick info
apktool info app.apk

# Security audit with a 0-100 risk score
apktool security app.apk

# Compare two versions
apktool diff app_v1.apk app_v2.apk

# Search for patterns
apktool search app.apk "password" -t strings

# Generate an LLM-ready prompt
apktool ai app.apk -a security-review

# Machine-readable help catalog
apktool help --format=json
```

All analysis commands print JSON to stdout, so you can pipe straight into `jq`:

```bash
apktool info app.apk      | jq '.packageName'
apktool security app.apk  | jq '.riskScore'
apktool api-surface app.apk | jq '.exportedActivities[].name'
```

---

## Skills

| Skill | Description | When to use |
|-------|-------------|-------------|
| `quick-analysis` | Fast APK assessment | First encounter with an APK |
| `security-audit` | Comprehensive security audit | Vulnerability assessment, OWASP compliance |
| `compare` | Version comparison | Checking changes between app versions |
| `reverse` | Full reverse engineering | Deep analysis, modification, malware investigation |
| `reference` | CLI command reference | Looking up exact syntax or output format |
| `decode-build` | Decode & build workflow | Decoding an APK, rebuilding, framework management |
| `dex-deep-dive` | DEX deep analysis | Class/method/field exploration, inheritance tracing |
| `network-analysis` | Network communication analysis | Finding endpoints, URLs, cleartext traffic |
| `malware-hunt` | Malware indicator hunting | Suspicious APK investigation, malicious patterns |
| `resource-explorer` | Resource & file exploration | Resources, locales, assets, file structure |
| `signing-verify` | Signing verification | Certificate analysis, signing scheme assessment |

---

## Installation

### Prerequisites

- [Claude Code](https://claude.ai/code) installed
- JDK 17+ (to build the CLI)

### Build the CLI

```bash
git clone https://github.com/android-security-engineer/Apktool-skills.git
cd Apktool-skills
./gradlew build shadowJar
# The unified wrapper is ./apktool — add it to your PATH if you like
```

### Option 1 — Install as a Claude Code plugin

```bash
# Add the marketplace
claude config add marketplace ai-apktool https://github.com/android-security-engineer/Apktool-skills.git

# Install the plugin
claude plugin install ai-apktool@ai-apktool
```

### Option 2 — Install the Skills manually

```bash
git clone https://github.com/android-security-engineer/Apktool-skills.git ~/.claude/skills/ai-apktool
```

### Verify

```bash
claude skill list
# Expected:
#   ai-apktool:quick-analysis
#   ai-apktool:security-audit
#   ai-apktool:compare
#   ai-apktool:reverse
#   ai-apktool:reference
#   ai-apktool:decode-build
#   ai-apktool:dex-deep-dive
#   ai-apktool:network-analysis
#   ai-apktool:malware-hunt
#   ai-apktool:resource-explorer
#   ai-apktool:signing-verify
```

---

## Usage

### Automatic

Once installed, Claude Code automatically recognizes APK-related tasks and invokes the right Skill.

### Manual invocation

```
/quick-analysis  analyze this APK: /path/to/app.apk
/security-audit  run a security audit on app.apk
/compare         compare app_v1.apk and app_v2.apk
/reverse         reverse engineer app.apk
/reference       show the usage of the search command
```

### A typical workflow

```
User: analyze this APK file

AI: [uses the quick-analysis skill]
1. runs: apktool analyze /path/to/app.apk
2. reports findings:
   - package: com.example.app v2.1.0
   - risk score: 35/100 (medium)
   - 3 dangerous permissions: CAMERA, RECORD_AUDIO, ACCESS_FINE_LOCATION
   - 2 unprotected exported activities
   - signer: CN=Developer, O=Example Inc
```

---

## CLI Command Reference

51 commands across 7 categories. Run `apktool help --format=json` for the full machine-readable catalog.

### Core operations (6)

| Command | Description |
|---------|-------------|
| `decode` / `d` | Decode an APK to smali + resources |
| `build` / `b` | Build an APK from a decoded directory |
| `install-framework` / `if` | Install a framework APK |
| `clean-frameworks` / `cf` | Clean framework files |
| `list-frameworks` / `lf` | List installed framework files |
| `publicize-resources` / `pr` | Make resources public in the ARSC |

### Analysis (38, JSON output)

| Group | Commands |
|-------|----------|
| Metadata | `info`, `manifest`, `manifest-xml`, `sdk-info`, `version`, `apk-version`, `apk-info` |
| Components | `activities`, `services`, `receivers`, `providers`, `components`, `api-surface` |
| Permissions | `permissions`, `permission-detail` |
| Security | `security`, `signing`, `manifest-flags` |
| DEX & code | `dex-list`, `dex-info`, `dex-strings`, `class-list`, `class-info`, `method-search`, `field-search`, `inheritance`, `structure` |
| Resources & files | `resources`, `resource-packages`, `lib-frame-packages`, `uses-libs`, `locales`, `native-libs`, `file-list`, `file-hash`, `asset-list` |
| Combined | `analyze` (one-shot, everything) |

### Search (1)

| Command | Description |
|---------|-------------|
| `search` | Search strings / classes / methods by regex (`-t strings\|classes\|methods`); `strings` extracts with `-p <pattern>` |

### Scripting (2)

| Command | Description |
|---------|-------------|
| `run` | Run a JSON script of analysis commands against an APK (single shared parse) |
| `pipe` | Read JSON commands from stdin and execute them against an APK |

Example script (`analysis.json`):

```json
{
  "apk": "app.apk",
  "commands": [
    "info",
    "security",
    "signing",
    { "command": "search", "params": { "type": "strings", "pattern": "password|secret|key" } },
    "analyze"
  ]
}
```

```bash
apktool run analysis.json
echo '{"apk":"app.apk","commands":["info","security"]}' | apktool pipe app.apk
```

Commands run against a single shared parse of the APK, and a failure in one command never stops the others (error isolation).

### AI & service (2)

| Command | Description |
|---------|-------------|
| `ai` | Generate LLM-ready prompts (`-a explain\|security-review\|summarize\|context`) |
| `serve` | Start the HTTP API server (`-p <port>`, default 8080) |

---

## HTTP API

```bash
apktool serve -p 8080
curl 'http://localhost:8080/api/v1/info?apk=/path/to/app.apk'
curl 'http://localhost:8080/api/v1/security?apk=/path/to/app.apk' | jq '.riskScore'
```

Nearly every analysis command has a matching `GET /api/v1/<command>?apk=<path>` endpoint, plus `POST` endpoints for operations (`decode`, `build`, `install-framework`, …). See [CLAUDE.md](CLAUDE.md#http-api-endpoints-serve-command) for the full endpoint list.

---

## Architecture

```
skills/
  quick-analysis/      — fast triage workflow
  security-audit/      — security audit workflow
  compare/             — version comparison workflow
  reverse/             — reverse engineering workflow
  reference/           — command reference
  decode-build/        — decode & build workflow
  dex-deep-dive/       — DEX deep analysis workflow
  network-analysis/    — network communication workflow
  malware-hunt/        — malware hunting workflow
  resource-explorer/   — resource exploration workflow
  signing-verify/      — signing verification workflow
brut.apktool/
  apktool-lib/         — core library (ApkAnalyzer, ApkSearcher, ApkDiff, ScriptRunner)
  apktool-cli/         — unified `apktool` CLI entry point (Main.java)
  apktool-serve/       — HTTP API server (Javalin)
  apktool-ai-cli/      — skill dispatch layer
.claude-plugin/
  plugin.json          — plugin metadata
  marketplace.json     — marketplace config
CLAUDE.md              — AI entry-point documentation
apktool                — unified CLI wrapper script
```

The library layer is the single source of truth: every CLI command, HTTP endpoint, and skill dispatch ultimately calls the same `ApkAnalyzer` / `ApkSearcher` / `ApkDiff` methods, guaranteeing consistent JSON across all surfaces.

---

## Building

```bash
./gradlew build shadowJar
# then use: apktool <command>
```

Requires JDK 17+. The `shadowJar` task produces a self-contained CLI jar that the `apktool` wrapper script invokes.

---

## Acknowledgements

AI-Apktool is built on top of [Apktool](https://apktool.org) by Connor Tumbleson and the iBotPeaches team. All decode/build/resource-handling capabilities come from that project — this repository layers an AI-native analysis, scripting, and HTTP surface on top. Huge thanks to the upstream maintainers.

---

## License

[Apache License 2.0](LICENSE.md) — consistent with upstream Apktool.
