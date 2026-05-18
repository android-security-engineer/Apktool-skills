# CLI 完整性补全 & HTTP API 对齐 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 补全 AI-Apktool CLI 入口的所有缺口（添加 `strings` 独立命令、完善 `structure` 和 `diff` 分析逻辑），并将 HTTP API 对齐到 CLI 的完整能力，确保 AI agent 通过 CLI 或 HTTP API 都能调用全部分析能力。

**Architecture:** CLI 入口 → ApkAnalyzer/ApkDiff/ApkSearcher → JSON 输出。HTTP API 端点 → ApiHandler → 同样的分析引擎 → JSON 响应。补全方向：CLI 侧添加缺失的 `strings` 命令入口，引擎侧完善 `getStructure()` 和 `diff()` 的 receivers/providers 对比，HTTP API 侧添加 11 个缺失端点以对齐 CLI。

**Tech Stack:** Java 17, Gradle Kotlin DSL, Javalin 5.6.3, Apache Commons CLI

**Risks:**
- Task 1 修改 ApkDiff.java 添加 receivers/providers diff → 缓解：只添加新字段和新逻辑，不修改现有字段
- Task 2 修改 ApkDiff.getStructure() 需要读取 DEX 文件 → 缓解：复用 ApkSearcher 的 DEX 解析模式
- Task 3 修改 Main.java 添加 strings 命令 → 缓解：复用 search 命令的 strings 分支逻辑
- Task 4 修改 HTTP API 模块 → 缓解：每个新端点遵循现有端点的模式

---

### Task 1: 完善 DiffResult 和 ApkDiff — 添加 receivers/providers 对比

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java`
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java`

- [ ] **Step 1: 修改 DiffResult — 添加 receivers/providers 增删字段**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java`

```java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class DiffResult {
    private List<String> addedPermissions = new ArrayList<>();
    private List<String> removedPermissions = new ArrayList<>();
    private List<String> addedActivities = new ArrayList<>();
    private List<String> removedActivities = new ArrayList<>();
    private List<String> addedServices = new ArrayList<>();
    private List<String> removedServices = new ArrayList<>();
    private List<String> addedReceivers = new ArrayList<>();
    private List<String> removedReceivers = new ArrayList<>();
    private List<String> addedProviders = new ArrayList<>();
    private List<String> removedProviders = new ArrayList<>();
    private List<String> addedDexFiles = new ArrayList<>();
    private List<String> removedDexFiles = new ArrayList<>();
    private List<String> addedNativeLibs = new ArrayList<>();
    private List<String> removedNativeLibs = new ArrayList<>();
    private String versionCodeChange;
    private String versionNameChange;
    private String targetSdkChange;

    public List<String> getAddedPermissions() { return addedPermissions; }
    public void setAddedPermissions(List<String> addedPermissions) { this.addedPermissions = addedPermissions; }
    public List<String> getRemovedPermissions() { return removedPermissions; }
    public void setRemovedPermissions(List<String> removedPermissions) { this.removedPermissions = removedPermissions; }
    public List<String> getAddedActivities() { return addedActivities; }
    public void setAddedActivities(List<String> addedActivities) { this.addedActivities = addedActivities; }
    public List<String> getRemovedActivities() { return removedActivities; }
    public void setRemovedActivities(List<String> removedActivities) { this.removedActivities = removedActivities; }
    public List<String> getAddedServices() { return addedServices; }
    public void setAddedServices(List<String> addedServices) { this.addedServices = addedServices; }
    public List<String> getRemovedServices() { return removedServices; }
    public void setRemovedServices(List<String> removedServices) { this.removedServices = removedServices; }
    public List<String> getAddedReceivers() { return addedReceivers; }
    public void setAddedReceivers(List<String> addedReceivers) { this.addedReceivers = addedReceivers; }
    public List<String> getRemovedReceivers() { return removedReceivers; }
    public void setRemovedReceivers(List<String> removedReceivers) { this.removedReceivers = removedReceivers; }
    public List<String> getAddedProviders() { return addedProviders; }
    public void setAddedProviders(List<String> addedProviders) { this.addedProviders = addedProviders; }
    public List<String> getRemovedProviders() { return removedProviders; }
    public void setRemovedProviders(List<String> removedProviders) { this.removedProviders = removedProviders; }
    public List<String> getAddedDexFiles() { return addedDexFiles; }
    public void setAddedDexFiles(List<String> addedDexFiles) { this.addedDexFiles = addedDexFiles; }
    public List<String> getRemovedDexFiles() { return removedDexFiles; }
    public void setRemovedDexFiles(List<String> removedDexFiles) { this.removedDexFiles = removedDexFiles; }
    public List<String> getAddedNativeLibs() { return addedNativeLibs; }
    public void setAddedNativeLibs(List<String> addedNativeLibs) { this.addedNativeLibs = addedNativeLibs; }
    public List<String> getRemovedNativeLibs() { return removedNativeLibs; }
    public void setRemovedNativeLibs(List<String> removedNativeLibs) { this.removedNativeLibs = removedNativeLibs; }
    public String getVersionCodeChange() { return versionCodeChange; }
    public void setVersionCodeChange(String versionCodeChange) { this.versionCodeChange = versionCodeChange; }
    public String getVersionNameChange() { return versionNameChange; }
    public void setVersionNameChange(String versionNameChange) { this.versionNameChange = versionNameChange; }
    public String getTargetSdkChange() { return targetSdkChange; }
    public void setTargetSdkChange(String targetSdkChange) { this.targetSdkChange = targetSdkChange; }
}
```

- [ ] **Step 2: 修改 ApkDiff.diff() — 添加 receivers/providers 对比逻辑**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java:10-47`（替换 diff 方法）

