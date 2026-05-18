# AI-Apktool Skills Repository Restructuring Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 将 AI-Apktool 仓库从"Java 项目 + 内嵌 .claude/skills"改造为独立的 Claude Code Skills 插件仓库，遵循 superpowers-auto 的标准结构（skills/ 目录 + SKILL.md + .claude-plugin/ 配置），让 AI agent 通过 `claude plugin install` 获得全部 APK 逆向分析能力。

**Architecture:** Skills 仓库结构：`skills/<name>/SKILL.md`（每个 Skill 一个目录 + SKILL.md 入口）+ `.claude-plugin/plugin.json`（插件元数据）+ `.claude-plugin/marketplace.json`（市场配置）+ `README.md`（安装指南）+ `CLAUDE.md`（AI 入口文档）。每个 Skill 引用外部 `apktool.jar` CLI 命令，不内嵌 Java 代码。

**Tech Stack:** Claude Code Plugin System, Markdown Skills, apktool.jar CLI (外部依赖)

**Risks:**
- 删除 `.claude/skills/` 旧文件后，项目级 skills 不再生效 → 缓解：改造后通过 plugin install 安装，旧位置不再需要
- Skills 引用 `apktool.jar` 但仓库本身不含该 JAR → 缓解：README 中说明需要单独构建或下载 apktool.jar

---

### Task 1: Create Skills Directory Structure — 迁移 Skills 到标准目录

**Depends on:** None
**Files:**
- Create: `skills/quick-analysis/SKILL.md`
- Create: `skills/security-audit/SKILL.md`
- Create: `skills/compare/SKILL.md`
- Create: `skills/reverse/SKILL.md`
- Create: `skills/reference/SKILL.md`
- Delete: `.claude/skills/apktool-quick-analysis.md`
- Delete: `.claude/skills/apktool-security-audit.md`
- Delete: `.claude/skills/apktool-compare.md`
- Delete: `.claude/skills/apktool-reverse.md`
- Delete: `.claude/skills/apktool-reference.md`

- [ ] **Step 1: 创建 skills/quick-analysis/SKILL.md — 快速分析 Skill**

```markdown
---
name: quick-analysis
description: Quick APK analysis workflow for initial assessment. Use when you need a fast overview of an unknown APK file.
autoInvoke: true
---

# APK Quick Analysis

Quick analysis workflow for getting a rapid overview of any APK file.

## When to Use

- First time encountering an APK
- Need a quick security assessment
- Want to understand what an app does
- Evaluating an app before deeper analysis

## Prerequisites

AI-Apktool CLI must be available. Build from source or download:
```bash
./gradlew build
# JAR at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```

## Workflow

### Step 1: Get APK Summary

Run a full analysis to get all data at once:

```bash
java -jar apktool.jar analyze <apk-file>
```

This returns a comprehensive JSON object with: summary, manifest, security report, API surface, resources, signing info, and code structure.

### Step 2: Quick Assessment Checklist

Based on the `analyze` output, check these fields:

1. **Package Identity** — `summary.packageName`, `summary.versionName`, `summary.versionCode`
2. **Security Risk** — `security.riskScore` (0-100, higher = more risky)
   - 0-20: Low risk
   - 21-50: Medium risk
   - 51-100: High risk
3. **Dangerous Permissions** — `security.dangerousPermissions` (empty = good)
4. **Exported Components** — `apiSurface.totalExportedComponents` (more = larger attack surface)
5. **Signing** — `signing.certificates[0].subject` (who signed it?)
6. **Debug Flag** — `security.debuggable` (true = dangerous in production)

### Step 3: Targeted Deep Dive (if needed)

Based on findings, run targeted commands:

```bash
# Focus on security issues
java -jar apktool.jar security <apk-file>

# Check what's exposed to other apps
java -jar apktool.jar api-surface <apk-file>

# Search for sensitive strings
java -jar apktool.jar search <apk-file> "password|secret|key|token" -t strings

# Search for specific classes
java -jar apktool.jar search <apk-file> "LoginActivity|AuthHelper|Encryption" -t classes
```

