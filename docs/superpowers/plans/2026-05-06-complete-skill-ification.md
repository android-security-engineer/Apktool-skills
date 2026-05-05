# AI-Apktool Complete Skill-ification Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 将 AI-Apktool 项目的所有能力零遗漏地炼化为 Skills，每个 Skill 对应一组 CLI 命令入口，形成完整的 AI 代理可调用能力矩阵。

**Architecture:** ApkAnalyzer/ApkDiff/ApkSearcher/AiPromptBuilder 的全部 34 个 public 方法已暴露为 35+ 个 CLI 命令和 HTTP 端点。现在需要：(1) 补齐缺失的 6 个 Skill 文档覆盖所有命令组合场景；(2) 更新现有 5 个 Skill 引用新增命令；(3) 更新 CLAUDE.md 和 reference Skill 反映完整能力矩阵；(4) 确保每个 Skill 的命令引用与 CommandRegistry 完全对齐。

**Tech Stack:** Java 17, Gradle 8.x, Javalin 5.6.3, dexlib2, baksmali/smali

**Risks:**
- Task 1-6 创建新 Skill 文档，无代码改动风险
- Task 7 修改现有 Skill 文档，需确保不破坏已有引用
- Task 8 修改 CLAUDE.md，需保持与 CommandRegistry 同步

---

### Task 1: 创建 decode-build Skill — 覆盖 APK 解码和构建工作流

**Depends on:** None
**Files:**
- Create: `skills/decode-build/SKILL.md`

- [ ] **Step 1: 创建 decode-build SKILL.md — 覆盖 decode/build/framework 全流程**

```markdown
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

AI-Apktool CLI must be available.

## Workflow

### Step 1: Decode APK

```bash
# Full decode (smali + resources)
java -jar apktool.jar decode app.apk -o decoded_app

# Decode resources only (no smali)
java -jar apktool.jar decode app.apk -o decoded_app -s

# Decode manifest only
java -jar apktool.jar decode app.apk -o decoded_app --only-manifest

# Force decode (overwrite existing directory)
java -jar apktool.jar decode app.apk -o decoded_app -f
```

### Step 2: Inspect Decoded Content

```bash
# Read decoded APK metadata
java -jar apktool.jar apk-info decoded_app

# View full manifest XML
java -jar apktool.jar manifest-xml app.apk
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
java -jar apktool.jar build decoded_app -o modified.apk

# Build with custom output
java -jar apktool.jar build decoded_app -o output/modified.apk
```

### Step 5: Framework Management

```bash
# Install framework APK (needed for proper resource decoding)
java -jar apktool.jar install-framework framework-res.apk

# List installed frameworks
java -jar apktool.jar list-frameworks

# Clean all frameworks
java -jar apktool.jar clean-frameworks

# Publicize resources in ARSC
java -jar apktool.jar publicize-resources resources.arsc
```

## Common Patterns

```bash
# Decode → Modify → Rebuild cycle
java -jar apktool.jar decode app.apk -o app_dir
# ... edit files in app_dir ...
java -jar apktool.jar build app_dir -o modified.apk

# Decode with framework for OEM ROMs
java -jar apktool.jar install-framework framework-res.apk
java -jar apktool.jar decode app.apk -o app_dir
```
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/decode-build/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/decode-build/SKILL.md && git commit -m "feat(skills): add decode-build skill covering decode/build/framework workflow"`

---

### Task 2: 创建 dex-deep-dive Skill — 覆盖 DEX 深度分析工作流

**Depends on:** None
**Files:**
- Create: `skills/dex-deep-dive/SKILL.md`

- [ ] **Step 1: 创建 dex-deep-dive SKILL.md — 覆盖 DEX/类/方法/字段深度分析**

```markdown
---
name: dex-deep-dive
description: Deep DEX analysis workflow for class, method, and field exploration. Use when you need to understand code structure, trace class hierarchies, or search for specific implementations.
autoInvoke: true
---

# DEX Deep Dive

Workflow for deep analysis of DEX bytecode — classes, methods, fields, and inheritance.

## When to Use

- Understanding app code architecture
- Tracing class inheritance hierarchies
- Searching for specific method implementations
- Finding field usage patterns
- Analyzing multi-DEX applications
- Reverse engineering specific functionality

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Get DEX Overview

```bash
# List all DEX files
java -jar apktool.jar dex-list app.apk

