# AI-Apktool CLI Capability Gap Fill Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 填补所有已识别的 CLI 能力 gap — 增强现有数据模型的字段填充，新增 4 个面向 AI 的 CLI 命令，并同步 HTTP API 和文档。

**Architecture:** 现有数据模型字段（usesLibraries, networkSecurityConfig, usesCleartextTraffic, dex/native diff fields）已定义但未填充 → 在 ApkAnalyzer.parseManifestFields() 和 ApkDiff.diff() 中填充 → 新增 CLI 命令（dex-list, locales, native-libs, dex-info）通过 ApkAnalyzer/ApkDiff 已有逻辑输出 → 同步 HTTP API 和 CommandRegistry → 更新文档。

**Tech Stack:** Java 17, Gradle, Javalin 5.6.3, smali dexlib2

**Risks:**
- Task 1 修改 ApkAnalyzer.parseManifestFields() 需新增 regex pattern — 低风险，纯字符串匹配
- Task 2 修改 ApkDiff.diff() 需 ZIP entry 扫描 — 低风险，已有类似逻辑在 getStructure()
- Task 3 新增 CLI 命令需在 Main.java switch/case 中注册 — 低风险，模式清晰

---

### Task 1: 增强数据模型字段填充 — usesLibraries, networkSecurityConfig, usesCleartextTraffic

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:34-46` (新增 regex patterns)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:166-205` (parseManifestFields 方法)

- [ ] **Step 1: 新增 regex pattern 常量 — 匹配 uses-library 和 usesCleartextTraffic 和 networkSecurityConfig**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:34-46`

在现有 pattern 常量之后添加 3 个新 pattern：

```java
private static final Pattern MANIFEST_USES_LIBRARY = Pattern.compile(
    "<uses-library\\s+[^>]*android:name=\"([^\"]+)\"");
private static final Pattern MANIFEST_CLEARTEXT = Pattern.compile(
    "android:usesCleartextTraffic=\"(true|false)\"");
private static final Pattern MANIFEST_NET_SEC_CONFIG = Pattern.compile(
    "android:networkSecurityConfig=\"([^\"]+)\"");
```

- [ ] **Step 2: 修改 parseManifestFields 方法 — 填充 usesLibraries, usesCleartextTraffic, networkSecurityConfig**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:166-205`

在 parseManifestFields 方法末尾（`m = MANIFEST_ALLOW_BACKUP.matcher(xml)` 之后）添加：

```java
m = MANIFEST_USES_LIBRARY.matcher(xml);
while (m.find()) {
    info.getUsesLibraries().add(m.group(1));
}

m = MANIFEST_CLEARTEXT.matcher(xml);
if (m.find()) {
    info.setUsesCleartextTraffic("true".equals(m.group(1)));
}

m = MANIFEST_NET_SEC_CONFIG.matcher(xml);
if (m.find()) {
    info.setNetworkSecurityConfig(m.group(1));
}
```

注意：`usesCleartextTraffic` 和 `networkSecurityConfig` 字段在 ManifestInfo 中不存在，需要先添加到 ManifestInfo。

- [ ] **Step 3: 添加 usesCleartextTraffic 字段到 ManifestInfo**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ManifestInfo.java`

在 `networkSecurityConfig` 字段之后添加：

```java
private boolean usesCleartextTraffic;

public boolean isUsesCleartextTraffic() { return usesCleartextTraffic; }
public void setUsesCleartextTraffic(boolean usesCleartextTraffic) { this.usesCleartextTraffic = usesCleartextTraffic; }
```

- [ ] **Step 4: 修改 getSecurityReport — 从 manifest 填充 usesCleartextTraffic**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:207-254`

在 `report.setAllowBackup(manifest.isAllowBackup());` 之后添加：

```java
report.setUsesCleartextTraffic(manifest.isUsesCleartextTraffic());

if (manifest.isUsesCleartextTraffic()) {
    report.getFindings().add("MEDIUM: Application uses cleartext traffic - android:usesCleartextTraffic=true");
}
```

同时更新 riskScore 计算，在 `score += report.isAllowBackup() ? 10 : 0;` 之后添加：

```java
score += report.isUsesCleartextTraffic() ? 10 : 0;
```

- [ ] **Step 5: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 6: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ManifestInfo.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SecurityReport.java && git commit -m "feat(analyze): populate usesLibraries, usesCleartextTraffic, networkSecurityConfig fields"`

---

### Task 2: 增强 ApkDiff.diff — 填充 dex/native 文件 diff 字段

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java:15-62`

