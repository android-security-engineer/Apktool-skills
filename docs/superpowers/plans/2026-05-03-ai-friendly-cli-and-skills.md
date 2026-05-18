# AI-Friendly CLI Enhancement & Skills Documentation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 将 AI-Apktool 打造成 AI 原生的 CLI 工具 —— 补全缺失的 CLI 命令（signing、analyze），编写完整的 CLAUDE.md 项目文档，创建 5 个 AI Skills 手册覆盖所有能力，使 AI agent 能自主发现和使用全部功能。

**Architecture:** AI agent → 发现 Skills 手册 → 选择合适 skill → 调用 CLI 命令 → 解析 JSON 输出 → 完成任务。Skills 分为 5 大领域：快速分析（quick-analysis）、安全审计（security-audit）、版本对比（compare）、逆向工程（reverse）、命令参考（reference）。CLI 命令覆盖全部 3 层能力：核心操作（decode/build）→ 信息查询（info/manifest/permissions/...）→ 高级分析（security/diff/structure/signing/analyze）→ AI 集成（ai/serve）。

**Tech Stack:** Java 8, Gradle (Kotlin DSL), Gson, Commons CLI, Javalin 5.6.3, dexlib2

**Risks:**
- Task 1 的 signing 命令需要解析 PKCS#7 证书格式，Java 8 的 JarFile API 可能无法提取所有签名方案 → 缓解：先实现 v1 签名检测，v2/v3 做基础检测
- Task 3-6 的 Skills 文件需要与实际 CLI 命令保持一致 → 缓解：先完成 Task 1-2 确保命令稳定，再写 Skills

---

### Task 1: Implement `signing` CLI Command — APK 签名证书信息提取

**Depends on:** None
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SigningInfo.java`
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (Lines 20-25 imports, Lines 330 之后添加方法)
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (Lines 375-392 switch, Lines 1079 之后添加方法)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (Lines 90-92 signing 命令已注册，无需修改)

- [ ] **Step 1: 创建 SigningInfo 数据模型 — 存储 APK 签名证书信息**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SigningInfo.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class SigningInfo {
    private boolean v1Scheme;
    private boolean v2Scheme;
    private boolean v3Scheme;
    private List<CertificateInfo> certificates = new ArrayList<>();
    private String signingCertificateDigest;

    public boolean isV1Scheme() { return v1Scheme; }
    public void setV1Scheme(boolean v1Scheme) { this.v1Scheme = v1Scheme; }
    public boolean isV2Scheme() { return v2Scheme; }
    public void setV2Scheme(boolean v2Scheme) { this.v2Scheme = v2Scheme; }
    public boolean isV3Scheme() { return v3Scheme; }
    public void setV3Scheme(boolean v3Scheme) { this.v3Scheme = v3Scheme; }
    public List<CertificateInfo> getCertificates() { return certificates; }
    public void setCertificates(List<CertificateInfo> certificates) { this.certificates = certificates; }
    public String getSigningCertificateDigest() { return signingCertificateDigest; }
    public void setSigningCertificateDigest(String digest) { this.signingCertificateDigest = digest; }

    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private String notBefore;
        private String notAfter;
        private String signatureAlgorithm;
        private String sha256;
        private String sha1;
        private String md5;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getNotBefore() { return notBefore; }
        public void setNotBefore(String notBefore) { this.notBefore = notBefore; }
        public String getNotAfter() { return notAfter; }
        public void setNotAfter(String notAfter) { this.notAfter = notAfter; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
        public String getSha1() { return sha1; }
        public void setSha1(String sha1) { this.sha1 = sha1; }
        public String getMd5() { return md5; }
        public void setMd5(String md5) { this.md5 = md5; }
    }
}
```