## Output Interpretation

### riskScore Breakdown
- `debuggable=true`: +30 points
- `allowBackup=true`: +10 points
- Each dangerous permission: +2 points (max 20)
- Each unprotected exported component: +5 points (max 30)

### Common Findings
- `"HIGH: Application is debuggable"` — app can be attached to a debugger
- `"MEDIUM: Application allows backup"` — app data can be extracted via adb
- `"HIGH: N exported components without permission protection"` — attack surface
```

- [ ] **Step 2: 创建 skills/security-audit/SKILL.md — 安全审计 Skill**

```markdown
---
name: security-audit
description: Comprehensive security audit workflow for Android APK files. Use when performing security analysis, vulnerability assessment, or compliance checking.
autoInvoke: true
---

# APK Security Audit

Comprehensive security audit workflow for Android applications.

## When to Use

- Security review of an Android app
- Vulnerability assessment
- Pre-release security check
- Compliance audit (OWASP Mobile Top 10)
- Third-party library security review

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Automated Security Report

```bash
java -jar apktool.jar security <apk-file>
```

Review: `dangerousPermissions[]`, `highRiskComponents[]`, `debuggable`, `allowBackup`, `usesCleartextTraffic`, `findings[]`, `riskScore`

### Step 2: Attack Surface Analysis

```bash
java -jar apktool.jar api-surface <apk-file>
```

For each exported component, verify: Is `exported=true` intentional? Is there permission protection? What data can it receive/process?

### Step 3: Permission Deep Dive

```bash
java -jar apktool.jar permissions <apk-file>
```

Cross-reference with app functionality.

### Step 4: Sensitive Data Search

```bash
java -jar apktool.jar search <apk-file> "password|passwd|secret|api.?key|token|credential" -t strings
java -jar apktool.jar search <apk-file> "https?://" -t strings
java -jar apktool.jar search <apk-file> "Cipher|SecretKey|MessageDigest|SSLContext|TrustManager" -t classes
java -jar apktool.jar search <apk-file> "encrypt|decrypt|hash|sign|verify" -t methods
```

### Step 5: Signing Verification

```bash
java -jar apktool.jar signing <apk-file>
```

### Step 6: Generate AI Prompt for Deeper Analysis

```bash
java -jar apktool.jar ai <apk-file> -a security-review
```

## OWASP Mobile Top 10 Mapping

| OWASP Category | AI-Apktool Commands |
|---|---|
| M1: Platform Misuse | `security` |
| M2: Insecure Data Storage | `search` (find hardcoded keys) |
| M3: Insecure Communication | `search` (cleartext URLs), `manifest` |
| M4: Insecure Authentication | `search` (auth classes) |
| M5: Insufficient Cryptography | `search` (crypto classes), `signing` |
| M6: Insecure Authorization | `api-surface` |
| M7: Client Code Quality | `structure` |
| M8: Code Tampering | `signing`, `security` |
| M9: Reverse Engineering | `structure` |
| M10: Extraneous Functionality | `manifest`, `search` |
```

- [ ] **Step 3: 创建 skills/compare/SKILL.md — 版本对比 Skill**

```markdown
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
java -jar apktool.jar diff <old-apk> <new-apk>
```

Review: `addedPermissions[]`, `removedPermissions[]`, `addedActivities[]`, `removedActivities[]`, `addedServices[]`, `addedDexFiles[]`, `addedNativeLibs[]`, `versionCodeChange`, `versionNameChange`, `targetSdkChange`

### Step 2: Detailed Analysis

```bash
java -jar apktool.jar security <old-apk> > old_security.json
java -jar apktool.jar security <new-apk> > new_security.json
java -jar apktool.jar api-surface <old-apk> > old_surface.json
java -jar apktool.jar api-surface <new-apk> > new_surface.json
java -jar apktool.jar structure <old-apk> > old_structure.json
java -jar apktool.jar structure <new-apk> > new_structure.json
```

### Step 3: Identify Key Changes

Focus on: New Permissions, New Exported Components, Native Library Changes, DEX Changes, Target SDK Change, Risk Score Delta.

### Step 4: Search for Specific Changes

```bash
java -jar apktool.jar search <new-apk> "changelog|whats.?new|updated" -t strings
java -jar apktool.jar search <new-apk> "NewFeature|UpdatedActivity" -t classes
```
```

