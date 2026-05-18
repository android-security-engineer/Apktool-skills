# API 优化建议

本文档基于对 AI-Apktool 当前 API 的分析，提出改进建议。每条建议包含当前状态分析、提议方案、预期收益和实现复杂度评估。

---

## 目录

- [当前能力评估](#当前能力评估)
- [1. API 设计改进](#1-api-设计改进)
- [2. 缺失的分析功能](#2-缺失的分析功能)
- [3. 搜索改进](#3-搜索改进)
- [4. Diff 改进](#4-diff-改进)
- [5. AI 集成改进](#5-ai-集成改进)
- [6. HTTP Server 改进](#6-http-server-改进)
- [7. 输出格式改进](#7-输出格式改进)
- [8. 性能优化](#8-性能优化)
- [9. 测试与质量](#9-测试与质量)

---

## 当前能力评估

| 模块 | 能力等级 | 说明 |
|------|---------|------|
| 核心 decode/build | 完备成熟 | 完整的 APK 解码和构建功能，framework 管理 |
| APK 分析 | 良好 | 覆盖 Manifest、组件、安全、资源、API 攻击面 |
| 搜索 | 基础 | 支持三种搜索类型：strings、classes、methods |
| Diff | 基础 | Manifest 层面的权限和组件对比 |
| AI 集成 | 基础 | 仅生成文本 prompt，不包含结构化输出 |
| HTTP API | 基础 | GET 端点，缺少部分 CLI 命令的对应端点 |

---

## 1. API 设计改进

### 1.1 为 Config 添加 Builder 模式

**当前状态**: `Config` 类通过构造函数传入 version 字符串，然后逐个调用 setter 方法配置选项。创建复杂的 Config 实例需要多行代码。

```java
Config config = new Config("2.9.3");
config.setVerbose(true);
config.setFrameworkDirectory("/path/to/fw");
config.setDecodeSources(Config.DecodeSources.FULL);
config.setBaksmaliDebugMode(false);
```

**提议方案**: 添加 fluent builder 模式。

```java
Config config = Config.builder("2.9.3")
    .verbose(true)
    .frameworkDirectory("/path/to/fw")
    .decodeSources(Config.DecodeSources.FULL)
    .baksmaliDebugMode(false)
    .build();
```

**预期收益**:
- 代码更简洁，配置意图更清晰
- 链式调用减少样板代码
- 便于在测试和工具中快速创建配置

**实现复杂度**: 低

---

### 1.2 为 ApkAnalyzer 添加接口抽象

**当前状态**: `ApkAnalyzer` 是一个具体类，没有对应的接口。在编写单元测试时难以 mock。

**提议方案**: 提取 `ApkAnalyzerInterface` 接口，`ApkAnalyzer` 作为默认实现。

```java
public interface ApkAnalyzerInterface {
    ApkSummary getSummary() throws AndrolibException;
    ManifestInfo getManifestInfo() throws AndrolibException;
    SecurityReport getSecurityReport() throws AndrolibException;
    ResourceSummary getResourceSummary() throws AndrolibException;
    ApiSurfaceInfo getApiSurface() throws AndrolibException;
    Map<String, List<ComponentInfo>> getAllComponents() throws AndrolibException;
}
```

**预期收益**:
- 支持在测试中 mock `ApkAnalyzer`
- 允许创建缓存装饰器、日志装饰器等
- 降低模块间耦合度

**实现复杂度**: 低

---

### 1.3 ApkAnalyzer 实现 AutoCloseable

**当前状态**: `ApkAnalyzer` 在构造时打开 `ExtFile`（底层打开 ZIP 文件），但不是 `AutoCloseable`。在 `getSummary()` 方法的 finally 块中手动关闭文件，但其他方法（如 `getManifestInfo()`、`getResourceSummary()`）没有关闭文件。

**提议方案**: 让 `ApkAnalyzer` 实现 `AutoCloseable`。

```java
public class ApkAnalyzer implements AutoCloseable {
    @Override
    public void close() {
        try { mApkFile.close(); } catch (Exception ignored) {}
    }
}

// 使用方式
try (ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config)) {
    ApkSummary summary = analyzer.getSummary();
    ManifestInfo manifest = analyzer.getManifestInfo();
}
```

**预期收益**:
- 防止资源泄漏（ZIP 文件句柄）
- 所有分析方法可以安全复用同一实例
- 符合 Java 最佳实践

**实现复杂度**: 低

---

### 1.4 统一异常处理策略

**当前状态**: 混合使用 checked exception (`AndrolibException`) 和 unchecked exception (`SecurityException` in `ApkInfo.setApkFileName()`)，且 `AndrolibException` 作为通用异常包装了各种不同性质的错误。

**提议方案**:
- 保留 `AndrolibException` 作为 checked exception 用于可恢复错误
- 为不可恢复的编程错误引入 `RuntimeException` 子类
- 细化异常类型，区分 I/O 错误、解析错误和配置错误

```java
// 可恢复的 checked exception
public class ApkReadException extends AndrolibException { ... }
public class ManifestParseException extends AndrolibException { ... }
public class ResourceDecodeException extends AndrolibException { ... }

// 不可恢复的 unchecked exception
public class ApktoolConfigurationException extends RuntimeException { ... }
```

**预期收益**:
- 调用方可以针对不同错误类型采取不同恢复策略
- 减少 "catch all" 的异常处理模式
- 更清晰的 API 语义

**实现复杂度**: 中

---

## 2. 缺失的分析功能

### 2.1 DEX 字节码深度分析

**当前状态**: `ApkAnalyzer` 仅解析 AndroidManifest.xml 和资源表。`StructureInfo` 中的 `totalClasses`、`totalMethods`、`totalFields` 等字段未被实际填充。`ApkSearcher` 可以遍历 DEX 中的类和方法，但结果不包含调用关系或继承关系。

**提议方案**:
- 在 `ApkAnalyzer` 中实现 DEX 字节码遍历，填充 `StructureInfo` 的所有字段
- 添加方法调用图 (call graph) 分析
- 添加类继承层次分析

```java
public class StructureInfo {
    // 现有字段 + 新增
    private Map<String, List<String>> classHierarchy;    // 父类 -> 子类列表
    private Map<String, List<String>> methodCalls;       // 方法 -> 调用的方法列表
    private List<String> entryPoints;                     // 入口点（Activity、Service 等生命周期方法）
}
```

**预期收益**:
- 完整的代码结构分析
- 支持恶意代码检测中的行为分析
- 为 AI 分析提供更丰富的上下文

**实现复杂度**: 高

---

### 2.2 字符串加密检测

**当前状态**: 搜索功能可以找到 DEX 中的字符串，但无法识别字符串是否被加密/混淆。

**提议方案**: 添加加密字符串检测分析。

```java
public class ObfuscationInfo {
    private int encryptedStringCount;
    private List<String> suspectedEncryptionMethods;  // 疑似解密方法
    private List<String> encryptedPatterns;            // 检测到的加密模式
    private double obfuscationScore;                   // 混淆程度评分 0-100
}
```

**预期收益**:
- 检测代码保护措施
- 帮助逆向工程师定位解密逻辑
- 为安全报告增加混淆检测维度

**实现复杂度**: 中

---

### 2.3 签名证书验证

**当前状态**: `CommandRegistry` 中注册了 `signing` 命令，但 `Main.java` 中没有对应的 `cmdSigning()` 方法，该命令未实际实现。

**提议方案**: 实现签名信息提取。

```java
public class SigningInfo {
    private boolean v1Signing;
    private boolean v2Signing;
    private boolean v3Signing;
    private List<CertificateInfo> certificates;

    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serial;
        private Date notBefore;
        private Date notAfter;
        private Map<String, String> fingerprints;  // sha256, sha1, md5
    }
}
```

**预期收益**:
- 完成 `signing` 命令的实现
- 支持签名验证和安全审计
- 检测自签名证书和过期证书

**实现复杂度**: 中

---

### 2.4 原生库分析

**当前状态**: `ApkSummary` 报告了是否存在原生库和架构列表，但不分析 .so 文件内容。

**提议方案**: 添加 ELF 文件基础分析。

```java
public class NativeLibInfo {
    private String architecture;
    private String fileName;
    private long fileSize;
    private List<String> exportedSymbols;
    private List<String> importedSymbols;
    private List<String> dependencies;  // 依赖的其他 .so 文件
}
```

**预期收益**:
- 识别原生代码中的潜在安全风险
- 分析 JNI 接口
- 检测可疑的 native 层行为

**实现复杂度**: 高

---

### 2.5 网络端点提取

**当前状态**: 搜索字符串可以找到 URL，但不专门提取网络端点。

**提议方案**: 添加 URL 和域名提取分析。

```java
public class NetworkInfo {
    private List<String> urls;
    private List<String> domains;
    private List<String> ipAddresses;
    private List<String> httpSchemes;     // 明文 HTTP 端点
    private boolean usesCleartextTraffic;
    private boolean usesCertificatePinning;
}
```

**预期收益**:
- 快速了解应用通信目标
- 检测明文通信风险
- 辅助恶意软件分析

**实现复杂度**: 中

---

### 2.6 Tracker/第三方 SDK 检测

**当前状态**: 无此功能。`ApkSearcher` 可以搜索类名，但需要用户手动判断是否为已知 SDK。

**提议方案**: 维护已知 tracker 和 SDK 的特征数据库。

```java
public class TrackerInfo {
    private String name;
    private String category;          // analytics, ads, crash, push, etc.
    private List<String> signatures;  // 匹配的类名或包名
    private double confidence;
}

public class SdkDetectionResult {
    private List<TrackerInfo> detectedTrackers;
    private List<TrackerInfo> detectedSdks;
    private int trackerCount;
    private List<String> unknownPackages;
}
```

**预期收益**:
- 自动识别应用中集成的第三方服务
- 隐私合规检测
- 与 Exodus Privacy 等数据库对接

**实现复杂度**: 中

---

### 2.7 权限使用路径分析

**当前状态**: 安全报告列出了危险权限和导出组件，但不分析代码中实际使用权限的路径。

**提议方案**: 结合 DEX 分析，追踪权限使用。

```java
public class PermissionUsage {
    private String permission;
    private List<String> usedByClasses;
    private List<String> usedByMethods;
    private boolean actuallyUsed;  // 声明但未使用 = 过度授权
}
```

**预期收益**:
- 检测过度权限声明
- 权限最小化建议
- 隐私合规审计

**实现复杂度**: 高

---

## 3. 搜索改进

### 3.1 添加字段名搜索

**当前状态**: `ApkSearcher` 支持 `searchClasses()`、`searchMethods()` 和 `searchStrings()`，但不支持字段名搜索。

**提议方案**: 添加 `searchFields()` 方法。

```java
public SearchResult searchFields(String pattern) throws AndrolibException {
    // 遍历 DEX 中所有类的字段名进行匹配
}
```

**预期收益**: 完善代码搜索覆盖面，常量字段名搜索对逆向分析很有价值。

**实现复杂度**: 低

---

### 3.2 添加注解搜索

**当前状态**: 不支持按注解名称搜索类或方法。

**提议方案**: 添加 `searchAnnotations()` 方法。

```java
public SearchResult searchAnnotations(String pattern) throws AndrolibException {
    // 遍历 DEX 中所有注解进行匹配
    // 返回被注解标注的类/方法/字段信息
}
```

**预期收益**: 快速定位使用特定注解的代码（如 `@BindView`、`@Inject`、`@JavascriptInterface`）。

**实现复杂度**: 中

---

### 3.3 通配符和 Glob 模式支持

**当前状态**: 搜索仅支持 Java 正则表达式，对不熟悉正则语法的用户不够友好。

**提议方案**: 除正则外增加 glob 模式支持。

```java
public SearchResult searchClasses(String pattern, SearchMode mode) {
    // SearchMode.REGEX  - 正则表达式（默认）
    // SearchMode.GLOB   - glob 模式 (*, ?, [a-z])
    // SearchMode.EXACT  - 精确匹配
    // SearchMode.FUZZY  - 模糊匹配
}
```

**预期收益**: 降低搜索使用门槛，支持更多匹配模式。

**实现复杂度**: 低

---

### 3.4 流式搜索结果

**当前状态**: `searchClasses()` 等方法将所有匹配结果加载到内存中的 `ArrayList`。对于大型 APK（数万个类），可能导致内存问题。

**提议方案**: 添加流式/分页搜索接口。

```java
public interface SearchResultHandler {
    void onMatch(SearchMatch match);
    void onComplete(int totalMatches);
}

public void searchClassesStream(String pattern, SearchResultHandler handler) {
    // 逐条处理匹配结果，不全部缓存
}
```

**预期收益**: 降低内存使用，支持超大 APK 的搜索。

**实现复杂度**: 中

---

## 4. Diff 改进

### 4.1 深度 DEX 内容对比

**当前状态**: `ApkDiff.diff()` 仅对比 Manifest 层面的差异（权限、组件、版本号），不对比实际代码内容。

**提议方案**: 添加 DEX 内容层面的对比。

```java
public class DexDiffResult {
    private List<String> addedClasses;
    private List<String> removedClasses;
    private List<MethodChange> modifiedMethods;
    private List<String> addedMethods;
    private List<String> removedMethods;
}

public static class MethodChange {
    private String className;
    private String methodName;
    private String changeType;  // ADDED, REMOVED, MODIFIED
}
```

**预期收益**: 真正的代码级别变更检测，安全审计中检测新增的恶意代码。

**实现复杂度**: 高

---

### 4.2 资源文件对比

**当前状态**: 不对比资源文件的变化。

**提议方案**: 添加 drawable、layout、string 等资源类型的对比。

```java
public class ResourceDiffResult {
    private List<String> addedDrawables;
    private List<String> removedDrawables;
    private List<String> addedLayouts;
    private List<String> removedLayouts;
    private Map<String, String> changedStrings;      // key -> old_value -> new_value
    private List<String> addedStrings;
    private List<String> removedStrings;
}
```

**预期收益**: 全面了解两个版本之间的 UI 和资源变化。

**实现复杂度**: 中

---

### 4.3 HTML 差异报告

**当前状态**: Diff 结果以 JSON 格式输出，不生成可视化报告。

**提议方案**: 添加 HTML 格式的 diff 报告生成。

```java
public static void generateHtmlReport(DiffResult result, File outputFile) {
    // 生成带有颜色标记的 HTML 报告
    // 新增项用绿色，删除项用红色
}
```

**预期收益**: 生成可直接分享的可视化对比报告，方便人工审查。

**实现复杂度**: 中

---

## 5. AI 集成改进

### 5.1 结构化 JSON 上下文输出

**当前状态**: `AiPromptBuilder` 只生成纯文本 prompt。`AiContext` 对象包含结构化数据，但仅被用于拼接字符串。

**提议方案**: 支持将 `AiContext` 直接序列化为 JSON。

```java
public String buildJsonContext() throws AndrolibException {
    AiContext context = buildContext();
    return JsonOutput.toJson(context);
}
```

**预期收益**:
- AI agent 可以直接消费结构化数据
- 支持不同 LLM 的 prompt 格式需求
- 便于下游工具链处理

**实现复杂度**: 低

---

### 5.2 流式 Prompt 生成

**当前状态**: `buildExplainPrompt()` 等方法一次性生成完整 prompt。对于大型 APK，生成的 prompt 可能超出 LLM 上下文窗口。

**提议方案**: 添加分段 prompt 生成。

```java
public List<String> buildChunkedPrompts(int maxTokensPerChunk) throws AndrolibException {
    // 根据 token 预算将 prompt 分成多段
    // 每段包含独立的上下文信息
}
```

**预期收益**: 支持大型 APK 分析，适配不同大小的 LLM 上下文窗口。

**实现复杂度**: 中

---

### 5.3 可配置 Prompt 模板

**当前状态**: prompt 模板硬编码在 `AiPromptBuilder` 的方法中。

**提议方案**: 支持外部模板文件。

```java
public class AiPromptBuilder {
    private PromptTemplate template;

    public AiPromptBuilder(File apkFile, Config config) {
        this(apkFile, config, PromptTemplate.DEFAULT);
    }

    public AiPromptBuilder(File apkFile, Config config, PromptTemplate template) {
        // ...
    }
}

// 模板文件示例 (YAML)
// explain: |
//   Analyze the following Android application...
// security-review: |
//   Perform a security review...
```

**预期收益**: 用户可自定义 prompt 风格和关注点，适配不同 LLM 的最佳实践。

**实现复杂度**: 中

---

### 5.4 Token 预算管理

**当前状态**: `AiContext.estimatedTokenCount` 使用简单的字符数除以 4 进行粗略估算，且生成 prompt 时不考虑预算约束。

**提议方案**: 添加精确的 token 估算和预算控制。

```java
public class TokenBudget {
    private int maxTokens;
    private int reservedForResponse;

    public boolean hasBudget(int estimatedTokens);
    public int remainingBudget();
}

public AiPromptBuilder withTokenBudget(TokenBudget budget) {
    // 在生成 prompt 时自动裁剪超出预算的内容
    // 优先保留关键信息（权限、导出组件、安全报告）
}
```

**预期收益**: 避免 prompt 超出 LLM 上下文窗口，确保关键信息不被截断。

**实现复杂度**: 中

---

## 6. HTTP Server 改进

### 6.1 POST 端点支持文件上传

**当前状态**: 所有 API 端点使用 GET 方法，通过 query parameter 传入 APK 文件路径。这要求 APK 文件在服务器本地文件系统上。

**提议方案**: 添加 POST 端点，支持直接上传 APK 文件。

```java
app.post("/api/v1/analyze", ctx -> {
    UploadedFile file = ctx.uploadedFile("apk");
    File tempFile = saveToTemp(file);
    try {
        String result = handler.handleInfo(tempFile.getAbsolutePath());
        ctx.contentType("application/json").result(result);
    } finally {
        tempFile.delete();
    }
});
```

**预期收益**: 支持远程分析场景，无需预先将文件放在服务器上。

**实现复杂度**: 中

---

### 6.2 批量分析端点

**当前状态**: 每次请求只能分析一个 APK。

**提议方案**: 添加批量分析端点。

```java
app.post("/api/v1/batch", ctx -> {
    List<String> apkPaths = parseJsonArray(ctx.body());
    Map<String, Object> results = new LinkedHashMap<>();
    for (String path : apkPaths) {
        results.put(path, handler.handleInfo(path));
    }
    ctx.json(results);
});
```

**预期收益**: 支持一次性分析多个 APK，减少网络往返。

**实现复杂度**: 低

---

### 6.3 WebSocket 支持

**当前状态**: 仅支持 HTTP 请求-响应模式。长时间的分析操作（如大型 APK 搜索）阻塞请求线程。

**提议方案**: 添加 WebSocket 端点用于实时分析结果推送。

```java
app.ws("/api/v1/ws/analyze", ws -> {
    ws.onMessage(ctx -> {
        String apkPath = ctx.message();
        // 异步分析，逐条推送结果
        analyzer.analyzeStream(apkPath, result -> {
            ws.send(JsonOutput.toJson(result));
        });
    });
});
```

**预期收益**: 支持长时间分析任务的实时反馈，改善用户体验。

**实现复杂度**: 高

---

### 6.4 API 认证

**当前状态**: HTTP API 无任何认证机制，任何人都可访问。

**提议方案**: 添加 API Key 认证。

```java
app.before(ctx -> {
    String apiKey = ctx.header("X-API-Key");
    if (apiKey == null || !validKeys.contains(apiKey)) {
        ctx.status(401).result("{\"error\":\"Unauthorized\"}");
    }
});
```

**预期收益**: 防止未授权访问，适用于生产部署场景。

**实现复杂度**: 低

---

### 6.5 速率限制

**当前状态**: 无请求速率限制，可能被滥用导致资源耗尽。

**提议方案**: 添加速率限制中间件。

```java
app.before(ctx -> {
    String clientIp = ctx.ip();
    if (rateLimiter.isLimited(clientIp)) {
        ctx.status(429).result("{\"error\":\"Rate limit exceeded\"}");
    }
});
```

**预期收益**: 防止 API 滥用，保障服务稳定性。

**实现复杂度**: 低

---

### 6.6 CORS 头支持

**当前状态**: 服务器未设置 CORS 头，浏览器前端应用无法调用 API。

**提议方案**: 添加 CORS 配置。

```java
Javalin app = Javalin.create(config -> {
    config.enableCorsForAllOrigins();
});
```

**预期收益**: 支持浏览器前端、Web IDE 等场景直接调用 API。

**实现复杂度**: 低

---

### 6.7 补全缺失的 API 端点

**当前状态**: HTTP API 缺少以下 CLI 命令的对应端点：`api-surface`、`structure`、`components`、`sdk-info`、`ai`。

**提议方案**: 为所有 CLI 分析命令添加 REST 端点。

| 缺失端点 | CLI 命令 |
|----------|---------|
| `/api/v1/api-surface` | `api-surface` |
| `/api/v1/structure` | `structure` |
| `/api/v1/components` | `components` |
| `/api/v1/sdk-info` | `sdk-info` |
| `/api/v1/ai` | `ai` |
| `/api/v1/activities` | `activities` |
| `/api/v1/services` | `services` |
| `/api/v1/receivers` | `receivers` |
| `/api/v1/providers` | `providers` |

**预期收益**: HTTP API 与 CLI 功能完全对等。

**实现复杂度**: 低

---

## 7. 输出格式改进

### 7.1 YAML 输出实现

**当前状态**: `OutputFormat` 枚举中定义了 `YAML` 值，但未实现 YAML 输出功能。所有分析命令仅支持 JSON 输出。

**提议方案**: 实现统一的输出格式选择机制。

```java
public interface OutputWriter {
    String write(Object obj);
}

public class JsonOutputWriter implements OutputWriter { ... }
public class YamlOutputWriter implements OutputWriter { ... }
public class TextOutputWriter implements OutputWriter { ... }
```

**预期收益**: 用户可选择最适合的输出格式，YAML 格式更适合配置管理场景。

**实现复杂度**: 低

---

### 7.2 CSV 输出支持

**当前状态**: 不支持 CSV 格式输出。

**提议方案**: 为表格型数据添加 CSV 输出。

```java
// 适用于权限列表、组件列表等
apktool permissions app.apk --format=csv
// 输出:
// permission
// android.permission.INTERNET
// android.permission.CAMERA
```

**预期收益**: 方便导入到 Excel、数据库等工具进行进一步分析。

**实现复杂度**: 低

---

### 7.3 SARIF 安全报告输出

**当前状态**: 安全报告仅以 JSON 格式输出。

**提议方案**: 添加 SARIF (Static Analysis Results Interchange Format) 格式支持。

```java
apktool security app.apk --format=sarif
```

**预期收益**: 与 GitHub Security tab、Azure DevOps 等 CI/CD 平台集成。

**实现复杂度**: 中

---

### 7.4 流式 JSON 输出

**当前状态**: `JsonOutput.toJson()` 一次性将整个对象序列化为字符串。对于大型搜索结果，这可能导致内存问题。

**提议方案**: 添加流式 JSON 输出支持。

```java
public static void writeStream(Object obj, OutputStream out) {
    // 使用 Gson 的流式 API 逐步写入
}
```

**预期收益**: 降低大型结果的内存占用。

**实现复杂度**: 中

---

## 8. 性能优化

### 8.1 ApkAnalyzer 结果缓存

**当前状态**: `ApkAnalyzer` 的每个方法独立解析 APK。如果连续调用 `getSummary()`、`getManifestInfo()`、`getSecurityReport()`，Manifest 会被重复解析多次。

**提议方案**: 缓存方法结果。

```java
public class CachingApkAnalyzer implements ApkAnalyzerInterface {
    private final ApkAnalyzer delegate;
    private ManifestInfo cachedManifest;
    private ApkSummary cachedSummary;

    @Override
    public ManifestInfo getManifestInfo() {
        if (cachedManifest == null) {
            cachedManifest = delegate.getManifestInfo();
        }
        return cachedManifest;
    }
}
```

**预期收益**: 避免重复 I/O 和解析操作，特别是在安全报告（调用 `getManifestInfo()` 获取组件和权限信息后计算风险分数）等场景中。

**实现复杂度**: 低

---

### 8.2 大数据集延迟加载

**当前状态**: 搜索结果全部加载到 `ArrayList`，资源表全量加载到内存。

**提议方案**: 对大型数据集使用延迟加载和分页。

```java
public class PagedSearchResult {
    private int page;
    private int pageSize;
    private int totalMatches;
    private List<SearchMatch> matches;
    private boolean hasMore;
}
```

**预期收益**: 降低大型 APK 分析的内存占用，提升响应速度。

**实现复杂度**: 中

---

### 8.3 多 DEX 并行处理

**当前状态**: `ApkSearcher` 和 `StructureInfo` 的 DEX 遍历是串行的。

**提议方案**: 使用 `Config.getJobs()` 指定的并行度处理多 DEX 文件。

```java
public SearchResult searchClassesParallel(String pattern) {
    ExecutorService executor = Executors.newFixedThreadPool(config.getJobs());
    List<Future<List<SearchMatch>>> futures = new ArrayList<>();

    for (String dexName : container.getDexEntryNames()) {
        futures.add(executor.submit(() -> searchInDex(dexName, pattern)));
    }

    SearchResult result = new SearchResult();
    for (Future<List<SearchMatch>> f : futures) {
        result.getMatches().addAll(f.get());
    }
    return result;
}
```

**预期收益**: 对于包含多个 DEX 文件的大型 APK，显著提升搜索和分析速度。

**实现复杂度**: 中

---

### 8.4 内存高效流式搜索

**当前状态**: 搜索结果在返回前全部缓存在内存中。

**提议方案**: 使用回调模式流式处理搜索结果。

```java
@FunctionalInterface
public interface MatchHandler {
    void onMatch(SearchMatch match);
}

public void searchClassesStreaming(String pattern, MatchHandler handler) {
    // 逐条遍历匹配结果，通过回调传递
    // 不在内存中积累全部结果
}
```

**预期收益**: 支持超大 APK 的搜索，避免 OOM。

**实现复杂度**: 中

---

## 9. 测试与质量

### 9.1 真实 APK 集成测试

**当前状态**: 项目包含 `BaseTest` 和多个 `*Test` 类使用 `build/` 目录中的测试 APK 进行集成测试。但分析功能 (`ApkAnalyzer`, `ApkSearcher`) 的测试覆盖有限。

**提议方案**: 为分析模块添加系统的集成测试。

```java
public class ApkAnalyzerIntegrationTest {
    @Test
    void testInfoCommand() throws AndrolibException {
        ApkAnalyzer analyzer = new ApkAnalyzer(
            new File("build/test-libs/app.apk"), config);
        ApkSummary summary = analyzer.getSummary();

        assertNotNull(summary.getPackageName());
        assertTrue(summary.getDexCount() > 0);
    }
}
```

**预期收益**: 确保分析功能在各种 APK 格式下正确工作。

**实现复杂度**: 中

---

### 9.2 性能基准测试

**当前状态**: 无性能基准测试。

**提议方案**: 使用 JMH 建立性能基准。

```java
@BenchmarkMode(Mode.AverageTime)
public class ApkAnalyzerBenchmark {
    @Benchmark
    public void benchmarkGetSummary(Blackhole bh) {
        bh.consume(analyzer.getSummary());
    }

    @Benchmark
    public void benchmarkSearchClasses(Blackhole bh) {
        bh.consume(searcher.searchClasses("Activity"));
    }
}
```

**预期收益**: 量化性能改进效果，防止性能回归。

**实现复杂度**: 中

---

### 9.3 API 兼容性测试

**当前状态**: 没有 API 兼容性保障机制。

**提议方案**: 添加 API 契约测试。

```java
// 确保所有分析命令的 JSON 输出格式稳定
public class ApiContractTest {
    @Test
    void testInfoOutputFormat() {
        ApkSummary summary = getTestSummary();
        String json = JsonOutput.toJson(summary);

        // 验证所有预期字段都存在
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        assertTrue(obj.has("fileName"));
        assertTrue(obj.has("fileSize"));
        assertTrue(obj.has("packageName"));
        // ...
    }
}
```

**预期收益**: 确保 API 输出格式在版本间保持兼容，防止破坏下游消费者。

**实现复杂度**: 低

---

## 优先级矩阵

根据实现复杂度和预期收益，以下是建议的实施优先级排序：

### 高优先级（低复杂度 + 高收益）

| 编号 | 建议 | 复杂度 |
|------|------|--------|
| 1.1 | Config Builder 模式 | 低 |
| 1.2 | ApkAnalyzer 接口抽象 | 低 |
| 1.3 | ApkAnalyzer AutoCloseable | 低 |
| 5.1 | AI JSON 上下文输出 | 低 |
| 6.4 | API 认证 | 低 |
| 6.5 | 速率限制 | 低 |
| 6.6 | CORS 头 | 低 |
| 6.7 | 补全缺失 API 端点 | 低 |
| 7.1 | YAML 输出实现 | 低 |
| 7.2 | CSV 输出支持 | 低 |
| 8.1 | ApkAnalyzer 结果缓存 | 低 |
| 9.3 | API 兼容性测试 | 低 |

### 中优先级（中复杂度 + 高收益）

| 编号 | 建议 | 复杂度 |
|------|------|--------|
| 1.4 | 统一异常处理策略 | 中 |
| 2.2 | 字符串加密检测 | 中 |
| 2.3 | 签名证书验证 | 中 |
| 2.5 | 网络端点提取 | 中 |
| 2.6 | Tracker/SDK 检测 | 中 |
| 3.2 | 注解搜索 | 中 |
| 3.4 | 流式搜索结果 | 中 |
| 4.2 | 资源文件对比 | 中 |
| 4.3 | HTML 差异报告 | 中 |
| 5.2 | 流式 Prompt 生成 | 中 |
| 5.3 | 可配置 Prompt 模板 | 中 |
| 5.4 | Token 预算管理 | 中 |
| 6.1 | POST 文件上传 | 中 |
| 7.3 | SARIF 输出 | 中 |
| 8.2 | 延迟加载 | 中 |
| 8.3 | 多 DEX 并行处理 | 中 |
| 9.1 | 真实 APK 集成测试 | 中 |
| 9.2 | 性能基准测试 | 中 |

### 低优先级（高复杂度 + 高收益 或 低复杂度 + 低收益）

| 编号 | 建议 | 复杂度 |
|------|------|--------|
| 2.1 | DEX 字节码深度分析 | 高 |
| 2.4 | 原生库分析 | 高 |
| 2.7 | 权限使用路径分析 | 高 |
| 3.1 | 字段名搜索 | 低 |
| 3.3 | 通配符/Glob 模式 | 低 |
| 4.1 | 深度 DEX 对比 | 高 |
| 6.3 | WebSocket 支持 | 高 |