```java
    public static DiffResult diff(File apk1, File apk2, Config config) throws AndrolibException {
        DiffResult result = new DiffResult();

        ApkAnalyzer analyzer1 = new ApkAnalyzer(apk1, config);
        ApkAnalyzer analyzer2 = new ApkAnalyzer(apk2, config);

        ManifestInfo m1 = analyzer1.getManifestInfo();
        ManifestInfo m2 = analyzer2.getManifestInfo();

        if (m1 != null && m2 != null) {
            Set<String> p1 = new HashSet<>(m1.getPermissions());
            Set<String> p2 = new HashSet<>(m2.getPermissions());
            result.setAddedPermissions(findAdded(p1, p2));
            result.setRemovedPermissions(findRemoved(p1, p2));

            Set<String> a1 = extractNames(m1.getActivities());
            Set<String> a2 = extractNames(m2.getActivities());
            result.setAddedActivities(findAdded(a1, a2));
            result.setRemovedActivities(findRemoved(a1, a2));

            Set<String> s1 = extractNames(m1.getServices());
            Set<String> s2 = extractNames(m2.getServices());
            result.setAddedServices(findAdded(s1, s2));
            result.setRemovedServices(findRemoved(s1, s2));

            Set<String> r1 = extractNames(m1.getReceivers());
            Set<String> r2 = extractNames(m2.getReceivers());
            result.setAddedReceivers(findAdded(r1, r2));
            result.setRemovedReceivers(findRemoved(r1, r2));

            Set<String> pv1 = extractNames(m1.getProviders());
            Set<String> pv2 = extractNames(m2.getProviders());
            result.setAddedProviders(findAdded(pv1, pv2));
            result.setRemovedProviders(findRemoved(pv1, pv2));

            if (m1.getVersionCode() != m2.getVersionCode()) {
                result.setVersionCodeChange(m1.getVersionCode() + " -> " + m2.getVersionCode());
            }
            if (!Objects.equals(m1.getVersionName(), m2.getVersionName())) {
                result.setVersionNameChange(m1.getVersionName() + " -> " + m2.getVersionName());
            }
            if (!Objects.equals(m1.getTargetSdkVersion(), m2.getTargetSdkVersion())) {
                result.setTargetSdkChange(m1.getTargetSdkVersion() + " -> " + m2.getTargetSdkVersion());
            }
        }

        return result;
    }
```

