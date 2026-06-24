---
name: reference
description: Complete CLI command reference for AI-Apktool. Use as a lookup when you need the exact syntax, options, output fields, or HTTP endpoint of any command.
autoInvoke: true
---

# AI-Apktool Command Reference

Authoritative reference for every AI-Apktool CLI command, flag, output field, and HTTP endpoint. This is the lookup skill — consult it whenever you are unsure of exact syntax or which command emits a given field.

## Conventions

- **Invocation**: `apktool <command> <apk-file> [options]`. The unified `apktool` wrapper invokes the shaded CLI jar.
- **Output**: every *analysis* command prints a single JSON object to **stdout**. Diagnostics go to stderr. Pipe into `jq` to extract fields.
- **Regex**: all `pattern` arguments are **Java regular expressions** (`java.util.regex.Pattern`), case-sensitive by default. Use `(?i)` for case-insensitive matching, e.g. `(?i)password`.
- **Exit codes**: `0` on success, non-zero on failure (bad arguments, missing file, parse error). The error message is printed to stderr.
- **Machine-readable catalog**: `apktool help --format json` emits the full command catalog (names, aliases, descriptions, usage, output format, examples) — the best source of truth for tooling.
- **Targets**: most commands take an `.apk` file. A few read a *decoded directory* instead (noted below): `apk-info`. `diff` takes two APKs. `run` takes a script file; `pipe` reads stdin.

## Core Commands

| Command | Alias | Target | Description |
|---------|-------|--------|-------------|
| `decode` | `d` | apk | Decode an APK to smali + resources |
| `build` | `b` | dir | Build an APK from a decoded directory |
| `install-framework` | `if` | apk | Install a framework APK for resource decoding |
| `clean-frameworks` | `cf` | — | Remove installed framework files |
| `list-frameworks` | `lf` | — | List installed framework files |
| `publicize-resources` | `pr` | arsc | Make all resources public in an ARSC file |

### `decode` options

| Flag | Long | Meaning |
|------|------|---------|
| `-o` | `--output` | Output directory |
| `-f` | `--force` | Overwrite existing output directory |
| `-s` | `--no-src` | Do not decode sources (resources only) |
| `-r` | `--no-res` | Do not decode resources (smali only) |
| `-a` | `--all-src` | Decode all sources, including unknown DEX files |
| | `--only-manifest` | Decode only `AndroidManifest.xml` |
| | `--no-debug-info` | Strip `.local`/`.param`/`.line` debug directives |
| | `--no-assets` | Do not copy the `assets/` directory |
| | `--keep-broken-res` | Keep resources that fail to decode |
| | `--ignore-raw-values` | Ignore raw attribute values in XML resources |
| | `--match-original` | Keep files closest to original (best for re-signing) |
| | `--res-resolve-mode` | Resource resolve mode: `remove` \| `dummy` \| `keep` |
| `-p` | `--frame-path` | Framework directory to use |
| `-t` | `--frame-tag` | Framework tag to use |
| `-l` | `--lib` | Shared library in `package:path` form |

### `build` options

| Flag | Long | Meaning |
|------|------|---------|
| `-o` | `--output` | Output APK path |
| `-f` | `--force` | Overwrite / skip change detection |
| | `--no-apk` | Produce files but do not package the APK |
| | `--no-crunch` | Disable resource crunching |
| | `--copy-original` | Copy original `AndroidManifest.xml` and `META-INF` |
| | `--debuggable` | Set `android:debuggable="true"` in the built APK |
| | `--net-sec-conf` | Inject a permissive network security config |
| | `--aapt` | Path to an external `aapt`/`aapt2` binary |
| `-p` | `--frame-path` | Framework directory to use |

## Analysis Commands (JSON output)

| Command | Target | Key fields |
|---------|--------|------------|
| `info` | apk | packageName, versionName, versionCode, fileSize, component counts |
| `manifest` | apk | permissions, components, sdkInfo, flags (structured) |
| `manifest-xml` | apk | manifestXml (full decoded XML as text) |
| `permissions` | apk | all declared permissions |
| `permission-detail` | apk | dangerousCount, normalCount, customCount, permissions[] |
| `activities` | apk | name, exported, intentFilters |
| `services` | apk | name, exported, intentFilters |
| `receivers` | apk | name, exported, intentFilters |
| `providers` | apk | name, exported, intentFilters |
| `components` | apk | activities, services, receivers, providers (all at once) |
| `api-surface` | apk | totalExportedComponents, exported components + intentFilters |
| `sdk-info` | apk | minSdk, targetSdk, maxSdk |
| `version` | apk | packageName, versionCode, versionName |
| `apk-version` | apk | packageName, versionCode, versionName (alias of `version`) |
| `apk-info` | **dir** | version, sdkInfo, usesFramework, featureFlags (reads `apktool.yml`) |
| `resources` | apk | typeCounts, locales, totalEntries |
| `resource-packages` | apk | packageGroups, packageGroupCount |
| `lib-frame-packages` | apk | libPackageIds, framePackageIds |
| `uses-libs` | apk | usesLibraries[] |
| `locales` | apk | locale strings from the resource table |
| `native-libs` | apk | architectures, libsByArch |
| `security` | apk | dangerousPermissions, highRiskComponents, debuggable, allowBackup, usesCleartextTraffic, findings[], riskScore (0-100) |
| `signing` | apk | v1/v2/v3 signing, certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}] |
| `manifest-flags` | apk | debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig |
| `dex-list` | apk | dexCount, dexFiles |
| `dex-info` | apk | per-DEX classes, methods, fields |
| `dex-strings` | apk | totalStrings, strings[] (DEX only) |
| `class-list` | apk | totalClasses, classes[] |
| `class-info` | apk | superClass, methods[], fields[], interfaces[] (needs `<class>`) |
| `method-search` | apk | totalMatches, methods[{className, methodName, returnType}] (needs `-p`) |
| `field-search` | apk | totalMatches, fields[{className, fieldName, type}] (needs `-p`) |
| `inheritance` | apk | className, inheritanceChain[] (needs `<class>`) |
| `structure` | apk | class/method/field counts, package distribution |
| `strings` | apk | all strings from DEX + resources (optional `-p` filter) |
| `file-list` | apk | totalFiles, totalSize, entries[] |
| `file-hash` | apk | sha256, sha1, md5 |
| `asset-list` | apk | hasAssets, totalAssets, assets[] |
| `analyze` | apk | everything above combined in one object |