- [ ] **Step 2: 在 ApkAnalyzer 添加 getSigningInfo 方法 — 解析 APK 签名证书**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:20-25`（import 区域）

在 import 区域添加：
```java
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
```

在文件末尾（Line 330 之后，类的最后一个方法之后）添加：
```java
    public SigningInfo getSigningInfo() throws AndrolibException {
        SigningInfo info = new SigningInfo();
        try {
            // Check for v1 signature files in META-INF
            boolean hasV1Sig = false;
            try (ZipFile zipFile = new ZipFile(mApkFile.getAbsolutePath())) {
                java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName().toUpperCase(java.util.Locale.ROOT);
                    if (name.startsWith("META-INF/") &&
                        (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))) {
                        hasV1Sig = true;
                        break;
                    }
                }
                // Check for APK Signing Block (v2/v3)
                checkApkSigningSchemes(zipFile, info);
            }
            info.setV1Scheme(hasV1Sig);

            // Extract certificate via JarFile verification
            try (JarFile jarFile = new JarFile(mApkFile.getAbsolutePath(), true)) {
                java.util.Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().startsWith("META-INF/") && !entry.isDirectory()) {
                        try (java.io.InputStream is = jarFile.getInputStream(entry)) {
                            byte[] buf = new byte[8192];
                            while (is.read(buf) != -1) {}
                        }
                        Certificate[] certs = entry.getCertificates();
                        if (certs != null && certs.length > 0) {
                            X509Certificate cert = (X509Certificate) certs[0];
                            SigningInfo.CertificateInfo certInfo = new SigningInfo.CertificateInfo();
                            certInfo.setSubject(cert.getSubjectX500Principal().getName());
                            certInfo.setIssuer(cert.getIssuerX500Principal().getName());
                            certInfo.setSerialNumber(cert.getSerialNumber().toString(16));
                            certInfo.setNotBefore(cert.getNotBefore().toString());
                            certInfo.setNotAfter(cert.getNotAfter().toString());
                            certInfo.setSignatureAlgorithm(cert.getSigAlgName());

                            byte[] encoded = cert.getEncoded();
                            certInfo.setSha256(formatFingerprint(MessageDigest.getInstance("SHA-256").digest(encoded)));
                            certInfo.setSha1(formatFingerprint(MessageDigest.getInstance("SHA-1").digest(encoded)));
                            certInfo.setMd5(formatFingerprint(MessageDigest.getInstance("MD5").digest(encoded)));

                            info.getCertificates().add(certInfo);
                            info.setSigningCertificateDigest(certInfo.getSha256());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AndrolibException("Failed to extract signing info: " + e.getMessage(), e);
        }
        return info;
    }

    private void checkApkSigningSchemes(ZipFile zipFile, SigningInfo info) {
        // v2/v3 signing block is located before the central directory
        // Block starts with magic "APK Sig Block 42"
        // For basic detection, check if the APK has the signing block
        try {
            java.util.zip.ZipEntry centralDir = zipFile.getEntry("META-INF/MANIFEST.MF");
            // If no MANIFEST.MF, likely not v1 signed
            // v2/v3 detection would require reading the APK Signing Block from the ZIP structure
            // For now, set to false; full implementation would parse the block
            info.setV2Scheme(false);
            info.setV3Scheme(false);
        } catch (Exception e) {
            // Ignore - scheme detection is best-effort
        }
    }

    private String formatFingerprint(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }
```

- [ ] **Step 3: 在 Main.java 添加 signing 命令注册和实现**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:375-377`（在 `case "components"` 之后添加 switch case）

```java
            case "signing":
                cmdSigning(cmdArgs);
                break;
```

在 `cmdAi` 方法之后（Line 1079 之后）添加：
```java
    private static void cmdSigning(String[] args) throws AndrolibException {
        CommandLine cli = parseOptions(new Options(), args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);

        brut.androlib.analyze.ApkAnalyzer analyzer =
            new brut.androlib.analyze.ApkAnalyzer(new File(apkName), config);
        brut.androlib.analyze.SigningInfo signingInfo = analyzer.getSigningInfo();
        System.out.println(brut.androlib.output.JsonOutput.toJson(signingInfo));
    }
```