- [ ] **Step 1: 修改 ApkDiff.diff 方法 — 扫描 ZIP entries 比较 dex 和 native lib 文件**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java:15-62`

在 diff 方法中，在 manifest 比较之后（`if (m1 != null && m2 != null)` block 之后）添加 ZIP entry 扫描逻辑：

```java
// Compare DEX files and native libraries
try {
    java.util.zip.ZipFile zip1 = new java.util.zip.ZipFile(apk1);
    java.util.zip.ZipFile zip2 = new java.util.zip.ZipFile(apk2);

    java.util.Set<String> dex1 = new java.util.HashSet<>();
    java.util.Set<String> dex2 = new java.util.HashSet<>();
    java.util.Set<String> native1 = new java.util.HashSet<>();
    java.util.Set<String> native2 = new java.util.HashSet<>();

    java.util.Enumeration<? extends java.util.zip.ZipEntry> e1 = zip1.entries();
    while (e1.hasMoreElements()) {
        String name = e1.nextElement().getName();
        if (name.matches("classes([2-9]|[1-9][0-9]+)?\\.dex") || name.equals("classes.dex")) {
            dex1.add(name);
        }
        if (name.startsWith("lib/") && !name.endsWith("/")) {
            native1.add(name);
        }
    }

    java.util.Enumeration<? extends java.util.zip.ZipEntry> e2 = zip2.entries();
    while (e2.hasMoreElements()) {
        String name = e2.nextElement().getName();
        if (name.matches("classes([2-9]|[1-9][0-9]+)?\\.dex") || name.equals("classes.dex")) {
            dex2.add(name);
        }
        if (name.startsWith("lib/") && !name.endsWith("/")) {
            native2.add(name);
        }
    }

    result.setAddedDexFiles(findAdded(dex1, dex2));
    result.setRemovedDexFiles(findRemoved(dex1, dex2));
    result.setAddedNativeLibs(findAdded(native1, native2));
    result.setRemovedNativeLibs(findRemoved(native1, native2));

    zip1.close();
    zip2.close();
} catch (java.io.IOException ex) {
    // Best-effort: if ZIP scanning fails, dex/native diff fields remain empty
}
```

- [ ] **Step 2: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 3: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java && git commit -m "feat(diff): populate addedDexFiles/removedDexFiles/addedNativeLibs/removedNativeLibs"`

---

### Task 3: 新增 4 个 AI-facing CLI 命令 — dex-list, locales, native-libs, dex-info

**Depends on:** Task 1, Task 2
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:317-425` (switch/case)
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (新增 cmd 方法)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java` (注册新命令)
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java` (新增分析方法)

- [ ] **Step 1: 在 ApkAnalyzer 中新增 4 个分析方法**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java`

在 `getAllComponents()` 方法之后添加：