# Per-DEX statistics (classes, methods, fields)
java -jar apktool.jar dex-info app.apk

# Code structure overview
java -jar apktool.jar structure app.apk

# List all class names
java -jar apktool.jar class-list app.apk
```

### Step 2: Explore Specific Classes

```bash
# Get detailed class info (methods, fields, superclass, interfaces)
java -jar apktool.jar class-info app.apk com.example.MyActivity

# Get class inheritance chain
java -jar apktool.jar inheritance app.apk com.example.MyActivity
```

### Step 3: Search Methods and Fields

```bash
# Search methods by regex pattern
java -jar apktool.jar method-search2 app.apk -p 'onCreate'
java -jar apktool.jar method-search app.apk -p 'encrypt|decrypt|cipher'

# Search fields by regex pattern
java -jar apktool.jar field-search app.apk -p 'mContext'
java -jar apktool.jar field-search app.apk -p 'apiKey|secret|token'
```

### Step 4: Extract Strings

```bash
# All DEX strings
java -jar apktool.jar dex-strings app.apk

# All strings (DEX + resources)
java -jar apktool.jar strings app.apk

# Filter strings by pattern
java -jar apktool.jar strings app.apk -p 'http.*'
```

## Common Patterns

```bash
# Find all crypto-related code
java -jar apktool.jar method-search app.apk -p 'Cipher|SecretKey|MessageDigest|AES|RSA'

# Find all network-related classes
java -jar apktool.jar class-list app.apk | grep -i 'http\|url\|request\|api'

# Trace Activity lifecycle
java -jar apktool.jar method-search app.apk -p 'onCreate|onStart|onResume|onPause|onStop|onDestroy'

# Find hardcoded credentials
java -jar apktool.jar field-search app.apk -p 'password|secret|key|token|credential'
java -jar apktool.jar dex-strings app.apk | grep -i 'password\|api.key\|bearer'
```
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/dex-deep-dive/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/dex-deep-dive/SKILL.md && git commit -m "feat(skills): add dex-deep-dive skill for DEX/class/method analysis"`

---

### Task 3: 创建 network-analysis Skill — 覆盖网络和通信分析工作流

**Depends on:** None
**Files:**
- Create: `skills/network-analysis/SKILL.md`

- [ ] **Step 1: 创建 network-analysis SKILL.md — 覆盖网络端点/URL/通信安全分析**

```markdown
---
name: network-analysis
description: Network and communication analysis workflow. Use when you need to find API endpoints, URLs, network security issues, or cleartext traffic in an APK.
autoInvoke: true
---

# Network & Communication Analysis

Workflow for discovering and analyzing network communication in Android apps.

## When to Use

- Finding API endpoints and server URLs
- Checking for cleartext (HTTP) traffic
- Analyzing network security configuration
- Finding WebSocket or gRPC connections
- Security audit of network communication

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Find Network Endpoints

```bash
# Search for URLs in strings
java -jar apktool.jar strings app.apk -p 'https?://.*'

# Search DEX strings for URLs
java -jar apktool.jar dex-strings app.apk | grep -i 'http'

# Search for specific API patterns
java -jar apktool.jar strings app.apk -p 'api\.|/v[0-9]/|graphql|websocket'
```

### Step 2: Find Network-Related Classes

```bash
# Search for HTTP client classes
java -jar apktool.jar method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection|Volley|HttpClient'

# Search for network-related methods
java -jar apktool.jar method-search app.apk -p 'execute|enqueue|doRequest|sendRequest|fetch'

# Search for SSL/TLS classes
java -jar apktool.jar method-search app.apk -p 'SSLSocket|TrustManager|HostnameVerifier|SSLContext'
```

### Step 3: Check Network Security

```bash
# Check manifest flags for cleartext traffic
java -jar apktool.jar manifest-flags app.apk

# Check for network security config
java -jar apktool.jar manifest app.apk | jq '.networkSecurityConfig'

# Full security report
java -jar apktool.jar security app.apk
```

### Step 4: Find Intent-Based Communication

```bash
# Check exported components (attack surface)
java -jar apktool.jar api-surface app.apk

