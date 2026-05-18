# 快速开始

本文档介绍如何在 Java 项目中将 AI-Apktool 作为 JAR 依赖使用。

## 前置条件

- JDK 11 或更高版本
- Gradle 或 Maven 构建工具

## 添加依赖

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("brut.apktool:apktool-lib:3.0.3-SNAPSHOT")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'brut.apktool:apktool-lib:3.0.3-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>brut.apktool</groupId>
    <artifactId>apktool-lib</artifactId>
    <version>3.0.3-SNAPSHOT</version>
</dependency>
```

如需使用 HTTP 服务模块，额外添加：

```kotlin
implementation("brut.apktool:apktool-serve:3.0.3-SNAPSHOT")
```

## 关键导入

```java
import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkBuilder;
import brut.androlib.res.Framework;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ApkDiff;
import brut.androlib.search.ApkSearcher;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.exceptions.AndrolibException;
```

## 基本用法

### 1. 解码 APK

将 APK 文件解码为可读的目录结构（smali 代码、XML 资源、Manifest 等）。

```java
import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import java.io.File;

Config config = new Config("3.0.3");
// 默认只解码 classes.dex，如需全部 dex 文件可取消下面注释
// config.setDecodeSources(Config.DecodeSources.FULL);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### 2. 分析 APK

获取 APK 的摘要信息、Manifest 解析结果和安全报告。

```java
import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ApkSummary;
import brut.androlib.analyze.ManifestInfo;
import brut.androlib.analyze.SecurityReport;
import java.io.File;

Config config = new Config("3.0.3");
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);

// 获取 APK 摘要（文件大小、dex 数量、架构等）
ApkSummary summary = analyzer.getSummary();

// 获取 Manifest 信息（包名、权限、四大组件等）
ManifestInfo manifest = analyzer.getManifestInfo();

// 获取安全报告（危险权限、导出组件、风险评分等）
SecurityReport report = analyzer.getSecurityReport();
```

### 3. 搜索 APK 内容

在 APK 内搜索字符串资源、类名或方法名。

```java
import brut.androlib.Config;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import java.io.File;

Config config = new Config("3.0.3");
ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

// 搜索字符串资源
SearchResult strings = searcher.searchStrings("password");

// 搜索类名
SearchResult classes = searcher.searchClasses("com\\.example\\..*Activity");

// 搜索方法名
SearchResult methods = searcher.searchMethods("onClick");
```

### 4. 获取安全报告

```java
import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.SecurityReport;
import java.io.File;

Config config = new Config("3.0.3");
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);

SecurityReport report = analyzer.getSecurityReport();
// report.getDangerousPermissions() -- 危险权限列表
// report.getHighRiskComponents()    -- 高风险导出组件列表
// report.getFindings()              -- 安全发现列表
// report.getRiskScore()             -- 风险评分 (0-100)
// report.isDebuggable()             -- 是否可调试
// report.isAllowBackup()            -- 是否允许备份
```

### 5. 对比两个 APK

比较两个 APK 版本之间的差异（权限变化、组件变化、版本号变化等）。

```java
import brut.androlib.Config;
import brut.androlib.analyze.ApkDiff;
import brut.androlib.analyze.DiffResult;
import java.io.File;

Config config = new Config("3.0.3");
DiffResult diff = ApkDiff.diff(
    new File("app-v1.apk"),
    new File("app-v2.apk"),
    config
);
// diff.getAddedPermissions()    -- 新增权限
// diff.getRemovedPermissions()  -- 移除权限
// diff.getAddedActivities()     -- 新增 Activity
// diff.getRemovedActivities()   -- 移除 Activity
// diff.getVersionCodeChange()   -- 版本号变化
```

### 6. 生成 AI 提示

为 AI 模型生成结构化的分析上下文和提示词。

```java
import brut.androlib.Config;
import brut.androlib.ai.AiPromptBuilder;
import brut.androlib.ai.AiContext;
import java.io.File;

Config config = new Config("3.0.3");
AiPromptBuilder builder = new AiPromptBuilder(new File("app.apk"), config);

// 获取结构化上下文
AiContext context = builder.buildContext();

// 生成"功能解释"提示词
String explainPrompt = builder.buildExplainPrompt();

// 生成"安全审计"提示词
String securityPrompt = builder.buildSecurityReviewPrompt();

// 生成"摘要总结"提示词
String summarizePrompt = builder.buildSummarizePrompt();
```

### 7. 启动 HTTP 服务

通过 HTTP API 暴露 AI-Apktool 的全部能力。

```java
import brut.apktool.serve.ApktoolServer;

ApktoolServer server = new ApktoolServer(8080);
// 服务启动后可通过以下端点访问：
// GET /api/v1/health       -- 健康检查
// GET /api/v1/info?apk=    -- APK 信息
// GET /api/v1/manifest?apk=-- Manifest 信息
// GET /api/v1/permissions?apk= -- 权限列表
// GET /api/v1/security?apk=    -- 安全报告
// GET /api/v1/search?apk=&type=&pattern= -- 搜索
// GET /api/v1/diff?apk1=&apk2= -- 差异对比
// GET /api/v1/resources?apk=   -- 资源信息

// 停止服务
server.stop();
```

## 下一步

- [Config 配置参考](config.md) -- 了解所有可配置的解码和构建选项
- [ApkDecoder 解码参考](apk-decoder.md) -- 深入了解解码 API 的详细用法
- [ApkBuilder 构建参考](apk-builder.md) -- 深入了解构建 API 的详细用法
- [Framework 管理参考](framework.md) -- 了解框架资源管理
