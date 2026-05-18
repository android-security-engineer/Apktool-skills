# CLI Gap Fill Round 4 — ManifestInfo 字段暴露 + 新逆向工程命令

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 暴露 ManifestInfo 中未独立暴露的字段（usesLibraries, manifest flags, version），并新增 3 个实用逆向工程命令（file-list, file-hash, class-info），使 AI 代理可以完整地通过 CLI 访问所有 APK 分析能力。

**Architecture:** 新增 3 个 ApkAnalyzer 方法（getFileList, getFileHash, getClassDetail）→ 6 个新 CLI 命令（uses-libs, manifest-flags, version, file-list, file-hash, class-info）→ 6 个 HTTP API 端点 → CommandRegistry 注册 → 文档更新。所有新命令遵循现有模式：case label → cmd* 方法 → ApkAnalyzer 方法 → JsonOutput.toJson()。

**Tech Stack:** Java 17, Gradle, Javalin 5.6.3, dexlib2 (smali)

**Risks:**
- Task 1 新增 getClassDetail 需要遍历 DEX 文件查找特定类，大 APK 可能较慢 → 缓解：只扫描第一个匹配的类，找到即停止
- Task 1 getFileList 对大 APK 可能输出很多条目 → 缓解：默认返回文件列表摘要（count + entries），不限制条目数

---

### Task 1: ApkAnalyzer 新增 3 个分析方法

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:637-649`（在 getLibFramePackageIds 方法之后添加）

- [ ] **Step 1: 新增 getFileList 方法 — 列出 APK 内所有 ZIP 条目**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:649`（在类末尾 `}` 之前添加）

```java
    public Map<String, Object> getFileList() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(mApkFile.getAbsolutePath());
            List<Map<String, Object>> entries = new ArrayList<>();
            long totalSize = 0;
            long totalCompressedSize = 0;

            java.util.Enumeration<? extends java.util.zip.ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                java.util.zip.ZipEntry entry = e.nextElement();
                Map<String, Object> entryInfo = new LinkedHashMap<>();
                entryInfo.put("name", entry.getName());
                entryInfo.put("size", entry.getSize());
                entryInfo.put("compressedSize", entry.getCompressedSize());
                entryInfo.put("directory", entry.isDirectory());
                entries.add(entryInfo);
                totalSize += entry.getSize();
                totalCompressedSize += entry.getCompressedSize();
            }

            result.put("totalFiles", entries.size());
            result.put("totalSize", totalSize);
            result.put("totalCompressedSize", totalCompressedSize);
            result.put("entries", entries);
            zipFile.close();
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        }
        return result;
    }
```

- [ ] **Step 2: 新增 getFileHash 方法 — 计算 APK 文件的 SHA-256 和 MD5 哈希**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java`（在 getFileList 方法之后添加）

```java
    public Map<String, String> getFileHash() throws AndrolibException {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(mApkFile.toPath());
            result.put("sha256", formatFingerprint(MessageDigest.getInstance("SHA-256").digest(fileBytes)));
            result.put("sha1", formatFingerprint(MessageDigest.getInstance("SHA-1").digest(fileBytes)));
            result.put("md5", formatFingerprint(MessageDigest.getInstance("MD5").digest(fileBytes)));
            result.put("fileSize", String.valueOf(mApkFile.length()));
            result.put("fileName", mApkFile.getName());
        } catch (IOException | java.security.NoSuchAlgorithmException ex) {
            throw new AndrolibException(ex);
        }
        return result;
    }
```

- [ ] **Step 3: 新增 getClassDetail 方法 — 查看特定类的详细信息**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java`（在 getFileHash 方法之后添加）