# Check all components
java -jar apktool.jar components app.apk
```

## Common Patterns

```bash
# Complete network audit
java -jar apktool.jar strings app.apk -p 'https?://.*'
java -jar apktool.jar manifest-flags app.apk
java -jar apktool.jar method-search app.apk -p 'OkHttp|Retrofit|HttpURLConnection'
java -jar apktool.jar method-search app.apk -p 'TrustManager|HostnameVerifier'

# Find all REST API endpoints
java -jar apktool.jar strings app.apk -p '/api/.*'
java -jar apktool.jar method-search app.apk -p '@GET|@POST|@PUT|@DELETE'
```
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/network-analysis/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/network-analysis/SKILL.md && git commit -m "feat(skills): add network-analysis skill for network/URL/communication analysis"`

---

### Task 4: 创建 malware-hunt Skill — 覆盖恶意软件检测工作流

**Depends on:** None
**Files:**
- Create: `skills/malware-hunt/SKILL.md`

- [ ] **Step 1: 创建 malware-hunt SKILL.md — 覆盖恶意行为指标检测**

```markdown
---
name: malware-hunt
description: Malware indicator hunting workflow. Use when investigating suspicious APKs, looking for malicious patterns, or checking for common malware indicators.
autoInvoke: true
---

# Malware Indicator Hunting

Workflow for detecting malware indicators and suspicious patterns in Android APKs.

## When to Use

- Investigating a suspicious APK
- Looking for common malware indicators
- Checking for dynamic code loading
- Finding command execution patterns
- Detecting data exfiltration

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Quick Risk Assessment

```bash
# Full security report with risk score
java -jar apktool.jar security app.apk

# Check manifest security flags
java -jar apktool.jar manifest-flags app.apk

# Check signing information
java -jar apktool.jar signing app.apk
```

### Step 2: Find Dangerous Capabilities

```bash
# Command execution
java -jar apktool.jar method-search app.apk -p 'Runtime\.exec|ProcessBuilder|su'

# Dynamic code loading
java -jar apktool.jar method-search app.apk -p 'DexClassLoader|PathClassLoader|loadClass|loadLibrary'

# Reflection abuse
java -jar apktool.jar method-search app.apk -p 'getDeclaredMethod|invoke|setAccessible|forName'

# Root detection bypass
java -jar apktool.jar method-search app.apk -p 'su|Superuser|isRooted|RootBeer'
```

### Step 3: Find Suspicious Strings

```bash
# Hardcoded IPs/domains (C2 indicators)
java -jar apktool.jar strings app.apk -p '\d+\.\d+\.\d+\.\d+'
java -jar apktool.jar strings app.apk -p '\.onion|\.tk|\.ml|\.ga'

# Base64 encoded strings (obfuscation)
java -jar apktool.jar dex-strings app.apk | grep -E '^[A-Za-z0-9+/]{20,}={0,2}$'

# Shell commands
java -jar apktool.jar strings app.apk -p 'chmod|chown|mount|insmod|rm -rf'
```

### Step 4: Check Permissions and Components

```bash
# Detailed permission analysis
java -jar apktool.jar permission-detail app.apk

# Exported components (attack surface)
java -jar apktool.jar api-surface app.apk

# Check for dangerous permission combinations
java -jar apktool.jar permissions app.apk
```

### Step 5: Check File and Asset Contents

```bash
# List all files (look for suspicious files)
java -jar apktool.jar file-list app.apk

# List assets
java -jar apktool.jar asset-list app.apk

# Check native libraries
java -jar apktool.jar native-libs app.apk
```

## Malware Indicator Checklist

| Indicator | Command |
|-----------|---------|
| Debuggable app | `manifest-flags` → debuggable=true |
| Unprotected exported components | `api-surface` → totalExportedComponents > 0 |
| Dynamic code loading | `method-search` → DexClassLoader |
| Command execution | `method-search` → Runtime.exec |
| Hardcoded C2 IPs | `strings` → IP patterns |
| Root access | `method-search` → su, Superuser |
| Excessive permissions | `permission-detail` → dangerousCount high |
| No signing / self-signed | `signing` → check issuer |
| Native libs | `native-libs` → unexpected architectures |
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/malware-hunt/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/malware-hunt/SKILL.md && git commit -m "feat(skills): add malware-hunt skill for malware indicator detection"`