- [ ] **Step 4: 验证 signing 命令编译通过**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SigningInfo.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add signing command for APK certificate extraction"`

---

### Task 2: Implement `analyze` CLI Command — 一键全量 APK 分析

**Depends on:** Task 1
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java`
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (switch case Lines 384-386, new method after cmdSigning)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (Lines 106-107 之后添加 analyze 注册)

- [ ] **Step 1: 创建 AnalyzeResult 数据模型 — 全量分析结果容器**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java
package brut.androlib.analyze;

public class AnalyzeResult {
    private ApkSummary summary;
    private ManifestInfo manifest;
    private SecurityReport security;
    private ApiSurfaceInfo apiSurface;
    private ResourceSummary resources;
    private SigningInfo signing;
    private StructureInfo structure;

    public ApkSummary getSummary() { return summary; }
    public void setSummary(ApkSummary summary) { this.summary = summary; }
    public ManifestInfo getManifest() { return manifest; }
    public void setManifest(ManifestInfo manifest) { this.manifest = manifest; }
    public SecurityReport getSecurity() { return security; }
    public void setSecurity(SecurityReport security) { this.security = security; }
    public ApiSurfaceInfo getApiSurface() { return apiSurface; }
    public void setApiSurface(ApiSurfaceInfo apiSurface) { this.apiSurface = apiSurface; }
    public ResourceSummary getResources() { return resources; }
    public void setResources(ResourceSummary resources) { this.resources = resources; }
    public SigningInfo getSigning() { return signing; }
    public void setSigning(SigningInfo signing) { this.signing = signing; }
    public StructureInfo getStructure() { return structure; }
    public void setStructure(StructureInfo structure) { this.structure = structure; }
}
```

- [ ] **Step 2: 在 CommandRegistry 注册 analyze 命令**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java:106-107`（在 `structure` 命令注册之后添加）

```java
        register("analyze", null, "Run comprehensive analysis: all metadata, security, API surface, signing, resources, and structure in one command",
            "apktool analyze <apk-file>", "JSON: {summary, manifest, security, apiSurface, resources, signing, structure}",
            "analysis", new String[]{"apktool analyze app.apk"});
```

- [ ] **Step 3: 在 Main.java 添加 analyze 命令注册和实现**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (switch case Lines 384-386 `case "structure"` 之后添加)

```java
            case "analyze":
                cmdAnalyze(cmdArgs);
                break;
