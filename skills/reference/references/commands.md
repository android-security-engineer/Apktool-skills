# Analysis Command Reference

Full output fields for every analysis command. All print a single JSON object to stdout. See [cli-flags.md](cli-flags.md) for core decode/build options, [scripting.md](scripting.md) for `run`/`pipe`/`ai`, and [http-api.md](http-api.md) for the REST surface.

## Metadata & components

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

## Resources & files

| Command | Target | Key fields |
|---------|--------|------------|
| `resources` | apk | typeCounts, locales, totalEntries |
| `resource-packages` | apk | packageGroups, packageGroupCount |
| `lib-frame-packages` | apk | libPackageIds, framePackageIds |
| `uses-libs` | apk | usesLibraries[] |
| `locales` | apk | locale strings from the resource table |
| `native-libs` | apk | architectures, libsByArch |
| `file-list` | apk | totalFiles, totalSize, entries[] |
| `file-hash` | apk | sha256, sha1, md5 |
| `asset-list` | apk | hasAssets, totalAssets, assets[] |

## Security

| Command | Target | Key fields |
|---------|--------|------------|
| `security` | apk | dangerousPermissions, highRiskComponents, debuggable, allowBackup, usesCleartextTraffic, findings[], riskScore (0-100) |
| `signing` | apk | v1/v2/v3 signing, certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}] |
| `manifest-flags` | apk | debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig |

## DEX & code

| Command | Target | Key fields |
|---------|--------|------------|
| `dex-list` | apk | dexCount, dexFiles |
| `dex-info` | apk | per-DEX classes, methods, fields |
| `dex-strings` | apk | totalStrings, strings[] (DEX only) |
| `class-list` | apk | totalClasses, classes[] |
| `class-info` | apk | superClass, methods[], fields[], interfaces[] (needs `<class>`) |
| `method-search` | apk | totalMatches, methods[{className, methodName, returnType}] (needs `-p`) |
| `field-search` | apk | totalMatches, fields[{className, fieldName, type}] (needs `-p`) |
| `inheritance` | apk | className, inheritanceChain[] (needs `<class>`) |
| `structure` | apk | class/method/field counts, package distribution |

## Strings, search, diff, combined

| Command | Target | Key fields / usage |
|---------|--------|--------------------|
| `strings` | apk | all strings from DEX + resources; optional `-p <pattern>` filter |
| `search` | apk | `search <apk> [pattern] -t strings\|classes\|methods` (default type: classes, default pattern: `.*`) |
| `diff` | 2× apk | addedPermissions[], removedPermissions[], added/removed components, addedDexFiles[], addedNativeLibs[], versionCodeChange, versionNameChange, targetSdkChange |
| `analyze` | apk | everything above combined in one object |

## Commands that take a second positional argument

```bash
apktool class-info   app.apk com.example.MyActivity
apktool inheritance  app.apk com.example.MyActivity
```

`method-search` / `field-search` take a Java regex via `-p`:

```bash
apktool method-search app.apk -p 'encrypt|decrypt|Cipher'
apktool field-search  app.apk -p '(?i)apikey|secret|token'
```
