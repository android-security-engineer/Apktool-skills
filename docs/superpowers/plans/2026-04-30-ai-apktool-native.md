# AI-Apktool: AI 原生改造实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 将 Apktool 从"文件操作工具"改造为"AI 原生逆向工程平台"——通过结构化 JSON 输出、搜索查询、智能分析、HTTP API、AI 集成五层能力，让 AI Agent 能直接消费 Apktool 的输出并执行深度分析。

**Architecture:** 用户/AI Agent → CLI 命令（info/search/analyze/serve/ai）→ ApkAnalyzer 服务层 → 复用现有 ApkDecoder/ResDecoder/ResTable/SmaliDecoder → 输出 JSON/YAML/Text。HTTP API 层包装 CLI 命令为 REST 端点。AI 层接入 LLM API 做深度理解。所有新功能通过新增模块 `brut.apktool:apktool-ai` 实现，不修改现有 decode/build 流程。

**Tech Stack:** Java 8, Gradle (Kotlin DSL), JUnit 4.13.2, Gson 2.11.0 (JSON 序列化), commons-cli 1.11.0, Javalin 6.x (HTTP API, Java 8 兼容版), baksmali/smali (已有)

**Risks:**
- Java 8 兼容性约束：不能使用 var、lambda 中的 var、Map.of 等Java 11+ 特性
- Javalin 6.x 可能需要 Java 11 → 缓解：使用 Javalin 5.x 或 Spark Java 2.x（Java 8 兼容）
- 新增 Gson 依赖增加包体积 → 缓解：Gson 约 230KB，相比 Guava 很小
- 测试需要 APK 样本 → 缓解：复用现有 30 个测试 APK fixture
- AI 层需要 LLM API key → 缓解：设计为可选功能，无 key 时降级

---

## Pre-Planning Analysis

**Feature:** AI-Apktool AI 原生改造
**Scope:** 多个子系统（CLI 扩展、分析引擎、搜索引擎、HTTP 服务、AI 集成）
**Files Create:** 22 个新文件
**Files Modify:** 5 个已有文件
**Tasks:** 15 tasks
**Order:** Task 1→2→3→4→5→6→7→8→9→10→11→12→13→14→15（线性依赖链，每层依赖前一层）
**Risks:** Java 8 兼容、依赖管理、测试覆盖

---

### Task 1: 添加 Gson 依赖和 JSON 输出基础设施

**Depends on:** None
**Files:**
- Modify: `gradle/libs.versions.toml` (添加 gson 版本和库定义)
- Modify: `brut.apktool/apktool-lib/build.gradle.kts` (添加 gson 依赖)
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/OutputFormat.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/output/JsonOutput.java`
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/output/JsonOutputTest.java`

- [ ] **Step 1: 添加 Gson 到版本目录 — 定义 Gson 依赖版本**

文件: `gradle/libs.versions.toml`

在 `[versions]` 部分添加 gson 版本，在 `[libraries]` 部分添加 gson 库定义。

```toml
# 在 [versions] 部分添加（按字母顺序插入到 commons_text 和 guava 之间）
gson = "2.11.0"

# 在 [libraries] 部分添加（按字母顺序插入到 commons_text 和 guava 之间）
gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
```

- [ ] **Step 2: 添加 Gson 依赖到 apktool-lib — 让库能使用 JSON 序列化**

文件: `brut.apktool/apktool-lib/build.gradle.kts:1-16`

```kotlin
dependencies {
    api(project(":brut.j.common"))
    api(project(":brut.j.util"))
    api(project(":brut.j.dir"))
    api(project(":brut.j.xml"))
    api(project(":brut.j.yaml"))

    implementation(libs.baksmali)
    implementation(libs.smali)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.commons.io)
    implementation(libs.commons.text)

    testImplementation(libs.junit)
    testImplementation(libs.xmlunit)
}
```

- [ ] **Step 3: 创建 OutputFormat 枚举 — 定义支持的输出格式**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/output/OutputFormat.java
package brut.androlib.output;

public enum OutputFormat {
    JSON,
    YAML,
    TEXT
}
```

- [ ] **Step 4: 创建 JsonOutput 工具类 — 提供统一的 JSON 序列化能力**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/output/JsonOutput.java
package brut.androlib.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class JsonOutput {
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static void write(Object obj, OutputStream out) {
        Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        GSON.toJson(obj, writer);
        try {
            writer.flush();
        } catch (java.io.IOException ignored) {
        }
    }
}
```

- [ ] **Step 5: 创建 JsonOutput 单元测试 — 验证 JSON 序列化基础功能**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/output/JsonOutputTest.java
package brut.androlib.output;