- [ ] **Step 4: 创建 skills/reverse/SKILL.md — 逆向工程 Skill**

```markdown
---
name: reverse
description: Full reverse engineering workflow for Android APK files. Use when you need to deeply analyze, modify, or understand an Android application's internals.
autoInvoke: true
---

# APK Reverse Engineering

Full reverse engineering workflow using AI-Apktool's decode, analysis, and search capabilities.

## When to Use

- Reverse engineering an unknown Android app
- Understanding app architecture and behavior
- Modifying or repackaging an APK
- Investigating malware or suspicious apps

## Prerequisites

AI-Apktool CLI must be available.

## Workflow

### Step 1: Reconnaissance

```bash
java -jar apktool.jar analyze <apk-file>
```

### Step 2: Decode

```bash
java -jar apktool.jar decode <apk-file> -o <output-dir>
java -jar apktool.jar decode <apk-file> -o <output-dir> -s       # resources only
java -jar apktool.jar decode <apk-file> -o <output-dir> --only-manifest
```

### Step 3: Search

```bash
java -jar apktool.jar search <apk-file> "LoginActivity|MainActivity" -t classes
java -jar apktool.jar search <apk-file> "https?://.*" -t strings
java -jar apktool.jar search <apk-file> "onCreate|onClick|doLogin|encrypt" -t methods
```

### Step 4: Security Review

```bash
java -jar apktool.jar security <apk-file>
java -jar apktool.jar api-surface <apk-file>
java -jar apktool.jar signing <apk-file>
```

### Step 5: AI-Powered Analysis

```bash
java -jar apktool.jar ai <apk-file> -a explain
java -jar apktool.jar ai <apk-file> -a security-review
java -jar apktool.jar ai <apk-file> -a summarize
```

### Step 6: Rebuild (if modifications needed)

```bash
java -jar apktool.jar build <decoded-dir> -o <output.apk>
```

## Common Patterns

```bash
# Find hardcoded secrets
java -jar apktool.jar search <apk> "password|secret|api.?key|Bearer|token" -t strings

# Find network endpoints
java -jar apktool.jar search <apk> "https?://" -t strings

# Find encryption implementation
java -jar apktool.jar search <apk> "Cipher|AES|RSA|DES|SecretKey" -t classes

# Find auth logic
java -jar apktool.jar search <apk> "Login|Auth|Session|Token|OAuth" -t classes

# Malware indicators
java -jar apktool.jar search <apk> "Runtime|exec|ProcessBuilder|su|root" -t classes
java -jar apktool.jar search <apk> "exec|loadLibrary|DexClassLoader" -t methods
```
```

- [ ] **Step 5: 创建 skills/reference/SKILL.md — 完整命令参考 Skill**