```java
    public Map<String, Object> getClassDetail(String className) throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        String dexType = "L" + className.replace('.', '/') + ";";
        ExtFile extFile = new ExtFile(mApkFile);

        try {
            com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer container =
                new com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer(extFile, null);

            for (String dexName : container.getDexEntryNames()) {
                com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer.DexEntry<
                    com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile dexFile = entry.getDexFile();
                for (com.android.tools.smali.dexlib2.iface.ClassDef classDef : dexFile.getClasses()) {
                    if (classDef.getType().equals(dexType)) {
                        result.put("className", className);
                        result.put("dexFile", dexName);
                        result.put("superClass", classDef.getSuperclass() != null ?
                            classDef.getSuperclass().substring(1, classDef.getSuperclass().length() - 1).replace('/', '.') : null);
                        result.put("accessFlags", classDef.getAccessFlags());

                        List<String> interfaces = new ArrayList<>();
                        for (String iface : classDef.getInterfaces()) {
                            interfaces.add(iface.substring(1, iface.length() - 1).replace('/', '.'));
                        }
                        result.put("interfaces", interfaces);

                        List<Map<String, Object>> methods = new ArrayList<>();
                        for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                            Map<String, Object> methodInfo = new LinkedHashMap<>();
                            methodInfo.put("name", method.getName());
                            methodInfo.put("accessFlags", method.getAccessFlags());
                            methodInfo.put("returnType", method.getReturnType() != null ?
                                method.getReturnType().substring(1, method.getReturnType().length() - 1).replace('/', '.') : null);
                            List<String> paramTypes = new ArrayList<>();
                            for (com.android.tools.smali.dexlib2.iface.MethodParameter param : method.getParameters()) {
                                String pType = param.getType();
                                paramTypes.add(pType.substring(1, pType.length() - 1).replace('/', '.'));
                            }
                            methodInfo.put("parameters", paramTypes);
                            methods.add(methodInfo);
                        }
                        result.put("methods", methods);

                        List<Map<String, Object>> fields = new ArrayList<>();
                        for (com.android.tools.smali.dexlib2.iface.Field field : classDef.getFields()) {
                            Map<String, Object> fieldInfo = new LinkedHashMap<>();
                            fieldInfo.put("name", field.getName());
                            fieldInfo.put("accessFlags", field.getAccessFlags());
                            fieldInfo.put("type", field.getType() != null ?
                                field.getType().substring(1, field.getType().length() - 1).replace('/', '.') : null);
                            fields.add(fieldInfo);
                        }
                        result.put("fields", fields);

                        result.put("methodCount", methods.size());
                        result.put("fieldCount", fields.size());
                        return result;
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }

        result.put("className", className);
        result.put("error", "Class not found");
        return result;
    }
```

- [ ] **Step 4: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java && git commit -m "feat(analyze): add getFileList, getFileHash, getClassDetail methods"`

---

### Task 2: 6 个新 CLI 命令 + HTTP API + CommandRegistry

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:414-417`（新增 case labels）+ 新增 cmd 方法
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java:135`（新增注册）
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java:199`（新增 handler 方法）
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java:48`（新增 route + handler）

- [ ] **Step 1: 新增 6 个 case labels 到 Main.java switch/case**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:414-417`（在 `lib-frame-packages` case 之后，`serve` case 之前添加）

```java
            case "uses-libs":
            case "manifest-flags":
            case "version":
            case "file-list":
            case "file-hash":
            case "class-info":
```

