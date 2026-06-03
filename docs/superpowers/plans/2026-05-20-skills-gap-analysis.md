# Research: AI-Apktool Skills 能力补全分析

**Question:** 现有 11 个 Skills 覆盖了哪些逆向工程工作流？哪些关键工作流缺失？如何补全？

**Context:** AI-Apktool 引擎已支持 40+ CLI 命令和 34+ HTTP 端点，但 Skills 层只定义了 11 个工作流。需要识别引擎已具备但未暴露为 Skill 的能力，以及逆向工程实践中常见但引擎和 Skill 都缺失的场景。

**Deliverable:** 分层补全建议（优先级排序 + 具体实现路径）

**Scope:** Medium

---

## 1. 现状盘点：11 个 Skills 覆盖矩阵

| Skill | 覆盖域 | 使用的命令数 | 深度 |
|-------|--------|------------|------|
| quick-analysis | 快速评估 | 7 | 浅 — 只读不深入 |
| security-audit | 安全审计 | 9 | 中 — OWASP 映射但无修复建议 |
| compare | 版本对比 | 7 | 浅 — 无语义级 diff |
| reverse | 完整逆向 | 11 | 深 — 但缺 smali 导航 |
| reference | 命令参考 | 0 | 文档型 |
| decode-build | 解码构建 | 8 | 中 — 缺签名和排错 |
| dex-deep-dive | DEX 深潜 | 10 | 中 — 缺跨 DEX 和反混淆 |
| network-analysis | 网络分析 | 8 | 浅 — 纯字符串匹配 |
| malware-hunt | 恶软猎杀 | 12 | 中 — 无置信度评分 |
| resource-explorer | 资源探索 | 12 | 浅 — 不能提取具体资源值 |
| signing-verify | 签名验证 | 5 | 中 — 无证书库比对 |

---

## 2. 缺失能力分析（三层缺口）

### 第一层：引擎已有能力，Skill 未暴露（最易补全，只需写 SKILL.md）

#### 2.1 第三方库审计（third-party-audit）

**现状：** 引擎有 `uses-libs`、`native-libs`、`class-list`、`structure`、`dex-info`、`resource-packages`、`lib-frame-packages`，但没有 Skill 将它们组合成"识别所有第三方依赖 → 版本识别 → 已知漏洞检查"的工作流。

**逆向工程价值：** 高。第三方库是漏洞的主要来源，识别库版本是安全审计的前置步骤。

**工作流设计：**
```
输入: APK 文件
Step 1: structure → 识别包名模式（com.google.*, okhttp3.*, retrofit2.*, androidx.* 等）
Step 2: class-list → grep 已知库的包名前缀，识别具体库
Step 3: dex-strings → grep 版本字符串（"3.14.2", "v2.7.1" 等模式）
Step 4: uses-libs + native-libs → 识别共享库和 native 依赖
Step 5: resource-packages + lib-frame-packages → 识别嵌入的 SDK 和框架
输出: 第三方库清单 + 版本 + 来源（Java/Native/资源包）
```

---

#### 2.2 数据泄露检测（data-leakage-detect）

**现状：** `security-audit` 的 Step 4 提到"Sensitive Data Search"但只是简单 grep。引擎有 `strings`、`dex-strings`、`search`、`field-search`、`manifest-flags`，可以构建更精确的泄露检测。

**逆向工程价值：** 高。硬编码密钥是 OWASP M2 (Insecure Data Storage) 的核心问题。

**工作流设计：**
```
输入: APK 文件
Step 1: strings + dex-strings → 提取所有字符串
Step 2: 正则匹配已知密钥模式:
  - AWS: AKIA[0-9A-Z]{16}, aws_secret_access_key
  - Google AI: AIza[0-9A-Za-z\-_]{35}
  - Firebase: https://[a-z0-9]+.firebaseio.com
  - JWT: eyJ[A-Za-z0-9-_]+\.eyJ[A-Za-z0-9-_]+
  - Private keys: -----BEGIN (RSA |EC |DSA )?PRIVATE KEY-----
  - Database: (mysql|postgres|mongodb)://[^\s]+
  - Generic tokens: [a-zA-Z0-9]{32,} (高熵字符串)
Step 3: field-search → 搜索名为 apiKey/secret/token/password 的字段
Step 4: manifest → 检查 meta-data 中的 API key
输出: 泄露清单（类型 + 值 + 位置 + 严重程度）
```

---

#### 2.3 混淆检测与分析（obfuscation-detect）

**现状：** 引擎有 `class-list`、`structure`、`dex-info`，可以检测 ProGuard/R8 混淆特征，但没有 Skill。

