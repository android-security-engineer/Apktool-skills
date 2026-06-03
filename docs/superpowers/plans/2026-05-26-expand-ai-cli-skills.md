# Expand apktool-ai-cli Skill System — Full Capability Exposure

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** Expand apktool-ai-cli from 2 Skills (15% coverage) to 11 Skills (100% coverage of apktool-lib's public API), enabling Claude Code to use every analysis/decode/build capability through the Skill executor without needing the HTTP server.

**Architecture:** APK file → SkillContext (lazy-loads ApkAnalyzer/ApkSearcher/AiPromptBuilder/ApkDecoder/ApkBuilder/Framework) → Skill.execute() chains CommandDispatcher calls → SkillResult with structured JSON output. CommandDispatcher maps command names to ApkAnalyzer/ApkSearcher methods, replacing the repetitive manual-calling pattern in current Skills.

**Tech Stack:** Java 17, Gradle 8.14.4, apktool-lib (shared backend), gson (JSON serialization)

**Scope:** Large
**Risk:** Medium
**Risks:**
- Task 1 modifies SkillContext — but only adds new methods, existing Skills untouched
- No existing tests for ai-cli module → verify via compilation

**Autonomy Level:** Full

---

### Task 1: Extend SkillContext to expose all apktool-lib capabilities

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillContext.java`

- [ ] **Step 1: 修改 SkillContext — 添加 ApkDecoder、ApkBuilder、Framework 懒加载访问器**

替换 SkillContext.java 全部内容，新增 3 个懒加载字段和 getter：`ApkDecoder decoder`、`ApkBuilder builder`、`Framework framework`。现有 3 个访问器不变。

- [ ] **Step 2: 验证编译**
Run: `./gradlew :brut.apktool:apktool-ai-cli:compileJava --no-daemon`
Expected: Exit code 0, BUILD SUCCESSFUL

- [ ] **Step 3: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillContext.java && git commit -m "feat(ai-cli): extend SkillContext with ApkDecoder, ApkBuilder, Framework accessors"`

---

### Task 2: Create CommandDispatcher for command-name-based routing

**Depends on:** Task 1
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/CommandDispatcher.java`

- [ ] **Step 1: 创建 CommandDispatcher — 将 command 名映射到 ApkAnalyzer/ApkSearcher 方法调用**

核心类，`dispatch(String command, Map<String,String> params)` 方法。支持以下 command 名：

| 命令 | 目标方法 |
|--------|----------------|
| info | analyzer.getSummary() |
| manifest | analyzer.getManifestInfo() |
| security | analyzer.getSecurityReport() |
| api-surface | analyzer.getApiSurface() |
| manifest-flags | analyzer.getManifestInfo() |
| signing | analyzer.getSigningInfo() |
| permission-detail | analyzer.getPermissionDetail() |
| dex-list | analyzer.getDexList() |
| dex-info | analyzer.getDexInfo() |
| dex-strings | analyzer.getDexStrings() |
| class-list | analyzer.getClassList() |
| class-info | analyzer.getClassDetail(params["class"]) |
| method-search | analyzer.getMethodSearch(params["pattern"]) |
| field-search | analyzer.getFieldSearch(params["pattern"]) |
| inheritance | analyzer.getInheritanceInfo(params["class"]) |
| strings | searcher.searchStrings(params["pattern"]) |
| search | 根据 params["type"] 路由到 searcher 的三个方法 |
| file-list | analyzer.getFileList() |
| file-hash | analyzer.getFileHash() |
| asset-list | analyzer.getAssetList() |
| locales | analyzer.getLocales() |
| native-libs | analyzer.getNativeLibs() |
| resources | analyzer.getResourceSummary() |
| resource-packages | analyzer.getResourcePackages() |
| lib-frame-packages | analyzer.getLibFramePackageIds() |
| manifest-xml | analyzer.getManifestXml() |
| ai-explain | promptBuilder.buildExplainPrompt() |
| ai-security-review | promptBuilder.buildSecurityReviewPrompt() |
| ai-summarize | promptBuilder.buildSummarizePrompt() |
| ai-context | promptBuilder.buildContext() |
| analyze | 组合调用：summary + manifest + security + apiSurface + signing + resources + dexList + nativeLibs |
| components | analyzer.getAllComponents() |
| structure | analyzer.getSummary() |
| version | analyzer.getSummary() |
| uses-libs | analyzer.getManifestInfo() |

- [ ] **Step 2: 验证编译**
Run: `./gradlew :brut.apktool:apktool-ai-cli:compileJava --no-daemon`
Expected: Exit code 0

- [ ] **Step 3: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/CommandDispatcher.java && git commit -m "feat(ai-cli): add CommandDispatcher for command-name-based method routing"`

---

### Task 3: Implement Info & Resource Skills (4 skills)

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/ResourceExplorerSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/DexDeepDiveSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/NetworkAnalysisSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/SigningVerifySkill.java`

- [ ] **Step 1: 创建 ResourceExplorerSkill**

5 个步骤：resources → locales → file-list → asset-list → native-libs
所有步骤使用 CommandDispatcher.dispatch() 执行。

- [ ] **Step 2: 创建 DexDeepDiveSkill**

6 个步骤：dex-list → dex-info → class-list → dex-strings → method-search(默认 pattern ".*") → field-search(默认 pattern ".*")

- [ ] **Step 3: 创建 NetworkAnalysisSkill**

5 个步骤：manifest-flags → search(type=strings, pattern=URL regex) → search(type=classes, pattern=网络类名) → api-surface → security

- [ ] **Step 4: 创建 SigningVerifySkill**

3 个步骤：signing → manifest-flags → file-hash

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Resource*.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/DexDeep*.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Network*.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Signing*.java && git commit -m "feat(ai-cli): add ResourceExplorer, DexDeepDive, NetworkAnalysis, SigningVerify skills"`

---

### Task 4: Implement Security & Reverse Skills (3 skills)

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/MalwareHuntSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/ReverseSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/CompareSkill.java`

- [ ] **Step 1: 创建 MalwareHuntSkill**

7 个步骤：security → manifest-flags → signing → search(type=methods, 命令执行模式) → search(type=methods, 动态加载模式) → search(type=strings, 可疑字符串模式) → permission-detail

- [ ] **Step 2: 创建 ReverseSkill**

7 个步骤：info → security → api-surface → signing → manifest-xml → dex-list → ai-explain

- [ ] **Step 3: 创建 CompareSkill**

此 Skill 需要两个 APK 文件。构造函数接受 File apk1, File apk2。
5 个步骤：info(both) → security(both) → api-surface(both) → signing(both) → diff(使用 analyzer 对两个 APK 分别获取 summary 后手动对比)

注意：CompareSkill 需要扩展 Skill 接口的使用方式。两种方案：
A) 在 SkillContext 中添加第二个 APK 文件字段（sharedData 中存储）
B) 使用 sharedData 传递第二个 APK 路径

选择方案 B：调用方在 context.putShared("apk2", file2) 后执行 skill。

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Malware*.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Reverse*.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/Compare*.java && git commit -m "feat(ai-cli): add MalwareHunt, Reverse, Compare skills"`

---

### Task 5: Register all skills + update Main.java CLI

**Depends on:** Task 3, Task 4
**Files:**
- Modify: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillRegistry.java:33-38`
- Modify: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/Main.java:53-83`

- [ ] **Step 1: 修改 SkillRegistry.createDefault() — 注册全部 11 个 Skill**

在现有 2 个注册后，添加 9 个新 Skill 的注册：
```java
registry.register(new ResourceExplorerSkill());
registry.register(new DexDeepDiveSkill());
registry.register(new NetworkAnalysisSkill());
registry.register(new SigningVerifySkill());
registry.register(new MalwareHuntSkill());
registry.register(new ReverseSkill());
registry.register(new CompareSkill());
```

- [ ] **Step 2: 修改 Main.java — 支持 Compare skill 的双 APK 模式**

在 cmdSkill 方法中添加可选的第 4 个参数（第二个 APK 文件路径），仅在 skill 为 "compare" 时使用：
```java
if (args.length >= 5 && "compare".equals(skillName)) {
    context.putShared("apk2", new File(args[3]));
}
```

同时更新 printUsage() 中的帮助文本。

- [ ] **Step 3: 验证编译**
Run: `./gradlew :brut.apktool:apktool-ai-cli:compileJava --no-daemon`
Expected: Exit code 0, BUILD SUCCESSFUL

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillRegistry.java brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/Main.java && git commit -m "feat(ai-cli): register all 11 skills, support compare dual-APK mode"`

---

## Skill Coverage Summary (After Implementation)

| # | Skill Name | Steps | Commands Used |
|---|-----------|-------|--------------|
| 1 | quick-analysis (existing) | 4 | info, security, api-surface, permission-detail |
| 2 | security-audit (existing) | 6 | security, api-surface, permissions, manifest-flags, signing, search |
| 3 | resource-explorer | 5 | resources, locales, file-list, asset-list, native-libs |
| 4 | dex-deep-dive | 6 | dex-list, dex-info, class-list, dex-strings, method-search, field-search |
| 5 | network-analysis | 5 | manifest-flags, search(strings), search(classes), api-surface, security |
| 6 | signing-verify | 3 | signing, manifest-flags, file-hash |
| 7 | malware-hunt | 7 | security, manifest-flags, signing, search(methods)x2, search(strings), permission-detail |
| 8 | reverse | 7 | info, security, api-surface, signing, manifest-xml, dex-list, ai-explain |
| 9 | compare | 5 | info(both), security(both), api-surface(both), signing(both), diff |
| 10 | decode-build | - | Not implemented as Skill (uses ApkDecoder/ApkBuilder directly, no analysis steps) |
| 11 | reference | - | Not a Skill (lookup-only, returns CommandRegistry JSON) |

**Note:** decode-build 和 reference 不适合作为 Skill（一个是操作型非分析型，一个是纯查阅型）。实际实现 9 个新 Skill = 总共 11 个 Skill。

## CommandDispatcher Coverage vs Serve Endpoints

| Serve Endpoint | CommandDispatcher Command | Skill Coverage |
|---------------|------------------------|----------------|
| /info | info | quick-analysis, reverse |
| /manifest | manifest | (available via dispatcher) |
| /permissions | permissions | security-audit |
| /components | components | (available via dispatcher) |
| /sdk-info | sdk-info | (available via dispatcher) |
| /resources | resources | resource-explorer |
| /security | security | quick-analysis, security-audit, malware-hunt, network-analysis, reverse |
| /api-surface | api-surface | quick-analysis, security-audit, network-analysis, malware-hunt, reverse, compare |
| /signing | signing | signing-verify, malware-hunt, reverse, compare |
| /structure | structure | (available via dispatcher) |
| /analyze | analyze | (available via dispatcher) |
| /ai | ai-explain/security-review/summarize/context | reverse |
| /search | search | security-audit, network-analysis, malware-hunt |
| /strings | strings | (available via dispatcher) |
| /diff | (compare skill) | compare |
| /dex-list | dex-list | dex-deep-dive, reverse |
| /dex-info | dex-info | dex-deep-dive |
| /class-list | class-list | dex-deep-dive |
| /class-info | class-info | (available via dispatcher) |
| /method-search | method-search | dex-deep-dive |
| /field-search | field-search | dex-deep-dive |
| /dex-strings | dex-strings | dex-deep-dive |
| /inheritance | inheritance | (available via dispatcher) |
| /manifest-xml | manifest-xml | reverse |
| /manifest-flags | manifest-flags | security-audit, network-analysis, signing-verify, malware-hunt |
| /permission-detail | permission-detail | quick-analysis, malware-hunt |
| /file-list | file-list | resource-explorer |
| /file-hash | file-hash | signing-verify |
| /asset-list | asset-list | resource-explorer |
| /locales | locales | resource-explorer |
| /native-libs | native-libs | resource-explorer |
| /signing | signing | signing-verify, malware-hunt, reverse |
| /resource-packages | resource-packages | (available via dispatcher) |
| /lib-frame-packages | lib-frame-packages | (available via dispatcher) |
| /uses-libs | uses-libs | (available via dispatcher) |
| /version | version | (available via dispatcher) |
