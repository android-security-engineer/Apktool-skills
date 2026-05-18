# JsonOutput 与 CommandRegistry API 参考

> 包路径: `brut.androlib.output`

该模块提供 JSON 序列化和命令注册表功能，是 AI-Apktool 所有分析结果输出的基础设施。

---

## JsonOutput

> 包路径: `brut.androlib.output.JsonOutput`

JSON 序列化工具类，所有方法均为静态方法。基于 Gson 实现，配置为美化输出（Pretty Printing）并禁用 HTML 转义。

### Gson 配置

```java
private static final Gson GSON = new GsonBuilder()
    .disableHtmlEscaping()    // 禁用 HTML 转义，保留原始字符
    .setPrettyPrinting()      // 美化输出，带缩进的 JSON
    .create();
```

---

### toJson

```java
public static String toJson(Object obj)
```

将任意 Java 对象序列化为美化格式的 JSON 字符串。

| 参数 | 类型 | 说明 |
|------|------|------|
| `obj` | `Object` | 要序列化的 Java 对象 |

**返回:** 格式化的 JSON 字符串（带缩进）。

**特性:**
- 禁用 HTML 转义: `&`、`<`、`>` 等字符不会被转义为 `<` 等 Unicode 转义序列
- 美化输出: JSON 包含缩进和换行，便于阅读
- `null` 值字段默认不输出（Gson 默认行为）

---

### write

```java
public static void write(Object obj, OutputStream out)
```

将 Java 对象序列化为 JSON 并写入输出流。使用 UTF-8 编码。

| 参数 | 类型 | 说明 |
|------|------|------|
| `obj` | `Object` | 要序列化的 Java 对象 |
| `out` | `OutputStream` | 输出流 |

**特性:**
- 使用 `OutputStreamWriter` 以 UTF-8 编码写入
- 写入完成后自动调用 `flush()`

---

### 代码示例

#### 序列化简单对象

```java
import brut.androlib.output.JsonOutput;

Map<String, Object> data = new LinkedHashMap<>();
data.put("name", "test-app");
data.put("version", 2);
data.put("permissions", Arrays.asList("INTERNET", "CAMERA"));

String json = JsonOutput.toJson(data);
System.out.println(json);
```

输出:

```json
{
  "name": "test-app",
  "version": 2,
  "permissions": [
    "INTERNET",
    "CAMERA"
  ]
}
```

#### 写入输出流

```java
import brut.androlib.output.JsonOutput;
import java.io.FileOutputStream;

Map<String, String> result = new LinkedHashMap<>();
result.put("status", "success");
result.put("message", "Analysis complete");

try (FileOutputStream fos = new FileOutputStream("result.json")) {
    JsonOutput.write(result, fos);
}
```

#### 序列化 SearchResult

```java
import brut.androlib.search.SearchResult;
import brut.androlib.output.JsonOutput;

SearchResult result = searcher.searchClasses("Activity");
String json = JsonOutput.toJson(result);
// json 为美化格式的 JSON 字符串
```

#### 序列化 DiffResult

```java
import brut.androlib.analyze.DiffResult;
import brut.androlib.output.JsonOutput;

DiffResult diff = ApkDiff.diff(apk1, apk2, config);
System.out.println(JsonOutput.toJson(diff));
```

---

## CommandRegistry

> 包路径: `brut.androlib.output.CommandRegistry`

命令注册表，管理所有已注册的 CLI 命令信息。提供按名称、短名称和分类查询命令的能力。所有方法均为静态方法，命令在类加载时通过 `static` 初始化块注册。

### 命令分类

| 分类 | 说明 |
|------|------|
| `core` | 核心命令（decode、build、framework 管理等） |
| `analysis` | 分析命令（info、manifest、permissions、security 等） |
| `search` | 搜索命令（search） |
| `service` | 服务命令（serve） |
| `ai` | AI 命令（ai） |
| `general` | 通用命令（help、version） |

---

### getAllCommands

```java
public static List<CommandInfo> getAllCommands()
```

获取所有已注册的命令列表。返回的是新创建的 ArrayList 副本，对返回列表的修改不影响内部注册表。

**返回:** `List<CommandInfo>`，包含所有已注册的命令。

---

### getCommand

```java
public static CommandInfo getCommand(String name)
```

根据命令名称获取命令信息。

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 命令名称（如 `"info"`、`"decode"`、`"search"`） |

**返回:** `CommandInfo` 对象，如果名称不存在则返回 `null`。