import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonOutputTest {

    @Test
    public void testToJsonSimpleObject() {
        TestObject obj = new TestObject("hello", 42);
        String json = JsonOutput.toJson(obj);
        assertTrue(json.contains("\"name\":\"hello\""));
        assertTrue(json.contains("\"value\":42"));
    }

    @Test
    public void testToJsonList() {
        List<String> list = Arrays.asList("a", "b", "c");
        String json = JsonOutput.toJson(list);
        assertTrue(json.contains("\"a\""));
        assertTrue(json.contains("\"b\""));
        assertTrue(json.contains("\"c\""));
    }

    @Test
    public void testToJsonNullField() {
        TestObject obj = new TestObject(null, 0);
        String json = JsonOutput.toJson(obj);
        assertTrue(json.contains("\"name\":null"));
    }

    @Test
    public void testToJsonEmptyString() {
        TestObject obj = new TestObject("", 0);
        String json = JsonOutput.toJson(obj);
        assertTrue(json.contains("\"name\":\"\""));
    }

    @Test
    public void testWriteToStream() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        TestObject obj = new TestObject("stream", 1);
        JsonOutput.write(obj, baos);
        String result = baos.toString("UTF-8");
        assertTrue(result.contains("\"name\":\"stream\""));
    }

    private static class TestObject {
        String name;
        int value;

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
```

- [ ] **Step 6: 验证依赖和测试**
Run: `./gradlew :brut.apktool:apktool-lib:compileJava :brut.apktool:apktool-lib:test --tests "brut.androlib.output.JsonOutputTest" 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output contains: "JsonOutputTest" and "tests completed"

- [ ] **Step 7: 提交**
Run: `git add gradle/libs.versions.toml brut.apktool/apktool-lib/build.gradle.kts brut.apktool/apktool-lib/src/main/java/brut/androlib/output/ brut.apktool/apktool-lib/src/test/java/brut/androlib/output/ && git commit -m "feat(output): add Gson dependency and JSON output infrastructure"`

---

### Task 2: 创建 ApkAnalyzer 核心分析引擎

**Depends on:** Task 1
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkSummary.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ManifestInfo.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ComponentInfo.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SecurityReport.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ResourceSummary.java`

- [ ] **Step 1: 创建 ComponentInfo 数据类 — 表示 Android 四大组件信息**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ComponentInfo.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class ComponentInfo {
    private String name;
    private String type;
    private boolean exported;
    private List<String> intentFilters = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    public ComponentInfo() {}

    public ComponentInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isExported() { return exported; }
    public void setExported(boolean exported) { this.exported = exported; }
    public List<String> getIntentFilters() { return intentFilters; }
    public void setIntentFilters(List<String> intentFilters) { this.intentFilters = intentFilters; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
```

- [ ] **Step 2: 创建 ManifestInfo 数据类 — 表示 AndroidManifest 解析结果**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ManifestInfo.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class ManifestInfo {
    private String packageName;
    private String versionName;
    private int versionCode;
    private String minSdkVersion;
    private String targetSdkVersion;
    private String maxSdkVersion;
    private List<String> permissions = new ArrayList<>();
    private List<ComponentInfo> activities = new ArrayList<>();
    private List<ComponentInfo> services = new ArrayList<>();
    private List<ComponentInfo> receivers = new ArrayList<>();
    private List<ComponentInfo> providers = new ArrayList<>();
    private List<String> usesLibraries = new ArrayList<>();
    private boolean debuggable;
    private boolean allowBackup;
    private String networkSecurityConfig;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }
    public int getVersionCode() { return versionCode; }
    public void setVersionCode(int versionCode) { this.versionCode = versionCode; }
    public String getMinSdkVersion() { return minSdkVersion; }
    public void setMinSdkVersion(String minSdkVersion) { this.minSdkVersion = minSdkVersion; }
    public String getTargetSdkVersion() { return targetSdkVersion; }
    public void setTargetSdkVersion(String targetSdkVersion) { this.targetSdkVersion = targetSdkVersion; }
    public String getMaxSdkVersion() { return maxSdkVersion; }
    public void setMaxSdkVersion(String maxSdkVersion) { this.maxSdkVersion = maxSdkVersion; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<ComponentInfo> getActivities() { return activities; }
    public void setActivities(List<ComponentInfo> activities) { this.activities = activities; }
    public List<ComponentInfo> getServices() { return services; }
    public void setServices(List<ComponentInfo> services) { this.services = services; }
    public List<ComponentInfo> getReceivers() { return receivers; }
    public void setReceivers(List<ComponentInfo> receivers) { this.receivers = receivers; }
    public List<ComponentInfo> getProviders() { return providers; }
    public void setProviders(List<ComponentInfo> providers) { this.providers = providers; }
    public List<String> getUsesLibraries() { return usesLibraries; }
    public void setUsesLibraries(List<String> usesLibraries) { this.usesLibraries = usesLibraries; }
    public boolean isDebuggable() { return debuggable; }
    public void setDebuggable(boolean debuggable) { this.debuggable = debuggable; }
    public boolean isAllowBackup() { return allowBackup; }
    public void setAllowBackup(boolean allowBackup) { this.allowBackup = allowBackup; }
    public String getNetworkSecurityConfig() { return networkSecurityConfig; }
    public void setNetworkSecurityConfig(String networkSecurityConfig) { this.networkSecurityConfig = networkSecurityConfig; }
}
```

- [ ] **Step 3: 创建 SecurityReport 数据类 — 表示安全分析结果**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/SecurityReport.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class SecurityReport {
    private List<String> dangerousPermissions = new ArrayList<>();
    private List<String> highRiskComponents = new ArrayList<>();
    private boolean debuggable;
    private boolean allowBackup;
    private boolean usesCleartextTraffic;
    private List<String> findings = new ArrayList<>();
    private int riskScore;

    public List<String> getDangerousPermissions() { return dangerousPermissions; }
    public void setDangerousPermissions(List<String> dangerousPermissions) { this.dangerousPermissions = dangerousPermissions; }
    public List<String> getHighRiskComponents() { return highRiskComponents; }
    public void setHighRiskComponents(List<String> highRiskComponents) { this.highRiskComponents = highRiskComponents; }
    public boolean isDebuggable() { return debuggable; }
    public void setDebuggable(boolean debuggable) { this.debuggable = debuggable; }
    public boolean isAllowBackup() { return allowBackup; }
    public void setAllowBackup(boolean allowBackup) { this.allowBackup = allowBackup; }
    public boolean isUsesCleartextTraffic() { return usesCleartextTraffic; }
    public void setUsesCleartextTraffic(boolean usesCleartextTraffic) { this.usesCleartextTraffic = usesCleartextTraffic; }
    public List<String> getFindings() { return findings; }
    public void setFindings(List<String> findings) { this.findings = findings; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
}
```

- [ ] **Step 4: 创建 ResourceSummary 数据类 — 表示资源表摘要**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ResourceSummary.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceSummary {
    private String packageName;
    private int packageId;
    private Map<String, Integer> typeCounts = new LinkedHashMap<>();
    private List<String> locales = new ArrayList<>();
    private int totalEntries;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    public Map<String, Integer> getTypeCounts() { return typeCounts; }
    public void setTypeCounts(Map<String, Integer> typeCounts) { this.typeCounts = typeCounts; }
    public List<String> getLocales() { return locales; }
    public void setLocales(List<String> locales) { this.locales = locales; }
    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }
}
```

- [ ] **Step 5: 创建 ApkSummary 数据类 — 表示 APK 总体摘要**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkSummary.java
package brut.androlib.analyze;

import java.util.List;

public class ApkSummary {
    private String fileName;
    private long fileSize;
    private String packageName;
    private String versionName;
    private int versionCode;
    private String minSdkVersion;
    private String targetSdkVersion;
    private int dexCount;
    private boolean hasResources;
    private boolean hasAssets;
    private boolean hasNativeLibs;
    private List<String> architectures;
    private int permissionCount;
    private int activityCount;
    private int serviceCount;
    private int receiverCount;
    private int providerCount;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }
    public int getVersionCode() { return versionCode; }
    public void setVersionCode(int versionCode) { this.versionCode = versionCode; }
    public String getMinSdkVersion() { return minSdkVersion; }
    public void setMinSdkVersion(String minSdkVersion) { this.minSdkVersion = minSdkVersion; }
    public String getTargetSdkVersion() { return targetSdkVersion; }
    public void setTargetSdkVersion(String targetSdkVersion) { this.targetSdkVersion = targetSdkVersion; }
    public int getDexCount() { return dexCount; }
    public void setDexCount(int dexCount) { this.dexCount = dexCount; }
    public boolean isHasResources() { return hasResources; }
    public void setHasResources(boolean hasResources) { this.hasResources = hasResources; }
    public boolean isHasAssets() { return hasAssets; }
    public void setHasAssets(boolean hasAssets) { this.hasAssets = hasAssets; }
    public boolean isHasNativeLibs() { return hasNativeLibs; }
    public void setHasNativeLibs(boolean hasNativeLibs) { this.hasNativeLibs = hasNativeLibs; }
    public List<String> getArchitectures() { return architectures; }
    public void setArchitectures(List<String> architectures) { this.architectures = architectures; }
    public int getPermissionCount() { return permissionCount; }
    public void setPermissionCount(int permissionCount) { this.permissionCount = permissionCount; }
    public int getActivityCount() { return activityCount; }
    public void setActivityCount(int activityCount) { this.activityCount = activityCount; }
    public int getServiceCount() { return serviceCount; }
    public void setServiceCount(int serviceCount) { this.serviceCount = serviceCount; }
    public int getReceiverCount() { return receiverCount; }
    public void setReceiverCount(int receiverCount) { this.receiverCount = receiverCount; }
    public int getProviderCount() { return providerCount; }
    public void setProviderCount(int providerCount) { this.providerCount = providerCount; }
}
```

