# AI-Friendly CLI Capability Gap Fill Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 补齐 ApkAnalyzer 引擎中已有能力但未暴露到 CLI 的差距，并新增 AI 逆向工程场景中高价值的 10 个新 CLI 命令，实现引擎→CLI→HTTP API→CommandRegistry 全链路覆盖。

**Architecture:** 新能力从 ApkAnalyzer 引擎层新增方法 → Main.java 注册 CLI switch case → ApiHandler 新增 handler 方法 → ApktoolServer 注册 Javalin 路由 → CommandRegistry 注册命令元数据。数据流：APK 文件输入 → ApkAnalyzer/ZipDexContainer 解析 → JSON 输出到 stdout/HTTP response。复用现有 dexlib2 DEX 解析能力和 ResTable 资源表解析能力，不引入新依赖。

**Tech Stack:** Java 11, dexlib2 (smali), Javalin 5.6.3, Gson, Gradle (Kotlin DSL)

**Risks:**
- Task 1 修改 ApkAnalyzer.java 新增 10 个方法，文件已 770 行 → 缓解：方法均为独立分析逻辑，不修改现有方法
- Task 2 修改 Main.java switch 块（行 317-463），新增 10 个 case → 缓解：在现有 case 块末尾追加，不修改现有 case
- Task 3 修改 ApktoolServer.java 路由注册 → 缓解：在现有路由之后追加新路由
- Task 4 修改 CommandRegistry.java static 块 → 缓解：在现有 register 调用之后追加

---

### Task 1: Add 10 New Analysis Methods to ApkAnalyzer

**Depends on:** None
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:770` (append after class closing brace)

- [ ] **Step 1: Add classList() method to ApkAnalyzer — list all class names from DEX files**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java:769` (在最后一个 `}` 之前插入)

