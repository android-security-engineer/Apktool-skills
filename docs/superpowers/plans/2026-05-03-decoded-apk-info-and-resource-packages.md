# Decoded APK Metadata & Resource Packages CLI Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 新增 2 个面向 AI 的 CLI 命令 — `apk-info`（从已解码目录读取 apktool.yml 元数据）和 `resource-packages`（列出 APK 的资源包组信息），填补库能力到 CLI 的最后 gap。

**Architecture:** ApkInfo.load(File decodedDir) 从已解码 APK 目录的 apktool.yml 读取元数据 → 转为 JSON-friendly Map 输出 → CLI `apk-info <decoded-dir>` 命令。ResTable.listPackageGroups() 返回 ResPackageGroup 集合 → ApkAnalyzer 新方法遍历包组提取 id/name/locale/subPackages → CLI `resource-packages <apk-file>` 命令。

**Tech Stack:** Java 17, Gradle, Javalin 5.6.3, smali dexlib2

**Risks:**
- Task 1 中 ApkInfo.load 需要已解码目录（含 apktool.yml），不是原始 APK — 参数类型是 File 目录而非 APK 文件，需在 CLI 和 HTTP 中明确区分
- ResTable.listPackageGroups 返回 ResPackageGroup 对象，需手动转为 Map 避免 JSON 序列化问题

---

### Task 1: 在 ApkAnalyzer 中新增 2 个分析方法

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (新增 getDecodedApkInfo 和 getResourcePackages)

- [ ] **Step 1: 新增 getDecodedApkInfo 方法 — 从已解码目录读取 apktool.yml 元数据**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (在 getDexInfo 方法之后添加)

```java
public Map<String, Object> getDecodedApkInfo(File decodedDir) throws AndrolibException {
    ApkInfo apkInfo = ApkInfo.load(decodedDir);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("version", apkInfo.getVersion());
    result.put("apkFileName", apkInfo.getApkFileName());

    UsesFramework usesFramework = apkInfo.getUsesFramework();
    Map<String, Object> frameworkInfo = new LinkedHashMap<>();
    frameworkInfo.put("ids", usesFramework.getIds());
    frameworkInfo.put("tag", usesFramework.getTag());
    result.put("usesFramework", frameworkInfo);

    result.put("usesLibrary", apkInfo.getUsesLibrary());

    SdkInfo sdkInfo = apkInfo.getSdkInfo();
    Map<String, String> sdkMap = new LinkedHashMap<>();
    if (sdkInfo.getMinSdkVersion() != null) sdkMap.put("minSdkVersion", sdkInfo.getMinSdkVersion());
    if (sdkInfo.getTargetSdkVersion() != null) sdkMap.put("targetSdkVersion", sdkInfo.getTargetSdkVersion());
    if (sdkInfo.getMaxSdkVersion() != null) sdkMap.put("maxSdkVersion", sdkInfo.getMaxSdkVersion());
    result.put("sdkInfo", sdkMap);

    VersionInfo versionInfo = apkInfo.getVersionInfo();
    Map<String, Object> versionMap = new LinkedHashMap<>();
    versionMap.put("versionCode", versionInfo.getVersionCode());
    versionMap.put("versionName", versionInfo.getVersionName());
    result.put("versionInfo", versionMap);

    ResourcesInfo resourcesInfo = apkInfo.getResourcesInfo();
    Map<String, Object> resourcesMap = new LinkedHashMap<>();
    resourcesMap.put("packageId", resourcesInfo.getPackageId());
    resourcesMap.put("packageName", resourcesInfo.getPackageName());
    resourcesMap.put("sparseEntries", resourcesInfo.isSparseEntries());
    resourcesMap.put("compactEntries", resourcesInfo.isCompactEntries());
    resourcesMap.put("keepRawValues", resourcesInfo.isKeepRawValues());
    result.put("resourcesInfo", resourcesMap);

    result.put("featureFlags", apkInfo.getFeatureFlags());
    result.put("doNotCompress", apkInfo.getDoNotCompress());

    try {
        result.put("hasSources", apkInfo.hasSources());
        result.put("hasManifest", apkInfo.hasManifest());
        result.put("hasResources", apkInfo.hasResources());
    } catch (AndrolibException ignored) {}

    return result;
}
```

- [ ] **Step 2: 新增 getResourcePackages 方法 — 列出 APK 的资源包组信息**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (在 getDecodedApkInfo 方法之后添加)

```java
public Map<String, Object> getResourcePackages() throws AndrolibException {
    ResourceSummary summary = getResourceSummary();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("packageName", summary.getPackageName());
    result.put("packageId", summary.getPackageId());
    result.put("totalEntries", summary.getTotalEntries());
    result.put("typeCounts", summary.getTypeCounts());
    result.put("locales", summary.getLocales());

    // Get package group details from ResTable
    ApkInfo apkInfo = new ApkInfo();
    apkInfo.setApkFile(mApkFile);
    ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
    ResTable table = resDecoder.getTable();
    table.load();

    List<Map<String, Object>> packageGroups = new ArrayList<>();
    for (ResPackageGroup group : table.listPackageGroups()) {
        Map<String, Object> groupInfo = new LinkedHashMap<>();
        groupInfo.put("id", group.getId());
        groupInfo.put("name", group.getName());
        ResPackage basePkg = group.getBasePackage();
        if (basePkg != null) {
            groupInfo.put("basePackageName", basePkg.getName());
            groupInfo.put("basePackageId", basePkg.getId());
        }
        List<Map<String, Object>> subPackages = new ArrayList<>();
        for (ResPackage subPkg : group.listSubPackages()) {
            Map<String, Object> subInfo = new LinkedHashMap<>();
            subInfo.put("name", subPkg.getName());
            subInfo.put("id", subPkg.getId());
            subPackages.add(subInfo);
        }
        groupInfo.put("subPackages", subPackages);
        packageGroups.add(groupInfo);
    }
    result.put("packageGroups", packageGroups);
    result.put("packageGroupCount", table.getPackageGroupCount());

    return result;
}
```