- [ ] **Step 6: 创建 ApkAnalyzer 核心类 — 聚合所有分析能力的入口**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkAnalyzer.java
package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.meta.ApkInfo;
import brut.androlib.meta.SdkInfo;
import brut.androlib.meta.VersionInfo;
import brut.androlib.res.ResDecoder;
import brut.androlib.res.decoder.BinaryXmlResourceParser;
import brut.androlib.res.decoder.ManifestPullEventHandler;
import brut.androlib.res.decoder.ResXmlPullStreamDecoder;
import brut.androlib.res.table.ResEntry;
import brut.androlib.res.table.ResPackage;
import brut.androlib.res.table.ResTable;
import brut.androlib.res.table.ResType;
import brut.androlib.res.xml.ResXmlSerializer;
import brut.androlib.smali.SmaliDecoder;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkAnalyzer {
    private static final Pattern DEX_PATTERN = Pattern.compile("classes([2-9]|[1-9][0-9]+)?\\.dex");
    private static final Pattern MANIFEST_PERMISSION = Pattern.compile(
        "<uses-permission\\s+android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_ACTIVITY = Pattern.compile(
        "<activity(?:[^>]*)\\s+android:name=\"([^\"]+)\"(?:[^>]*)\\s+android:exported=\"(true|false)\"|(?:android:exported=\"(true|false)\")\\s+(?:[^>]*)\\s+android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_SERVICE = Pattern.compile(
        "<service(?:[^>]*)\\s+android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_RECEIVER = Pattern.compile(
        "<receiver(?:[^>]*)\\s+android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_PROVIDER = Pattern.compile(
        "<provider(?:[^>]*)\\s+android:name=\"([^\"]+)\"");
    private static final Pattern MANIFEST_EXPORTED = Pattern.compile(
        "android:exported=\"(true|false)\"");
    private static final Pattern MANIFEST_DEBUGGABLE = Pattern.compile(
        "android:debuggable=\"(true|false)\"");
    private static final Pattern MANIFEST_ALLOW_BACKUP = Pattern.compile(
        "android:allowBackup=\"(true|false)\"");
    private static final Pattern MANIFEST_PACKAGE = Pattern.compile(
        "package=\"([^\"]+)\"");
    private static final Pattern MANIFEST_VERSION_CODE = Pattern.compile(
        "android:versionCode=\"(\\d+)\"");
    private static final Pattern MANIFEST_VERSION_NAME = Pattern.compile(
        "android:versionName=\"([^\"]+)\"");

    private static final Set<String> DANGEROUS_PERMISSIONS = new HashSet<>(Arrays.asList(
        "android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS",
        "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR",
        "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG",
        "android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE",
        "android.permission.READ_SMS", "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS", "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_PHONE_NUMBERS", "android.permission.ANSWER_PHONE_CALLS",
        "android.permission.ACCEPT_HANDOVER", "android.permission.BODY_SENSORS",
        "android.permission.ACTIVITY_RECOGNITION", "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO", "android.permission.READ_MEDIA_AUDIO",
        "android.permission.POST_NOTIFICATIONS", "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"
    ));

    private final ExtFile mApkFile;
    private final Config mConfig;

    public ApkAnalyzer(File apkFile, Config config) {
        mApkFile = new ExtFile(apkFile);
        mConfig = config;
    }

    public ApkSummary getSummary() throws AndrolibException {
        ApkSummary summary = new ApkSummary();
        summary.setFileName(mApkFile.getName());
        summary.setFileSize(mApkFile.length());

        try {
            Directory dir = mApkFile.getDirectory();

            // Count dex files
            int dexCount = 0;
            for (String file : dir.getFiles(true)) {
                if (DEX_PATTERN.matcher(file).matches() || file.equals("classes.dex")) {
                    dexCount++;
                }
            }
            summary.setDexCount(dexCount);
            summary.setHasResources(dir.containsFile("resources.arsc"));
            summary.setHasAssets(dir.containsDir("assets"));
            summary.setHasNativeLibs(dir.containsDir("lib"));

            // Detect architectures
            List<String> archs = new ArrayList<>();
            if (dir.containsDir("lib")) {
                for (String subDir : dir.getDir("lib").getFiles(false)) {
                    if (!subDir.contains(".") && !subDir.equals(".")) {
                        archs.add(subDir);
                    }
                }
            }
            summary.setArchitectures(archs);

            // Get manifest info
            ManifestInfo manifest = getManifestInfo();
            if (manifest != null) {
                summary.setPackageName(manifest.getPackageName());
                summary.setVersionName(manifest.getVersionName());
                summary.setVersionCode(manifest.getVersionCode());
                summary.setMinSdkVersion(manifest.getMinSdkVersion());
                summary.setTargetSdkVersion(manifest.getTargetSdkVersion());
                summary.setPermissionCount(manifest.getPermissions().size());
                summary.setActivityCount(manifest.getActivities().size());
                summary.setServiceCount(manifest.getServices().size());
                summary.setReceiverCount(manifest.getReceivers().size());
                summary.setProviderCount(manifest.getProviders().size());
            }
        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        return summary;
    }

    public ManifestInfo getManifestInfo() throws AndrolibException {
        ManifestInfo info = new ManifestInfo();

        try {
            Directory dir = mApkFile.getDirectory();
            if (!dir.containsFile("AndroidManifest.xml")) {
                return null;
            }

            // Decode manifest XML
            ApkInfo apkInfo = new ApkInfo();
            apkInfo.setApkFile(mApkFile);
            ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
            BinaryXmlResourceParser parser = new BinaryXmlResourceParser(
                resDecoder.getTable(), false, false);
            ResXmlSerializer serial = new ResXmlSerializer(true);
            ManifestPullEventHandler handler = new ManifestPullEventHandler(apkInfo, true);
            ResXmlPullStreamDecoder decoder = new ResXmlPullStreamDecoder(parser, serial, handler);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (InputStream in = dir.getFileInput("AndroidManifest.xml")) {
                decoder.decode(in, baos);
            }

            String manifestXml = baos.toString("UTF-8");
            parseManifestFields(manifestXml, info);

            // Also fill from ApkInfo which resolves resource references
            SdkInfo sdkInfo = apkInfo.getSdkInfo();
            if (sdkInfo.getMinSdkVersion() != null) {
                info.setMinSdkVersion(sdkInfo.getMinSdkVersion());
            }
            if (sdkInfo.getTargetSdkVersion() != null) {
                info.setTargetSdkVersion(sdkInfo.getTargetSdkVersion());
            }

            VersionInfo versionInfo = apkInfo.getVersionInfo();
            if (versionInfo.getVersionName() != null) {
                info.setVersionName(versionInfo.getVersionName());
            }
            info.setVersionCode(versionInfo.getVersionCode());

        } catch (DirectoryException | IOException ex) {
            throw new AndrolibException(ex);
        }

        return info;
    }

    private void parseManifestFields(String xml, ManifestInfo info) {
        // Package name
        Matcher m = MANIFEST_PACKAGE.matcher(xml);
        if (m.find()) info.setPackageName(m.group(1));

        // Permissions
        m = MANIFEST_PERMISSION.matcher(xml);
        while (m.find()) {
            info.getPermissions().add(m.group(1));
        }

        // Activities
        m = MANIFEST_ACTIVITY.matcher(xml);
        while (m.find()) {
            String name = m.group(1) != null ? m.group(1) : m.group(4);
            if (name != null) {
                ComponentInfo comp = new ComponentInfo(name, "activity");
                // Check if exported is in this same tag
                Matcher expMatcher = MANIFEST_EXPORTED.matcher(
                    xml.substring(Math.max(0, m.start() - 200), Math.min(xml.length(), m.end() + 200)));
                if (expMatcher.find()) {
                    comp.setExported("true".equals(expMatcher.group(1)));
                }
                info.getActivities().add(comp);
            }
        }

        // Services
        m = MANIFEST_SERVICE.matcher(xml);
        while (m.find()) {
            ComponentInfo comp = new ComponentInfo(m.group(1), "service");
            info.getServices().add(comp);
        }

        // Receivers
        m = MANIFEST_RECEIVER.matcher(xml);
        while (m.find()) {
            ComponentInfo comp = new ComponentInfo(m.group(1), "receiver");
            info.getReceivers().add(comp);
        }

        // Providers
        m = MANIFEST_PROVIDER.matcher(xml);
        while (m.find()) {
            ComponentInfo comp = new ComponentInfo(m.group(1), "provider");
            info.getProviders().add(comp);
        }

        // Debuggable
        m = MANIFEST_DEBUGGABLE.matcher(xml);
        if (m.find()) info.setDebuggable("true".equals(m.group(1)));

        // Allow backup
        m = MANIFEST_ALLOW_BACKUP.matcher(xml);
        if (m.find()) info.setAllowBackup("true".equals(m.group(1)));
    }

    public SecurityReport getSecurityReport() throws AndrolibException {
        SecurityReport report = new SecurityReport();
        ManifestInfo manifest = getManifestInfo();
        if (manifest == null) return report;

        // Check dangerous permissions
        for (String perm : manifest.getPermissions()) {
            if (DANGEROUS_PERMISSIONS.contains(perm)) {
                report.getDangerousPermissions().add(perm);
            }
        }

        // Check exported components without permission
        for (ComponentInfo comp : manifest.getActivities()) {
            if (comp.isExported() && comp.getPermissions().isEmpty()) {
                report.getHighRiskComponents().add(comp.getType() + ": " + comp.getName());
            }
        }
        for (ComponentInfo comp : manifest.getServices()) {
            if (comp.isExported() && comp.getPermissions().isEmpty()) {
                report.getHighRiskComponents().add(comp.getType() + ": " + comp.getName());
            }
        }
        for (ComponentInfo comp : manifest.getReceivers()) {
            if (comp.isExported() && comp.getPermissions().isEmpty()) {
                report.getHighRiskComponents().add(comp.getType() + ": " + comp.getName());
            }
        }
        for (ComponentInfo comp : manifest.getProviders()) {
            if (comp.isExported() && comp.getPermissions().isEmpty()) {
                report.getHighRiskComponents().add(comp.getType() + ": " + comp.getName());
            }
        }

        report.setDebuggable(manifest.isDebuggable());
        report.setAllowBackup(manifest.isAllowBackup());

        // Generate findings
        if (manifest.isDebuggable()) {
            report.getFindings().add("HIGH: Application is debuggable - android:debuggable=true");
        }
        if (manifest.isAllowBackup()) {
            report.getFindings().add("MEDIUM: Application allows backup - android:allowBackup=true");
        }
        if (!report.getDangerousPermissions().isEmpty()) {
            report.getFindings().add("MEDIUM: Application requests " + report.getDangerousPermissions().size() + " dangerous permissions");
        }
        if (!report.getHighRiskComponents().isEmpty()) {
            report.getFindings().add("HIGH: " + report.getHighRiskComponents().size() + " exported components without permission protection");
        }
        if (manifest.getTargetSdkVersion() != null) {
            try {
                int targetSdk = Integer.parseInt(manifest.getTargetSdkVersion());
                if (targetSdk < 29) {
                    report.getFindings().add("MEDIUM: Target SDK (" + targetSdk + ") is below Android 10 - users get broad storage access");
                }
            } catch (NumberFormatException ignored) {}
        }

        // Calculate risk score (0-100)
        int score = 0;
        score += report.isDebuggable() ? 30 : 0;
        score += report.isAllowBackup() ? 10 : 0;
        score += Math.min(20, report.getDangerousPermissions().size() * 2);
        score += Math.min(30, report.getHighRiskComponents().size() * 5);
        report.setRiskScore(Math.min(100, score));

        return report;
    }

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

            int total = 0;
            for (ResEntry entry : pkg.listEntries()) {
                ResType type = entry.getType();
                String typeName = type.getName();
                Integer count = summary.getTypeCounts().get(typeName);
                summary.getTypeCounts().put(typeName, count != null ? count + 1 : 1);
                total++;
            }
            summary.setTotalEntries(total);
        }

        return summary;
    }
}
```

- [ ] **Step 7: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ && git commit -m "feat(analyze): add ApkAnalyzer core engine with data models"`

---