**逆向工程价值：** 中高。混淆检测是逆向工程的第一步 — 知道代码是否被混淆决定了后续分析策略。

**工作流设计：**
```
输入: APK 文件
Step 1: class-list → 统计类名模式:
  - 短名类（a/b/c 单字符）比例
  - 保留的类名比例
  - 混淆置信度 = 短名类数 / 总类数
Step 2: structure → 检查包结构:
  - 是否有大量类在根包或单字符包下
  - 方法数/类数比例（混淆后通常方法数少）
Step 3: dex-info → 多 DEX 可能表示大型混淆后代码
Step 4: method-search → 搜索 ProGuard/R8 特征:
  - 保持规则保留的类（*Application, *Activity）
  - 混淆映射文件（mapping.txt）是否嵌入
输出: 混淆状态（是/否/部分）+ 混淆工具推断 + 混淆程度 + 建议分析策略
```

---

#### 2.4 攻击面建模（attack-surface-model）

**现状：** `api-surface` 返回 exported components + intent filters，但没有任何 Skill 将其转化为"攻击者可以通过什么路径做什么"的攻击图。

**逆向工程价值：** 高。安全审计的核心不是列出组件，而是建模攻击路径。

**工作流设计：**
```
输入: APK 文件
Step 1: api-surface → 获取所有 exported components + intent filters
Step 2: 对每个 exported component:
  - 分析 intent filter 的 action/data/category
  - 检查是否有 permission 保护
  - 检查组件类型（Activity 可被启动、Service 可被绑定、Receiver 可被触发、Provider 可被查询）
Step 3: components → 获取所有组件，识别组件间通信路径
Step 4: method-search → 搜索每个 exported component 的关键方法:
  - Activity: onCreate, onNewIntent, onActivityResult
  - Service: onBind, onStartCommand
  - Receiver: onReceive
  - Provider: query, insert, update, delete
Step 5: 构建攻击图: 外部入口 → 组件 → 方法 → 数据流
输出: 攻击面图 + 每条路径的风险等级 + 无保护组件列表
```

---

#### 2.5 AI Context 集成（ai-context）

**现状：** `ai` 命令支持 4 种 action（explain/security-review/summarize/context），但 `context` action 在所有 Skill 中从未被使用。`context` 返回结构化 JSON 上下文，是 AI Agent 理解 APK 最直接的方式。

**逆向工程价值：** 中。这是 AI-Apktool 的核心差异化能力，却被忽略了。

**工作流设计：**
```
输入: APK 文件 + 分析目标
Step 1: ai -a context → 获取完整结构化上下文（含 token 估算）
Step 2: 根据 token 估算决定策略:
  - < 4K tokens: 完整上下文直接使用
  - 4K-100K tokens: 选择性裁剪（保留 manifest + security + api-surface）
  - > 100K tokens: 分片处理（按模块/包拆分）
Step 3: ai -a explain/security-review/summarize → 生成针对性 prompt
输出: AI 可直接消费的结构化上下文 + token 优化的 prompt
```

---

### 第二层：引擎部分有能力，需要增强代码 + 新 Skill（中等难度）

#### 2.6 Smali 代码导航与阅读（smali-navigator）

**现状：** `reverse` 和 `decode-build` 都有 decode 步骤，但解码后没有任何指导如何阅读 smali 代码。引擎目前无法读取或搜索 smali 文件内容。

**逆向工程价值：** 极高。解码的目的是阅读代码，但解码后用户/AI 面对数千个 smali 文件无从下手。

**需要增强：** 新增 CLI 命令 `smali-read`（读取指定 smali 文件内容）和 `smali-search`（在解码后的 smali 中搜索模式）。

**工作流设计：**
```
输入: 已解码的 APK 目录
Step 1: 定位目标 smali 文件（通过 class-list → 类名 → 文件路径映射）
Step 2: smali-read → 读取文件内容（支持行号范围）
Step 3: smali-search → 在 smali 中搜索:
  - 方法调用模式（invoke-*, sget/sput）
  - 字符串引用（const-string）
  - 类引用（const-class）
Step 4: 交叉引用分析（方法 X 在哪里被调用）
输出: 可读的 smali 内容 + 交叉引用图
```

---

#### 2.7 风险修复建议（security-remediation）

**现状：** `security-audit` 输出 findings 和 riskScore，但没有"如何修复"的指导。

**逆向工程价值：** 高。安全审计的价值不在于发现问题，而在于指导修复。

**需要增强：** 在 SecurityReport 中为每个 finding 添加 remediation 字段。