- [ ] **Step 3: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java && git commit -m "feat(analyze): add getDecodedApkInfo and getResourcePackages methods"`

---

### Task 2: 新增 CLI 命令和 HTTP API 端点

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (新增 switch/case + cmd 方法 + printUsage)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (注册新命令)
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java` (新增 2 个 handler)
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java` (新增 2 个路由 + handler 方法)

- [ ] **Step 1: 在 Main.java switch/case 中注册 2 个新命令**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (在 `case "dex-info":` 之后添加)

```java
case "apk-info":
    cmdApkInfo(cmdArgs);
    break;
case "resource-packages":
    cmdResourcePackages(cmdArgs);
    break;
```

- [ ] **Step 2: 在 Main.java 中新增 2 个 cmd 方法**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (在 cmdDexInfo 方法之后添加)

```java
private static void cmdApkInfo(String[] args) throws AndrolibException {
    CommandLine cli = parseOptions(new Options(), args);
    List<String> argList = cli.getArgList();
    if (argList.isEmpty()) {
        System.err.println("Input decoded directory was not specified.");
        System.exit(1);
        return;
    }
    String dirName = argList.get(0);

    brut.androlib.analyze.ApkAnalyzer analyzer =
        new brut.androlib.analyze.ApkAnalyzer(new File(dirName), config);
    java.util.Map<String, Object> apkInfo = analyzer.getDecodedApkInfo(new File(dirName));
    System.out.println(brut.androlib.output.JsonOutput.toJson(apkInfo));
}

private static void cmdResourcePackages(String[] args) throws AndrolibException {
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
    java.util.Map<String, Object> packages = analyzer.getResourcePackages();
    System.out.println(brut.androlib.output.JsonOutput.toJson(packages));
}
```

- [ ] **Step 3: 更新 Main.java printUsage**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (在 `dex-info` 行之后添加)

```java
writer.println("  apk-info <dir>           - Read decoded APK metadata from apktool.yml");
writer.println("  resource-packages <apk>  - List resource package groups (IDs, names, sub-packages)");
```

- [ ] **Step 4: 在 CommandRegistry 中注册 2 个新命令**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (在 dex-info 注册之后添加)

```java
register("apk-info", null, "Read decoded APK metadata from apktool.yml in a decoded directory",
    "apktool apk-info <decoded-dir>", "JSON: {version, apkFileName, usesFramework, usesLibrary, sdkInfo, versionInfo, resourcesInfo, featureFlags, doNotCompress, hasSources, hasManifest, hasResources}",
    "analysis", new String[]{"apktool apk-info decoded_app_dir"});

register("resource-packages", null, "List resource package groups with IDs, names, and sub-packages",
    "apktool resource-packages <apk-file>", "JSON: {packageName, packageId, packageGroups[{id,name,basePackageName,subPackages}], packageGroupCount}",
    "analysis", new String[]{"apktool resource-packages app.apk"});
```

- [ ] **Step 5: 在 ApiHandler 中新增 2 个 handler 方法**
文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java` (在 handleDexInfo 方法之后添加)

```java
public String handleApkInfo(String dirPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(dirPath), config);
    return JsonOutput.toJson(analyzer.getDecodedApkInfo(new File(dirPath)));
}

public String handleResourcePackages(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getResourcePackages());
}
```

- [ ] **Step 6: 在 ApktoolServer 中新增 2 个路由和 handler 方法**
文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java` (在 dex-info 路由之后添加路由)

```java
app.get("/api/v1/apk-info", this::handleApkInfo);
app.get("/api/v1/resource-packages", this::handleResourcePackages);
```

在 handleDexInfo 方法之后添加 handler 方法：

```java
private void handleApkInfo(Context ctx) {
    try {
        String dir = getRequiredParam(ctx, "dir");
        ctx.contentType("application/json").result(handler.handleApkInfo(dir));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}

private void handleResourcePackages(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleResourcePackages(apk));
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
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(cli): add apk-info and resource-packages commands with HTTP endpoints"`

---

### Task 3: 更新文档

**Depends on:** Task 2
**Files:**
- Modify: `CLAUDE.md`
- Modify: `skills/reference/SKILL.md`

- [ ] **Step 1: 更新 CLAUDE.md — 添加新命令到 Analysis Commands 表和 HTTP API**

在 Analysis Commands 表中 `dex-info` 行之后添加：

```markdown
| `apk-info` | Decoded APK metadata | version, sdkInfo, usesFramework, featureFlags |
| `resource-packages` | Resource package groups | packageGroups, packageGroupCount |
```

在 HTTP API Endpoints 部分添加：

```markdown
- `GET /api/v1/apk-info?dir=<path>` — Decoded APK metadata
- `GET /api/v1/resource-packages?apk=<path>` — Resource package groups
```

- [ ] **Step 2: 更新 skills/reference/SKILL.md — 添加新命令和 HTTP 端点**

在 Analysis Commands 表中添加 `apk-info` 和 `resource-packages` 行。在 HTTP API Endpoints 部分添加对应端点。

- [ ] **Step 3: 提交**
Run: `git add CLAUDE.md skills/reference/SKILL.md && git commit -m "docs: add apk-info and resource-packages to CLI and HTTP references"`