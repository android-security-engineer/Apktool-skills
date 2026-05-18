# CLI Capability Gap Fill — Round 3 Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 修复 6 个库能力到 CLI 的 gap：locales 未填充、lib/frame 包 ID 未暴露、AnalyzeResult 缺 dexInfo/nativeLibs、AiContext 信息不够丰富、DiffResult 缺 minSdk/packageName 变更。

**Architecture:** 修复 getResourceSummary() 中 locales 填充逻辑（从 ResConfig 提取 locale）→ 新增 getLibFramePackageIds() 方法 → 扩展 AnalyzeResult 添加 dexInfo/nativeLibs 字段并在 cmdAnalyze 中填充 → 扩展 DiffResult 添加 minSdkChange/packageNameChange → 丰富 AiContext 添加 signing/sdk/resources 信息 → 更新 cmdAnalyze 调用新方法。

**Tech Stack:** Java 17, Gradle, Javalin 5.6.3, smali dexlib2

**Risks:**
- Task 1 修改数据模型（AnalyzeResult/DiffResult/AiContext），新增字段默认为 null/空，JSON 序列化兼容
- Task 2 修改 getResourceSummary() 的 locales 填充，需从 ResPackage.listTypes() → ResType.getConfig() → ResConfig.getLanguage()/getRegion() 提取 locale，需确保不重复

---

### Task 1: 修复数据模型和分析逻辑

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (修复 locales 填充 + 新增 getLibFramePackageIds)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java` (添加 dexInfo/nativeLibs 字段)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java` (添加 minSdkChange/packageNameChange)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java` (填充新 diff 字段)

- [ ] **Step 1: 修复 getResourceSummary() 中 locales 填充逻辑 — 从 ResConfig 提取 locale 信息**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (替换 getResourceSummary 方法，约 line 285-311)

```java
public ResourceSummary getResourceSummary() throws AndrolibException {
    ResourceSummary summary = new ResourceSummary();

    ApkInfo apkInfo = new ApkInfo();
    apkInfo.setApkFile(mApkFile);
    ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
    ResTable table = resDecoder.getTable();
    table.load();

    ResPackage pkg = table.getMainPackage();
    if (pkg != null) {
        summary.setPackageName(pkg.getName());
        summary.setPackageId(pkg.getId());

        Set<String> localeSet = new LinkedHashSet<>();
        int total = 0;
        for (ResEntry entry : pkg.listEntries()) {
            ResType type = entry.getType();
            String typeName = type.getName();
            Integer count = summary.getTypeCounts().get(typeName);
            summary.getTypeCounts().put(typeName, count != null ? count + 1 : 1);
            total++;

            brut.androlib.res.table.ResConfig config = type.getConfig();
            String lang = config.getLanguage();
            String region = config.getRegion();
            if (lang != null && !lang.isEmpty()) {
                String locale = region != null && !region.isEmpty() ? lang + "-r" + region : lang;
                localeSet.add(locale);
            }
        }
        summary.setTotalEntries(total);
        summary.setLocales(new ArrayList<>(localeSet));
    }

    return summary;
}
```

- [ ] **Step 2: 新增 getLibFramePackageIds 方法 — 返回共享库和框架包 ID**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (在 getResourcePackages 方法之后添加)

```java
public Map<String, Object> getLibFramePackageIds() throws AndrolibException {
    ApkInfo apkInfo = new ApkInfo();
    apkInfo.setApkFile(mApkFile);
    ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
    ResTable table = resDecoder.getTable();
    table.load();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("libPackageIds", new ArrayList<>(table.getLibPackageIds()));
    result.put("framePackageIds", new ArrayList<>(table.getFramePackageIds()));
    return result;
}
```

- [ ] **Step 3: 扩展 AnalyzeResult — 添加 dexInfo 和 nativeLibs 字段**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java` (替换整个文件)

```java
package brut.androlib.analyze;

import java.util.Map;

public class AnalyzeResult {
    private ApkSummary summary;
    private ManifestInfo manifest;
    private SecurityReport security;
    private ApiSurfaceInfo apiSurface;
    private ResourceSummary resources;
    private SigningInfo signing;
    private StructureInfo structure;
    private Map<String, Map<String, Integer>> dexInfo;
    private Map<String, Object> nativeLibs;

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
    public Map<String, Map<String, Integer>> getDexInfo() { return dexInfo; }
    public void setDexInfo(Map<String, Map<String, Integer>> dexInfo) { this.dexInfo = dexInfo; }
    public Map<String, Object> getNativeLibs() { return nativeLibs; }
    public void setNativeLibs(Map<String, Object> nativeLibs) { this.nativeLibs = nativeLibs; }
}
```