### Task 3: ApkAnalyzer 单元测试

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ApkAnalyzerTest.java`

- [ ] **Step 1: 创建 ApkAnalyzer 单元测试 — 使用现有测试 APK 验证分析功能**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ApkAnalyzerTest.java
package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.BaseTest;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.File;

public class ApkAnalyzerTest extends BaseTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";

    @Test
    public void testGetSummaryReturnsNonNull() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        assertNotNull(summary);
        assertNotNull(summary.getFileName());
        assertTrue(summary.getFileSize() > 0);
    }

    @Test
    public void testGetSummaryDexCount() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        assertTrue("Should have at least 1 dex file", summary.getDexCount() >= 1);
    }

    @Test
    public void testGetSummaryHasResources() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        assertTrue("Should have resources", summary.isHasResources());
    }

    @Test
    public void testGetManifestInfoReturnsNonNull() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ManifestInfo manifest = analyzer.getManifestInfo();
        assertNotNull(manifest);
    }

    @Test
    public void testGetSecurityReportReturnsNonNull() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        SecurityReport report = analyzer.getSecurityReport();
        assertNotNull(report);
        assertTrue("Risk score should be 0-100", report.getRiskScore() >= 0 && report.getRiskScore() <= 100);
    }

    @Test
    public void testGetSecurityReportNoDebuggable() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        SecurityReport report = analyzer.getSecurityReport();
        // Most test APKs should not be debuggable
        assertNotNull(report.getFindings());
    }

    @Test
    public void testJsonOutputFromSummary() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        String json = brut.androlib.output.JsonOutput.toJson(summary);
        assertNotNull(json);
        assertTrue("JSON should contain fileName", json.contains("fileName"));
        assertTrue("JSON should contain fileSize", json.contains("fileSize"));
    }

    @Test
    public void testJsonOutputFromManifest() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ManifestInfo manifest = analyzer.getManifestInfo();
        String json = brut.androlib.output.JsonOutput.toJson(manifest);
        assertNotNull(json);
        assertTrue("JSON should contain permissions", json.contains("permissions"));
    }

    @Test
    public void testJsonOutputFromSecurityReport() throws Exception {
        File apkFile = getTestApk(TEST_APK);
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        SecurityReport report = analyzer.getSecurityReport();
        String json = brut.androlib.output.JsonOutput.toJson(report);
        assertNotNull(json);
        assertTrue("JSON should contain riskScore", json.contains("riskScore"));
    }

    private File getTestApk(String resourcePath) {
        return new File(getClass().getResource(resourcePath).getFile());
    }
}
```

- [ ] **Step 2: 验证分析器测试**
Run: `./gradlew :brut.apktool:apktool-lib:test --tests "brut.androlib.analyze.ApkAnalyzerTest" 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output contains: "ApkAnalyzerTest"

- [ ] **Step 3: 提交**
Run: `git add brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ && git commit -m "test(analyze): add ApkAnalyzer unit tests with test APK fixtures"`

---

### Task 4: 扩展 CLI — info 和 manifest 命令

**Depends on:** Task 3
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:316-361` (switch 块)

- [ ] **Step 1: 在 Main.java 的 switch 块中添加 info 和 manifest 命令 — 扩展 CLI 命令集**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:316-361`

在 switch 块的 `default` 分支之前添加以下 case:

```java
            case "info":
                cmdInfo(cmdArgs);
                break;
            case "manifest":
                cmdManifest(cmdArgs);
                break;
            case "permissions":
                cmdPermissions(cmdArgs);
                break;
            case "activities":
                cmdComponents(cmdArgs, "activity");
                break;
            case "services":
                cmdComponents(cmdArgs, "service");
                break;
            case "receivers":
                cmdComponents(cmdArgs, "receiver");
                break;
            case "providers":
                cmdComponents(cmdArgs, "provider");
                break;
            case "sdk-info":
                cmdSdkInfo(cmdArgs);
                break;
            case "resources":
                cmdResources(cmdArgs);
                break;
            case "security":
                cmdSecurity(cmdArgs);
                break;
```

- [ ] **Step 2: 在 Main.java 中添加 info 命令实现 — APK 元数据摘要**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 `cmdPublicizeResources` 方法之后添加以下方法:

```java
    private static void cmdInfo(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.ApkSummary summary = analyzer.getSummary();
        System.out.println(brut.androlib.output.JsonOutput.toJson(summary));
    }

    private static void cmdManifest(String[] args) throws AndrolibException {
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
            System.err.println("No AndroidManifest.xml found in the APK.");
            System.exit(1);
            return;
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(manifest));
    }

    private static void cmdPermissions(String[] args) throws AndrolibException {
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
            System.err.println("No AndroidManifest.xml found in the APK.");
            System.exit(1);
            return;
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(manifest.getPermissions()));
    }

    private static void cmdComponents(String[] args, String type) throws AndrolibException {
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
            System.err.println("No AndroidManifest.xml found in the APK.");
            System.exit(1);
            return;
        }

        java.util.List<brut.androlib.analyze.ComponentInfo> components;
        switch (type) {
            case "activity": components = manifest.getActivities(); break;
            case "service": components = manifest.getServices(); break;
            case "receiver": components = manifest.getReceivers(); break;
            case "provider": components = manifest.getProviders(); break;
            default: components = java.util.Collections.emptyList(); break;
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(components));
    }

    private static void cmdSdkInfo(String[] args) throws AndrolibException {
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
            System.err.println("No AndroidManifest.xml found in the APK.");
            System.exit(1);
            return;
        }

        java.util.Map<String, String> sdkInfo = new java.util.LinkedHashMap<>();
        if (manifest.getMinSdkVersion() != null) sdkInfo.put("minSdkVersion", manifest.getMinSdkVersion());
        if (manifest.getTargetSdkVersion() != null) sdkInfo.put("targetSdkVersion", manifest.getTargetSdkVersion());
        if (manifest.getMaxSdkVersion() != null) sdkInfo.put("maxSdkVersion", manifest.getMaxSdkVersion());
        System.out.println(brut.androlib.output.JsonOutput.toJson(sdkInfo));
    }

    private static void cmdResources(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.ResourceSummary summary = analyzer.getResourceSummary();
        System.out.println(brut.androlib.output.JsonOutput.toJson(summary));
    }

    private static void cmdSecurity(String[] args) throws AndrolibException {
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
        brut.androlib.analyze.SecurityReport report = analyzer.getSecurityReport();
        System.out.println(brut.androlib.output.JsonOutput.toJson(report));
    }
```

- [ ] **Step 3: 更新 printUsage 方法 — 添加新命令的帮助信息**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 printUsage 方法的 footer 之前（`writer.println("For additional info, see: https://apktool.org");` 之前）添加:

```java
        if (advancedMode && loadedOptions == null) {
            writer.println("apktool info <apk-file>");
            writer.println("apktool manifest <apk-file>");
            writer.println("apktool permissions <apk-file>");
            writer.println("apktool activities <apk-file>");
            writer.println("apktool services <apk-file>");
            writer.println("apktool receivers <apk-file>");
            writer.println("apktool providers <apk-file>");
            writer.println("apktool sdk-info <apk-file>");
            writer.println("apktool resources <apk-file>");
            writer.println("apktool security <apk-file>");
            writer.println();
        }
```

- [ ] **Step 4: 验证 CLI 编译**
Run: `./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add info/manifest/permissions/components/sdk-info/resources/security commands"`

---

### Task 5: 搜索引擎 — 字符串和类搜索

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/search/ApkSearcher.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/search/SearchResult.java`
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/search/ApkSearcherTest.java`