---

### Task 5: 创建 resource-explorer Skill — 覆盖资源探索工作流

**Depends on:** None
**Files:**
- Create: `skills/resource-explorer/SKILL.md`

- [ ] **Step 1: 创建 resource-explorer SKILL.md — 覆盖资源表/文件/资产探索**

```markdown
---
name: resource-explorer
description: Resource and file exploration workflow. Use when you need to understand app resources, locales, assets, or file structure.
autoInvoke: true
---

# Resource & File Explorer

Workflow for exploring Android app resources, assets, and file structure.

## When to Use

- Understanding app resource organization
- Finding specific resources (strings, drawables, layouts)
- Checking localization support
- Exploring assets directory
- Analyzing APK file structure

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Resource Overview

```bash
# Resource table summary
java -jar apktool.jar resources app.apk

# Resource package groups
java -jar apktool.jar resource-packages app.apk

# Library and framework package IDs
java -jar apktool.jar lib-frame-packages app.apk

# Supported locales
java -jar apktool.jar locales app.apk
```

### Step 2: File Exploration

```bash
# List all files in APK
java -jar apktool.jar file-list app.apk

# APK file hashes (integrity check)
java -jar apktool.jar file-hash app.apk

# Assets directory
java -jar apktool.jar asset-list app.apk

# Native libraries per architecture
java -jar apktool.jar native-libs app.apk
```

### Step 3: String Resources

```bash
# All strings (DEX + resources)
java -jar apktool.jar strings app.apk

# DEX-only strings
java -jar apktool.jar dex-strings app.apk

# Search strings by pattern
java -jar apktool.jar strings app.apk -p 'app_name|version|url'
```

### Step 4: Decoded APK Info

```bash
# Read apktool.yml from decoded directory
java -jar apktool.jar apk-info decoded_app_dir

# Shared libraries used
java -jar apktool.jar uses-libs app.apk
```

## Common Patterns

```bash
# Full resource audit
java -jar apktool.jar resources app.apk
java -jar apktool.jar locales app.apk
java -jar apktool.jar asset-list app.apk
java -jar apktool.jar native-libs app.apk

# Find all configuration files
java -jar apktool.jar file-list app.apk | grep -i '\.xml\|\.json\|\.properties\|\.conf'

# Check for tracking/analytics resources
java -jar apktool.jar strings app.apk -p 'google-analytics|firebase|mixpanel|flurry|amplitude'
```
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/resource-explorer/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/resource-explorer/SKILL.md && git commit -m "feat(skills): add resource-explorer skill for resource/file/asset exploration"`

---

### Task 6: 创建 signing-verify Skill — 覆盖签名验证工作流

**Depends on:** None
**Files:**
- Create: `skills/signing-verify/SKILL.md`

- [ ] **Step 1: 创建 signing-verify SKILL.md — 覆盖 APK 签名验证和证书分析**

```markdown
---
name: signing-verify
description: APK signing and certificate verification workflow. Use when you need to verify APK signing, check certificate details, or assess signing security.
autoInvoke: true
---

# APK Signing & Verification

Workflow for verifying and analyzing APK signing certificates.

## When to Use

- Verifying APK authenticity
- Checking signing certificate details
- Assessing signing scheme security (v1/v2/v3/v4)
- Comparing certificates between app versions
- Detecting self-signed or debug certificates

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Get Signing Information

```bash
# Full signing details
java -jar apktool.jar signing app.apk
```

Review: `v1Signing`, `v2Signing`, `v3Signing`, `certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}]`

### Step 2: Analyze Certificate

Key checks:
- **Subject/Issuer**: Is it self-signed? (subject == issuer)
- **Validity**: Is the certificate expired? (notAfter < now)
- **Fingerprints**: Compare with known good certificates
- **Signing schemes**: v1 only = vulnerable to Janus attack, v2+ recommended

### Step 3: Compare with Another APK

```bash
# Compare signing between versions
java -jar apktool.jar signing app_v1.apk > signing_v1.json
java -jar apktool.jar signing app_v2.apk > signing_v2.json