```markdown
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
| `analyze` | all of the above combined |

## Search Commands

```bash
java -jar apktool.jar search <apk-file> [pattern] -t <type>
# type: strings, classes, methods (default: classes)
# pattern: Java regex (default: .*)
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
- `GET /api/v1/security?apk=<path>`
- `GET /api/v1/search?apk=<path>&type=<type>&pattern=<pattern>`
- `GET /api/v1/diff?apk1=<path>&apk2=<path>`
- `GET /api/v1/resources?apk=<path>`
```

- [ ] **Step 6: 删除旧的 .claude/skills/ 目录**
Run: `rm -rf .claude/skills/`
Expected:
  - `.claude/skills/` directory no longer exists
  - `skills/` directory contains 5 subdirectories

- [ ] **Step 7: 验证 Skills 目录结构**
Run: `find skills/ -type f | sort`
Expected:
  - Output contains exactly 5 files:
    - `skills/compare/SKILL.md`
    - `skills/quick-analysis/SKILL.md`
    - `skills/reference/SKILL.md`
    - `skills/reverse/SKILL.md`
    - `skills/security-audit/SKILL.md`

- [ ] **Step 8: 提交**
Run: `git add -A && git commit -m "refactor(skills): migrate from .claude/skills/ to skills/ directory with SKILL.md format"`

---

### Task 2: Create Plugin Configuration — .claude-plugin/ 配置

**Depends on:** Task 1
**Files:**
- Create: `.claude-plugin/plugin.json`
- Create: `.claude-plugin/marketplace.json`

- [ ] **Step 1: 创建 .claude-plugin/plugin.json — 插件元数据**

```json
{
  "name": "ai-apktool",
  "description": "AI-native Android reverse engineering skills - APK analysis, security audit, and code exploration for Claude Code",
  "version": "1.0.0",
  "author": {
    "name": "CC11001100",
    "url": "https://github.com/CC11001100"
  },
  "homepage": "https://github.com/CC11001100/android-reverse-hub/AI-Apktool",
  "repository": "https://github.com/CC11001100/android-reverse-hub/AI-Apktool",
  "license": "Apache-2.0",
  "keywords": ["apk", "android", "reverse-engineering", "security", "analysis", "skills"]
}
```

- [ ] **Step 2: 创建 .claude-plugin/marketplace.json — 市场配置**

```json
{
  "name": "ai-apktool",
  "description": "AI-native Android reverse engineering skills for Claude Code",
  "owner": {
    "name": "CC11001100",
    "url": "https://github.com/CC11001100"
  },
  "plugins": [
    {
      "name": "ai-apktool",
      "description": "APK analysis, security audit, reverse engineering, and version comparison skills for AI agents",
      "version": "1.0.0",
      "source": "./",
      "author": {
        "name": "CC11001100",
        "url": "https://github.com/CC11001100"
      }
    }
  ]
}
```

- [ ] **Step 3: 验证配置文件**
Run: `cat .claude-plugin/plugin.json | python3 -m json.tool > /dev/null && echo "plugin.json valid" && cat .claude-plugin/marketplace.json | python3 -m json.tool > /dev/null && echo "marketplace.json valid"`
Expected:
  - Exit code: 0
  - Output contains: "plugin.json valid" and "marketplace.json valid"

- [ ] **Step 4: 提交**
Run: `git add .claude-plugin/ && git commit -m "feat(plugin): add .claude-plugin configuration for Claude Code plugin system"`

---

### Task 3: Rewrite README.md — Skills 仓库安装指南

**Depends on:** Task 2
**Files:**
- Modify: `README.md` (full rewrite)

- [ ] **Step 1: 重写 README.md 为 Skills 仓库入口**

```markdown
# AI-Apktool Skills

> AI-native Android reverse engineering skills for Claude Code.

5 个 Skills 覆盖 APK 分析全流程：快速分析、安全审计、版本对比、逆向工程、命令参考。

## 包含的 Skills（5个）

| Skill | 描述 |
|-------|------|
| **quick-analysis** | 快速 APK 评估 — 一键获取包名、风险分数、危险权限、导出组件 |
| **security-audit** | 安全审计 — OWASP Mobile Top 10 映射、攻击面分析、敏感数据搜索 |
| **compare** | 版本对比 — 权限变更、组件增删、风险分数变化 |
| **reverse** | 逆向工程 — 解码、搜索、安全审查、AI 提示生成、重打包 |
| **reference** | 命令参考 — 全部 22 个 CLI 命令的语法、选项、输出格式 |