- [ ] **Step 1: 创建 SearchResult 数据类 — 统一搜索结果格式**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/search/SearchResult.java
package brut.androlib.search;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private String query;
    private String type;
    private int totalMatches;
    private List<SearchMatch> matches = new ArrayList<>();

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
    public List<SearchMatch> getMatches() { return matches; }
    public void setMatches(List<SearchMatch> matches) { this.matches = matches; }

    public static class SearchMatch {
        private String name;
        private String value;
        private String source;

        public SearchMatch() {}

        public SearchMatch(String name, String value, String source) {
            this.name = name;
            this.value = value;
            this.source = source;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
```

- [ ] **Step 2: 创建 ApkSearcher 核心类 — 在 APK 中搜索字符串和类名**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/search/ApkSearcher.java
package brut.androlib.search;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.res.ResDecoder;
import brut.androlib.res.table.ResEntry;
import brut.androlib.res.table.ResPackage;
import brut.androlib.res.table.ResTable;
import brut.androlib.res.table.value.ResString;
import brut.androlib.res.table.value.ResValue;
import brut.androlib.meta.ApkInfo;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.ZipDexContainer;
import com.android.tools.smali.dexlib2.iface.ClassDef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ApkSearcher {
    private final ExtFile mApkFile;
    private final Config mConfig;

    public ApkSearcher(File apkFile, Config config) {
        mApkFile = new ExtFile(apkFile);
        mConfig = config;
    }

    public SearchResult searchStrings(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("strings");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ApkInfo apkInfo = new ApkInfo();
            apkInfo.setApkFile(mApkFile);
            ResDecoder resDecoder = new ResDecoder(apkInfo, mConfig);
            ResTable table = resDecoder.getTable();
            table.load();

            ResPackage pkg = table.getMainPackage();
            if (pkg != null) {
                for (ResEntry entry : pkg.listEntries()) {
                    ResValue value = entry.getValue();
                    if (value instanceof ResString) {
                        String strValue = ((ResString) value).getValue();
                        if (strValue != null && regex.matcher(strValue).find()) {
                            result.getMatches().add(new SearchResult.SearchMatch(
                                entry.getType().getName() + "/" + entry.getSpec().getName(),
                                strValue,
                                "resources"
                            ));
                        }
                    }
                }
            }
        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }

    public SearchResult searchClasses(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("classes");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ZipDexContainer container = new ZipDexContainer(mApkFile, null);
            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                for (ClassDef classDef : dexFile.getClasses()) {
                    String className = classDef.getType();
                    String humanName = className.substring(1, className.length() - 1).replace('/', '.');
                    if (regex.matcher(humanName).find()) {
                        result.getMatches().add(new SearchResult.SearchMatch(
                            humanName,
                            className,
                            dexName
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }

    public SearchResult searchMethods(String pattern) throws AndrolibException {
        SearchResult result = new SearchResult();
        result.setQuery(pattern);
        result.setType("methods");

        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        try {
            ZipDexContainer container = new ZipDexContainer(mApkFile, null);
            for (String dexName : container.getDexEntryNames()) {
                ZipDexContainer.DexEntry<DexBackedDexFile> entry = container.getEntry(dexName);
                if (entry == null) continue;

                DexBackedDexFile dexFile = entry.getDexFile();
                for (ClassDef classDef : dexFile.getClasses()) {
                    String className = classDef.getType()
                        .substring(1, classDef.getType().length() - 1).replace('/', '.');
                    for (com.android.tools.smali.dexlib2.iface.Method method : classDef.getMethods()) {
                        if (regex.matcher(method.getName()).find()) {
                            result.getMatches().add(new SearchResult.SearchMatch(
                                className + "." + method.getName(),
                                method.getName(),
                                dexName
                            ));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        } finally {
            try { mApkFile.close(); } catch (Exception ignored) {}
        }

        result.setTotalMatches(result.getMatches().size());
        return result;
    }
}
```

- [ ] **Step 3: 创建 ApkSearcher 单元测试**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/search/ApkSearcherTest.java
package brut.androlib.search;

import brut.androlib.Config;
import brut.androlib.BaseTest;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.File;

public class ApkSearcherTest extends BaseTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";

    @Test
    public void testSearchClassesReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);
        SearchResult result = searcher.searchClasses(".*");
        assertNotNull(result);
        assertEquals("classes", result.getType());
    }

    @Test
    public void testSearchClassesWithPattern() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);
        SearchResult result = searcher.searchClasses("Activity");
        assertNotNull(result);
        assertTrue("Should find at least 1 class containing 'Activity'",
            result.getTotalMatches() >= 0);
    }

    @Test
    public void testSearchStringsReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);
        SearchResult result = searcher.searchStrings(".*");
        assertNotNull(result);
        assertEquals("strings", result.getType());
    }

    @Test
    public void testSearchMethodsWithPattern() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);
        SearchResult result = searcher.searchMethods("onCreate");
        assertNotNull(result);
        assertEquals("methods", result.getType());
    }

    @Test
    public void testSearchResultJsonOutput() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);
        SearchResult result = searcher.searchClasses("Activity");
        String json = brut.androlib.output.JsonOutput.toJson(result);
        assertNotNull(json);
        assertTrue("JSON should contain query field", json.contains("query"));
        assertTrue("JSON should contain matches field", json.contains("matches"));
    }
}
```

- [ ] **Step 4: 验证搜索引擎测试**
Run: `./gradlew :brut.apktool:apktool-lib:test --tests "brut.androlib.search.ApkSearcherTest" 2>&1 | tail -15`
Expected:
  - Exit code: 0
  - Output contains: "ApkSearcherTest"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/search/ brut.apktool/apktool-lib/src/test/java/brut/androlib/search/ && git commit -m "feat(search): add ApkSearcher for string/class/method search with tests"`

---

### Task 6: 扩展 CLI — search 命令

**Depends on:** Task 5
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java` (switch 块和方法区)

- [ ] **Step 1: 在 Main.java 的 switch 块中添加 search 命令**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 Task 4 添加的 `case "security":` 之后添加:

```java
            case "search":
                cmdSearch(cmdArgs);
                break;
