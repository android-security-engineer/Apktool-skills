# CLI 命令完整参考

本文档详细说明 AI-Apktool 的所有命令行命令，包括用法、选项和输出格式。AI-Apktool 在原版 Apktool 的基础上新增了 APK 分析、搜索、对比、HTTP 服务和 AI 集成等命令。

---

## 目录

- [通用选项](#通用选项)
- [核心命令](#核心命令)
  - [decode](#decode)
  - [build](#build)
  - [install-framework](#install-framework)
  - [clean-frameworks](#clean-frameworks)
  - [list-frameworks](#list-frameworks)
  - [publicize-resources](#publicize-resources)
- [分析命令 (JSON 输出)](#分析命令)
  - [info](#info)
  - [manifest](#manifest)
  - [permissions](#permissions)
  - [activities](#activities)
  - [services](#services)
  - [receivers](#receivers)
  - [providers](#providers)
  - [components](#components)
  - [sdk-info](#sdk-info)
  - [resources](#resources)
  - [security](#security)
  - [api-surface](#api-surface)
  - [structure](#structure)
- [搜索命令](#搜索命令)
  - [search](#search)
- [对比命令](#对比命令)
  - [diff](#diff)
- [服务命令](#服务命令)
  - [serve](#serve)
- [AI 命令](#ai-命令)
  - [ai](#ai)
- [通用命令](#通用命令)
  - [help](#help)
  - [version](#version)

---

## 通用选项

以下选项可用于所有命令：

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--verbose` | `-v` | 增加输出详细程度 |
| `--quiet` | `-q` | 抑制正常输出 |

---

## 核心命令

### decode

将 APK 文件解码为目录结构，包含 smali 代码、资源文件和 AndroidManifest.xml。

**用法**:

```bash
apktool d|decode [options] <apk-file>
```

**选项**:

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--force` | `-f` | 强制删除目标目录 |
| `--no-src` | `-s` | 不解码源代码 (DEX -> smali) |
| `--no-res` | `-r` | 不解码资源文件 |
| `--all-src` | `-a` | 解码所有源代码（包括非标准 DEX 文件） |
| `--output <dir>` | `-o` | 指定输出目录（默认: 去掉 .apk 后缀的目录名） |
| `--frame-path <dir>` | `-p` | 指定 framework 文件目录 |
| `--frame-tag <tag>` | `-t` | 使用带有特定标签的 framework 文件 |
| `--jobs <num>` | `-j` | 设置并行任务数 |
| `--lib <package:file>` | `-l` | 指定共享库（可多次使用） |
| `--no-debug-info` | | 不包含调试信息（.local、.param、.line 等） |
| `--only-manifest` | | 仅解码 AndroidManifest.xml，不处理资源 |
| `--no-assets` | | 不解码 assets 目录 |
| `--keep-broken-res` | | 保留损坏的资源（需手动修复后才能重新构建） |
| `--ignore-raw-values` | | 忽略 XML 资源文件中的原始属性值 |
| `--match-original` | | 保持文件尽可能接近原始格式（将无法重新构建） |
| `--res-resolve-mode <mode>` | | 资源解析模式：`default`、`greedy` 或 `lazy` |

**示例**:

```bash
# 基本解码
apktool d app.apk

# 指定输出目录
apktool d app.apk -o output_dir

# 只解码资源，不解码源代码
apktool d app.apk -s

# 只解码 Manifest，不解码其他资源
apktool d app.apk --only-manifest

# 使用贪婪模式解析资源
apktool d app.apk --res-resolve-mode greedy

# 使用特定 framework 解码
apktool d app.apk -p /path/to/frameworks -t samsung
```

---

### build

从已解码的目录构建 APK 文件。

**用法**:

```bash
apktool b|build [options] <apk-dir>
```

**选项**:

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--force` | `-f` | 跳过变更检测，构建所有文件 |
| `--output <file>` | `-o` | 指定输出 APK 文件路径（默认: `dist/<name>.apk`） |
| `--frame-path <dir>` | `-p` | 指定 framework 文件目录 |
| `--jobs <num>` | `-j` | 设置并行任务数 |
| `--lib <package:file>` | `-l` | 指定共享库 |
| `--no-apk` | | 仅编译不打包（不生成 APK 文件） |
| `--no-crunch` | | 禁用资源文件压缩 |
| `--copy-original` | | 复制原始 AndroidManifest.xml 和 META-INF |
| `--debuggable` | | 在 Manifest 中设置 `android:debuggable="true"` |
| `--net-sec-conf` | | 添加通用网络安全配置文件 |
| `--aapt <file>` | | 指定 aapt2 二进制文件路径 |

**示例**:

```bash
# 基本构建
apktool b app_dir

# 指定输出文件
apktool b app_dir -o custom_output.apk

# 构建可调试的 APK
apktool b app_dir --debuggable

# 使用自定义 aapt2
apktool b app_dir --aapt /path/to/aapt2
```

---

### install-framework

将 framework APK 安装到本地 framework 目录，供后续解码使用。

**用法**:

```bash
apktool if|install-framework [options] <apk-file>
```

**选项**:

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--frame-path <dir>` | `-p` | 指定 framework 文件安装目录 |
| `--frame-tag <tag>` | `-t` | 为 framework 文件添加标签后缀 |

**示例**:

```bash
# 安装标准 framework
apktool if framework-res.apk

# 安装带标签的 framework（用于厂商 ROM）
apktool if samsung_framework.apk -t samsung
apktool if miui_framework.apk -t miui
```

---

### clean-frameworks

清除已安装的 framework 文件。

**用法**:

```bash
apktool cf|clean-frameworks [options]
```

**选项**:

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--all` | `-a` | 清除所有 framework 文件（忽略标签过滤） |
| `--frame-path <dir>` | `-p` | 指定 framework 文件目录 |
| `--frame-tag <tag>` | `-t` | 只清除带有特定标签的 framework |

**示例**:

```bash
# 清除所有 framework
apktool cf -a

# 清除特定标签的 framework
apktool cf -t samsung
```

---

### list-frameworks

列出已安装的 framework 文件。

**用法**:

```bash
apktool lf|list-frameworks [options]
```

**选项**:

| 选项 | 短选项 | 说明 |
|------|--------|------|
| `--all` | `-a` | 列出所有 framework 文件（忽略标签过滤） |
| `--frame-path <dir>` | `-p` | 指定 framework 文件目录 |
| `--frame-tag <tag>` | `-t` | 只列出带有特定标签的 framework |

**示例**:

```bash
# 列出所有 framework
apktool lf -a
```

---

### publicize-resources

将 ARSC 文件中的所有资源设为 public。

**用法**:

```bash
apktool pr|publicize-resources <arsc-file>
```

**示例**:

```bash
apktool pr resources.arsc
```

---

## 分析命令

分析命令以 JSON 格式输出结果，适合程序化处理和 AI 集成。

### info

获取 APK 元数据概要信息。

**用法**:

```bash
apktool info <apk-file>
```

**JSON 输出示例**:

```json
{
  "fileName": "app.apk",
  "fileSize": 15728640,
  "packageName": "com.example.app",
  "versionName": "2.1.0",
  "versionCode": 42,
  "minSdkVersion": "24",
  "targetSdkVersion": "34",
  "dexCount": 3,
  "hasResources": true,
  "hasAssets": true,
  "hasNativeLibs": true,
  "architectures": ["arm64-v8a", "armeabi-v7a"],
  "permissionCount": 12,
  "activityCount": 8,
  "serviceCount": 3,
  "receiverCount": 2,
  "providerCount": 1
}
```

**输出字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `fileName` | String | APK 文件名 |
| `fileSize` | long | 文件大小（字节） |
| `packageName` | String | 应用包名 |
| `versionName` | String | 版本名称 |
| `versionCode` | int | 版本号 |
| `minSdkVersion` | String | 最低 SDK 版本 |
| `targetSdkVersion` | String | 目标 SDK 版本 |
| `dexCount` | int | DEX 文件数量 |
| `hasResources` | boolean | 是否包含 resources.arsc |
| `hasAssets` | boolean | 是否包含 assets 目录 |
| `hasNativeLibs` | boolean | 是否包含原生库 |
| `architectures` | List<String> | 支持的 CPU 架构列表 |
| `permissionCount` | int | 权限数量 |
| `activityCount` | int | Activity 数量 |
| `serviceCount` | int | Service 数量 |
| `receiverCount` | int | BroadcastReceiver 数量 |
| `providerCount` | int | ContentProvider 数量 |

---

### manifest

获取解码后的 AndroidManifest.xml 结构化 JSON 数据。

**用法**:

```bash
apktool manifest <apk-file>
```

**JSON 输出示例**:

```json
{
  "packageName": "com.example.app",
  "versionName": "2.1.0",
  "versionCode": 42,
  "minSdkVersion": "24",
  "targetSdkVersion": "34",
  "permissions": [
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.CAMERA"
  ],
  "activities": [
    {
      "name": "com.example.app.MainActivity",
      "type": "activity",
      "exported": true,
      "intentFilters": ["android.intent.action.MAIN"],
      "permissions": []
    }
  ],
  "services": [
    {
      "name": "com.example.app.BackgroundService",
      "type": "service",
      "exported": false,
      "intentFilters": [],
      "permissions": []
    }
  ],
  "receivers": [],
  "providers": [],
  "usesLibraries": [],
  "debuggable": false,
  "allowBackup": true
}
```

---

### permissions

获取权限列表。

**用法**:

```bash
apktool permissions <apk-file>
```

**JSON 输出示例**:

```json
[
  "android.permission.INTERNET",
  "android.permission.ACCESS_NETWORK_STATE",
  "android.permission.CAMERA",
  "android.permission.READ_EXTERNAL_STORAGE",
  "android.permission.WRITE_EXTERNAL_STORAGE"
]
```

---

### activities

获取所有 Activity 组件列表。

**用法**:

```bash
apktool activities <apk-file>
```

**JSON 输出示例**:

```json
[
  {
    "name": "com.example.app.MainActivity",
    "type": "activity",
    "exported": true,
    "intentFilters": ["android.intent.action.MAIN"],
    "permissions": []
  },
  {
    "name": "com.example.app.SettingsActivity",
    "type": "activity",
    "exported": false,
    "intentFilters": [],
    "permissions": []
  }
]
```

---

### services

获取所有 Service 组件列表。

**用法**:

```bash
apktool services <apk-file>
```

**JSON 输出示例**:

```json
[
  {
    "name": "com.example.app.DataSyncService",
    "type": "service",
    "exported": false,
    "intentFilters": [],
    "permissions": []
  }
]
```

---

### receivers

获取所有 BroadcastReceiver 组件列表。

**用法**:

```bash
apktool receivers <apk-file>
```

**JSON 输出示例**:

```json
[
  {
    "name": "com.example.app.BootReceiver",
    "type": "receiver",
    "exported": true,
    "intentFilters": ["android.intent.action.BOOT_COMPLETED"],
    "permissions": []
  }
]
```

---

### providers

获取所有 ContentProvider 组件列表。

**用法**:

```bash
apktool providers <apk-file>
```

**JSON 输出示例**:

```json
[
  {
    "name": "com.example.app.DataProvider",
    "type": "provider",
    "exported": true,
    "intentFilters": [],
    "permissions": ["com.example.app.READ_DATA"]
  }
]
```

---

### components

在一条命令中获取所有组件。

**用法**:

```bash
apktool components <apk-file>
```

**JSON 输出示例**:

```json
{
  "activities": [
    {
      "name": "com.example.app.MainActivity",
      "type": "activity",
      "exported": true,
      "intentFilters": [],
      "permissions": []
    }
  ],
  "services": [],
  "receivers": [
    {
      "name": "com.example.app.BootReceiver",
      "type": "receiver",
      "exported": true,
      "intentFilters": [],
      "permissions": []
    }
  ],
  "providers": []
}
```

---

### sdk-info

获取 SDK 版本要求信息。

**用法**:

```bash
apktool sdk-info <apk-file>
```

**JSON 输出示例**:

```json
{
  "minSdkVersion": "24",
  "targetSdkVersion": "34",
  "maxSdkVersion": "35"
}
```

---

### resources

获取资源表概要信息。

**用法**:

```bash
apktool resources <apk-file>
```

**JSON 输出示例**:

```json
{
  "packageName": "com.example.app",
  "packageId": 127,
  "typeCounts": {
    "anim": 12,
    "drawable": 156,
    "layout": 34,
    "string": 289,
    "color": 18,
    "dimen": 45,
    "style": 23,
    "bool": 7,
    "integer": 4,
    "array": 11,
    "menu": 8,
    "raw": 3,
    "xml": 5,
    "id": 67
  },
  "locales": [],
  "totalEntries": 682
}
```

**输出字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `packageName` | String | 资源包名 |
| `packageId` | int | 资源包 ID |
| `typeCounts` | Map<String, Integer> | 各资源类型的数量统计 |
| `locales` | List<String> | 支持的语言区域列表 |
| `totalEntries` | int | 总资源条目数 |

---

### security

获取安全分析报告，包含风险评估分数。

**用法**:

```bash
apktool security <apk-file>
```

**JSON 输出示例**:

```json
{
  "dangerousPermissions": [
    "android.permission.CAMERA",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.ACCESS_FINE_LOCATION"
  ],
  "highRiskComponents": [
    "activity: com.example.app.MainActivity",
    "receiver: com.example.app.BootReceiver"
  ],
  "debuggable": false,
  "allowBackup": true,
  "usesCleartextTraffic": false,
  "findings": [
    "MEDIUM: Application allows backup - android:allowBackup=true",
    "MEDIUM: Application requests 3 dangerous permissions",
    "HIGH: 2 exported components without permission protection"
  ],
  "riskScore": 35
}
```

**风险分数计算规则**:

| 因素 | 分值 |
|------|------|
| `debuggable=true` | +30 |
| `allowBackup=true` | +10 |
| 每个危险权限 | +2（最多 +20） |
| 每个无权限保护的导出组件 | +5（最多 +30） |
| 总分上限 | 100 |

**危险权限列表**:

安全报告检测以下已知的高危权限：

- `READ_CONTACTS`, `WRITE_CONTACTS`
- `READ_CALENDAR`, `WRITE_CALENDAR`
- `READ_CALL_LOG`, `WRITE_CALL_LOG`
- `READ_PHONE_STATE`, `CALL_PHONE`
- `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`
- `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- `CAMERA`, `RECORD_AUDIO`
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`
- `READ_PHONE_NUMBERS`, `ANSWER_PHONE_CALLS`
- `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`

---

### api-surface

获取所有导出组件及其 intent-filter，即应用的公开 API 攻击面。

**用法**:

```bash
apktool api-surface <apk-file>
```

**JSON 输出示例**:

```json
{
  "exportedActivities": [
    {
      "name": "com.example.app.MainActivity",
      "type": "activity",
      "exported": true,
      "intentFilters": ["android.intent.action.MAIN"],
      "permissions": []
    }
  ],
  "exportedServices": [],
  "exportedReceivers": [
    {
      "name": "com.example.app.BootReceiver",
      "type": "receiver",
      "exported": true,
      "intentFilters": ["android.intent.action.BOOT_COMPLETED"],
      "permissions": []
    }
  ],
  "exportedProviders": [],
  "intentFilters": [
    {
      "component": "com.example.app.MainActivity",
      "componentType": "activity",
      "actions": ["android.intent.action.MAIN"],
      "categories": [],
      "dataSchemes": []
    },
    {
      "component": "com.example.app.BootReceiver",
      "componentType": "receiver",
      "actions": ["android.intent.action.BOOT_COMPLETED"],
      "categories": [],
      "dataSchemes": []
    }
  ],
  "totalExportedComponents": 2
}
```

---

### structure

获取代码结构概览。

**用法**:

```bash
apktool structure <apk-file>
```

**JSON 输出示例**:

```json
{
  "totalClasses": 0,
  "totalMethods": 0,
  "totalFields": 0,
  "packageCounts": {},
  "topClasses": [],
  "dexCount": 2,
  "dexClassCounts": {}
}
```

**注意**: `structure` 命令当前仅返回 `dexCount`，其余字段为占位值。

---

## 搜索命令

### search

在 APK 中搜索字符串、类名或方法名。支持正则表达式模式匹配。

**用法**:

```bash
apktool search <apk-file> [pattern] -t <type>
```

**选项**:

| 选项 | 短选项 | 说明 | 默认值 |
|------|--------|------|--------|
| `--type <type>` | `-t` | 搜索类型：`strings`、`classes` 或 `methods` | `classes` |

**参数**:

| 参数 | 必需 | 说明 |
|------|------|------|
| `<apk-file>` | 是 | APK 文件路径 |
| `[pattern]` | 否 | 正则表达式模式（默认: `.*` 匹配全部） |

**搜索类型说明**:

| 类型 | 搜索范围 | 说明 |
|------|---------|------|
| `classes` | DEX 文件中的类名 | 使用 dalvik 类型格式的类名进行匹配（如 `Lcom/example/MyClass;`） |
| `methods` | DEX 文件中的方法名 | 匹配方法名称（如 `onCreate`） |
| `strings` | 资源表中的字符串值 | 匹配资源字符串的值 |

**示例**:

```bash
# 搜索包含 "Activity" 的类名
apktool search app.apk "Activity" -t classes

# 搜索匹配 URL 模式的字符串
apktool search app.apk "http.*" -t strings

# 搜索名为 onCreate 的方法
apktool search app.apk "onCreate" -t methods

# 列出所有类名
apktool search app.apk -t classes
```

**JSON 输出示例**:

```json
{
  "query": "Activity",
  "type": "classes",
  "totalMatches": 5,
  "matches": [
    {
      "name": "com.example.app.MainActivity",
      "value": "Lcom/example/app/MainActivity;",
      "source": "classes.dex"
    },
    {
      "name": "com.example.app.SettingsActivity",
      "value": "Lcom/example/app/SettingsActivity;",
      "source": "classes.dex"
    },
    {
      "name": "android.app.Activity",
      "value": "Landroid/app/Activity;",
      "source": "classes.dex"
    }
  ]
}
```

**输出字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `query` | String | 使用的搜索模式 |
| `type` | String | 搜索类型 |
| `totalMatches` | int | 匹配结果总数 |
| `matches[].name` | String | 可读名称（类名使用点分格式） |
| `matches[].value` | String | 原始值（类名为 dalvik 格式） |
| `matches[].source` | String | 来源（DEX 文件名或 "resources"） |

---

## 对比命令

### diff

对比两个 APK 文件的差异。

**用法**:

```bash
apktool diff <apk1> <apk2>
```

**参数**:

| 参数 | 必需 | 说明 |
|------|------|------|
| `<apk1>` | 是 | 旧版 APK 文件路径 |
| `<apk2>` | 是 | 新版 APK 文件路径 |

**示例**:

```bash
apktool diff app_v1.apk app_v2.apk
```

**JSON 输出示例**:

```json
{
  "addedPermissions": [
    "android.permission.BLUETOOTH_CONNECT"
  ],
  "removedPermissions": [],
  "addedActivities": [
    "com.example.app.NewFeatureActivity"
  ],
  "removedActivities": [],
  "addedServices": [
    "com.example.app.SyncService"
  ],
  "removedServices": [],
  "addedDexFiles": [],
  "removedDexFiles": [],
  "addedNativeLibs": [],
  "removedNativeLibs": [],
  "versionCodeChange": "41 -> 42",
  "versionNameChange": "2.0.0 -> 2.1.0",
  "targetSdkChange": "33 -> 34"
}
```

**对比范围**:

| 对比项 | 说明 |
|--------|------|
| 权限变化 | 新增/移除的 `<uses-permission>` |
| Activity 变化 | 新增/移除的 Activity |
| Service 变化 | 新增/移除的 Service |
| 版本号变化 | versionCode、versionName 变化 |
| 目标 SDK 变化 | targetSdkVersion 变化 |
| DEX 文件变化 | 新增/移除的 DEX 文件 |
| 原生库变化 | 新增/移除的原生库 |

---

## 服务命令

### serve

启动 HTTP API 服务器，供 AI 代理和外部工具通过 REST API 访问分析功能。

**用法**:

```bash
apktool serve [-p <port>]
```

**选项**:

| 选项 | 短选项 | 说明 | 默认值 |
|------|--------|------|--------|
| `--port <port>` | `-p` | 服务端口 | `8080` |

**示例**:

```bash
# 使用默认端口启动
apktool serve

# 指定端口启动
apktool serve -p 9090
```

**可用 API 端点**:

| 端点 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/v1/health` | GET | 无 | 健康检查 |
| `/api/v1/info` | GET | `apk` | APK 概要信息 |
| `/api/v1/manifest` | GET | `apk` | Manifest 解析结果 |
| `/api/v1/permissions` | GET | `apk` | 权限列表 |
| `/api/v1/security` | GET | `apk` | 安全分析报告 |
| `/api/v1/resources` | GET | `apk` | 资源表概要 |
| `/api/v1/search` | GET | `apk`, `type`, `pattern` | 搜索内容 |
| `/api/v1/diff` | GET | `apk1`, `apk2` | APK 对比 |

**API 调用示例**:

```bash
# 获取 APK 概要
curl "http://localhost:8080/api/v1/info?apk=/path/to/app.apk"

# 搜索类名
curl "http://localhost:8080/api/v1/search?apk=/path/to/app.apk&type=classes&pattern=Activity"

# 对比两个 APK
curl "http://localhost:8080/api/v1/diff?apk1=/path/to/v1.apk&apk2=/path/to/v2.apk"

# 安全分析
curl "http://localhost:8080/api/v1/security?apk=/path/to/app.apk"
```

**健康检查响应**:

```json
{"status":"ok"}
```

**错误响应格式**:

```json
{"error":"Missing required parameter: apk"}
```

---

## AI 命令

### ai

生成 LLM 可用的分析提示词 (prompt)，将 APK 分析结果转化为 LLM 可以理解的自然语言输入。

**用法**:

```bash
apktool ai <apk-file> -a <action>
```

**选项**:

| 选项 | 短选项 | 说明 | 默认值 |
|------|--------|------|--------|
| `--action <action>` | `-a` | AI 动作：`explain`、`security-review` 或 `summarize` | `explain` |

**参数**:

| 参数 | 必需 | 说明 |
|------|------|------|
| `<apk-file>` | 是 | APK 文件路径 |

**动作说明**:

| 动作 | 说明 |
|------|------|
| `explain` | 生成应用功能分析提示词，包含权限、组件和安全分析概览 |
| `security-review` | 生成安全审查提示词，重点关注漏洞、隐私和攻击面 |
| `summarize` | 生成简短技术概要提示词，3-5 句话总结应用特征 |

**示例**:

```bash
# 生成功能分析提示词
apktool ai app.apk

# 生成安全审查提示词
apktool ai app.apk -a security-review

# 生成概要提示词
apktool ai app.apk -a summarize
```

**explain 输出示例**:

```
Analyze the following Android application and explain its main functionality:

APK File: app.apk
Package: com.example.app

Permissions:
- android.permission.INTERNET
- android.permission.CAMERA
- android.permission.ACCESS_FINE_LOCATION

Components:
- Activity: com.example.app.MainActivity
- Activity: com.example.app.CameraActivity
- Service: com.example.app.LocationService

Security Analysis:
{... security report JSON ...}

Please provide:
1. A summary of what this application does
2. Key features based on components and permissions
3. Security concerns
```

---

## 通用命令

### help

显示帮助信息。

**用法**:

```bash
apktool h|help [--format=json]
```

**选项**:

| 选项 | 说明 |
|------|------|
| `--format=json` | 输出机器可读的 JSON 命令目录 |

**JSON 目录输出**:

使用 `--format=json` 时，输出包含所有命令的完整目录：

```bash
apktool help --format=json
```

输出的 JSON 结构包含 `tool`、`version`、`description` 和 `commands` 数组，每个命令条目包含 `name`、`shortName`、`description`、`usage`、`outputFormat`、`category` 和 `examples` 字段。

**命令分类**:

| 分类 | 说明 |
|------|------|
| `core` | 核心 decode/build/framework 命令 |
| `analysis` | APK 分析命令 |
| `search` | 搜索命令 |
| `service` | HTTP 服务命令 |
| `ai` | AI 集成命令 |
| `general` | help/version 命令 |

---

### version

显示版本信息。

**用法**:

```bash
apktool v|version
```

**示例**:

```bash
apktool version
# 输出: 2.9.3
```

---

## 命令速查表

| 命令 | 短名 | 类别 | 输出格式 | 说明 |
|------|------|------|---------|------|
| `decode` | `d` | core | 目录 | 解码 APK |
| `build` | `b` | core | APK | 构建 APK |
| `install-framework` | `if` | core | 文本 | 安装 framework |
| `clean-frameworks` | `cf` | core | 文本 | 清除 framework |
| `list-frameworks` | `lf` | core | 文本 | 列出 framework |
| `publicize-resources` | `pr` | core | 文件 | 公开化资源 |
| `info` | - | analysis | JSON | APK 概要 |
| `manifest` | - | analysis | JSON | Manifest 解析 |
| `permissions` | - | analysis | JSON | 权限列表 |
| `activities` | - | analysis | JSON | Activity 列表 |
| `services` | - | analysis | JSON | Service 列表 |
| `receivers` | - | analysis | JSON | Receiver 列表 |
| `providers` | - | analysis | JSON | Provider 列表 |
| `components` | - | analysis | JSON | 全部组件 |
| `sdk-info` | - | analysis | JSON | SDK 版本 |
| `resources` | - | analysis | JSON | 资源概要 |
| `security` | - | analysis | JSON | 安全报告 |
| `api-surface` | - | analysis | JSON | API 攻击面 |
| `structure` | - | analysis | JSON | 代码结构 |
| `search` | - | search | JSON | 搜索内容 |
| `diff` | - | analysis | JSON | APK 对比 |
| `serve` | - | service | HTTP | 启动 API 服务 |
| `ai` | - | ai | 文本 | 生成 AI 提示词 |
| `help` | `h` | general | 文本/JSON | 帮助信息 |
| `version` | `v` | general | 文本 | 版本信息 |