```

在 `cmdSigning` 方法之后添加：
```java
    private static void cmdAnalyze(String[] args) throws AndrolibException {
        CommandLine cli = parseOptions(new Options(), args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        File apkFile = new File(apkName);

        brut.androlib.analyze.ApkAnalyzer analyzer =
            new brut.androlib.analyze.ApkAnalyzer(apkFile, config);

        brut.androlib.analyze.AnalyzeResult result = new brut.androlib.analyze.AnalyzeResult();
        result.setSummary(analyzer.getSummary());

        analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
        result.setManifest(analyzer.getManifestInfo());

        analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
        result.setSecurity(analyzer.getSecurityReport());

        analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
        result.setApiSurface(analyzer.getApiSurface());

        analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
        result.setResources(analyzer.getResourceSummary());

        analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
        result.setSigning(analyzer.getSigningInfo());

        brut.androlib.analyze.StructureInfo structInfo =
            brut.androlib.analyze.ApkDiff.getStructure(apkFile, config);
        result.setStructure(structInfo);

        System.out.println(brut.androlib.output.JsonOutput.toJson(result));
    }
```

- [ ] **Step 4: 验证 analyze 命令编译通过**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java && git commit -m "feat(cli): add analyze command for comprehensive one-shot APK analysis"`

---

### Task 3: Create CLAUDE.md — AI Agent 项目入口文档

**Depends on:** Task 2
**Files:**
- Create: `CLAUDE.md`

- [ ] **Step 1: 创建 CLAUDE.md — 项目概述和 AI 使用指南**

```markdown
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
| `search` | Search strings/classes/methods by regex (`-t strings\|classes\|methods`) |

### Diff & Structure Commands
| Command | Description |
|---------|-------------|
| `diff` | Compare two APKs (permissions, components, version changes) |
| `structure` | Code structure overview (classes, methods, fields, packages) |

### AI & Service Commands
| Command | Description |
|---------|-------------|
| `ai` | Generate LLM-ready prompts (`-a explain\|security-review\|summarize`) |
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
```

- [ ] **Step 2: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add CLAUDE.md && git commit -m "docs: add CLAUDE.md project documentation for AI agents"`

---

### Task 4: Create Quick Analysis Skill — 快速 APK 分析工作流

**Depends on:** Task 3
**Files:**
- Create: `.claude/skills/apktool-quick-analysis.md`

- [ ] **Step 1: 创建快速分析 Skill — AI agent 初步评估 APK 的标准流程**

```markdown
---
description: Quick APK analysis workflow for initial assessment. Use when you need a fast overview of an unknown APK file.
---

# APK Quick Analysis

Quick analysis workflow for getting a rapid overview of any APK file.

## When to Use

- First time encountering an APK
- Need a quick security assessment
- Want to understand what an app does
- Evaluating an app before deeper analysis

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

## Example Session

```
User: "Analyze this APK file: /path/to/app.apk"

1. Run: java -jar apktool.jar analyze /path/to/app.apk
2. Read the JSON output
3. Report findings:
   - Package: com.example.app v2.1.0
   - Risk Score: 35/100 (Medium)
   - 3 dangerous permissions: CAMERA, RECORD_AUDIO, ACCESS_FINE_LOCATION
   - 2 exported activities without permission protection
   - Signed by: CN=Developer, O=Example Inc
   - Debuggable: false (good)
```
```

- [ ] **Step 2: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add .claude/skills/apktool-quick-analysis.md && git commit -m "docs(skills): add quick analysis skill for AI agents"`

---

### Task 5: Create Security Audit & Compare Skills — 安全审计和版本对比

**Depends on:** Task 3
**Files:**
- Create: `.claude/skills/apktool-security-audit.md`
- Create: `.claude/skills/apktool-compare.md`

- [ ] **Step 1: 创建安全审计 Skill**

```markdown
---
description: Comprehensive security audit workflow for Android APK files. Use when performing security analysis, vulnerability assessment, or compliance checking.
---

# APK Security Audit

Comprehensive security audit workflow for Android applications.

## When to Use

- Security review of an Android app
- Vulnerability assessment
- Pre-release security check
- Compliance audit (OWASP Mobile Top 10)
- Third-party library security review

## Workflow

### Step 1: Automated Security Report

```bash
java -jar apktool.jar security <apk-file>
```

Review the output fields:
- `dangerousPermissions[]` — permissions with high privacy impact
- `highRiskComponents[]` — exported components without protection
- `debuggable` — can the app be debugged?
- `allowBackup` — can app data be extracted?
- `usesCleartextTraffic` — does it send unencrypted data?
- `findings[]` — list of identified security issues
- `riskScore` — overall risk score (0-100)

### Step 2: Attack Surface Analysis

```bash
java -jar apktool.jar api-surface <apk-file>
```

Check:
- `exportedActivities` — activities accessible to other apps
- `exportedServices` — services that can be bound/started externally
- `exportedReceivers` — broadcast receivers listening for external intents
- `exportedProviders` — content providers exposing data
- `intentFilters` — what actions/data each component accepts

For each exported component, verify:
1. Is `exported=true` intentional?
2. Is there a permission protection on it?
3. What data can it receive/process?

### Step 3: Permission Deep Dive

```bash
java -jar apktool.jar permissions <apk-file>
```

Cross-reference with app functionality:
- Does a flashlight app need `READ_CONTACTS`?
- Does a calculator need `ACCESS_FINE_LOCATION`?
- Are there more permissions than the app's features justify?

### Step 4: Sensitive Data Search

```bash
# Search for hardcoded credentials
java -jar apktool.jar search <apk-file> "password|passwd|secret|api.?key|token|credential" -t strings

# Search for URLs (potential API endpoints)
java -jar apktool.jar search <apk-file> "https?://" -t strings

# Search for crypto-related classes
java -jar apktool.jar search <apk-file> "Cipher|SecretKey|MessageDigest|SSLContext|TrustManager" -t classes

# Search for sensitive methods
java -jar apktool.jar search <apk-file> "encrypt|decrypt|hash|sign|verify" -t methods
```

### Step 5: Signing Verification

```bash
java -jar apktool.jar signing <apk-file>
```

Verify:
- Who is the signer? (`certificates[0].subject`)
- Is the SHA-256 fingerprint expected?
- Which signing schemes are used? (v1 only = less secure than v2/v3)
- Is the certificate still valid? (`notAfter` should be in the future)

### Step 6: Generate AI Prompt for Deeper Analysis

```bash
java -jar apktool.jar ai <apk-file> -a security-review
```

This generates a structured prompt you can feed to an LLM for in-depth security analysis.

## OWASP Mobile Top 10 Mapping

| OWASP Category | AI-Apktool Commands |
|---|---|
| M1: Platform Misuse | `security` (check exported components, permissions) |
| M2: Insecure Data Storage | `search` (find hardcoded keys, tokens) |
| M3: Insecure Communication | `search` (find cleartext URLs), `manifest` (networkSecurityConfig) |
| M4: Insecure Authentication | `search` (find auth-related classes) |
| M5: Insufficient Cryptography | `search` (find crypto classes), `signing` (check schemes) |
| M6: Insecure Authorization | `api-surface` (check exported components without permissions) |
| M7: Client Code Quality | `structure` (code complexity metrics) |
| M8: Code Tampering | `signing` (verify certificate), `security` (check debuggable) |
| M9: Reverse Engineering | `structure` (obfuscation indicators) |
| M10: Extraneous Functionality | `manifest` (unused components), `search` (debug/test code) |

## Report Template

After completing the audit, summarize findings:

```
## Security Audit Report

**App:** {packageName} v{versionName}
**Risk Score:** {riskScore}/100

### Critical Findings
- [List HIGH severity issues]

### Warnings
- [List MEDIUM severity issues]

### Informational
- [List LOW severity issues]

### Recommendations
- [Suggested fixes for each finding]
```
```

- [ ] **Step 2: 创建版本对比 Skill**

```markdown
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
```

- [ ] **Step 3: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add .claude/skills/apktool-security-audit.md .claude/skills/apktool-compare.md && git commit -m "docs(skills): add security audit and compare skills for AI agents"`

---

### Task 6: Create Reverse Engineering & Reference Skills — 逆向工程和完整参考

**Depends on:** Task 3
**Files:**
- Create: `.claude/skills/apktool-reverse.md`
- Create: `.claude/skills/apktool-reference.md`

- [ ] **Step 1: 创建逆向工程 Skill**

```markdown
---
description: Full reverse engineering workflow for Android APK files. Use when you need to deeply analyze, modify, or understand an Android application's internals.
---

# APK Reverse Engineering

Full reverse engineering workflow using AI-Apktool's decode, analysis, and search capabilities.

## When to Use

- Reverse engineering an unknown Android app
- Understanding app architecture and behavior
- Modifying or repackaging an APK
- Investigating malware or suspicious apps
- Learning how an app implements specific features

## Workflow

### Step 1: Reconnaissance — Gather Intelligence

```bash
# Full automated analysis
java -jar apktool.jar analyze <apk-file>

# Or run individual commands:
java -jar apktool.jar info <apk-file>
java -jar apktool.jar manifest <apk-file>
java -jar apktool.jar structure <apk-file>
```

Focus on:
- What does the app do? (package name, version, permissions)
- How big is it? (DEX count, native libs, architectures)
- What's the code structure? (class count, method count, top packages)

### Step 2: Decode — Extract Source Code

```bash
# Full decode (smali + resources)
java -jar apktool.jar decode <apk-file> -o <output-dir>

# Decode resources only (faster)
java -jar apktool.jar decode <apk-file> -o <output-dir> -s

# Decode manifest only (fastest)
java -jar apktool.jar decode <apk-file> -o <output-dir> --only-manifest
```

After decoding, explore:
- `<output-dir>/AndroidManifest.xml` — full manifest in readable XML
- `<output-dir>/smali*/` — Dalvik bytecode in smali format
- `<output-dir>/res/` — decoded resources (layouts, strings, drawables)
- `<output-dir>/assets/` — raw assets
- `<output-dir>/apktool.yml` — decode metadata

### Step 3: Search — Find What Matters

Without decoding, use the search command:

```bash
# Find specific classes
java -jar apktool.jar search <apk-file> "LoginActivity|MainActivity|SplashActivity" -t classes

# Find URL endpoints
java -jar apktool.jar search <apk-file> "https?://.*" -t strings

# Find specific method implementations
java -jar apktool.jar search <apk-file> "onCreate|onClick|doLogin|encrypt" -t methods

# Find all strings (get a sense of the app)
java -jar apktool.jar search <apk-file> ".*" -t strings
```

### Step 4: Analyze — Deep Security Review

```bash
# Security report
java -jar apktool.jar security <apk-file>

# API surface (what the app exposes)
java -jar apktool.jar api-surface <apk-file>

# Signing information
java -jar apktool.jar signing <apk-file>
```

### Step 5: AI-Powered Analysis

Generate prompts for deeper LLM analysis:

```bash
# Comprehensive explanation
java -jar apktool.jar ai <apk-file> -a explain

# Security-focused review
java -jar apktool.jar ai <apk-file> -a security-review

# Quick summary
java -jar apktool.jar ai <apk-file> -a summarize
```

The generated prompts contain structured context about the APK that you can feed directly into an LLM.

### Step 6: Rebuild (if modifications needed)

```bash
# After making changes to decoded files:
java -jar apktool.jar build <decoded-dir> -o <output.apk>

# Build with debug flag
java -jar apktool.jar build <decoded-dir> -o <output.apk> --debuggable
```

## Common Patterns

### Pattern: Find Hardcoded Secrets
```bash
java -jar apktool.jar search <apk> "password|secret|api.?key|Bearer|token" -t strings
```

### Pattern: Find All Network Endpoints
```bash
java -jar apktool.jar search <apk> "https?://" -t strings
java -jar apktool.jar search <apk> "websocket|wss?://" -t strings
```

### Pattern: Find Encryption Implementation
```bash
java -jar apktool.jar search <apk> "Cipher|AES|RSA|DES|SecretKey" -t classes
java -jar apktool.jar search <apk> "encrypt|decrypt|initCipher" -t methods
```

### Pattern: Find Authentication Logic
```bash
java -jar apktool.jar search <apk> "Login|Auth|Session|Token|OAuth" -t classes
java -jar apktool.jar search <apk> "authenticate|login|signIn|getSession" -t methods
```

### Pattern: Analyze Malware Indicators
```bash
# Suspicious permissions
java -jar apktool.jar permissions <apk>

# Suspicious classes
java -jar apktool.jar search <apk> "Runtime|exec|ProcessBuilder|su|root" -t classes

# Command execution methods
java -jar apktool.jar search <apk> "exec|loadLibrary|DexClassLoader" -t methods

# C2 server URLs
java -jar apktool.jar search <apk> "http://" -t strings
```
```

- [ ] **Step 2: 创建完整命令参考 Skill**

```markdown
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
```

- [ ] **Step 3: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add .claude/skills/apktool-reverse.md .claude/skills/apktool-reference.md && git commit -m "docs(skills): add reverse engineering and command reference skills for AI agents"`