- [ ] **Step 2: 新增 6 个 cmd 方法到 Main.java**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`（在 cmdLibFramePackages 方法之后添加）

```java
    private static void cmdUsesLibs(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) {
            System.out.println("[]");
            return;
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(manifest.getUsesLibraries()));
    }

    private static void cmdManifestFlags(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.ManifestInfo manifest = analyzer.getManifestInfo();
        java.util.Map<String, Object> flags = new java.util.LinkedHashMap<>();
        if (manifest != null) {
            flags.put("debuggable", manifest.isDebuggable());
            flags.put("allowBackup", manifest.isAllowBackup());
            flags.put("usesCleartextTraffic", manifest.isUsesCleartextTraffic());
            flags.put("networkSecurityConfig", manifest.getNetworkSecurityConfig());
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(flags));
    }

    private static void cmdVersion(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.ManifestInfo manifest = analyzer.getManifestInfo();
        java.util.Map<String, Object> version = new java.util.LinkedHashMap<>();
        if (manifest != null) {
            version.put("packageName", manifest.getPackageName());
            version.put("versionCode", manifest.getVersionCode());
            version.put("versionName", manifest.getVersionName());
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(version));
    }

    private static void cmdFileList(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> fileList = analyzer.getFileList();
        System.out.println(brut.androlib.output.JsonOutput.toJson(fileList));
    }

    private static void cmdFileHash(String[] args) throws AndrolibException {
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
        java.util.Map<String, String> hash = analyzer.getFileHash();
        System.out.println(brut.androlib.output.JsonOutput.toJson(hash));
    }

    private static void cmdClassInfo(String[] args) throws AndrolibException {
        CommandLine cli = parseOptions(new Options(), args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String className = argList.size() > 1 ? argList.get(1) : "";

        brut.androlib.analyze.ApkAnalyzer analyzer =
            new brut.androlib.analyze.ApkAnalyzer(new File(apkName), config);
        java.util.Map<String, Object> classDetail = analyzer.getClassDetail(className);
        System.out.println(brut.androlib.output.JsonOutput.toJson(classDetail));
    }
```

- [ ] **Step 3: 新增 6 个 case 分发调用到 Main.java switch/case**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`（在 case labels 对应的 break 之前添加 cmd 调用）

需要在 switch/case 中为每个新 case 添加对应的 cmd 调用：
- `case "uses-libs":` → `cmdUsesLibs(cmdArgs); break;`
- `case "manifest-flags":` → `cmdManifestFlags(cmdArgs); break;`
- `case "version":` → `cmdVersion(cmdArgs); break;`
- `case "file-list":` → `cmdFileList(cmdArgs); break;`
- `case "file-hash":` → `cmdFileHash(cmdArgs); break;`
- `case "class-info":` → `cmdClassInfo(cmdArgs); break;`

- [ ] **Step 4: 更新 printUsage 添加新命令描述**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`（在 printUsage 方法中添加）

```
  uses-libs <apk>          - List shared libraries used by the APK
  manifest-flags <apk>     - Show manifest security flags (debuggable, allowBackup, etc.)
  version <apk>            - Show APK version info (package, versionCode, versionName)
  file-list <apk>          - List all files in the APK
  file-hash <apk>          - Calculate SHA-256/SHA-1/MD5 hash of the APK
  class-info <apk> <class> - Show detailed info about a DEX class
```

- [ ] **Step 5: 注册 6 个新命令到 CommandRegistry**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java`（在 `lib-frame-packages` 注册之后添加）

```java
        register("uses-libs", null, "List shared libraries declared in AndroidManifest.xml",
            "apktool uses-libs <apk-file>", "JSON array of library names",
            "analysis", new String[]{"apktool uses-libs app.apk"});

        register("manifest-flags", null, "Get manifest security flags: debuggable, allowBackup, cleartext traffic, network security config",
            "apktool manifest-flags <apk-file>", "JSON: {debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig}",
            "analysis", new String[]{"apktool manifest-flags app.apk"});

        register("version", null, "Get APK version information: package name, version code, version name",
            "apktool version <apk-file>", "JSON: {packageName, versionCode, versionName}",
            "analysis", new String[]{"apktool version app.apk"});

        register("file-list", null, "List all files in the APK with sizes and compression info",
            "apktool file-list <apk-file>", "JSON: {totalFiles, totalSize, totalCompressedSize, entries[{name,size,compressedSize,directory}]}",
            "analysis", new String[]{"apktool file-list app.apk"});

        register("file-hash", null, "Calculate SHA-256, SHA-1, and MD5 hash of the APK file",
            "apktool file-hash <apk-file>", "JSON: {sha256, sha1, md5, fileSize, fileName}",
            "analysis", new String[]{"apktool file-hash app.apk"});

        register("class-info", null, "Get detailed info about a DEX class: methods, fields, superclass, interfaces",
            "apktool class-info <apk-file> <class-name>", "JSON: {className, superClass, accessFlags, interfaces[], methods[{name,accessFlags,returnType,parameters[]}], fields[{name,accessFlags,type}]}",
            "analysis", new String[]{"apktool class-info app.apk com.example.MyActivity"});
```

- [ ] **Step 6: 新增 6 个 HTTP API handler 到 ApiHandler**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java`（在 handleLibFramePackages 方法之后添加）

```java
    public String handleUsesLibs(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest == null) return "[]";
        return JsonOutput.toJson(manifest.getUsesLibraries());
    }

    public String handleManifestFlags(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        Map<String, Object> flags = new LinkedHashMap<>();
        if (manifest != null) {
            flags.put("debuggable", manifest.isDebuggable());
            flags.put("allowBackup", manifest.isAllowBackup());
            flags.put("usesCleartextTraffic", manifest.isUsesCleartextTraffic());
            flags.put("networkSecurityConfig", manifest.getNetworkSecurityConfig());
        }
        return JsonOutput.toJson(flags);
    }

    public String handleVersion(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        ManifestInfo manifest = analyzer.getManifestInfo();
        Map<String, Object> version = new LinkedHashMap<>();
        if (manifest != null) {
            version.put("packageName", manifest.getPackageName());
            version.put("versionCode", manifest.getVersionCode());
            version.put("versionName", manifest.getVersionName());
        }
        return JsonOutput.toJson(version);
    }

    public String handleFileList(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getFileList());
    }

    public String handleFileHash(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getFileHash());
    }

    public String handleClassInfo(String apkPath, String className) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getClassDetail(className));
    }