**工作流设计：**
```
输入: APK 文件 + security 审计结果
Step 1: security → 获取 findings + riskScore
Step 2: 对每个 finding 生成修复建议:
  - debuggable=true → 移除 android:debuggable 或设为 false
  - allowBackup=true → 设为 false
  - usesCleartextTraffic=true → 添加 network_security_config.xml
  - dangerous permission → 检查是否必要，考虑运行时权限
  - unprotected exported component → 添加 permission 或设 exported=false
Step 3: 计算修复后预期 riskScore
Step 4: 生成修复优先级排序（按 riskScore 贡献排序）
输出: 修复建议清单 + 修复后预期 riskScore + 优先级排序
```

---

#### 2.8 语义级 Diff（semantic-diff）

**现状：** `compare` Skill 使用 `diff` 命令，但只做表面级对比（权限增删、组件增删）。无法回答"代码逻辑有什么变化"。

**逆向工程价值：** 中高。版本对比的核心问题是行为变化，不只是元数据变化。

**需要增强：** 新增 `smali-diff` 命令，对比两个解码目录的 smali 差异。

**工作流设计：**
```
输入: 两个 APK 文件
Step 1: diff → 元数据级对比（权限、组件、版本）
Step 2: 两个 APK 都 decode → 解码
Step 3: smali-diff → 对比 smali 文件差异:
  - 新增/删除/修改的方法
  - 新增/删除的类
  - 字符串常量变化
  - 方法调用变化
Step 4: 安全影响分析:
  - 新增权限 → 权限提升？
  - 新增 exported component → 攻击面扩大？
  - 网络端点变化 → 新的 C2？
Step 5: 生成变更影响报告
输出: 元数据 diff + 代码 diff + 安全影响分析
```

---

### 第三层：引擎缺失能力，需要新功能开发 + 新 Skill（高难度）

#### 2.9 运行时/动态分析集成（runtime-analysis）

**现状：** 所有 11 个 Skill 都是纯静态分析。没有任何运行时能力（Frida hook、调试、流量拦截）。

**逆向工程价值：** 极高。静态分析只能看到代码，运行时分析才能看到行为。混淆代码的静态分析几乎无用，必须动态分析。

**需要新增：** 与 Frida/Xposed/adb 的集成。这不是在 Java 引擎中实现，而是作为 Skill 编排外部工具。

**工作流设计：**
```
输入: APK 文件 + 设备/模拟器
Step 1: 检查环境（adb devices, frida --version）
Step 2: 安装 APK 到设备
Step 3: 生成 Frida 脚本:
  - 基于静态分析结果（method-search 找到的方法）
  - Hook 关键方法（加密、网络、文件 IO）
Step 4: 运行 + 收集运行时数据:
  - 方法调用 trace
  - 参数/返回值
  - 网络请求/响应
  - 文件读写
Step 5: 合并静态 + 动态分析结果
输出: 静态分析 + 运行时行为数据
```

---

#### 2.10 应用克隆/重打包检测（clone-detect）

**现状：** 没有任何 Skill 检测应用是否被重新打包。这是 Android 恶意软件的常见手法。

**逆向工程价值：** 高。重打包检测是恶意软件分析和应用完整性验证的核心。

**需要新增：** 签名对比 + 代码相似度 + 资源相似度 + 元数据异常检测。

**工作流设计：**
```
输入: APK 文件（+ 可选的原始 APK）
Step 1: signing → 检查证书:
  - 自签名？（subject == issuer）
  - debug 签名？
  - 证书有效期异常？
Step 2: manifest → 检查重打包痕迹:
  - package name 与应用名不匹配
  - 多个 <application> 标签
  - 原始签名在 meta-data 中
Step 3: file-list + file-hash → 检查:
  - 额外注入的文件（classes2.dex 不在原始构建中）
  - 修改过的资源文件
Step 4: class-list → 检查:
  - 重打包工具特征类（com.secneo.*, com.stub.*）
  - 壳/packer 特征
输出: 重打包置信度 + 证据清单
```

---

#### 2.11 Split APK 支持（split-apk-analysis）

**现状：** TODO.md 明确记录"Split APK Support — Currently assumes single APK"。引擎不支持 split APKs。

**逆向工程价值：** 中。现代 Android 应用普遍使用 App Bundle / Split APK，不支持 split 意味着无法完整分析大量应用。

---

#### 2.12 置信度评分系统（confidence-scoring）

**现状：** `malware-hunt` 列出指标但没有置信度。`security-audit` 有 riskScore 但没有信心度。所有发现都是"是/否"而非"可能是/可能否"。

**逆向工程价值：** 中。减少误报是安全工具可用性的关键。

**需要新增：** 为每个 finding 添加 confidence 字段（0.0-1.0），基于指标特异性和上下文计算。

---

## 3. 现有 Skills 的增强建议（不新增 Skill，改进现有）