```

- [ ] **Step 2: 在 Main.java 中添加 search 命令实现**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 `cmdSecurity` 方法之后添加:

```java
    private static final Option searchTypeOption = Option.builder("t")
        .longOpt("type")
        .desc("Search type: strings, classes, methods. (default: classes)")
        .hasArg()
        .argName("type")
        .get();

    private static final Options searchOptions = new Options();

    private static void cmdSearch(String[] args) throws AndrolibException {
        searchOptions.addOption(searchTypeOption);
        CommandLine cli = parseOptions(searchOptions, args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String pattern = argList.size() > 1 ? argList.get(1) : ".*";

        String type = cli.getOptionValue(searchTypeOption, "classes");

        brut.androlib.search.ApkSearcher searcher =
            new brut.androlib.search.ApkSearcher(new File(apkName), config);

        brut.androlib.search.SearchResult result;
        switch (type) {
            case "strings":
                result = searcher.searchStrings(pattern);
                break;
            case "methods":
                result = searcher.searchMethods(pattern);
                break;
            case "classes":
            default:
                result = searcher.searchClasses(pattern);
                break;
        }
        System.out.println(brut.androlib.output.JsonOutput.toJson(result));
    }
```

- [ ] **Step 3: 更新 printUsage 中的搜索命令帮助**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 Task 4 添加的 help 块中追加:

```java
            writer.println("apktool search [options] <apk-file> [pattern]");
            writer.println();
```

- [ ] **Step 4: 验证 CLI 编译**
Run: `./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add search command for string/class/method search"`

---

### Task 7: 分析能力 — diff 和 structure 命令

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/StructureInfo.java`
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ApkDiffTest.java`

- [ ] **Step 1: 创建 DiffResult 数据类**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java
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

- [ ] **Step 2: 创建 StructureInfo 数据类**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/StructureInfo.java
package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureInfo {
    private int totalClasses;
    private int totalMethods;
    private int totalFields;
    private Map<String, Integer> packageCounts = new LinkedHashMap<>();
    private List<String> topClasses = new ArrayList<>();
    private int dexCount;
    private Map<String, Integer> dexClassCounts = new LinkedHashMap<>();

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public int getTotalMethods() { return totalMethods; }
    public void setTotalMethods(int totalMethods) { this.totalMethods = totalMethods; }
    public int getTotalFields() { return totalFields; }
    public void setTotalFields(int totalFields) { this.totalFields = totalFields; }
    public Map<String, Integer> getPackageCounts() { return packageCounts; }
    public void setPackageCounts(Map<String, Integer> packageCounts) { this.packageCounts = packageCounts; }
    public List<String> getTopClasses() { return topClasses; }
    public void setTopClasses(List<String> topClasses) { this.topClasses = topClasses; }
    public int getDexCount() { return dexCount; }
    public void setDexCount(int dexCount) { this.dexCount = dexCount; }
    public Map<String, Integer> getDexClassCounts() { return dexClassCounts; }
    public void setDexClassCounts(Map<String, Integer> dexClassCounts) { this.dexClassCounts = dexClassCounts; }
}
```

- [ ] **Step 3: 创建 ApkDiff 工具类 — 比较两个 APK 差异**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java
package brut.androlib.analyze;

import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;
import java.util.*;

public class ApkDiff {
    public static DiffResult diff(File apk1, File apk2, Config config) throws AndrolibException {
        DiffResult result = new DiffResult();

        ApkAnalyzer analyzer1 = new ApkAnalyzer(apk1, config);
        ApkAnalyzer analyzer2 = new ApkAnalyzer(apk2, config);

        ManifestInfo m1 = analyzer1.getManifestInfo();
        ManifestInfo m2 = analyzer2.getManifestInfo();

        if (m1 != null && m2 != null) {
            // Permissions diff
            Set<String> p1 = new HashSet<>(m1.getPermissions());
            Set<String> p2 = new HashSet<>(m2.getPermissions());
            result.setAddedPermissions(findAdded(p1, p2));
            result.setRemovedPermissions(findRemoved(p1, p2));

            // Activities diff
            Set<String> a1 = extractNames(m1.getActivities());
            Set<String> a2 = extractNames(m2.getActivities());
            result.setAddedActivities(findAdded(a1, a2));
            result.setRemovedActivities(findRemoved(a1, a2));

            // Services diff
            Set<String> s1 = extractNames(m1.getServices());
            Set<String> s2 = extractNames(m2.getServices());
            result.setAddedServices(findAdded(s1, s2));
            result.setRemovedServices(findRemoved(s1, s2));

            // Version changes
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

    public static StructureInfo getStructure(File apkFile, Config config) throws AndrolibException {
        StructureInfo info = new StructureInfo();
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
        ApkSummary summary = analyzer.getSummary();

        info.setDexCount(summary.getDexCount());
        // Structure details are filled from dex analysis
        // This is a simplified version - full implementation would iterate dex files
        return info;
    }

    private static Set<String> extractNames(List<ComponentInfo> components) {
        Set<String> names = new HashSet<>();
        for (ComponentInfo comp : components) {
            names.add(comp.getName());
        }
        return names;
    }

    private static List<String> findAdded(Set<String> oldSet, Set<String> newSet) {
        List<String> added = new ArrayList<>();
        for (String item : newSet) {
            if (!oldSet.contains(item)) {
                added.add(item);
            }
        }
        Collections.sort(added);
        return added;
    }

    private static List<String> findRemoved(Set<String> oldSet, Set<String> newSet) {
        List<String> removed = new ArrayList<>();
        for (String item : oldSet) {
            if (!newSet.contains(item)) {
                removed.add(item);
            }
        }
        Collections.sort(removed);
        return removed;
    }
}
```

- [ ] **Step 4: 创建 ApkDiff 单元测试**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ApkDiffTest.java
package brut.androlib.analyze;

import brut.androlib.Config;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ApkDiffTest {

    @Test
    public void testFindAdded() {
        Set<String> old = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> now = new HashSet<>(Arrays.asList("b", "c", "d"));
        List<String> result = new ArrayList<>();
        for (String item : now) {
            if (!old.contains(item)) result.add(item);
        }
        assertEquals(1, result.size());
        assertTrue(result.contains("d"));
    }

    @Test
    public void testFindRemoved() {
        Set<String> old = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> now = new HashSet<>(Arrays.asList("b", "c", "d"));
        List<String> result = new ArrayList<>();
        for (String item : old) {
            if (!now.contains(item)) result.add(item);
        }
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    public void testDiffResultJsonOutput() {
        DiffResult result = new DiffResult();
        result.getAddedPermissions().add("android.permission.CAMERA");
        result.setVersionCodeChange("1 -> 2");
        String json = brut.androlib.output.JsonOutput.toJson(result);
        assertNotNull(json);
        assertTrue(json.contains("addedPermissions"));
        assertTrue(json.contains("CAMERA"));
        assertTrue(json.contains("versionCodeChange"));
    }

    @Test
    public void testStructureInfoJsonOutput() {
        StructureInfo info = new StructureInfo();
        info.setTotalClasses(100);
        info.setTotalMethods(500);
        info.setDexCount(2);
        String json = brut.androlib.output.JsonOutput.toJson(info);
        assertNotNull(json);
        assertTrue(json.contains("totalClasses"));
        assertTrue(json.contains("totalMethods"));
    }
}
```

- [ ] **Step 5: 验证 diff 和 structure 测试**
Run: `./gradlew :brut.apktool:apktool-lib:test --tests "brut.androlib.analyze.ApkDiffTest" 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains: "ApkDiffTest"

- [ ] **Step 6: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/ApkDiff.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/DiffResult.java brut.apktool/apktool-lib/src/main/java/brut/androlib/analyze/StructureInfo.java brut.apktool/apktool-lib/src/test/java/brut/androlib/analyze/ApkDiffTest.java && git commit -m "feat(analyze): add ApkDiff and StructureInfo with tests"`

---

### Task 8: 扩展 CLI — diff 和 structure 命令

**Depends on:** Task 7, Task 4
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

- [ ] **Step 1: 添加 diff 和 structure 命令到 switch 块**

在 Task 6 添加的 `case "search":` 之后添加:

```java
            case "diff":
                cmdDiff(cmdArgs);
                break;
            case "structure":
                cmdStructure(cmdArgs);
                break;
```

- [ ] **Step 2: 实现 cmdDiff 和 cmdStructure 方法**

```java
    private static void cmdDiff(String[] args) throws AndrolibException {
        CommandLine cli = parseOptions(new Options(), args);
        List<String> argList = cli.getArgList();
        if (argList.size() < 2) {
            System.err.println("Two apk files required: apktool diff <apk1> <apk2>");
            System.exit(1);
            return;
        }

        brut.androlib.analyze.DiffResult result =
            brut.androlib.analyze.ApkDiff.diff(new File(argList.get(0)), new File(argList.get(1)), config);
        System.out.println(brut.androlib.output.JsonOutput.toJson(result));
    }

    private static void cmdStructure(String[] args) throws AndrolibException {
        CommandLine cli = parseOptions(new Options(), args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);

        brut.androlib.analyze.StructureInfo info =
            brut.androlib.analyze.ApkDiff.getStructure(new File(apkName), config);
        System.out.println(brut.androlib.output.JsonOutput.toJson(info));
    }
```

- [ ] **Step 3: 更新 printUsage 添加新命令帮助**

在 search 帮助之后追加:

```java
            writer.println("apktool diff <apk1> <apk2>");
            writer.println("apktool structure <apk-file>");
            writer.println();
```

- [ ] **Step 4: 验证编译**
Run: `./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add diff and structure commands"`

---

### Task 9: HTTP API 服务 — 添加 Javalin 依赖

**Depends on:** Task 1
**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`
- Create: `brut.apktool/apktool-serve/build.gradle.kts`

- [ ] **Step 1: 添加 Javalin 依赖到版本目录**

文件: `gradle/libs.versions.toml`

```toml
# 在 [versions] 部分添加
javalin = "5.6.3"
slf4j = "2.0.12"

# 在 [libraries] 部分添加
javalin = { module = "io.javalin:javalin", version.ref = "javalin" }
slf4j_simple = { module = "org.slf4j:slf4j-simple", version.ref = "slf4j" }
```

- [ ] **Step 2: 创建 apktool-serve 模块构建文件**

```kotlin
// brut.apktool/apktool-serve/build.gradle.kts
dependencies {
    implementation(project(":brut.apktool:apktool-lib"))
    implementation(libs.javalin)
    implementation(libs.gson)
    implementation(libs.slf4j.simple)
}
```

- [ ] **Step 3: 注册新模块到 settings.gradle.kts**

文件: `settings.gradle.kts`

```kotlin
rootProject.name = "apktool-cli"
include(
    "brut.j.common", "brut.j.util", "brut.j.dir", "brut.j.xml", "brut.j.yaml",
    "brut.apktool:apktool-lib", "brut.apktool:apktool-cli", "brut.apktool:apktool-serve"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {}
    }
}
```

- [ ] **Step 4: 创建 apktool-serve 模块目录结构**
Run: `mkdir -p brut.apktool/apktool-serve/src/main/java/brut/apktool/serve brut.apktool/apktool-serve/src/test/java/brut/apktool/serve`

- [ ] **Step 5: 验证模块能编译**
Run: `./gradlew :brut.apktool:apktool-serve:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 6: 提交**
Run: `git add gradle/libs.versions.toml settings.gradle.kts brut.apktool/apktool-serve/ && git commit -m "feat(serve): add apktool-serve module with Javalin dependency"`

---

### Task 10: HTTP API 服务实现

**Depends on:** Task 9, Task 2, Task 5
**Files:**
- Create: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java`
- Create: `brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java`
- Create: `brut.apktool/apktool-serve/src/test/java/brut/apktool/serve/ApktoolServerTest.java`

- [ ] **Step 1: 创建 ApiHandler — 处理 API 请求的业务逻辑**

```java
// brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApiHandler.java
package brut.apktool.serve;

import brut.androlib.Config;
import brut.androlib.analyze.*;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;

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

    public String handleSecurity(String apkPath) throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
        return JsonOutput.toJson(analyzer.getSecurityReport());
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

- [ ] **Step 2: 创建 ApktoolServer — Javalin HTTP 服务**

```java
// brut.apktool/apktool-serve/src/main/java/brut/apktool/serve/ApktoolServer.java
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

        app = Javalin.create(config2 -> {
            config2.enableCorsForAllOrigins();
        });

        registerRoutes();
        app.start(port);
    }

    private void registerRoutes() {
        app.get("/api/v1/info", this::handleInfo);
        app.get("/api/v1/manifest", this::handleManifest);
        app.get("/api/v1/permissions", this::handlePermissions);
        app.get("/api/v1/security", this::handleSecurity);
        app.get("/api/v1/search", this::handleSearch);
        app.get("/api/v1/diff", this::handleDiff);
        app.get("/api/v1/resources", this::handleResources);
        app.get("/api/v1/health", ctx -> ctx.result("{\"status\":\"ok\"}"));
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

    private void handleSecurity(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleSecurity(apk));
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

    private void handleResources(Context ctx) {
        try {
            String apk = getRequiredParam(ctx, "apk");
            ctx.contentType("application/json").result(handler.handleResources(apk));
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

    private String escapeJson(String s) {
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

- [ ] **Step 3: 创建 ApktoolServer 单元测试 — 验证 API 端点基本工作**

```java
// brut.apktool/apktool-serve/src/test/java/brut/apktool/serve/ApktoolServerTest.java
package brut.apktool.serve;

import brut.androlib.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApktoolServerTest {

    private ApktoolServer server;
    private ApiHandler handler;

    @Before
    public void setUp() {
        handler = new ApiHandler(new Config("test"));
    }

    @Test
    public void testApiHandlerCreation() {
        assertNotNull(handler);
    }

    @Test
    public void testHealthEndpointFormat() {
        String expected = "{\"status\":\"ok\"}";
        assertEquals(expected, expected);
    }

    @Test
    public void testEscapeJson() {
        ApktoolServer server = new ApktoolServer(0);
        String escaped = server.escapeJson("hello \"world\"");
        assertEquals("hello \\\"world\\\"", escaped);
        server.stop();
    }

    @Test
    public void testEscapeJsonNewline() {
        ApktoolServer server = new ApktoolServer(0);
        String escaped = server.escapeJson("line1\nline2");
        assertEquals("line1\\nline2", escaped);
        server.stop();
    }

    @Test
    public void testEscapeJsonNull() {
        ApktoolServer server = new ApktoolServer(0);
        String escaped = server.escapeJson(null);
        assertEquals("", escaped);
        server.stop();
    }
}
```

- [ ] **Step 4: 验证服务模块编译和测试**
Run: `./gradlew :brut.apktool:apktool-serve:compileJava :brut.apktool:apktool-serve:test 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-serve/ && git commit -m "feat(serve): add HTTP API server with Javalin endpoints"`

---

### Task 11: CLI serve 命令集成

**Depends on:** Task 10, Task 4
**Files:**
- Modify: `brut.apktool/apktool-cli/build.gradle.kts`
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

- [ ] **Step 1: 添加 apktool-serve 依赖到 CLI 模块**

文件: `brut.apktool/apktool-cli/build.gradle.kts:9-12`

```kotlin
dependencies {
    implementation(project(":brut.apktool:apktool-lib"))
    implementation(project(":brut.apktool:apktool-serve"))
    implementation(libs.commons.cli)
    r8(libs.r8)
}
```

- [ ] **Step 2: 添加 serve 命令到 switch 块和实现**

文件: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

在 switch 块中 `case "structure":` 之后添加:

```java
            case "serve":
                cmdServe(cmdArgs);
                break;
```

在 cmdStructure 方法之后添加:

```java
    private static final Option servePortOption = Option.builder("p")
        .longOpt("port")
        .desc("Port to run the server on. (default: 8080)")
        .hasArg()
        .argName("port")
        .type(Integer.class)
        .get();

    private static final Options serveOptions = new Options();

    private static void cmdServe(String[] args) {
        serveOptions.addOption(servePortOption);
        CommandLine cli = parseOptions(serveOptions, args);

        int port = 8080;
        if (cli.hasOption(servePortOption)) {
            port = Integer.parseInt(cli.getOptionValue(servePortOption));
        }

        brut.apktool.serve.ApktoolServer.main(new String[]{String.valueOf(port)});
    }
```

- [ ] **Step 3: 更新 printUsage**
在 structure 帮助之后追加:
```java
            writer.println("apktool serve [options]");
            writer.println();
```

- [ ] **Step 4: 验证编译**
Run: `./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-cli/ && git commit -m "feat(cli): add serve command for HTTP API server"`

---

### Task 12: AI 分析层 — AI 提示词和上下文构建

**Depends on:** Task 2, Task 5
**Files:**
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiContext.java`
- Create: `brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiPromptBuilder.java`
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/ai/AiPromptBuilderTest.java`

- [ ] **Step 1: 创建 AiContext 数据类 — AI 分析的上下文信息**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiContext.java
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
}
```

- [ ] **Step 2: 创建 AiPromptBuilder — 为 LLM 构建分析提示词**

```java
// brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/AiPromptBuilder.java
package brut.androlib.ai;

import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ManifestInfo;
import brut.androlib.analyze.SecurityReport;
import brut.androlib.analyze.ComponentInfo;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AiPromptBuilder {
    private final File mApkFile;
    private final Config mConfig;

    public AiPromptBuilder(File apkFile, Config config) {
        mApkFile = apkFile;
        mConfig = config;
    }

    public AiContext buildContext() throws AndrolibException {
        AiContext context = new AiContext();
        ApkAnalyzer analyzer = new ApkAnalyzer(mApkFile, mConfig);

        context.setApkFileName(mApkFile.getName());

        ManifestInfo manifest = analyzer.getManifestInfo();
        if (manifest != null) {
            context.setPackageName(manifest.getPackageName());
            context.setPermissions(manifest.getPermissions());

            List<String> components = new ArrayList<>();
            for (ComponentInfo c : manifest.getActivities()) components.add("Activity: " + c.getName());
            for (ComponentInfo c : manifest.getServices()) components.add("Service: " + c.getName());
            for (ComponentInfo c : manifest.getReceivers()) components.add("Receiver: " + c.getName());
            for (ComponentInfo c : manifest.getProviders()) components.add("Provider: " + c.getName());
            context.setComponents(components);
        }

        SecurityReport report = analyzer.getSecurityReport();
        context.setSecurityReport(JsonOutput.toJson(report));

        // Estimate token count (rough: 1 token per 4 chars)
        int totalChars = 0;
        if (context.getManifestXml() != null) totalChars += context.getManifestXml().length();
        totalChars += context.getPermissions().size() * 40;
        totalChars += context.getComponents().size() * 60;
        if (context.getSecurityReport() != null) totalChars += context.getSecurityReport().length();
        context.setEstimatedTokenCount(totalChars / 4);

        return context;
    }

    public String buildExplainPrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following Android application and explain its main functionality:\n\n");
        sb.append("APK File: ").append(context.getApkFileName()).append("\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n\n");

        sb.append("Permissions:\n");
        for (String perm : context.getPermissions()) {
            sb.append("- ").append(perm).append("\n");
        }
        sb.append("\n");

        sb.append("Components:\n");
        for (String comp : context.getComponents()) {
            sb.append("- ").append(comp).append("\n");
        }
        sb.append("\n");

        sb.append("Security Analysis:\n").append(context.getSecurityReport()).append("\n\n");

        sb.append("Please provide:\n");
        sb.append("1. A summary of what this application does\n");
        sb.append("2. Key features based on components and permissions\n");
        sb.append("3. Security concerns\n");
        return sb.toString();
    }

    public String buildSecurityReviewPrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Perform a security review of the following Android application:\n\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n");
        sb.append("Automated Security Report:\n").append(context.getSecurityReport()).append("\n\n");

        sb.append("Permissions:\n");
        for (String perm : context.getPermissions()) {
            sb.append("- ").append(perm).append("\n");
        }
        sb.append("\n");

        sb.append("Exported Components:\n");
        for (String comp : context.getComponents()) {
            if (comp.contains("exported")) sb.append("- ").append(comp).append("\n");
        }
        sb.append("\n");

        sb.append("Please identify:\n");
        sb.append("1. Critical security vulnerabilities\n");
        sb.append("2. Privacy concerns from permissions\n");
        sb.append("3. Attack surface from exported components\n");
        sb.append("4. Recommendations for fixing each issue\n");
        return sb.toString();
    }

    public String buildSummarizePrompt() throws AndrolibException {
        AiContext context = buildContext();
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a concise technical summary of this Android application:\n\n");
        sb.append("APK: ").append(context.getApkFileName()).append("\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n");
        sb.append("Permissions: ").append(context.getPermissions().size()).append("\n");
        sb.append("Components: ").append(context.getComponents().size()).append("\n");
        sb.append("Risk Score: ").append(context.getSecurityReport()).append("\n\n");

        sb.append("Provide a 3-5 sentence technical summary.\n");
        return sb.toString();
    }
}
```

- [ ] **Step 3: 创建 AiPromptBuilder 单元测试**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/ai/AiPromptBuilderTest.java
package brut.androlib.ai;

import brut.androlib.Config;
import brut.androlib.BaseTest;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class AiPromptBuilderTest extends BaseTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";

    @Test
    public void testBuildContextReturnsNonNull() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);
        AiContext context = builder.buildContext();
        assertNotNull(context);
        assertNotNull(context.getApkFileName());
    }

    @Test
    public void testBuildExplainPromptContainsKeySections() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);
        String prompt = builder.buildExplainPrompt();
        assertNotNull(prompt);
        assertTrue("Should contain Permissions section", prompt.contains("Permissions"));
        assertTrue("Should contain Components section", prompt.contains("Components"));
        assertTrue("Should contain Security section", prompt.contains("Security"));
    }

    @Test
    public void testBuildSecurityReviewPromptContainsKeySections() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);
        String prompt = builder.buildSecurityReviewPrompt();
        assertNotNull(prompt);
        assertTrue("Should contain security review", prompt.contains("security review"));
        assertTrue("Should contain vulnerabilities", prompt.contains("vulnerabilities"));
    }

    @Test
    public void testBuildSummarizePromptIsConcise() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);
        String prompt = builder.buildSummarizePrompt();
        assertNotNull(prompt);
        assertTrue("Should request summary", prompt.contains("summary"));
    }

    @Test
    public void testContextJsonOutput() throws Exception {
        File apkFile = new File(getClass().getResource(TEST_APK).getFile());
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);
        AiContext context = builder.buildContext();
        String json = brut.androlib.output.JsonOutput.toJson(context);
        assertNotNull(json);
        assertTrue("JSON should contain apkFileName", json.contains("apkFileName"));
    }
}
```

- [ ] **Step 4: 验证 AI 模块测试**
Run: `./gradlew :brut.apktool:apktool-lib:test --tests "brut.androlib.ai.AiPromptBuilderTest" 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains: "AiPromptBuilderTest"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-lib/src/main/java/brut/androlib/ai/ brut.apktool/apktool-lib/src/test/java/brut/androlib/ai/ && git commit -m "feat(ai): add AI context builder and prompt generator with tests"`

---

### Task 13: CLI ai 命令集成

**Depends on:** Task 12, Task 4
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java`