# Full diff including signing changes
java -jar apktool.jar diff app_v1.apk app_v2.apk
```

### Step 4: Check for Debug Signing

```bash
# Check manifest flags (debuggable)
java -jar apktool.jar manifest-flags app.apk

# Full security report
java -jar apktool.jar security app.apk
```

## Signing Scheme Security

| Scheme | Android Version | Security |
|--------|---------------|----------|
| v1 (JAR) | 7.0+ | Vulnerable to Janus attack, allows APK modification |
| v2 (APK Signature Scheme v2) | 7.0+ | Protects entire APK, prevents modification |
| v3 (APK Signature Scheme v3) | 9.0+ | Adds key rotation support |
| v4 | 11.0+ | Adds streaming verification |

## Common Patterns

```bash
# Complete signing audit
java -jar apktool.jar signing app.apk
java -jar apktool.jar manifest-flags app.apk
java -jar apktool.jar file-hash app.apk
```
```

- [ ] **Step 2: 验证文件创建**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/signing-verify/SKILL.md && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 提交**
Run: `git add skills/signing-verify/SKILL.md && git commit -m "feat(skills): add signing-verify skill for APK signing verification"`

---

### Task 7: 更新现有 5 个 Skill — 引用新增命令

**Depends on:** Task 1, Task 2, Task 3, Task 4, Task 5, Task 6
**Files:**
- Modify: `skills/quick-analysis/SKILL.md`
- Modify: `skills/security-audit/SKILL.md`
- Modify: `skills/compare/SKILL.md`
- Modify: `skills/reverse/SKILL.md`
- Modify: `skills/reference/SKILL.md`

- [ ] **Step 1: 更新 quick-analysis SKILL.md — 添加新命令引用**

在 Step 3 "Targeted Deep Dive" 末尾添加：

```bash
# Check DEX structure
java -jar apktool.jar dex-info <apk-file>

# Get file hashes for integrity
java -jar apktool.jar file-hash <apk-file>

# Detailed permission analysis
java -jar apktool.jar permission-detail <apk-file>
```

- [ ] **Step 2: 更新 security-audit SKILL.md — 添加新命令引用**

在 Step 4 "Sensitive Data Search" 末尾添加：

```bash
# Check manifest security flags
java -jar apktool.jar manifest-flags <apk-file>

# Detailed permission analysis
java -jar apktool.jar permission-detail <apk-file>

# Check for dynamic code loading
java -jar apktool.jar method-search <apk-file> -p 'DexClassLoader|PathClassLoader'
```

- [ ] **Step 3: 更新 compare SKILL.md — 添加新命令引用**

在 Step 4 "Search for Specific Changes" 末尾添加：

```bash
# Compare signing certificates
java -jar apktool.jar signing <old-apk> > old_signing.json
java -jar apktool.jar signing <new-apk> > new_signing.json

# Compare file hashes
java -jar apktool.jar file-hash <old-apk> > old_hash.json
java -jar apktool.jar file-hash <new-apk> > new_hash.json
```

- [ ] **Step 4: 更新 reverse SKILL.md — 添加新命令引用**

在 Step 3 "Search" 末尾添加：

```bash
# Search fields
java -jar apktool.jar field-search <apk-file> -p 'mContext|mView|mHandler'

# Get class inheritance
java -jar apktool.jar inheritance <apk-file> com.example.SuspiciousClass

# Get detailed class info
java -jar apktool.jar class-info <apk-file> com.example.TargetClass
```

- [ ] **Step 5: 更新 reference SKILL.md — 添加完整命令列表**

在 Analysis Commands 表格末尾添加缺失的命令：

```markdown
| `class-list` | totalClasses, classes[] |
| `method-search` | totalMatches, methods[{className, methodName, returnType}] |
| `field-search` | totalMatches, fields[{className, fieldName, type}] |
| `asset-list` | hasAssets, totalAssets, assets[] |
| `dex-strings` | totalStrings, strings[] |
| `permission-detail` | dangerousCount, normalCount, customCount, permissions[] |
| `inheritance` | className, inheritanceChain[] |
| `manifest-xml` | manifestXml |
| `uses-libs` | usesLibraries[] |
| `manifest-flags` | debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig |
| `file-list` | totalFiles, totalSize, entries[] |
| `file-hash` | sha256, sha1, md5 |
| `class-info` | superClass, methods[], fields[], interfaces[] |
```

