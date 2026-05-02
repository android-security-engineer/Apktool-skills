# AI-Apktool Skills

> AI-native Android reverse engineering skills for Claude Code.

5 个 Skills 覆盖 APK 分析全流程：快速分析、安全审计、版本对比、逆向工程、命令参考。

## 包含的 Skills（5个）

| Skill | 描述 |
|-------|------|
| **quick-analysis** | 快速 APK 评估 — 一键获取包名、风险分数、危险权限、导出组件 |
| **security-audit** | 安全审计 — OWASP Mobile Top 10 映射、攻击面分析、敏感数据搜索 |
| **compare** | 版本对比 — 权限变更、组件增删、风险分数变化 |
| **reverse** | 逆向工程 — 解码、搜索、安全审查、AI 提示生成、重打包 |
| **reference** | 命令参考 — 全部 22 个 CLI 命令的语法、选项、输出格式 |

## 前提条件

- 已安装 [Claude Code](https://claude.ai/code)
- 已构建 AI-Apktool CLI：

```bash
git clone https://github.com/CC11001100/android-reverse-hub.git
cd AI-Apktool
./gradlew build
# JAR at: brut.apktool/apktool-cli/build/libs/apktool-*.jar
```

## 安装

### 方式 1：作为 Claude Code 插件安装

```bash
# 添加 Marketplace
claude config add marketplace ai-apktool https://github.com/CC11001100/android-reverse-hub.git

# 安装插件
claude plugin install ai-apktool@ai-apktool
```

### 方式 2：手动安装 Skills

```bash
# 克隆到 Claude Code skills 目录
git clone https://github.com/CC11001100/android-reverse-hub.git ~/.claude/skills/ai-apktool
```

### 验证安装

```bash
claude skill list
# 应看到：
#   ai-apktool:quick-analysis
#   ai-apktool:security-audit
#   ai-apktool:compare
#   ai-apktool:reverse
#   ai-apktool:reference
```

## 使用

### 自动生效

安装后，当 Claude Code 遇到 APK 相关任务时，会自动识别并使用合适的 Skill。

### 手动调用

```
/quick-analysis 分析这个 APK: /path/to/app.apk
/security-audit 对 app.apk 做安全审计
/compare 对比 app_v1.apk 和 app_v2.apk
/reverse 逆向分析 app.apk
/reference 查看 search 命令的用法
```

### 典型工作流

```
用户: 分析这个 APK 文件

AI: [使用 quick-analysis skill]
1. 运行: java -jar apktool.jar analyze /path/to/app.apk
2. 报告发现:
   - 包名: com.example.app v2.1.0
   - 风险分数: 35/100 (中等)
   - 3 个危险权限: CAMERA, RECORD_AUDIO, ACCESS_FINE_LOCATION
   - 2 个未保护的导出 Activity
   - 签名者: CN=Developer, O=Example Inc
```

## CLI 命令总览

| 类别 | 命令 |
|------|------|
| 核心操作 | decode, build, install-framework, clean-frameworks, list-frameworks, publicize-resources |
| 信息查询 | info, manifest, permissions, activities, services, receivers, providers, components, sdk-info |
| 高级分析 | security, api-surface, signing, structure, analyze |
| 搜索 | search |
| 对比 | diff |
| AI/服务 | ai, serve |

所有分析命令输出 JSON，可用 `jq` 解析：
```bash
java -jar apktool.jar info app.apk | jq '.packageName'
java -jar apktool.jar security app.apk | jq '.riskScore'
```

## 架构

```
skills/
  quick-analysis/SKILL.md   — 快速分析工作流
  security-audit/SKILL.md   — 安全审计工作流
  compare/SKILL.md          — 版本对比工作流
  reverse/SKILL.md          — 逆向工程工作流
  reference/SKILL.md        — 命令参考
.claude-plugin/
  plugin.json               — 插件元数据
  marketplace.json          — 市场配置
CLAUDE.md                   — AI 入口文档
```

## 许可证

Apache License 2.0