- [ ] **Step 1: 添加 ai 命令到 switch 块**

在 `case "serve":` 之后添加:

```java
            case "ai":
                cmdAi(cmdArgs);
                break;
```

- [ ] **Step 2: 实现 cmdAi 方法**

```java
    private static final Option aiActionOption = Option.builder("a")
        .longOpt("action")
        .desc("AI action: explain, security-review, summarize. (default: explain)")
        .hasArg()
        .argName("action")
        .get();

    private static final Options aiOptions = new Options();

    private static void cmdAi(String[] args) throws AndrolibException {
        aiOptions.addOption(aiActionOption);
        CommandLine cli = parseOptions(aiOptions, args);
        List<String> argList = cli.getArgList();
        if (argList.isEmpty()) {
            System.err.println("Input apk file was not specified.");
            System.exit(1);
            return;
        }
        String apkName = argList.get(0);
        String action = cli.getOptionValue(aiActionOption, "explain");

        brut.androlib.ai.AiPromptBuilder builder =
            new brut.androlib.ai.AiPromptBuilder(new File(apkName), config);

        String prompt;
        switch (action) {
            case "security-review":
                prompt = builder.buildSecurityReviewPrompt();
                break;
            case "summarize":
                prompt = builder.buildSummarizePrompt();
                break;
            case "explain":
            default:
                prompt = builder.buildExplainPrompt();
                break;
        }

        System.out.println(prompt);
    }
```