```java
    public Map<String, Object> getClassList() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> classNames = new ArrayList<>();
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
                    String humanName = classDef.getType().substring(1, classDef.getType().length() - 1).replace('/', '.');
                    classNames.add(humanName);
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }
        result.put("totalClasses", classNames.size());
        result.put("classes", classNames);
        return result;
    }

    public Map<String, Object> getMethodSearch(String pattern) throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> methods = new ArrayList<>();
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
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
                    String className = classDef.getType().substring(1, classDef.getType().length() - 1).replace('/', '.');
                    for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                        if (regex.matcher(method.getName()).find()) {
                            Map<String, Object> methodInfo = new LinkedHashMap<>();
                            methodInfo.put("className", className);
                            methodInfo.put("methodName", method.getName());
                            methodInfo.put("returnType", method.getReturnType() != null ?
                                method.getReturnType().substring(1, method.getReturnType().length() - 1).replace('/', '.') : null);
                            List<String> paramTypes = new ArrayList<>();
                            for (com.android.tools.smali.dexlib2.iface.MethodParameter param : method.getParameters()) {
                                String pType = param.getType();
                                paramTypes.add(pType.substring(1, pType.length() - 1).replace('/', '.'));
                            }
                            methodInfo.put("parameters", paramTypes);
                            methodInfo.put("accessFlags", method.getAccessFlags());
                            methods.add(methodInfo);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }
        result.put("totalMatches", methods.size());
        result.put("methods", methods);
        return result;
    }

    public Map<String, Object> getFieldSearch(String pattern) throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
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
                    String className = classDef.getType().substring(1, classDef.getType().length() - 1).replace('/', '.');
                    for (com.android.tools.smali.dexlib2.iface.Field field : classDef.getFields()) {
                        if (regex.matcher(field.getName()).find()) {
                            Map<String, Object> fieldInfo = new LinkedHashMap<>();
                            fieldInfo.put("className", className);
                            fieldInfo.put("fieldName", field.getName());
                            fieldInfo.put("type", field.getType() != null ?
                                field.getType().substring(1, field.getType().length() - 1).replace('/', '.') : null);
                            fieldInfo.put("accessFlags", field.getAccessFlags());
                            fields.add(fieldInfo);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }
        result.put("totalMatches", fields.size());
        result.put("fields", fields);
        return result;
    }

    public Map<String, Object> getAssetList() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Directory dir = mApkFile.getDirectory();
            List<String> assets = new ArrayList<>();
            if (dir.containsDir("assets")) {
                for (String file : dir.getDir("assets").getFiles(true)) {
                    assets.add(file);
                }
            }
            result.put("hasAssets", dir.containsDir("assets"));
            result.put("totalAssets", assets.size());
            result.put("assets", assets);
        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        }
        return result;
    }

    public Map<String, Object> getDexStrings() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> strings = new ArrayList<>();
        ExtFile extFile = new ExtFile(mApkFile);
        try {
            com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer container =
                new com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer(extFile, null);
            for (String dexName : container.getDexEntryNames()) {
                com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer.DexEntry<
                    com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;
                com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile dexFile = entry.getDexFile();
                for (com.android.tools.smali.dexlib2.iface.StringItem stringItem : dexFile.getStrings()) {
                    strings.add(stringItem.getString());
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }
        result.put("totalStrings", strings.size());
        result.put("strings", strings);
        return result;
    }

    public Map<String, Object> getPermissionDetail() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        ManifestInfo manifest = getManifestInfo();
        if (manifest == null) return result;
        List<Map<String, Object>> permissionDetails = new ArrayList<>();
        for (String perm : manifest.getPermissions()) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("name", perm);
            detail.put("dangerous", DANGEROUS_PERMISSIONS.contains(perm));
            String category;
            if (perm.startsWith("android.permission")) {
                category = DANGEROUS_PERMISSIONS.contains(perm) ? "dangerous" : "normal";
            } else {
                category = "custom";
            }
            detail.put("category", category);
            permissionDetails.add(detail);
        }
        result.put("totalPermissions", permissionDetails.size());
        result.put("dangerousCount", (int) permissionDetails.stream().filter(p -> "dangerous".equals(p.get("category"))).count());
        result.put("normalCount", (int) permissionDetails.stream().filter(p -> "normal".equals(p.get("category"))).count());
        result.put("customCount", (int) permissionDetails.stream().filter(p -> "custom".equals(p.get("category"))).count());
        result.put("permissions", permissionDetails);
        return result;
    }

    public Map<String, Object> getInheritanceInfo(String className) throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        String dexType = "L" + className.replace('.', '/') + ";";
        ExtFile extFile = new ExtFile(mApkFile);
        try {
            com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer container =
                new com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer(extFile, null);
            List<String> inheritanceChain = new ArrayList<>();
            String currentType = dexType;
            Set<String> visited = new HashSet<>();
            while (currentType != null && !visited.contains(currentType)) {
                visited.add(currentType);
                String humanName = currentType.substring(1, currentType.length() - 1).replace('/', '.');
                inheritanceChain.add(humanName);
                boolean found = false;
                for (String dexName : container.getDexEntryNames()) {
                    com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer.DexEntry<
                        com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile> entry = container.getEntry(dexName);
                    if (entry == null) continue;
                    for (com.android.tools.smali.dexlib2.iface.ClassDef classDef : entry.getDexFile().getClasses()) {
                        if (classDef.getType().equals(currentType)) {
                            currentType = classDef.getSuperclass();
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (!found) break;
            }
            result.put("className", className);
            result.put("inheritanceChain", inheritanceChain);
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { extFile.close(); } catch (Exception ignored) {}
        }
        return result;
    }

    public Map<String, Object> getManifestXml() throws AndrolibException {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Directory dir = mApkFile.getDirectory();
            if (!dir.containsFile("AndroidManifest.xml")) {
                result.put("error", "No AndroidManifest.xml found");
                return result;
            }
            ApkInfo apkInfo = new ApkInfo();
            apkInfo.setApkFile(mApkFile);
            ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
            BinaryXmlResourceParser parser = new BinaryXmlResourceParser(resDecoder.getTable(), false, false);
            ResXmlSerializer serial = new ResXmlSerializer(true);
            ManifestPullEventHandler handler = new ManifestPullEventHandler(apkInfo, false);
            ResXmlPullStreamDecoder decoder = new ResXmlPullStreamDecoder(parser, serial, handler);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream in = dir.getFileInput("AndroidManifest.xml")) {
                decoder.decode(in, baos);
            }
            result.put("manifestXml", baos.toString("UTF-8"));
        } catch (DirectoryException | IOException ex) {
            throw new AndrolibException(ex);
        }
        return result;
    }
```