| Skill | 增强点 | 具体改进 |
|-------|--------|---------|
| security-audit | 添加修复建议 | 每个 finding 附加 remediation 字段 |
| security-audit | 使用 `ai -a context` | Step 6 改为 context + security-review 组合 |
| malware-hunt | 添加置信度评分 | 每个指标附加 confidence + 权重，综合为恶意置信度 |
| malware-hunt | 添加重打包检测 | 集成 clone-detect 的检查项 |
| compare | 添加安全影响分析 | diff 后自动判断权限提升/攻击面扩大 |
| reverse | 添加 smali 导航 | decode 后提供文件定位和阅读指导 |
| dex-deep-dive | 添加反混淆指导 | 检测到混淆时建议分析策略 |
| network-analysis | 添加动态 URL 检测 | 搜索字符串拼接模式（StringBuilder + URL） |
| decode-build | 添加重签名步骤 | build 后自动生成签名命令 |
| signing-verify | 添加证书库比对 | 支持与已知证书指纹库对比 |

---

## 4. 补全优先级排序

### P0 — 立即可做（只需写 SKILL.md，无需改代码）

| # | Skill | 价值 | 工作量 | 依赖 |
|---|-------|------|--------|------|
| 1 | **data-leakage-detect** | 高 | 1-2h | 无 — 纯命令组合 |
| 2 | **third-party-audit** | 高 | 1-2h | 无 — 纯命令组合 |
| 3 | **obfuscation-detect** | 中高 | 1h | 无 — 纯命令组合 |
| 4 | **attack-surface-model** | 高 | 2-3h | 无 — 纯命令组合 |
| 5 | **ai-context** | 中 | 0.5h | 无 — 已有 `ai -a context` |

### P1 — 短期可做（需要少量代码增强 + SKILL.md）

| # | Skill | 价值 | 工作量 | 依赖 |
|---|-------|------|--------|------|
| 6 | **smali-navigator** | 极高 | 2-3d | 新增 `smali-read`/`smali-search` 命令 |
| 7 | **security-remediation** | 高 | 1-2d | SecurityReport 添加 remediation 字段 |
| 8 | **semantic-diff** | 中高 | 2-3d | 新增 `smali-diff` 命令 |

### P2 — 中期目标（需要新功能开发 + SKILL.md）

| # | Skill | 价值 | 工作量 | 依赖 |
|---|-------|------|--------|------|
| 9 | **clone-detect** | 高 | 3-5d | 代码相似度算法 |
| 10 | **confidence-scoring** | 中 | 2-3d | SecurityReport 架构调整 |
| 11 | **split-apk-analysis** | 中 | 5-10d | 引擎核心改造 |

### P3 — 长期目标（跨系统集成）

| # | Skill | 价值 | 工作量 | 依赖 |
|---|-------|------|--------|------|
| 12 | **runtime-analysis** | 极高 | 5-10d | Frida/Xposed/adb 环境集成 |

---

## 5. 跨 Skill 一致性问题（现有缺陷）

| 问题 | 影响 | 修复 |
|------|------|------|
| `ai -a context` 从未被任何 Skill 使用 | AI-Apktool 的核心差异化能力被浪费 | 在 security-audit、reverse、quick-analysis 中集成 |
| 所有 Skill 都是纯静态分析 | 混淆代码分析效果差 | P3 补 runtime-analysis |
| decode 后无 smali 导航 | 解码后用户/AI 无从下手 | P1 补 smali-navigator |
| riskScore 无修复建议 | 发现问题但不知道如何修 | P1 补 security-remediation |
| malware-hunt 无置信度 | 误报多，无法判断严重程度 | P2 补 confidence-scoring |
| compare 无安全影响分析 | 只看到变化，不知道变化是否危险 | 增强 compare Skill |

---

## 6. 行动建议

**立即行动（本周）：** 完成 P0 的 5 个 SKILL.md 编写。这些只需组合现有命令，不需要改任何 Java 代码，但能立即填补最大的能力缺口。

**短期行动（2 周内）：** 完成 P1 的 3 个增强。其中 `smali-navigator` 影响最大 — 它是连接"解码"和"理解代码"的桥梁，没有它，reverse 和 decode-build 两个 Skill 的价值减半。

**中期行动（1 月内）：** 完成 P2 的 3 个新功能。`clone-detect` 与 `malware-hunt` 配合使用，能显著提升恶意软件分析能力。

**长期行动：** `runtime-analysis` 是终极目标。纯静态分析的天花板很低 — 混淆、加壳、动态加载都只能靠运行时分析突破。建议先以 Skill 编排 Frida 的方式实现，不修改 Java 引擎。