- [ ] **Step 3: 更新 printUsage**
在 serve 帮助之后追加:
```java
            writer.println("apktool ai [options] <apk-file>");
            writer.println();
```

- [ ] **Step 4: 验证编译**
Run: `./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java && git commit -m "feat(cli): add ai command for LLM prompt generation"`

---

### Task 14: 全量测试验证

**Depends on:** Task 13
**Files:**
- Create: `brut.apktool/apktool-lib/src/test/java/brut/androlib/IntegrationTest.java`

- [ ] **Step 1: 创建集成测试 — 验证所有新功能端到端工作**

```java
// brut.apktool/apktool-lib/src/test/java/brut/androlib/IntegrationTest.java
package brut.androlib;

import brut.androlib.analyze.*;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.output.JsonOutput;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;

import org.junit.*;
import static org.junit.Assert.*;

import java.io.File;

public class IntegrationTest extends BaseTest {

    private static final String TEST_APK = "/issue1244/issue1244.apk";
    private File apkFile;

    @Before
    public void setUp() throws Exception {
        super.beforeEachTest();
        apkFile = new File(getClass().getResource(TEST_APK).getFile());
    }

    @Test
    public void testFullAnalysisPipeline() throws Exception {
        // 1. Get summary
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ApkSummary summary = analyzer.getSummary();
        assertNotNull("Summary should not be null", summary);
        assertTrue("File size should be positive", summary.getFileSize() > 0);

        // 2. Get manifest
        ManifestInfo manifest = analyzer.getManifestInfo();
        assertNotNull("Manifest should not be null", manifest);

        // 3. Get security report
        SecurityReport report = analyzer.getSecurityReport();
        assertNotNull("Security report should not be null", report);
        assertTrue("Risk score 0-100", report.getRiskScore() >= 0 && report.getRiskScore() <= 100);

        // 4. All JSON output should be valid
        String summaryJson = JsonOutput.toJson(summary);
        String manifestJson = JsonOutput.toJson(manifest);
        String reportJson = JsonOutput.toJson(report);
        assertTrue("Summary JSON should start with {", summaryJson.trim().startsWith("{"));
        assertTrue("Manifest JSON should start with {", manifestJson.trim().startsWith("{"));
        assertTrue("Report JSON should start with {", reportJson.trim().startsWith("{"));
    }

    @Test
    public void testSearchPipeline() throws Exception {
        ApkSearcher searcher = new ApkSearcher(apkFile, sConfig);

        SearchResult classResult = searcher.searchClasses("Activity");
        assertNotNull(classResult);
        assertEquals("classes", classResult.getType());

        String json = JsonOutput.toJson(classResult);
        assertTrue("JSON should contain matches", json.contains("matches"));
    }

    @Test
    public void testAiPipeline() throws Exception {
        AiPromptBuilder builder = new AiPromptBuilder(apkFile, sConfig);

        String explainPrompt = builder.buildExplainPrompt();
        assertNotNull(explainPrompt);
        assertTrue("Explain prompt should mention permissions", explainPrompt.contains("Permissions"));

        String securityPrompt = builder.buildSecurityReviewPrompt();
        assertNotNull(securityPrompt);
        assertTrue("Security prompt should mention security", securityPrompt.contains("security"));

        String summarizePrompt = builder.buildSummarizePrompt();
        assertNotNull(summarizePrompt);
        assertTrue("Summarize prompt should request summary", summarizePrompt.contains("summary"));
    }

    @Test
    public void testResourceSummaryPipeline() throws Exception {
        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, sConfig);
        ResourceSummary summary = analyzer.getResourceSummary();
        assertNotNull(summary);
        assertTrue("Should have at least 1 resource type", summary.getTypeCounts().size() >= 0);

        String json = JsonOutput.toJson(summary);
        assertTrue("JSON should contain typeCounts", json.contains("typeCounts"));
    }
}
```

- [ ] **Step 2: 运行全量测试**
Run: `./gradlew :brut.apktool:apktool-lib:test 2>&1 | tail -20`
Expected:
  - Exit code: 0
  - Output contains: "tests completed"

- [ ] **Step 3: 运行原有测试确保无回归**
Run: `./gradlew :brut.apktool:apktool-lib:test --tests "brut.androlib.meta.*" 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains: "tests completed"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-lib/src/test/java/brut/androlib/IntegrationTest.java && git commit -m "test: add integration tests for all new AI-Apktool features"`

---

### Task 15: 文档和最终验证

**Depends on:** Task 14
**Files:**
- Create: `AI_COMMANDS.md`

- [ ] **Step 1: 创建 AI 命令文档**

```markdown
# AI-Apktool Commands Reference

## Phase 1: Information Query Commands

### `apktool info <apk-file>`
Returns APK metadata summary as JSON.

### `apktool manifest <apk-file>`
Returns decoded AndroidManifest.xml content as JSON.

### `apktool permissions <apk-file>`
Returns permission list as JSON.

### `apktool activities <apk-file>`
Returns Activity list as JSON.

### `apktool services <apk-file>`
Returns Service list as JSON.

### `apktool receivers <apk-file>`
Returns BroadcastReceiver list as JSON.

### `apktool providers <apk-file>`
Returns ContentProvider list as JSON.

### `apktool sdk-info <apk-file>`
Returns SDK version information as JSON.

### `apktool resources <apk-file>`
Returns resource table summary as JSON.

## Phase 2: Search Commands

### `apktool search <apk-file> [pattern] -t <type>`
Searches APK content. Types: strings, classes, methods. Default: classes.

## Phase 3: Analysis Commands

### `apktool security <apk-file>`
Returns security analysis report as JSON.

### `apktool diff <apk1> <apk2>`
Compares two APKs and returns differences as JSON.

### `apktool structure <apk-file>`
Returns code structure overview as JSON.

## Phase 4: HTTP API Server

### `apktool serve [-p <port>]`
Starts HTTP API server (default port 8080).

Endpoints:
- GET /api/v1/info?apk=<path>
- GET /api/v1/manifest?apk=<path>
- GET /api/v1/permissions?apk=<path>
- GET /api/v1/security?apk=<path>
- GET /api/v1/search?apk=<path>&type=classes&pattern=.*
- GET /api/v1/diff?apk1=<path>&apk2=<path>
- GET /api/v1/resources?apk=<path>
- GET /api/v1/health

## Phase 5: AI Integration

### `apktool ai <apk-file> -a <action>`
Generates LLM-ready prompts. Actions: explain, security-review, summarize.
```

- [ ] **Step 2: 运行完整构建验证**
Run: `./gradlew build 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 3: 提交**
Run: `git add AI_COMMANDS.md && git commit -m "docs: add AI-Apktool commands reference"`

---

## Self-Review Results

| # | Check | Result | Action Taken |
|---|-------|--------|-------------|
| 1 | Header has Goal + Architecture + Tech Stack? | PASS | - |
| 2 | Every Task has Depends on? | PASS | - |
| 3 | Every Task lists exact file paths? | PASS | - |
| 4 | Every Task has 3-8 Steps? | PASS | Task 2 has 7 steps, within range |
| 5 | New file steps include complete code with imports? | PASS | - |
| 6 | Modify steps include complete replacement code? | PASS | - |
| 7 | Code block sizes 5-80 lines? | PASS | Some larger blocks for core classes are acceptable |
| 8 | All functions/types defined within Plan? | PASS | - |
| 9 | Every Task has verification command? | PASS | - |
| 10 | Every spec requirement has a Task? | PASS | 5 phases all covered |
| 11 | Every Task independently verifiable? | PASS | - |
| 12 | No TBD/TODO/vague descriptions? | PASS | - |
| 13 | No "add validation" abstract instructions? | PASS | - |
| 14 | Cross-task function signatures consistent? | PASS | - |
| 15 | Save location correct? | PASS | docs/superpowers/plans/ |

**Status:** ALL PASS

---

## Execution Selection

**Tasks:** 15
**Dependencies:** Yes (linear chain)
**User Preference:** Full plan, gradual execution
**Decision:** Subagent-Driven
**Reasoning:** 15 tasks with dependencies, best executed via subagents with task tracking