- [ ] **Step 3: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java && git commit -m "feat(analyze): add receivers/providers diff to ApkDiff"`

---

### Task 2: 完善 ApkDiff.getStructure() — 真正扫描 DEX 获取代码结构

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java:49-55`（替换 getStructure 方法）

- [ ] **Step 1: 修改 ApkDiff.getStructure() — 扫描 DEX 文件获取类/方法/字段/包信息**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java:49-55`（替换 getStructure 方法，并添加必要的 import）

在文件顶部添加 import（在现有 import 之后）：

```java
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import java.io.IOException;
```

替换 getStructure 方法：

```java
    public static StructureInfo getStructure(File apkFile, Config config) throws AndrolibException {
        StructureInfo info = new StructureInfo();
        ExtFile extFile = new ExtFile(apkFile);

        try {
            ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
            ApkSummary summary = analyzer.getSummary();
            info.setDexCount(summary.getDexCount());

            ZipDexContainer container = new ZipDexContainer(extFile, null);
            int totalClasses = 0;
            int totalMethods = 0;
            int totalFields = 0;
            Map<String, Integer> packageCounts = new LinkedHashMap<>();
            List<String> topClasses = new ArrayList<>();
            Map<String, Integer> dexClassCounts = new LinkedHashMap<>();

            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                int dexClassCount = 0;

                for (ClassDef classDef : dexFile.getClasses()) {
                    dexClassCount++;
                    totalClasses++;

                    String className = classDef.getType();
                    String humanName = className.substring(1, className.length() - 1).replace('/', '.');

                    // Extract package name
                    int lastDot = humanName.lastIndexOf('.');
                    if (lastDot > 0) {
                        String pkg = humanName.substring(0, lastDot);
                        packageCounts.merge(pkg, 1, Integer::sum);
                    }

                    // Count methods and fields
                    int methodCount = 0;
                    for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                        methodCount++;
                    }
                    totalMethods += methodCount;

                    int fieldCount = 0;
                    for (com.android.tools.smali.dexlib2.iface.Field field : classDef.getFields()) {
                        fieldCount++;
                    }
                    totalFields += fieldCount;

                    topClasses.add(humanName + " (" + methodCount + " methods, " + fieldCount + " fields)");
                }

                dexClassCounts.put(dexName, dexClassCount);
            }

            info.setTotalClasses(totalClasses);
            info.setTotalMethods(totalMethods);
            info.setTotalFields(totalFields);
            info.setPackageCounts(packageCounts);
            // Keep top 20 classes by method count
            topClasses.sort((a, b) -> {
                int ma = Integer.parseInt(a.replaceAll(".*\\((\\d+) methods.*", "$1"));
                int mb = Integer.parseInt(b.replaceAll(".*\\((\\d+) methods.*", "$1"));
                return mb - ma;
            });
            info.setTopClasses(topClasses.subList(0, Math.min(20, topClasses.size())));
            info.setDexClassCounts(dexClassCounts);

        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }

        return info;
    }
```

- [ ] **Step 2: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 3: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java && git commit -m "feat(analyze): implement full DEX scanning in getStructure()"`

---

### Task 3: 添加 strings 独立命令到 CLI — 对齐 CommandRegistry

**Depends on:** Task 2
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:317-421`（在 switch 中添加 strings case）

- [ ] **Step 1: 修改 Main.java — 添加 strings 命令入口**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:384-386`（在 `case "search":` 之前添加 strings case）

在 `case "search":` 行之前插入：

```java
            case "strings":
                cmdStrings(cmdArgs);
                break;
```