---

### getCommandsByCategory

```java
public static List<CommandInfo> getCommandsByCategory(String category)
```

根据分类获取命令列表。

| 参数 | 类型 | 说明 |
|------|------|------|
| `category` | `String` | 分类名称（`core`、`analysis`、`search`、`service`、`ai`、`general`） |

**返回:** `List<CommandInfo>`，该分类下的所有命令。

---

### toJsonCatalog

```java
public static String toJsonCatalog()
```

生成完整的命令目录 JSON 字符串。包含工具名称、版本号、描述和所有命令信息。

**返回:** 美化格式的 JSON 字符串。

---

## CommandInfo 数据模型

> 包路径: `brut.androlib.output.CommandInfo`

```java
public class CommandInfo {
    private String name;                  // 命令名称
    private String shortName;             // 短名称（可为 null）
    private String description;           // 命令描述
    private String usage;                 // 使用方法
    private String outputFormat;          // 输出格式描述
    private List<String> examples;        // 使用示例列表
    private List<ParamInfo> params;       // 参数列表
    private String category;              // 命令分类
}
```

**字段说明:**

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 命令名称（如 `"decode"`、`"info"`、`"search"`） |
| `shortName` | `String` | 命令缩写（如 `"d"`、`"b"`），可为 `null` |
| `description` | `String` | 命令的功能描述 |
| `usage` | `String` | 使用格式说明（如 `"apktool d [options] <apk-file>"`） |
| `outputFormat` | `String` | 输出格式详细描述（如 `"JSON: {fileName, ...}"`） |
| `examples` | `List<String>` | 命令使用示例列表 |
| `params` | `List<ParamInfo>` | 命令参数详情列表 |
| `category` | `String` | 所属分类 |

### ParamInfo 内部类

```java
public static class ParamInfo {
    private String name;           // 参数名称
    private String shortName;      // 参数短名称
    private String description;    // 参数描述
    private boolean required;      // 是否必填
    private String defaultValue;   // 默认值
}
```

---

## OutputFormat 枚举

> 包路径: `brut.androlib.output.OutputFormat`

```java
public enum OutputFormat {
    JSON,    // JSON 格式输出
    YAML,    // YAML 格式输出
    TEXT     // 纯文本格式输出
}
```

定义支持的输出格式类型。

---

## 代码示例

### 获取所有命令

```java
import brut.androlib.output.CommandRegistry;
import brut.androlib.output.CommandInfo;
import brut.androlib.output.JsonOutput;

List<CommandInfo> commands = CommandRegistry.getAllCommands();
for (CommandInfo cmd : commands) {
    System.out.println(cmd.getName() + " - " + cmd.getDescription());
}

// 输出 JSON 格式
System.out.println(JsonOutput.toJson(commands));
```

### 按名称查询命令

```java
CommandInfo info = CommandRegistry.getCommand("info");
if (info != null) {
    System.out.println("命令: " + info.getName());
    System.out.println("描述: " + info.getDescription());
    System.out.println("用法: " + info.getUsage());
    System.out.println("输出格式: " + info.getOutputFormat());
    System.out.println("示例:");
    for (String example : info.getExamples()) {
        System.out.println("  " + example);
    }
}
```

### 按分类过滤命令

```java
// 获取所有分析类命令
List<CommandInfo> analysis = CommandRegistry.getCommandsByCategory("analysis");
System.out.println("分析命令数量: " + analysis.size());
for (CommandInfo cmd : analysis) {
    System.out.println("  " + cmd.getName() + ": " + cmd.getDescription());
}

// 获取所有核心命令
List<CommandInfo> core = CommandRegistry.getCommandsByCategory("core");
for (CommandInfo cmd : core) {
    System.out.println("  " + cmd.getName() + " (" + cmd.getShortName() + ")");
}
```

### 生成命令目录 JSON

```java
String catalog = CommandRegistry.toJsonCatalog();
System.out.println(catalog);
```

---

## 完整命令目录 JSON 结构

`CommandRegistry.toJsonCatalog()` 输出的完整 JSON 结构如下:

```json
{
  "tool": "AI-Apktool",
  "version": "3.0.3-SNAPSHOT",
  "description": "AI-native Android reverse engineering platform",
  "commands": [
    {
      "name": "decode",
      "shortName": "d",
      "description": "Decode an APK file to a directory of smali and resources",
      "usage": "apktool d [options] <apk-file>",
      "outputFormat": "Directory structure with smali/, res/, AndroidManifest.xml, apktool.yml",
      "examples": [
        "apktool d app.apk",
        "apktool d app.apk -o output_dir",
        "apktool d app.apk -s (no source decode)"
      ],
      "params": [],
      "category": "core"
    },
    {
      "name": "build",
      "shortName": "b",
      "description": "Build an APK from a decoded directory",
      "usage": "apktool b [options] <apk-dir>",
      "outputFormat": "Built APK file in dist/ directory",
      "examples": [
        "apktool b app_dir",
        "apktool b app_dir -o custom.apk"
      ],
      "params": [],
      "category": "core"
    },
    {
      "name": "info",
      "shortName": null,
      "description": "Get APK metadata summary: package name, version, file size, component counts",
      "usage": "apktool info <apk-file>",
      "outputFormat": "JSON: {fileName, fileSize, packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, dexCount, hasResources, hasAssets, hasNativeLibs, architectures, permissionCount, activityCount, serviceCount, receiverCount, providerCount}",
      "examples": [
        "apktool info app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "manifest",
      "shortName": null,
      "description": "Get decoded AndroidManifest.xml as structured JSON: all components, permissions, SDK info, flags",
      "usage": "apktool manifest <apk-file>",
      "outputFormat": "JSON: {packageName, versionName, versionCode, minSdkVersion, targetSdkVersion, permissions[], activities[{name,exported,intentFilters[],permissions[]}], services[], receivers[], providers[], debuggable, allowBackup}",
      "examples": [
        "apktool manifest app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "permissions",
      "shortName": null,
      "description": "Get list of all permissions declared in AndroidManifest.xml",
      "usage": "apktool permissions <apk-file>",
      "outputFormat": "JSON array of permission strings",
      "examples": [
        "apktool permissions app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "activities",
      "shortName": null,
      "description": "Get list of all Activity components with export status and intent filters",
      "usage": "apktool activities <apk-file>",
      "outputFormat": "JSON array: [{name, exported, intentFilters[], permissions[]}]",
      "examples": [
        "apktool activities app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "services",
      "shortName": null,
      "description": "Get list of all Service components with export status",
      "usage": "apktool services <apk-file>",
      "outputFormat": "JSON array: [{name, exported, intentFilters[], permissions[]}]",
      "examples": [
        "apktool services app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "receivers",
      "shortName": null,
      "description": "Get list of all BroadcastReceiver components with export status",
      "usage": "apktool receivers <apk-file>",
      "outputFormat": "JSON array: [{name, exported, intentFilters[], permissions[]}]",
      "examples": [
        "apktool receivers app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "providers",
      "shortName": null,
      "description": "Get list of all ContentProvider components with export status",
      "usage": "apktool providers <apk-file>",
      "outputFormat": "JSON array: [{name, exported, intentFilters[], permissions[]}]",
      "examples": [
        "apktool providers app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "components",
      "shortName": null,
      "description": "Get all Android components (activities, services, receivers, providers) in one command",
      "usage": "apktool components <apk-file>",
      "outputFormat": "JSON: {activities[], services[], receivers[], providers[]}",
      "examples": [
        "apktool components app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "sdk-info",
      "shortName": null,
      "description": "Get SDK version requirements (min/target/max SDK)",
      "usage": "apktool sdk-info <apk-file>",
      "outputFormat": "JSON: {minSdkVersion, targetSdkVersion, maxSdkVersion}",
      "examples": [
        "apktool sdk-info app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "resources",
      "shortName": null,
      "description": "Get resource table summary: type counts, locales, package info",
      "usage": "apktool resources <apk-file>",
      "outputFormat": "JSON: {packageName, packageId, typeCounts{typeName->count}, locales[], totalEntries}",
      "examples": [
        "apktool resources app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "security",
      "shortName": null,
      "description": "Get security analysis report: dangerous permissions, exported components risk, risk score",
      "usage": "apktool security <apk-file>",
      "outputFormat": "JSON: {dangerousPermissions[], highRiskComponents[], debuggable, allowBackup, usesCleartextTraffic, findings[], riskScore(0-100)}",
      "examples": [
        "apktool security app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "api-surface",
      "shortName": null,
      "description": "Get all exported components and their intent filters - the app's public API surface",
      "usage": "apktool api-surface <apk-file>",
      "outputFormat": "JSON: {exportedActivities[], exportedServices[], exportedReceivers[], exportedProviders[], intentFilters[{component,actions[],categories[],dataSchemes[]}], totalExportedComponents}",
      "examples": [
        "apktool api-surface app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "strings",
      "shortName": null,
      "description": "Extract all strings from DEX files and resources with optional pattern filtering",
      "usage": "apktool strings <apk-file> [pattern]",
      "outputFormat": "JSON: {query, type, totalMatches, matches[{name, value, source}]}",
      "examples": [
        "apktool strings app.apk",
        "apktool strings app.apk 'http.*'"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "signing",
      "shortName": null,
      "description": "Get APK signing certificate information: signer, certificates, digests",
      "usage": "apktool signing <apk-file>",
      "outputFormat": "JSON: {v1Signing, v2Signing, v3Signing, certificates[{subject, issuer, serial, notBefore, notAfter, fingerprints{sha256,sha1,md5}}]}",
      "examples": [
        "apktool signing app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "search",
      "shortName": null,
      "description": "Search APK content: strings, classes, or methods by regex pattern",
      "usage": "apktool search <apk-file> [pattern] -t <type>",
      "outputFormat": "JSON: {query, type, totalMatches, matches[{name, value, source}]}",
      "examples": [
        "apktool search app.apk 'Activity' -t classes",
        "apktool search app.apk 'http.*' -t strings",
        "apktool search app.apk 'onCreate' -t methods"
      ],
      "params": [],
      "category": "search"
    },
    {
      "name": "diff",
      "shortName": null,
      "description": "Compare two APKs: find added/removed permissions, components, version changes",
      "usage": "apktool diff <apk1> <apk2>",
      "outputFormat": "JSON: {addedPermissions[], removedPermissions[], addedActivities[], removedActivities[], addedServices[], removedServices[], versionCodeChange, versionNameChange, targetSdkChange}",
      "examples": [
        "apktool diff app_v1.apk app_v2.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "structure",
      "shortName": null,
      "description": "Get code structure overview: class/method/field counts, package distribution",
      "usage": "apktool structure <apk-file>",
      "outputFormat": "JSON: {totalClasses, totalMethods, totalFields, packageCounts{}, topClasses[], dexCount, dexClassCounts{}}",
      "examples": [
        "apktool structure app.apk"
      ],
      "params": [],
      "category": "analysis"
    },
    {
      "name": "serve",
      "shortName": null,
      "description": "Start HTTP API server for AI agent integration",
      "usage": "apktool serve [-p <port>]",
      "outputFormat": "HTTP server on specified port (default 8080)",
      "examples": [
        "apktool serve",
        "apktool serve -p 9090"
      ],
      "params": [],
      "category": "service"
    },
    {
      "name": "ai",
      "shortName": null,
      "description": "Generate LLM-ready analysis prompts for AI-powered APK review",
      "usage": "apktool ai <apk-file> -a <action>",
      "outputFormat": "Text prompt for LLM (explain/security-review/summarize)",
      "examples": [
        "apktool ai app.apk",
        "apktool ai app.apk -a security-review",
        "apktool ai app.apk -a summarize"
      ],
      "params": [],
      "category": "ai"
    },
    {
      "name": "help",
      "shortName": "h",
      "description": "Show help information. Use --format json for AI-consumable output",
      "usage": "apktool help [--format json]",
      "outputFormat": "Text help or JSON command catalog",
      "examples": [
        "apktool help",
        "apktool help --format json"
      ],
      "params": [],
      "category": "general"
    },
    {
      "name": "version",
      "shortName": "v",
      "description": "Show version information",
      "usage": "apktool version",
      "outputFormat": "Version string",
      "examples": [
        "apktool version"
      ],
      "params": [],
      "category": "general"
    }
  ]
}
```

---

## JSON 输出格式说明

### 序列化特性

| 特性 | 行为 |
|------|------|
| HTML 转义 | 禁用（`disableHtmlEscaping`），原始字符不会被转义 |
| 输出格式 | 美化输出（`setPrettyPrinting`），带 2 空格缩进 |
| null 值处理 | 默认不输出值为 `null` 的字段 |
| 编码 | UTF-8（`write` 方法使用 `StandardCharsets.UTF_8`） |

### 示例: null 值字段的行为

```java
// 对象含 null 字段
public class Sample {
    String name = "test";
    String value = null;  // 不会被序列化
    int count = 0;        // 会序列化为 0
}

// 输出:
// {
//   "name": "test",
//   "count": 0
// }
```
