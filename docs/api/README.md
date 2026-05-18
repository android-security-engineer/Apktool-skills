# AI-Apktool API 文档

## 项目概述

AI-Apktool 是一个基于 [Apktool](https://apktool.org/) 构建的 AI 原生 APK 逆向工程平台。它在保留 Apktool 原有 APK 解码（decode）与构建（build）能力的基础上，新增了 APK 分析、内容搜索、安全审计、差异对比、AI 提示生成以及 HTTP API 服务等面向 AI Agent 和自动化工作流的特性。

- **版本**: 3.0.3-SNAPSHOT
- **许可证**: Apache License 2.0

## 模块结构

| 模块 | 说明 |
|------|------|
| `brut.apktool:apktool-lib` | 核心库，包含解码、构建、分析、搜索、AI 等全部基础能力 |
| `brut.apktool:apktool-cli` | 命令行界面，提供 CLI 工具 |
| `brut.apktool:apktool-serve` | HTTP 服务器，基于 Javalin 提供 RESTful API |

## 依赖引入

在 Gradle 构建脚本中添加以下依赖：

```groovy
implementation("brut.apktool:apktool-lib:3.0.3-SNAPSHOT")
```

如需使用 HTTP 服务模块：

```groovy
implementation("brut.apktool:apktool-serve:3.0.3-SNAPSHOT")
```

## 核心包（Packages）

| 包名 | 说明 |
|------|------|
| `brut.androlib` | 核心类：Config、ApkDecoder、ApkBuilder |
| `brut.androlib.analyze` | APK 分析：ApkAnalyzer、ApkDiff、安全报告 |
| `brut.androlib.search` | 内容搜索：ApkSearcher（字符串、类名、方法名） |
| `brut.androlib.ai` | AI 集成：AiPromptBuilder（生成 AI 友好的上下文与提示） |
| `brut.androlib.output` | 输出格式化：JsonOutput |
| `brut.androlib.res` | 资源处理：Framework、ResDecoder |
| `brut.androlib.exceptions` | 异常定义：AndrolibException 及其子类 |
| `brut.apktool.serve` | HTTP 服务：ApktoolServer |

## 文档目录

- **[快速开始](quick-start.md)** -- 依赖配置与基本用法示例
- **[Config 配置参考](config.md)** -- Config 类完整 API 文档
- **[ApkDecoder 解码参考](apk-decoder.md)** -- APK 解码（decode）完整 API 文档
- **[ApkBuilder 构建参考](apk-builder.md)** -- APK 构建（build）完整 API 文档
- **[Framework 管理参考](framework.md)** -- 框架资源安装与管理完整 API 文档