- [ ] **Step 2: 验证 ApkAnalyzer 编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-lib:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output does NOT contain: "error" or "ERROR"

- [ ] **Step 3: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java && git commit -m "feat(analyze): add classList, methodSearch, fieldSearch, assetList, dexStrings, permissionDetail, inheritanceInfo, manifestXml methods"`

---

### Task 2: Add 8 New CLI Commands to Main.java

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:437` (after `case "ai":` block, before `case "h":`)

- [ ] **Step 1: Add 8 new case entries to Main.java switch block**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:437` (在 `case "ai":` 的 `cmdAi(cmdArgs); break;` 之后，`case "h":` 之前插入)

```java
            case "class-list":
                cmdClassList(cmdArgs);
                break;
            case "method-search":
                cmdMethodSearch(cmdArgs);
                break;
            case "field-search":
                cmdFieldSearch(cmdArgs);
                break;
            case "asset-list":
                cmdAssetList(cmdArgs);
                break;
            case "dex-strings":
                cmdDexStrings(cmdArgs);
                break;
            case "permission-detail":
                cmdPermissionDetail(cmdArgs);
                break;
            case "inheritance":
                cmdInheritance(cmdArgs);
                break;
            case "manifest-xml":
                cmdManifestXml(cmdArgs);
                break;
```

- [ ] **Step 2: Add 8 new cmd methods to Main.java**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:1445` (在 `cmdClassInfo` 方法之后插入)

```java
    private static void cmdClassList(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> classList = analyzer.getClassList();
        System.out.println(brut.androlib.output.JsonOutput.toJson(classList));
    }

    private static final Option methodSearchPatternOption = Option.builder("p")
        .longOpt("pattern")
        .desc("Search pattern (regex). (default: .*)")
        .hasArg()
        .argName("pattern")
        .get();
    private static final Options methodSearchOptions = new Options();

    private static void cmdMethodSearch(String[] args) throws AndrolibException {
        methodSearchOptions.addOption(methodSearchPatternOption);
        CommandLine cli = parseOptions(methodSearchOptions, args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String pattern = cli.getOptionValue(methodSearchPatternOption, ".*");
        brut.androlib.analyze.ApkAnalyzer analyzer =
            new brut.androlib.analyze.ApkAnalyzer(new File(apkName), config);
        java.util.Map<String, Object> methods = analyzer.getMethodSearch(pattern);
        System.out.println(brut.androlib.output.JsonOutput.toJson(methods));
    }

    private static final Options fieldSearchOptions = new Options();

    private static void cmdFieldSearch(String[] args) throws AndrolibException {
        fieldSearchOptions.addOption(methodSearchPatternOption);
        CommandLine cli = parseOptions(fieldSearchOptions, args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String pattern = cli.getOptionValue(methodSearchPatternOption, ".*");
        brut.androlib.analyze.ApkAnalyzer analyzer =
            new brut.androlib.analyze.ApkAnalyzer(new File(apkName), config);
        java.util.Map<String, Object> fields = analyzer.getFieldSearch(pattern);
        System.out.println(brut.androlib.output.JsonOutput.toJson(fields));
    }

    private static void cmdAssetList(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> assets = analyzer.getAssetList();
        System.out.println(brut.androlib.output.JsonOutput.toJson(assets));
    }

    private static void cmdDexStrings(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> dexStrings = analyzer.getDexStrings();
        System.out.println(brut.androlib.output.JsonOutput.toJson(dexStrings));
    }

    private static void cmdPermissionDetail(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> permDetail = analyzer.getPermissionDetail();
        System.out.println(brut.androlib.output.JsonOutput.toJson(permDetail));
    }

    private static void cmdInheritance(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> inheritance = analyzer.getInheritanceInfo(className);
        System.out.println(brut.androlib.output.JsonOutput.toJson(inheritance));
    }

    private static void cmdManifestXml(String[] args) throws AndrolibException {
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
        java.util.Map<String, Object> manifestXml = analyzer.getManifestXml();
        System.out.println(brut.androlib.output.JsonOutput.toJson(manifestXml));
    }
```