```

- [ ] **Step 7: 新增 6 个 HTTP route + handler 到 ApktoolServer**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`（在 registerRoutes 方法中 `lib-frame-packages` route 之后添加）

```java
        app.get("/api/v1/uses-libs", this::handleUsesLibs);
        app.get("/api/v1/manifest-flags", this::handleManifestFlags);
        app.get("/api/v1/version", this::handleVersion);
        app.get("/api/v1/file-list", this::handleFileList);
        app.get("/api/v1/file-hash", this::handleFileHash);
        app.get("/api/v1/class-info", this::handleClassInfo);
```

在 ApktoolServer 中添加 6 个 handler 方法（在 handleLibFramePackages 之后）：

```java
    private void handleUsesLibs(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleUsesLibs(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleManifestFlags(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleManifestFlags(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleVersion(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleVersion(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleFileList(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleFileList(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleFileHash(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleFileHash(apk));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleClassInfo(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            String className = getRequiredParam(ctx, "class");
            ctx.contentType("application/json").result(handler.handleClassInfo(apk, className));
        } catch (Exception e) {
            ctx.status(500).result("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
```

- [ ] **Step 8: 验证编译**
Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew :brut.apktool:apktool-lib:compileJava :brut.apktool:apktool-cli:compileJava :brut.apktool:apktool-serve:compileJava`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 9: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(cli): add uses-libs, manifest-flags, version, file-list, file-hash, class-info commands"`

---

### Task 3: 更新文档

**Depends on:** Task 2
**Files:**
- Modify: `CLAUDE.md:74-75`（Analysis Commands 表格）
- Modify: `CLAUDE.md:133-135`（HTTP API Endpoints 列表）
- Modify: `skills/reference/SKILL.md:54-55`（Analysis Commands 表格）
- Modify: `skills/reference/SKILL.md:112-113`（HTTP API Endpoints 列表）

- [ ] **Step 1: 更新 CLAUDE.md Analysis Commands 表格**

文件: `CLAUDE.md`（在 `lib-frame-packages` 行之后，`analyze` 行之前添加）

```markdown
| `uses-libs` | Shared libraries used | usesLibraries[] |
| `manifest-flags` | Manifest security flags | debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig |
| `version` | APK version info | packageName, versionCode, versionName |
| `file-list` | APK file listing | totalFiles, totalSize, entries[] |
| `file-hash` | APK file hashes | sha256, sha1, md5 |
| `class-info` | DEX class details | superClass, methods[], fields[], interfaces[] |
```

- [ ] **Step 2: 更新 CLAUDE.md HTTP API Endpoints 列表**

文件: `CLAUDE.md`（在 `/api/v1/lib-frame-packages` 行之后，`/api/v1/diff` 行之前添加）

```markdown
- `/api/v1/uses-libs?apk=<path>` — Shared libraries used
- `/api/v1/manifest-flags?apk=<path>` — Manifest security flags
- `/api/v1/version?apk=<path>` — APK version info
- `/api/v1/file-list?apk=<path>` — APK file listing
- `/api/v1/file-hash?apk=<path>` — APK file hashes
- `/api/v1/class-info?apk=<path>&class=<name>` — DEX class details
```

- [ ] **Step 3: 更新 skills/reference/SKILL.md Analysis Commands 表格**

文件: `skills/reference/SKILL.md`（在 `lib-frame-packages` 行之后，`analyze` 行之前添加）

```markdown
| `uses-libs` | usesLibraries[] |
| `manifest-flags` | debuggable, allowBackup, usesCleartextTraffic, networkSecurityConfig |
| `version` | packageName, versionCode, versionName |
| `file-list` | totalFiles, totalSize, entries[] |
| `file-hash` | sha256, sha1, md5 |
| `class-info` | superClass, methods[], fields[], interfaces[] |
```

- [ ] **Step 4: 更新 skills/reference/SKILL.md HTTP API Endpoints 列表**

文件: `skills/reference/SKILL.md`（在 `/api/v1/lib-frame-packages` 行之后添加）

```markdown
- `GET /api/v1/uses-libs?apk=<path>`
- `GET /api/v1/manifest-flags?apk=<path>`
- `GET /api/v1/version?apk=<path>`
- `GET /api/v1/file-list?apk=<path>`
- `GET /api/v1/file-hash?apk=<path>`
- `GET /api/v1/class-info?apk=<path>&class=<name>`
```

- [ ] **Step 5: 提交**
Run: `git add CLAUDE.md skills/reference/SKILL.md && git commit -m "docs: add 6 new commands and HTTP endpoints to references"`