```java
public java.util.Map<String, Object> getDexList() throws AndrolibException {
    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
    try {
        Directory dir = mApkFile.getDirectory();
        java.util.List<String> dexFiles = new java.util.ArrayList<>();
        for (String file : dir.getFiles(true)) {
            if (file.equals("classes.dex") || DEX_PATTERN.matcher(file).matches()) {
                dexFiles.add(file);
            }
        }
        result.put("dexCount", dexFiles.size());
        result.put("dexFiles", dexFiles);
    } catch (DirectoryException ex) {
        throw new AndrolibException(ex);
    }
    return result;
}

public java.util.List<String> getLocales() throws AndrolibException {
    ApkInfo apkInfo = new ApkInfo();
    apkInfo.setApkFile(mApkFile);
    ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
    ResTable table = resDecoder.getTable();
    table.load();

    java.util.List<String> locales = new java.util.ArrayList<>();
    for (brut.androlib.res.table.ResPackage pkg : table.listPackageGroups()) {
        for (brut.androlib.res.table.ResPackage subPkg : pkg.getPackages()) {
            if (subPkg.getLocale() != null && !subPkg.getLocale().isEmpty()) {
                locales.add(subPkg.getLocale());
            }
        }
    }
    java.util.Collections.sort(locales);
    return locales;
}

public java.util.Map<String, Object> getNativeLibs() throws AndrolibException {
    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
    try {
        Directory dir = mApkFile.getDirectory();
        java.util.List<String> architectures = new java.util.ArrayList<>();
        java.util.Map<String, java.util.List<String>> libsByArch = new java.util.LinkedHashMap<>();

        if (dir.containsDir("lib")) {
            for (String archDir : dir.getDir("lib").getFiles(false)) {
                architectures.add(archDir);
                java.util.List<String> libs = new java.util.ArrayList<>();
                try {
                    for (String libFile : dir.getDir("lib").getDir(archDir).getFiles(false)) {
                        libs.add(libFile);
                    }
                } catch (Exception ignored) {}
                java.util.Collections.sort(libs);
                libsByArch.put(archDir, libs);
            }
        }
        result.put("hasNativeLibs", dir.containsDir("lib"));
        result.put("architectures", architectures);
        result.put("libsByArch", libsByArch);
    } catch (DirectoryException ex) {
        throw new AndrolibException(ex);
    }
    return result;
}

public java.util.Map<String, java.util.Map<String, Integer>> getDexInfo() throws AndrolibException {
    java.util.Map<String, java.util.Map<String, Integer>> result = new java.util.LinkedHashMap<>();
    ExtFile extFile = new ExtFile(mApkFile);

    try {
        com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer container =
            new com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer(extFile, null);

        for (String dexName : container.getDexEntryNames()) {
            com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer.DexEntry<
                com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile> entry = container.getEntry(dexName);
            if (entry == null) continue;

            com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile dexFile = entry.getDexFile();
            java.util.Map<String, Integer> dexStats = new java.util.LinkedHashMap<>();
            dexStats.put("classes", 0);
            dexStats.put("methods", 0);
            dexStats.put("fields", 0);

            int classCount = 0;
            int methodCount = 0;
            int fieldCount = 0;

            for (com.android.tools.smali.dexlib2.iface.ClassDef classDef : dexFile.getClasses()) {
                classCount++;
                for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                    methodCount++;
                }
                for (com.android.tools.smali.dexlib2.iface.Field field : classDef.getFields()) {
                    fieldCount++;
                }
            }

            dexStats.put("classes", classCount);
            dexStats.put("methods", methodCount);
            dexStats.put("fields", fieldCount);
            result.put(dexName, dexStats);
        }
    } catch (java.io.IOException ex) {
        throw new AndrolibException(ex);
    } finally {
        try { extFile.close(); } catch (Exception ignored) {}
    }
    return result;
}
```

- [ ] **Step 2: 在 Main.java switch/case 中注册 4 个新命令**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:317-425`

在 `case "structure":` 之后添加：

```java
case "dex-list":
    cmdDexList(cmdArgs);
    break;
case "locales":
    cmdLocales(cmdArgs);
    break;
case "native-libs":
    cmdNativeLibs(cmdArgs);
    break;
case "dex-info":
    cmdDexInfo(cmdArgs);
    break;
```

- [ ] **Step 3: 在 Main.java 中新增 4 个 cmd 方法**

在 `cmdAnalyze` 方法之后添加：

```java
private static void cmdDexList(String[] args) throws AndrolibException {
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
    java.util.Map<String, Object> dexList = analyzer.getDexList();
    System.out.println(brut.androlib.output.JsonOutput.toJson(dexList));
}

private static void cmdLocales(String[] args) throws AndrolibException {
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
    java.util.List<String> locales = analyzer.getLocales();
    System.out.println(brut.androlib.output.JsonOutput.toJson(locales));
}

private static void cmdNativeLibs(String[] args) throws AndrolibException {
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
    java.util.Map<String, Object> nativeLibs = analyzer.getNativeLibs();
    System.out.println(brut.androlib.output.JsonOutput.toJson(nativeLibs));
}

private static void cmdDexInfo(String[] args) throws AndrolibException {
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
    java.util.Map<String, java.util.Map<String, Integer>> dexInfo = analyzer.getDexInfo();
    System.out.println(brut.androlib.output.JsonOutput.toJson(dexInfo));
}
```

- [ ] **Step 4: 在 CommandRegistry 中注册 4 个新命令**
文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java`

在 `register("analyze", ...)` 之后添加：

```java
register("dex-list", null, "List all DEX files in the APK with count",
    "apktool dex-list <apk-file>", "JSON: {dexCount, dexFiles[]}",
    "analysis", new String[]{"apktool dex-list app.apk"});

register("locales", null, "List all supported locales/regions from the resource table",
    "apktool locales <apk-file>", "JSON array of locale strings",
    "analysis", new String[]{"apktool locales app.apk"});

register("native-libs", null, "List native libraries per architecture with file names",
    "apktool native-libs <apk-file>", "JSON: {hasNativeLibs, architectures[], libsByArch{arch->[libs]}}",
    "analysis", new String[]{"apktool native-libs app.apk"});

register("dex-info", null, "Get per-DEX class/method/field statistics",
    "apktool dex-info <apk-file>", "JSON: {dexName->{classes,methods,fields}}",
    "analysis", new String[]{"apktool dex-info app.apk"});
```