- [ ] **Step 3: 验证 CLI 编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output does NOT contain: "error" or "ERROR"

- [ ] **Step 4: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add class-list, method-search, field-search, asset-list, dex-strings, permission-detail, inheritance, manifest-xml commands"`

---

### Task 3: Add 8 New HTTP API Endpoints

**Depends on:** Task 1
**Files:**
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java:248` (after handleClassInfo method)
- Modify: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java` (add routes)

- [ ] **Step 1: Add 8 new handler methods to ApiHandler.java**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java:248` (在 `handleClassInfo` 方法之后追加)

```java
    public String handleClassList(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getClassList());
    }

    public String handleMethodSearch(String apkPath, String pattern) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getMethodSearch(pattern));
    }

    public String handleFieldSearch(String apkPath, String pattern) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getFieldSearch(pattern));
    }

    public String handleAssetList(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getAssetList());
    }

    public String handleDexStrings(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getDexStrings());
    }

    public String handlePermissionDetail(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getPermissionDetail());
    }

    public String handleInheritance(String apkPath, String className) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getInheritanceInfo(className));
    }

    public String handleManifestXml(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getManifestXml());
    }
```

- [ ] **Step 2: Add 8 new routes to ApktoolServer.java**

文件: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java` (在现有路由注册之后追加)

```java
        app.get("/api/v1/class-list", ctx -> ctx.result(handler.handleClassList(ctx.queryParam("apk"))));
        app.get("/api/v1/method-search", ctx -> ctx.result(handler.handleMethodSearch(ctx.queryParam("apk"), ctx.queryParam("pattern", ".*"))));
        app.get("/api/v1/field-search", ctx -> ctx.result(handler.handleFieldSearch(ctx.queryParam("apk"), ctx.queryParam("pattern", ".*"))));
        app.get("/api/v1/asset-list", ctx -> ctx.result(handler.handleAssetList(ctx.queryParam("apk"))));
        app.get("/api/v1/dex-strings", ctx -> ctx.result(handler.handleDexStrings(ctx.queryParam("apk"))));
        app.get("/api/v1/permission-detail", ctx -> ctx.result(handler.handlePermissionDetail(ctx.queryParam("apk"))));
        app.get("/api/v1/inheritance", ctx -> ctx.result(handler.handleInheritance(ctx.queryParam("apk"), ctx.queryParam("class", ""))));
        app.get("/api/v1/manifest-xml", ctx -> ctx.result(handler.handleManifestXml(ctx.queryParam("apk"))));
```

- [ ] **Step 3: 验证 serve 模块编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-serve:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output does NOT contain: "error" or "ERROR"

- [ ] **Step 4: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java && git commit -m "feat(serve): add class-list, method-search, field-search, asset-list, dex-strings, permission-detail, inheritance, manifest-xml HTTP endpoints"`

---

### Task 4: Register 8 New Commands in CommandRegistry

**Depends on:** Task 2
**Files:**
- Modify: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java:163` (before `// === Service Commands ===`)

- [ ] **Step 1: Add 8 register() calls to CommandRegistry static block**

文件: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java:163` (在 `class-info` register 之后，`// === Service Commands ===` 之前插入)