## 前提条件

- 已安装 [Claude Code](https://claude.ai/code)
- 已构建 AI-Apktool CLI：

```bash
git clone https://github.com/CC11001100/android-reverse-hub.git
cd AI-Apktool
./gradlew build
# JAR at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```

## 安装

### 方式 1：作为 Claude Code 插件安装

```bash
# 添加 Marketplace
claude config add marketplace ai-apktool https://github.com/CC11001100/android-reverse-hub.git

# 安装插件
claude plugin install ai-apktool@ai-apktool
```

### 方式 2：手动安装 Skills

```bash
# 克隆到 Claude Code skills 目录
git clone https://github.com/CC11001100/android-reverse-hub.git ~/.claude/skills/ai-apktool
```

### 验证安装

```bash
claude skill list
# 应看到：
#   ai-apktool:quick-analysis
#   ai-apktool:security-audit
#   ai-apktool:compare
#   ai-apktool:reverse
#   ai-apktool:reference
```

## 使用

### 自动生效

安装后，当 Claude Code 遇到 APK 相关任务时，会自动识别并使用合适的 Skill。

### 手动调用

```
/quick-analysis 分析这个 APK: /path/to/app.apk
/security-audit 对 app.apk 做安全审计
/compare 对比 app_v1.apk 和 app_v2.apk
/reverse 逆向分析 app.apk
/reference 查看 search 命令的用法
```

### 典型工作流

```
用户: 分析这个 APK 文件

AI: [使用 quick-analysis skill]
1. 运行: java -jar apktool.jar analyze /path/to/app.apk
2. 报告发现:
   - 包名: com.example.app v2.1.0
   - 风险分数: 35/100 (中等)
   - 3 个危险权限: CAMERA, RECORD_AUDIO, ACCESS_FINE_LOCATION
   - 2 个未保护的导出 Activity
   - 签名者: CN=Developer, O=Example Inc
```

## CLI 命令总览

| 类别 | 命令 |
|------|------|
| 核心操作 | decode, build, install-framework, clean-frameworks, list-frameworks, publicize-resources |
| 信息查询 | info, manifest, permissions, activities, services, receivers, providers, components, sdk-info |
| 高级分析 | security, api-surface, signing, structure, analyze |
| 搜索 | search |
| 对比 | diff |
| AI/服务 | ai, serve |

所有分析命令输出 JSON，可用 `jq` 解析：
```bash
java -jar apktool.jar info app.apk | jq '.packageName'
java -jar apktool.jar security app.apk | jq '.riskScore'
```

## 架构

```
skills/
  quick-analysis/SKILL.md   — 快速分析工作流
  security-audit/SKILL.md   — 安全审计工作流
  compare/SKILL.md          — 版本对比工作流
  reverse/SKILL.md          — 逆向工程工作流
  reference/SKILL.md        — 命令参考
.claude-plugin/
  plugin.json               — 插件元数据
  marketplace.json          — 市场配置
CLAUDE.md                   — AI 入口文档
```

## 许可证

Apache License 2.0
```

- [ ] **Step 2: 提交**
Run: `git add README.md && git commit -m "docs: rewrite README.md as Skills repository with installation guide"`

---

### Task 4: Update CLAUDE.md — Skills 仓库入口文档

**Depends on:** Task 3
**Files:**
- Modify: `CLAUDE.md` (update to reflect Skills repo structure)

- [ ] **Step 1: 更新 CLAUDE.md 为 Skills 仓库入口**

```markdown
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
java -jar apktool.jar info app.apk | jq '.packageName'
java -jar apktool.jar security app.apk | jq '.riskScore'
java -jar apktool.jar api-surface app.apk | jq '.exportedActivities[].name'
```

## Build

```bash
./gradlew build
# CLI jar at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```
```

- [ ] **Step 2: 提交**
Run: `git add CLAUDE.md && git commit -m "docs: update CLAUDE.md as Skills repository entry point"`