- [ ] **Step 4: 扩展 DiffResult — 添加 minSdkChange 和 packageNameChange 字段**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java` (在 targetSdkChange 字段之后添加新字段和 getter/setter)

在 `private String targetSdkChange;` 之后添加：

```java
private String minSdkChange;
private String packageNameChange;
```

在 `public void setTargetSdkChange(String targetSdkChange) { this.targetSdkChange = targetSdkChange; }` 之后添加：

```java
public String getMinSdkChange() { return minSdkChange; }
public void setMinSdkChange(String minSdkChange) { this.minSdkChange = minSdkChange; }
public String getPackageNameChange() { return packageNameChange; }
public void setPackageNameChange(String packageNameChange) { this.packageNameChange = packageNameChange; }
```

- [ ] **Step 5: 填充 DiffResult 新字段 — 在 ApkDiff.diff() 中比较 minSdk 和 packageName**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java` (在 `result.setTargetSdkChange(...)` 之后添加)

```java
if (!Objects.equals(m1.getMinSdkVersion(), m2.getMinSdkVersion())) {
    result.setMinSdkChange(m1.getMinSdkVersion() + " -> " + m2.getMinSdkVersion());
}
if (!Objects.equals(m1.getPackageName(), m2.getPackageName())) {
    result.setPackageNameChange(m1.getPackageName() + " -> " + m2.getPackageName());
}
```

- [ ] **Step 6: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 7: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/AnalyzeResult.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java && git commit -m "feat(analyze): fix locales population, add lib/frame package IDs, enrich AnalyzeResult and DiffResult"`

---

### Task 2: 增强 CLI/HTTP/AI 集成

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (更新 cmdAnalyze + 新增 cmdLibFramePackages)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiContext.java` (添加 signing/sdk/resources 字段)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiPromptBuilder.java` (丰富 buildContext)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (注册 lib-frame-packages)
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java` (新增 handler)
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java` (新增路由)

- [ ] **Step 1: 更新 cmdAnalyze — 填充 dexInfo 和 nativeLibs**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (替换 cmdAnalyze 方法)

在 `result.setStructure(structInfo);` 之后添加：

```java
analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
result.setDexInfo(analyzer.getDexInfo());

analyzer = new brut.androlib.analyze.ApkAnalyzer(apkFile, config);
result.setNativeLibs(analyzer.getNativeLibs());
```

- [ ] **Step 2: 新增 cmdLibFramePackages 命令 — 暴露共享库和框架包 ID**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 switch/case 中 `case "resource-packages":` 之后添加：

```java
case "lib-frame-packages":
    cmdLibFramePackages(cmdArgs);
    break;
```

在 cmdResourcePackages 方法之后添加：

```java
private static void cmdLibFramePackages(String[] args) throws AndrolibException {
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
    java.util.Map<String, Object> packages = analyzer.getLibFramePackageIds();
    System.out.println(brut.androlib.output.JsonOutput.toJson(packages));
}
```

在 printUsage 中 `resource-packages` 行之后添加：

```java
writer.println("  lib-frame-packages <apk> - List shared library and framework package IDs");
```

- [ ] **Step 3: 丰富 AiContext — 添加 signing/sdk/resources 信息**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiContext.java` (替换整个文件)

```java
package brut.androlib.ai;

import java.util.ArrayList;
import java.util.List;

public class AiContext {
    private String apkFileName;
    private String packageName;
    private String manifestXml;
    private List<String> permissions = new ArrayList<>();
    private List<String> components = new ArrayList<>();
    private List<String> stringResources = new ArrayList<>();
    private String securityReport;
    private int estimatedTokenCount;
    private String signingInfo;
    private String sdkInfo;
    private String resourceSummary;

    public String getApkFileName() { return apkFileName; }
    public void setApkFileName(String apkFileName) { this.apkFileName = apkFileName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getManifestXml() { return manifestXml; }
    public void setManifestXml(String manifestXml) { this.manifestXml = manifestXml; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<String> getComponents() { return components; }
    public void setComponents(List<String> components) { this.components = components; }
    public List<String> getStringResources() { return stringResources; }
    public void setStringResources(List<String> stringResources) { this.stringResources = stringResources; }
    public String getSecurityReport() { return securityReport; }
    public void setSecurityReport(String securityReport) { this.securityReport = securityReport; }
    public int getEstimatedTokenCount() { return estimatedTokenCount; }
    public void setEstimatedTokenCount(int estimatedTokenCount) { this.estimatedTokenCount = estimatedTokenCount; }
    public String getSigningInfo() { return signingInfo; }
    public void setSigningInfo(String signingInfo) { this.signingInfo = signingInfo; }
    public String getSdkInfo() { return sdkInfo; }
    public void setSdkInfo(String sdkInfo) { this.sdkInfo = sdkInfo; }
    public String getResourceSummary() { return resourceSummary; }
    public void setResourceSummary(String resourceSummary) { this.resourceSummary = resourceSummary; }
}
```