### Positional-argument commands

A few analysis commands need a second positional argument:

```bash
apktool class-info   app.apk com.example.MyActivity
apktool inheritance  app.apk com.example.MyActivity
```

`method-search` / `field-search` take a regex via `-p`:

```bash
apktool method-search app.apk -p 'encrypt|decrypt|Cipher'
apktool field-search  app.apk -p '(?i)apikey|secret|token'
```

## Search Commands

```bash
apktool search <apk-file> [pattern] -t <type>
#   -t / --type : strings | classes | methods   (default: classes)
#   pattern     : Java regex                     (default: .*)

apktool strings <apk-file> [-p <pattern>]
#   shortcut for `search -t strings`; -p / --pattern filters results
```

## Diff & Structure

```bash
apktool diff <apk1> <apk2>
#   addedPermissions[], removedPermissions[], added/removed components,
#   addedDexFiles[], addedNativeLibs[], versionCodeChange, versionNameChange, targetSdkChange

apktool structure <apk-file>
#   class/method/field counts and package distribution
```

## Batch Scripting (`run` / `pipe`)

Run many analysis commands against a **single shared parse** of one APK. All commands reuse one `ApkAnalyzer`/`ApkSearcher` instance, so this is far faster than invoking the CLI N times. **Error isolation**: a failure in one command is captured in the result and does not stop the others.

```bash
apktool run <script.json>        # read commands from a JSON file
apktool pipe [apk] < script.json # read the JSON script from stdin
```

Script format — commands may be bare strings or `{command, params}` objects:

```json
{
  "apk": "app.apk",
  "commands": [
    "info",
    "security",
    "signing",
    { "command": "search", "params": { "type": "strings", "pattern": "password|secret|key" } },
    { "command": "method-search", "params": { "pattern": "Cipher|DexClassLoader" } },
    "analyze"
  ]
}
```

The output is a single JSON object: `{ apk, totalCommands, results: { <command>: <result-or-error> } }`. When the same command appears more than once, its key is suffixed with an index. For `pipe`, the `apk` field in the JSON is optional if you pass the APK path as the first positional argument.

## AI & Service

```bash
apktool ai <apk-file> -a <action>
#   -a / --action : explain | security-review | summarize | context   (default: explain)
#     explain          – natural-language prompt describing the app for an LLM
#     security-review  – prompt geared toward a security assessment
#     summarize        – concise summary prompt
#     context          – structured AiContext JSON (facts, not a prompt) to feed an agent

apktool serve [-p <port>]
#   -p / --port : HTTP API port (default: 8080)
```

## HTTP API Endpoints (`serve`)

Base path: `/api/v1`. Pass the APK with `?apk=<path>` unless noted.

**Analysis (GET):**
`health`, `info`, `manifest`, `manifest-xml`, `permissions`, `permission-detail`, `activities`, `services`, `receivers`, `providers`, `components`, `api-surface`, `sdk-info`, `version`, `resources`, `resource-packages`, `lib-frame-packages`, `uses-libs`, `locales`, `native-libs`, `security`, `signing`, `manifest-flags`, `dex-list`, `dex-info`, `dex-strings`, `class-list`, `structure`, `file-list`, `file-hash`, `asset-list`, `analyze`.

Parameterized GET endpoints:

```
GET /api/v1/search?apk=<path>&type=<type>&pattern=<pattern>
GET /api/v1/strings?apk=<path>&pattern=<pattern>
GET /api/v1/class-info?apk=<path>&class=<name>
GET /api/v1/inheritance?apk=<path>&class=<name>
GET /api/v1/method-search?apk=<path>&pattern=<pattern>
GET /api/v1/field-search?apk=<path>&pattern=<pattern>
GET /api/v1/apk-info?dir=<path>
GET /api/v1/diff?apk1=<path>&apk2=<path>
GET /api/v1/ai?apk=<path>&action=<explain|security-review|summarize|context>
GET /api/v1/list-frameworks
```

**Operations (POST):**

```
POST /api/v1/decode?apk=<path>&output=<dir>
POST /api/v1/build?dir=<path>&output=<apk>
POST /api/v1/install-framework?apk=<path>
POST /api/v1/clean-frameworks
POST /api/v1/publicize-resources?arsc=<path>
```

> The CLI and the HTTP API are thin wrappers over the same library methods, so JSON field names match across both surfaces.

## Help & Version

```bash
apktool help                 # human-readable help
apktool help --format json   # machine-readable command catalog
apktool version              # tool version (alias: -v)
```