- [ ] **Step 2: 添加 cmdStrings 方法到 Main.java — 调用 ApkSearcher.searchStrings()**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:994-995`（在 cmdSearch 方法之前添加）

在 `private static final Option searchTypeOption` 行之前插入：

```java
    private static final Option stringsPatternOption = Option.builder("p")
        .longOpt("pattern")
        .desc("Filter strings by regex pattern. (default: .*)")
        .hasArg()
        .argName("pattern")
        .get();

    private static final Options stringsOptions = new Options();

    private static void cmdStrings(String[] args) throws AndrolibException {
        stringsOptions.addOption(stringsPatternOption);
        CommandLine cli = parseOptions(stringsOptions, args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String pattern = cli.getOptionValue(stringsPatternOption, ".*");

        brut.androlib.search.ApkSearcher searcher =
            new brut.androlib.search.ApkSearcher(new File(apkName), config);
        brut.androlib.search.SearchResult result = searcher.searchStrings(pattern);
        System.out.println(brut.androlib.output.JsonOutput.toJson(result));
    }

```

- [ ] **Step 3: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add strings command as standalone entry point"`

---

### Task 4: 对齐 HTTP API — 添加 11 个缺失端点

**Depends on:** Task 3
**Files:**
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java`
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`

- [ ] **Step 1: 修改 ApiHandler — 添加 11 个缺失的处理方法**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java`（完整替换）

```java
package brut.apktool.serve;

import brut.androlib.Config;
import brut.androlib.analyze.*;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import brut.androlib.ai.AiPromptBuilder;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiHandler {
    private final Config config;

    public ApiHandler(Config config) {
        this.config = config;
    }

    public String handleInfo(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSummary());
    }

    public String handleManifest(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        return JsonOutput.toJson(manifest);
    }

    public String handlePermissions(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        return JsonOutput.toJson(manifest.getPermissions());
    }

    public String handleComponents(String apkPath, String type) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        java.util.List<ComponentInfo> components;
        switch (type) {
            case "activities": components = manifest.getActivities(); break;
            case "services": components = manifest.getServices(); break;
            case "receivers": components = manifest.getReceivers(); break;
            case "providers": components = manifest.getProviders(); break;
            default: components = java.util.Collections.emptyList(); break;
        }
        return JsonOutput.toJson(components);
    }

    public String handleAllComponents(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getAllComponents());
    }

    public String handleSdkInfo(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "No AndroidManifest.xml found");
            return JsonOutput.toJson(error);
        }
        Map<String, String> sdkInfo = new LinkedHashMap<>();
        if (manifest.getMinSdkVersion() != null) sdkInfo.put("minSdkVersion", manifest.getMinSdkVersion());
        if (manifest.getTargetSdkVersion() != null) sdkInfo.put("targetSdkVersion", manifest.getTargetSdkVersion());
        if (manifest.getMaxSdkVersion() != null) sdkInfo.put("maxSdkVersion", manifest.getMaxSdkVersion());
        return JsonOutput.toJson(sdkInfo);
    }

    public String handleSecurity(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSecurityReport());
    }

    public String handleApiSurface(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getApiSurface());
    }

    public String handleSigning(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSigningInfo());
    }

    public String handleStructure(String apkPath) throws Exception {
        StructureInfo info = ApkDiff.getStructure(new File(apkPath), config);
        return JsonOutput.toJson(info);
    }

    public String handleAnalyze(String apkPath) throws Exception {
        File apkFile = new File(apkPath);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        AnalyzeResult result = new AnalyzeResult();
        result.setSummary(analyzer.getSummary());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setManifest(analyzer.getManifestInfo());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setSecurity(analyzer.getSecurityReport());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setApiSurface(analyzer.getApiSurface());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setResources(analyzer.getResourceSummary());
        analyzer = new ApkAnalyzer(apkFile, config);
        result.setSigning(analyzer.getSigningInfo());
        result.setStructure(ApkDiff.getStructure(apkFile, config));
        return JsonOutput.toJson(result);
    }

    public String handleAi(String apkPath, String action) throws Exception {
        AiPromptBuilder builder = new AiPromptBuilder(new File(apkPath), config);
        String prompt;
        switch (action) {
            case "security-review": prompt = builder.buildSecurityReviewPrompt(); break;
            case "summarize": prompt = builder.buildSummarizePrompt(); break;
            default: prompt = builder.buildExplainPrompt(); break;
        }
        return prompt;
    }

    public String handleSearch(String apkPath, String type, String pattern) throws Exception {
        ApkSearcher searcher = new ApkSearcher(new File(apkPath), config);
        SearchResult result;
        switch (type) {
            case "strings": result = searcher.searchStrings(pattern); break;
            case "methods": result = searcher.searchMethods(pattern); break;
            default: result = searcher.searchClasses(pattern); break;
        }
        return JsonOutput.toJson(result);
    }

    public String handleDiff(String apkPath1, String apkPath2) throws Exception {
        DiffResult result = ApkDiff.diff(new File(apkPath1), new File(apkPath2), config);
        return JsonOutput.toJson(result);
    }

    public String handleResources(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getResourceSummary());
    }
}
```

- [ ] **Step 2: 修改 ApktoolServer — 注册 11 个新路由**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`（完整替换）