- [ ] **Step 4: 丰富 AiPromptBuilder.buildContext() — 填充 signing/sdk/resources**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiPromptBuilder.java` (在 `context.setSecurityReport(...)` 之后添加)

```java
try {
    context.setSigningInfo(JsonOutput.toJson(analyzer.getSigningInfo()));
} catch (Exception ignored) {}
try {
    brut.androlib.analyze.ResourceSummary resSummary = analyzer.getResourceSummary();
    context.setResourceSummary(JsonOutput.toJson(resSummary));
} catch (Exception ignored) {}
if (manifest != null) {
    java.util.Map<String, String> sdkMap = new java.util.LinkedHashMap<>();
    if (manifest.getMinSdkVersion() != null) sdkMap.put("minSdkVersion", manifest.getMinSdkVersion());
    if (manifest.getTargetSdkVersion() != null) sdkMap.put("targetSdkVersion", manifest.getTargetSdkVersion());
    context.setSdkInfo(JsonOutput.toJson(sdkMap));
}
```

同时更新 token 估算，在 `context.setEstimatedTokenCount(totalChars / 4);` 之前添加：

```java
if (context.getSigningInfo() != null) totalChars += context.getSigningInfo().length();
if (context.getSdkInfo() != null) totalChars += context.getSdkInfo().length();
if (context.getResourceSummary() != null) totalChars += context.getResourceSummary().length();
```

- [ ] **Step 5: 注册 lib-frame-packages 命令到 CommandRegistry**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (在 resource-packages 注册之后添加)

```java
register("lib-frame-packages", null, "List shared library and framework package IDs from resource table",
    "apktool lib-frame-packages <apk-file>", "JSON: {libPackageIds[], framePackageIds[]}",
    "analysis", new String[]{"apktool lib-frame-packages app.apk"});
```

- [ ] **Step 6: 新增 HTTP API handler 和路由**
文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java` (在 handleResourcePackages 方法之后添加)

```java
public String handleLibFramePackages(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getLibFramePackageIds());
}
```

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`

在路由注册中 `app.get("/api/v1/resource-packages", this::handleResourcePackages);` 之后添加：

```java
app.get("/api/v1/lib-frame-packages", this::handleLibFramePackages);
```

在 handleResourcePackages 方法之后添加：

```java
private void handleLibFramePackages(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleLibFramePackages(apk));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}
```

- [ ] **Step 7: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew build -x test`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 8: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiContext.java brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiPromptBuilder.java brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(cli): add lib-frame-packages command, enrich analyze and ai context"`

---

### Task 3: 更新文档

**Depends on:** Task 2
**Files:**
- Modify: `CLAUDE.md`
- Modify: `skills/reference/SKILL.md`

- [ ] **Step 1: 更新 CLAUDE.md — 添加新命令和更新 analyze 描述**

在 Analysis Commands 表中 `resource-packages` 行之后添加：

```markdown
| `lib-frame-packages` | Library/framework package IDs | libPackageIds, framePackageIds |
```

更新 `analyze` 行的 Key Fields：

```markdown
| `analyze` | Comprehensive one-shot analysis | all of the above + dexInfo + nativeLibs |
```

在 HTTP API Endpoints 部分添加：

```markdown
- `GET /api/v1/lib-frame-packages?apk=<path>` — Library/framework package IDs
```

- [ ] **Step 2: 更新 skills/reference/SKILL.md — 添加新命令和 HTTP 端点**

在 Analysis Commands 表中 `resource-packages` 行之后添加 `lib-frame-packages` 行。在 HTTP API Endpoints 部分添加对应端点。

- [ ] **Step 3: 提交**
Run: `git add CLAUDE.md skills/reference/SKILL.md && git commit -m "docs: add lib-frame-packages and enriched analyze to CLI and HTTP references"`