- [ ] **Step 6: 验证所有 Skill 文件存在**
Run: `for f in quick-analysis security-audit compare reverse reference; do test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/$f/SKILL.md || echo "MISSING: $f"; done && echo "ALL OK"`
Expected:
  - Exit code: 0
  - Output contains: "ALL OK"

- [ ] **Step 7: 提交**
Run: `git add skills/quick-analysis/SKILL.md skills/security-audit/SKILL.md skills/compare/SKILL.md skills/reverse/SKILL.md skills/reference/SKILL.md && git commit -m "feat(skills): update existing 5 skills with new command references"`

---

### Task 8: 更新 CLAUDE.md — 反映完整 11-Skill 能力矩阵

**Depends on:** Task 7
**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: 更新 CLAUDE.md Skills 表格 — 添加 6 个新 Skill**

将 Skills 表格替换为：

```markdown
## Skills

| Skill | Description | When to Use |
|-------|-------------|-------------|
| `quick-analysis` | Fast APK assessment | First encounter with an APK |
| `security-audit` | Comprehensive security audit | Vulnerability assessment, OWASP compliance |
| `compare` | Version comparison | Checking changes between app versions |
| `reverse` | Full reverse engineering | Deep analysis, modification, malware investigation |
| `reference` | CLI command reference | Looking up exact syntax or output format |
| `decode-build` | Decode & build workflow | Decoding APK, rebuilding, framework management |
| `dex-deep-dive` | DEX deep analysis | Class/method/field exploration, inheritance tracing |
| `network-analysis` | Network communication analysis | Finding endpoints, URLs, cleartext traffic |
| `malware-hunt` | Malware indicator hunting | Suspicious APK investigation, malicious patterns |
| `resource-explorer` | Resource & file exploration | Resources, locales, assets, file structure |
| `signing-verify` | Signing verification | Certificate analysis, signing scheme assessment |
```

- [ ] **Step 2: 验证 CLAUDE.md 更新**
Run: `grep -c 'decode-build\|dex-deep-dive\|network-analysis\|malware-hunt\|resource-explorer\|signing-verify' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/CLAUDE.md`
Expected:
  - Output contains: "6" (each new skill mentioned once)

- [ ] **Step 3: 提交**
Run: `git add CLAUDE.md && git commit -m "docs: update CLAUDE.md with complete 11-skill capability matrix"`

---

### Task 9: 验证完整能力覆盖 — 确保 CommandRegistry 与 Skills 完全对齐

**Depends on:** Task 8
**Files:**
- None (verification only)

- [ ] **Step 1: 验证所有 CLI 命令在 Skills 中被引用**
Run: `echo "Checking command coverage..." && for cmd in info manifest permissions activities services receivers providers components sdk-info resources security api-surface signing strings dex-list locales native-libs dex-info apk-info resource-packages lib-frame-packages uses-libs manifest-flags version file-list file-hash class-info class-list method-search field-search asset-list dex-strings permission-detail inheritance manifest-xml analyze search diff structure ai serve decode build install-framework clean-frameworks list-frameworks publicize-resources; do found=$(grep -rl "$cmd" /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/ 2>/dev/null | head -1); if [ -z "$found" ]; then echo "UNCOVERED: $cmd"; fi; done && echo "Coverage check complete"`
Expected:
  - Exit code: 0
  - Output does NOT contain: "UNCOVERED"

- [ ] **Step 2: 验证所有 Skill 文件存在**
Run: `for skill in quick-analysis security-audit compare reverse reference decode-build dex-deep-dive network-analysis malware-hunt resource-explorer signing-verify; do test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/$skill/SKILL.md || echo "MISSING: $skill"; done && echo "All 11 skills present"`
Expected:
  - Exit code: 0
  - Output contains: "All 11 skills present"

- [ ] **Step 3: 验证 CLAUDE.md 包含所有 11 个 Skill**
Run: `count=$(grep -c '`[a-z-]*`' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/CLAUDE.md | head -1) && echo "Skills in CLAUDE.md: $count" && test $count -ge 11 && echo "OK: 11+ skills referenced"`
Expected:
  - Exit code: 0
  - Output contains: "OK"