```java
package brut.apktool.serve;

import brut.androlib.Config;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ApktoolServer {
    private final Javalin app;
    private final ApiHandler handler;

    public ApktoolServer(int port) {
        Config config = new Config("ai-apktool-serve");
        handler = new ApiHandler(config);

        app = Javalin.create();

        registerRoutes();
        app.start(port);
    }

    private void registerRoutes() {
        app.get("/api/v1/health", ctx -> ctx.result("{\"status\":\"ok\"}"));
        app.get("/api/v1/info", this::handleInfo);
        app.get("/api/v1/manifest", this::handleManifest);
        app.get("/api/v1/permissions", this::handlePermissions);
        app.get("/api/v1/activities", this::handleActivities);
        app.get("/api/v1/services", this::handleServices);
        app.get("/api/v1/receivers", this::handleReceivers);
        app.get("/api/v1/providers", this::handleProviders);
        app.get("/api/v1/components", this::handleComponents);
        app.get("/api/v1/sdk-info", this::handleSdkInfo);
        app.get("/api/v1/resources", this::handleResources);
        app.get("/api/v1/security", this::handleSecurity);
        app.get("/api/v1/api-surface", this::handleApiSurface);
        app.get("/api/v1/signing", this::handleSigning);
        app.get("/api/v1/structure", this::handleStructure);
        app.get("/api/v1/analyze", this::handleAnalyze);
        app.get("/api/v1/ai", this::handleAi);
        app.get("/api/v1/search", this::handleSearch);
        app.get("/api/v1/diff", this::handleDiff);
    }

    private void handleInfo(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleInfo(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleManifest(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleManifest(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handlePermissions(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handlePermissions(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleActivities(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "activities"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleServices(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "services"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleReceivers(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "receivers"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleProviders(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleComponents(apk, "providers"));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleComponents(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleAllComponents(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSdkInfo(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSdkInfo(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleResources(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleResources(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSecurity(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSecurity(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleApiSurface(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleApiSurface(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSigning(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSigning(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleStructure(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleStructure(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAnalyze(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleAnalyze(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAi(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            String action = ctx.queryParamAsClass("action", String.class).getOrDefault("explain");
            ctx.contentType("text/plain").result(handler.handleAi(apk, action));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleSearch(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            String type = ctx.queryParamAsClass("type", String.class).getOrDefault("classes");
            String pattern = ctx.queryParamAsClass("pattern", String.class).getOrDefault(".*");
            ctx.contentType("application/json").result(handler.handleSearch(apk, type, pattern));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDiff(Context ctx) {
        try {
            String apk1 = getRequiredParam(ctx, "apk1");
            String apk2 = getRequiredParam(ctx, "apk2");
            ctx.contentType("application/json").result(handler.handleDiff(apk1, apk2));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String getRequiredParam(Context ctx, String name) {
        String value = ctx.queryParam(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: " + name);
        }
        return value;
    }

    public String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    public void stop() {
        app.stop();
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        System.out.println("Starting AI-Apktool server on port " + port + "...");
        new ApktoolServer(port);
    }
}
```

- [ ] **Step 3: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-serve:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(serve): add 11 missing HTTP API endpoints to align with CLI"`