- [ ] **Step 5: 更新 Main.java printUsage 中的 AI-Apktool 分析命令列表**
文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:1252-1277`

在 `writer.println("  structure <apk>         - Code structure overview (classes, methods, packages)");` 之后添加：

```java
writer.println("  dex-list <apk>          - List all DEX files in the APK");
writer.println("  locales <apk>           - List supported locales from resource table");
writer.println("  native-libs <apk>       - List native libraries per architecture");
writer.println("  dex-info <apk>          - Per-DEX class/method/field statistics");
```

- [ ] **Step 6: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :apktool-cli:compileJava :apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 7: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java && git commit -m "feat(cli): add dex-list, locales, native-libs, dex-info commands"`

---

### Task 4: 同步 HTTP API — 新增 4 个端点 + 更新 ai context action

**Depends on:** Task 3
**Files:**
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java`
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`

- [ ] **Step 1: 在 ApiHandler 中新增 4 个 handler 方法**

在 `handleStrings` 方法之后添加：

```java
public String handleDexList(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getDexList());
}

public String handleLocales(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getLocales());
}

public String handleNativeLibs(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getNativeLibs());
}

public String handleDexInfo(String apkPath) throws Exception {
    ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
    return JsonOutput.toJson(analyzer.getDexInfo());
}
```

- [ ] **Step 2: 在 ApktoolServer 中注册 4 个新路由**

在 `app.get("/api/v1/strings", this::handleStrings);` 之后添加：

```java
app.get("/api/v1/dex-list", this::handleDexList);
app.get("/api/v1/locales", this::handleLocales);
app.get("/api/v1/native-libs", this::handleNativeLibs);
app.get("/api/v1/dex-info", this::handleDexInfo);
```

- [ ] **Step 3: 在 ApktoolServer 中新增 4 个 handler 方法**

在 `handleStrings` 方法之后添加：

```java
private void handleDexList(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleDexList(apk));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}

private void handleLocales(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleLocales(apk));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}

private void handleNativeLibs(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleNativeLibs(apk));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}

private void handleDexInfo(Context ctx) {
    try {
        String apk = getRequiredParam(ctx, "apk");
        ctx.contentType("application/json").result(handler.handleDexInfo(apk));
    } catch (Exception e) {
        ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
    }
}
```

- [ ] **Step 4: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :apktool-serve:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(serve): add dex-list, locales, native-libs, dex-info HTTP endpoints"`

---

### Task 5: 更新文档 — CLAUDE.md, SKILL.md

**Depends on:** Task 3, Task 4
**Files:**
- Modify: `CLAUDE.md`
- Modify: `skills/reference/SKILL.md`

- [ ] **Step 1: 更新 CLAUDE.md — 添加新命令到 Analysis Commands 表和 HTTP API**

在 Analysis Commands 表中，`strings` 行之后添加：

```markdown
| `dex-list` | List DEX files | dexCount, dexFiles |
| `locales` | Supported locales | locale strings from resource table |
| `native-libs` | Native libraries | architectures, libsByArch |
| `dex-info` | Per-DEX statistics | classes, methods, fields per DEX |
```

在 HTTP API Endpoints 部分添加：

```markdown
- `GET /api/v1/dex-list?apk=<path>`
- `GET /api/v1/locales?apk=<path>`
- `GET /api/v1/native-libs?apk=<path>`
- `GET /api/v1/dex-info?apk=<path>`
```

- [ ] **Step 2: 更新 skills/reference/SKILL.md — 添加新命令和 HTTP 端点**

在 Analysis Commands 表中添加新行，在 HTTP API Endpoints 部分添加新端点。

- [ ] **Step 3: 提交**
Run: `git add CLAUDE.md skills/reference/SKILL.md && git commit -m "docs: add dex-list, locales, native-libs, dex-info to CLI and HTTP references"`

---

### Task 6: 全项目编译验证

**Depends on:** Task 1, Task 2, Task 3, Task 4, Task 5

- [ ] **Step 1: 全项目编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew build -x test`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"