```java
        register("class-list", null, "List all class names from DEX files",
            "apktool class-list <apk-file>", "JSON: {totalClasses, classes[]}",
            "analysis", new String[]{"apktool class-list app.apk"});

        register("method-search", null, "Search method signatures by regex pattern with full type info",
            "apktool method-search <apk-file> [-p <pattern>]", "JSON: {totalMatches, methods[{className, methodName, returnType, parameters[], accessFlags}]}",
            "analysis", new String[]{"apktool method-search app.apk -p 'onCreate'", "apktool method-search app.apk"});

        register("field-search", null, "Search field names by regex pattern with type info",
            "apktool field-search <apk-file> [-p <pattern>]", "JSON: {totalMatches, fields[{className, fieldName, type, accessFlags}]}",
            "analysis", new String[]{"apktool field-search app.apk -p 'mContext'", "apktool field-search app.apk"});

        register("asset-list", null, "List all files in the assets/ directory",
            "apktool asset-list <apk-file>", "JSON: {hasAssets, totalAssets, assets[]}",
            "analysis", new String[]{"apktool asset-list app.apk"});

        register("dex-strings", null, "Extract all strings from DEX files (not resources)",
            "apktool dex-strings <apk-file>", "JSON: {totalStrings, strings[]}",
            "analysis", new String[]{"apktool dex-strings app.apk"});

        register("permission-detail", null, "Get detailed permission analysis with danger level and category classification",
            "apktool permission-detail <apk-file>", "JSON: {totalPermissions, dangerousCount, normalCount, customCount, permissions[{name, dangerous, category}]}",
            "analysis", new String[]{"apktool permission-detail app.apk"});

        register("inheritance", null, "Get class inheritance chain (superclass hierarchy) for a given class",
            "apktool inheritance <apk-file> <class-name>", "JSON: {className, inheritanceChain[]}",
            "analysis", new String[]{"apktool inheritance app.apk com.example.MyActivity"});

        register("manifest-xml", null, "Get the full decoded AndroidManifest.xml as text (for AI context)",
            "apktool manifest-xml <apk-file>", "JSON: {manifestXml}",
            "analysis", new String[]{"apktool manifest-xml app.apk"});
```

- [ ] **Step 2: 验证编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-lib:compileJava 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output does NOT contain: "error" or "ERROR"

- [ ] **Step 3: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add brut.apktool/apktool-lib/src/main/java/brut/androlib/output/CommandRegistry.java && git commit -m "feat(output): register class-list, method-search, field-search, asset-list, dex-strings, permission-detail, inheritance, manifest-xml in CommandRegistry"`

---

### Task 5: Update CLAUDE.md Documentation

**Depends on:** Task 4
**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Add 8 new commands to CLI Command Reference table in CLAUDE.md**

文件: `CLAUDE.md` (在 Analysis Commands 表格中 `class-info` 行之后追加)

```markdown
| `class-list` | List all class names from DEX | totalClasses, classes[] |
| `method-search` | Search method signatures by regex | methods[{className, methodName, returnType, parameters[]}] |
| `field-search` | Search field names by regex | fields[{className, fieldName, type}] |
| `asset-list` | List files in assets/ directory | hasAssets, totalAssets, assets[] |
| `dex-strings` | Extract strings from DEX files | totalStrings, strings[] |
| `permission-detail` | Permission analysis with categories | permissions[{name, dangerous, category}] |
| `inheritance` | Class inheritance chain | className, inheritanceChain[] |
| `manifest-xml` | Full decoded AndroidManifest.xml text | manifestXml |
```

- [ ] **Step 2: Add 8 new HTTP API endpoints to CLAUDE.md**

文件: `CLAUDE.md` (在 HTTP API Endpoints 的 Analysis Endpoints 部分追加)

```markdown
- `/api/v1/class-list?apk=<path>` — List all class names
- `/api/v1/method-search?apk=<path>&pattern=<pattern>` — Search method signatures
- `/api/v1/field-search?apk=<path>&pattern=<pattern>` — Search field names
- `/api/v1/asset-list?apk=<path>` — List assets directory
- `/api/v1/dex-strings?apk=<path>` — Extract DEX strings
- `/api/v1/permission-detail?apk=<path>` — Detailed permission analysis
- `/api/v1/inheritance?apk=<path>&class=<name>` — Class inheritance chain
- `/api/v1/manifest-xml?apk=<path>` — Full decoded manifest XML
```

- [ ] **Step 3: 验证全项目编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew build 2>&1 | tail -30`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && git add CLAUDE.md && git commit -m "docs: add 8 new commands and HTTP endpoints to references"`
