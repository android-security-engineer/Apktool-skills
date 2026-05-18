# AiPromptBuilder 与 AiContext API 参考

> 包路径: `brut.androlib.ai.AiPromptBuilder` / `brut.androlib.ai.AiContext`

AiPromptBuilder 用于从 APK 文件中提取关键信息，构建可供大语言模型（LLM）直接使用的分析提示词（Prompt）。AiContext 是构建过程中产生的上下文数据模型。

---

## AiPromptBuilder

### 构造函数

```java
public AiPromptBuilder(File apkFile, Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `File` | 目标 APK 文件 |
| `config` | `Config` | 配置对象，可通过 `new Config("ai")` 创建 |

---

### buildContext

```java
public AiContext buildContext() throws AndrolibException
```

构建完整的 AI 上下文信息。该方法会解析 APK 文件，收集以下信息:

1. APK 文件名
2. 通过 `ApkAnalyzer` 解析 ManifestInfo，获取包名、权限列表和组件列表
3. 收集所有四大组件: Activity、Service、Receiver、Provider
4. 通过 `ApkAnalyzer` 生成安全报告（SecurityReport）
5. 估算 Token 数量

**Token 估算公式:**

```java
totalChars = manifestXml长度 + permissions数量 * 40 + components数量 * 60 + securityReport长度
estimatedTokenCount = totalChars / 4
```

**返回:** `AiContext` 对象，包含所有收集到的上下文信息。

---

### buildExplainPrompt

```java
public String buildExplainPrompt() throws AndrolibException
```

生成用于 **应用功能分析** 的 LLM Prompt。该方法内部调用 `buildContext()` 收集信息，然后构建一个结构化的分析提示词。

**Prompt 内容包括:**
- APK 文件名和包名
- 权限列表
- 组件列表
- 安全分析报告
- 分析请求指令（功能总结、关键特性、安全问题）

**返回:** 完整的文本提示词字符串。

---

### buildSecurityReviewPrompt

```java
public String buildSecurityReviewPrompt() throws AndrolibException
```

生成用于 **安全审计** 的 LLM Prompt。专注于安全漏洞、隐私风险和攻击面分析。

**Prompt 内容包括:**
- 包名
- 自动化安全报告
- 权限列表
- 导出组件（Exported Components）列表
- 安全审计请求指令（关键漏洞、隐私问题、攻击面、修复建议）

> 注意: 该方法会过滤出组件名中包含 "exported" 关键字的组件进行重点分析。

**返回:** 完整的安全审计文本提示词字符串。

---

### buildSummarizePrompt

```java
public String buildSummarizePrompt() throws AndrolibException
```

生成用于 **简洁技术摘要** 的 LLM Prompt。生成一个 3-5 句话的技术摘要。

**Prompt 内容包括:**
- APK 文件名
- 包名
- 权限数量
- 组件数量
- 安全报告
- 摘要请求指令

**返回:** 完整的摘要文本提示词字符串。

---

## AiContext 数据模型

> 包路径: `brut.androlib.ai.AiContext`

```java
public class AiContext {
    private String apkFileName;                  // APK 文件名
    private String packageName;                  // 应用包名
    private String manifestXml;                  // AndroidManifest.xml 内容
    private List<String> permissions;            // 权限列表
    private List<String> components;             // 组件列表（格式: "Activity: 全限定类名"）
    private List<String> stringResources;        // 字符串资源列表
    private String securityReport;               // 安全报告（JSON 格式字符串）
    private int estimatedTokenCount;             // 估算的 Token 数量
}
```

**字段详细说明:**

| 字段 | 类型 | 说明 |
|------|------|------|
| `apkFileName` | `String` | APK 文件名（不含路径），通过 `file.getName()` 获取 |
| `packageName` | `String` | 应用的包名（如 `com.example.app`） |
| `manifestXml` | `String` | 解码后的 AndroidManifest.xml 内容 |
| `permissions` | `List<String>` | Manifest 中声明的所有权限 |
| `components` | `List<String>` | 所有四大组件，每项格式为 `"类型: 全限定类名"`（类型为 Activity/Service/Receiver/Provider） |
| `stringResources` | `List<String>` | 字符串资源列表 |
| `securityReport` | `String` | 安全报告，由 `JsonOutput.toJson(securityReport)` 序列化后的 JSON 字符串 |
| `estimatedTokenCount` | `int` | 估算的 Token 数量，计算公式为 `totalChars / 4` |

### JSON 输出示例

```json
{
  "apkFileName": "my-app.apk",
  "packageName": "com.example.myapp",
  "manifestXml": null,
  "permissions": [
    "android.permission.INTERNET",
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION"
  ],
  "components": [
    "Activity: com.example.myapp.MainActivity",
    "Activity: com.example.myapp.SettingsActivity",
    "Service: com.example.myapp.UploadService",
    "Receiver: com.example.myapp.BootReceiver",
    "Provider: com.example.myapp.DataProvider"
  ],
  "stringResources": [],
  "securityReport": "{ ... JSON 格式的安全报告 ... }",
  "estimatedTokenCount": 1250
}
```

---

## Prompt 输出示例

### buildExplainPrompt 输出示例

```
Analyze the following Android application and explain its main functionality:

APK File: my-app.apk
Package: com.example.myapp

Permissions:
- android.permission.INTERNET
- android.permission.CAMERA
- android.permission.ACCESS_FINE_LOCATION
- android.permission.RECORD_AUDIO

Components:
- Activity: com.example.myapp.MainActivity
- Activity: com.example.myapp.CameraActivity
- Service: com.example.myapp.UploadService
- Receiver: com.example.myapp.BootReceiver

Security Analysis:
{"debuggable":false,"allowBackup":true,"dangerousPermissions":["CAMERA","ACCESS_FINE_LOCATION","RECORD_AUDIO"],"highRiskComponents":[],"riskScore":35}

Please provide:
1. A summary of what this application does
2. Key features based on components and permissions
3. Security concerns
```

### buildSecurityReviewPrompt 输出示例

```
Perform a security review of the following Android application:

Package: com.example.myapp
Automated Security Report:
{"debuggable":false,"allowBackup":true,"usesCleartextTraffic":false,"dangerousPermissions":["CAMERA","ACCESS_FINE_LOCATION","RECORD_AUDIO"],"highRiskComponents":[],"findings":["AllowBackup is enabled - data may be accessible via adb backup"],"riskScore":35}

Permissions:
- android.permission.INTERNET
- android.permission.CAMERA
- android.permission.ACCESS_FINE_LOCATION
- android.permission.RECORD_AUDIO

Exported Components:

Please identify:
1. Critical security vulnerabilities
2. Privacy concerns from permissions
3. Attack surface from exported components
4. Recommendations for fixing each issue
```

### buildSummarizePrompt 输出示例

```
Generate a concise technical summary of this Android application:

APK: my-app.apk
Package: com.example.myapp
Permissions: 4
Components: 4
Security Report:
{"debuggable":false,"allowBackup":true,"dangerousPermissions":["CAMERA","ACCESS_FINE_LOCATION","RECORD_AUDIO"],"riskScore":35}

Provide a 3-5 sentence technical summary.
```

---

## 代码示例

### 构建 AI 上下文

```java
import brut.androlib.Config;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.ai.AiContext;
import brut.androlib.output.JsonOutput;

import java.io.File;

public class AiContextExample {
    public static void main(String[] args) throws Exception {
        Config config = new Config("ai");
        AiPromptBuilder builder = new AiPromptBuilder(new File("app.apk"), config);

        // 构建完整上下文
        AiContext context = builder.buildContext();

        System.out.println("APK 文件名: " + context.getApkFileName());
        System.out.println("包名: " + context.getPackageName());
        System.out.println("权限数量: " + context.getPermissions().size());
        System.out.println("组件数量: " + context.getComponents().size());
        System.out.println("估算 Token 数: " + context.getEstimatedTokenCount());

        // 输出 JSON 格式的上下文
        System.out.println(JsonOutput.toJson(context));
    }
}
```

### 生成功能分析 Prompt

```java
Config config = new Config("ai");
AiPromptBuilder builder = new AiPromptBuilder(new File("app.apk"), config);

String explainPrompt = builder.buildExplainPrompt();
System.out.println(explainPrompt);

// 可直接将 explainPrompt 发送给 LLM API 进行分析
```

### 生成安全审计 Prompt

```java
Config config = new Config("ai");
AiPromptBuilder builder = new AiPromptBuilder(new File("app.apk"), config);

String securityPrompt = builder.buildSecurityReviewPrompt();
System.out.println(securityPrompt);

// 发送给 LLM 进行安全审计
```

### 生成摘要 Prompt

```java
Config config = new Config("ai");
AiPromptBuilder builder = new AiPromptBuilder(new File("app.apk"), config);

String summaryPrompt = builder.buildSummarizePrompt();
System.out.println(summaryPrompt);

// 发送给 LLM 生成简洁摘要
```

### 与 LLM API 集成的完整示例

```java
import brut.androlib.Config;
import brut.androlib.ai.AiPromptBuilder;

public class LlmIntegrationExample {
    public static void main(String[] args) throws Exception {
        Config config = new Config("ai");
        AiPromptBuilder builder = new AiPromptBuilder(new File("target.apk"), config);

        // 生成不同类型的 Prompt
        String[] prompts = {
            builder.buildExplainPrompt(),        // 功能分析
            builder.buildSecurityReviewPrompt(), // 安全审计
            builder.buildSummarizePrompt()       // 技术摘要
        };

        String[] labels = {
            "=== 功能分析 Prompt ===",
            "=== 安全审计 Prompt ===",
            "=== 技术摘要 Prompt ==="
        };

        for (int i = 0; i < prompts.length; i++) {
            System.out.println(labels[i]);
            System.out.println(prompts[i]);
            System.out.println();
        }
    }
}
```

---

## 注意事项

- 每次调用 `buildExplainPrompt()`、`buildSecurityReviewPrompt()` 或 `buildSummarizePrompt()` 都会内部调用 `buildContext()`，这意味着每次调用都会重新解析 APK 文件
- `estimatedTokenCount` 是一个估算值，基于字符数除以 4 计算，实际 Token 数量取决于 LLM 的分词器
- `securityReport` 字段是 JSON 格式的字符串（通过 `JsonOutput.toJson()` 序列化），而非对象
- 组件列表中的条目格式统一为 `"组件类型: 全限定类名"`，便于在 Prompt 中以结构化方式呈现
