# ApkAnalyzer 核心 API

> 包路径: `brut.androlib.analyze.ApkAnalyzer`

ApkAnalyzer 是 AI-Apktool 的核心分析引擎，提供对 APK 文件的全方位分析能力，包括元数据摘要、Manifest 解析、安全审计、资源统计和攻击面分析等功能。

---

## 构造 ApkAnalyzer

### 构造函数

```java
public ApkAnalyzer(File apkFile, Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `java.io.File` | 待分析的 APK 文件 |
| `config` | `brut.androlib.Config` | Apktool 配置对象 |

### 创建 Config 对象

对于纯分析场景，可以使用任意字符串作为版本号来创建 Config：

```java
import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import java.io.File;

// 创建 Config（分析场景下版本号无关紧要）
Config config = new Config("2.9.3");

// 构造 ApkAnalyzer
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
```

Config 还支持更多选项，以下是常用的分析相关配置：

```java
Config config = new Config("2.9.3");
config.setAnalysisMode(true);              // 启用分析模式
config.setVerbose(true);                   // 详细日志输出
config.setFrameworkDirectory("/path/to/fw"); // 指定 framework 目录
config.setDecodeSources(Config.DecodeSources.NONE); // 不反编译源码，加速分析
```

---

## API 方法总览

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getSummary()` | `ApkSummary` | APK 元数据摘要 |
| `getManifestInfo()` | `ManifestInfo` | Manifest 详细信息 |
| `getSecurityReport()` | `SecurityReport` | 安全分析报告 |
| `getResourceSummary()` | `ResourceSummary` | 资源表摘要 |
| `getApiSurface()` | `ApiSurfaceInfo` | 攻击面分析（导出组件与 Intent Filter） |
| `getAllComponents()` | `Map<String, List<ComponentInfo>>` | 按类型分组返回所有组件 |

> 所有方法均可能抛出 `brut.androlib.exceptions.AndrolibException`。

---

## getSummary() -- APK 元数据摘要

返回 APK 文件的基本元数据，包括文件信息、包名、版本、DEX 数量、资源/原生库检测以及各类组件计数。

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
ApkSummary summary = analyzer.getSummary();

System.out.println("文件名: " + summary.getFileName());
System.out.println("文件大小: " + summary.getFileSize() + " bytes");
System.out.println("包名: " + summary.getPackageName());
System.out.println("版本名: " + summary.getVersionName());
System.out.println("版本号: " + summary.getVersionCode());
System.out.println("最小 SDK: " + summary.getMinSdkVersion());
System.out.println("目标 SDK: " + summary.getTargetSdkVersion());
System.out.println("DEX 文件数: " + summary.getDexCount());
System.out.println("包含 resources.arsc: " + summary.isHasResources());
System.out.println("包含 assets 目录: " + summary.isHasAssets());
System.out.println("包含原生库: " + summary.isHasNativeLibs());
System.out.println("支持的架构: " + summary.getArchitectures());
System.out.println("权限数量: " + summary.getPermissionCount());
System.out.println("Activity 数量: " + summary.getActivityCount());
System.out.println("Service 数量: " + summary.getServiceCount());
System.out.println("Receiver 数量: " + summary.getReceiverCount());
System.out.println("Provider 数量: " + summary.getProviderCount());
```

输出 JSON 示例（通过 `JsonOutput.toJson()` 序列化）：

```json
{
  "fileName": "app.apk",
  "fileSize": 15728640,
  "packageName": "com.example.app",
  "versionName": "2.1.0",
  "versionCode": 210,
  "minSdkVersion": "21",
  "targetSdkVersion": "34",
  "dexCount": 2,
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

### 关键字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `fileName` | `String` | APK 文件名 |
| `fileSize` | `long` | 文件大小（字节） |
| `packageName` | `String` | 应用包名 |
| `versionName` | `String` | 用户可见的版本名 |
| `versionCode` | `int` | 内部版本号 |
| `minSdkVersion` | `String` | 最低支持 SDK 版本 |
| `targetSdkVersion` | `String` | 目标 SDK 版本 |
| `dexCount` | `int` | DEX 文件数量（classes.dex + classes2.dex + ...） |
| `hasResources` | `boolean` | 是否包含 resources.arsc |
| `hasAssets` | `boolean` | 是否包含 assets 目录 |
| `hasNativeLibs` | `boolean` | 是否包含原生库 |
| `architectures` | `List<String>` | 支持的 CPU 架构列表（如 arm64-v8a） |
| `permissionCount` | `int` | 声明的权限总数 |
| `activityCount` | `int` | Activity 组件数量 |
| `serviceCount` | `int` | Service 组件数量 |
| `receiverCount` | `int` | BroadcastReceiver 组件数量 |
| `providerCount` | `int` | ContentProvider 组件数量 |

---

## getManifestInfo() -- Manifest 详细信息

完整解析 AndroidManifest.xml，返回包名、版本、权限、四大组件列表、debuggable 标记等详细信息。

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
ManifestInfo manifest = analyzer.getManifestInfo();

// 基本信息
System.out.println("包名: " + manifest.getPackageName());
System.out.println("版本名: " + manifest.getVersionName());
System.out.println("版本号: " + manifest.getVersionCode());
System.out.println("最小 SDK: " + manifest.getMinSdkVersion());
System.out.println("目标 SDK: " + manifest.getTargetSdkVersion());
System.out.println("最大 SDK: " + manifest.getMaxSdkVersion());

// 安全标记
System.out.println("可调试: " + manifest.isDebuggable());
System.out.println("允许备份: " + manifest.isAllowBackup());
System.out.println("网络安全配置: " + manifest.getNetworkSecurityConfig());

// 权限列表
for (String perm : manifest.getPermissions()) {
    System.out.println("权限: " + perm);
}

// 四大组件
for (ComponentInfo activity : manifest.getActivities()) {
    System.out.println("Activity: " + activity.getName() + " (exported=" + activity.isExported() + ")");
}
for (ComponentInfo service : manifest.getServices()) {
    System.out.println("Service: " + service.getName() + " (exported=" + service.isExported() + ")");
}
for (ComponentInfo receiver : manifest.getReceivers()) {
    System.out.println("Receiver: " + receiver.getName() + " (exported=" + receiver.isExported() + ")");
}
for (ComponentInfo provider : manifest.getProviders()) {
    System.out.println("Provider: " + provider.getName() + " (exported=" + provider.isExported() + ")");
}

// 依赖库
for (String lib : manifest.getUsesLibraries()) {
    System.out.println("依赖库: " + lib);
}
```

> 注意：如果 APK 中不存在 AndroidManifest.xml，此方法返回 `null`。

输出 JSON 示例：

```json
{
  "packageName": "com.example.app",
  "versionName": "2.1.0",
  "versionCode": 210,
  "minSdkVersion": "21",
  "targetSdkVersion": "34",
  "maxSdkVersion": null,
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
  "services": [],
  "receivers": [],
  "providers": [],
  "usesLibraries": [],
  "debuggable": false,
  "allowBackup": true,
  "networkSecurityConfig": null
}
```

---

## getSecurityReport() -- 安全分析报告

基于 Manifest 信息进行安全分析，识别危险权限、高风险导出组件、可调试标记等安全问题，并计算风险评分（0-100）。

### 风险评分算法

| 风险因素 | 分值贡献 |
|----------|---------|
| `debuggable = true` | +30 |
| `allowBackup = true` | +10 |
| 每个危险权限 | +2（上限 20） |
| 每个无权限保护的导出组件 | +5（上限 30） |
| 总分上限 | 100 |

### 识别的危险权限

ApkAnalyzer 内置了以下危险权限列表（对应 Android 的 "dangerous" 保护级别）：

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

### 代码示例

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
SecurityReport report = analyzer.getSecurityReport();

System.out.println("风险评分: " + report.getRiskScore() + "/100");
System.out.println("可调试: " + report.isDebuggable());
System.out.println("允许备份: " + report.isAllowBackup());
System.out.println("使用明文流量: " + report.isUsesCleartextTraffic());

// 危险权限
System.out.println("危险权限 (" + report.getDangerousPermissions().size() + "):");
for (String perm : report.getDangerousPermissions()) {
    System.out.println("  - " + perm);
}

// 高风险组件（导出且无权限保护）
System.out.println("高风险组件 (" + report.getHighRiskComponents().size() + "):");
for (String comp : report.getHighRiskComponents()) {
    System.out.println("  - " + comp);
}

// 安全发现
System.out.println("安全发现:");
for (String finding : report.getFindings()) {
    System.out.println("  - " + finding);
}
```

输出 JSON 示例：

```json
{
  "dangerousPermissions": [
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION"
  ],
  "highRiskComponents": [
    "activity: com.example.app.ExportActivity",
    "service: com.example.app.ExportService"
  ],
  "debuggable": false,
  "allowBackup": true,
  "usesCleartextTraffic": false,
  "findings": [
    "MEDIUM: Application allows backup - android:allowBackup=true",
    "MEDIUM: Application requests 2 dangerous permissions",
    "HIGH: 2 exported components without permission protection"
  ],
  "riskScore": 30
}
```

### 安全发现级别

findings 列表中的条目以风险级别前缀开头：

| 前缀 | 含义 |
|------|------|
| `HIGH:` | 高风险问题，建议立即修复 |
| `MEDIUM:` | 中等风险，建议关注 |

---

## getResourceSummary() -- 资源表摘要

解析 resources.arsc 文件，返回资源包名、ID、各类型的资源数量统计以及总条目数。

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
ResourceSummary resources = analyzer.getResourceSummary();

System.out.println("资源包名: " + resources.getPackageName());
System.out.println("包 ID: 0x" + Integer.toHexString(resources.getPackageId()));
System.out.println("资源总条目: " + resources.getTotalEntries());

// 各类型资源数量
Map<String, Integer> typeCounts = resources.getTypeCounts();
for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
    System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " 条");
}

// 支持的语言
for (String locale : resources.getLocales()) {
    System.out.println("语言: " + locale);
}
```

输出 JSON 示例：

```json
{
  "packageName": "com.example.app",
  "packageId": 127,
  "typeCounts": {
    "anim": 15,
    "animator": 3,
    "color": 8,
    "dimen": 42,
    "drawable": 68,
    "id": 23,
    "layout": 12,
    "mipmap": 6,
    "string": 156,
    "style": 24
  },
  "locales": ["", "zh-CN", "en-US", "ja-JP"],
  "totalEntries": 357
}
```

> 注意：`packageId` 在 JSON 输出中为十进制整数（如 127 即 0x7f），这是 Android 应用的标准资源包 ID。

---

## getApiSurface() -- 攻击面分析

分析所有导出组件及其 Intent Filter，用于评估应用的攻击面。这是安全审计的核心方法之一。

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
ApiSurfaceInfo surface = analyzer.getApiSurface();

System.out.println("导出组件总数: " + surface.getTotalExportedComponents());

// 导出的 Activity
System.out.println("导出的 Activity:");
for (ComponentInfo comp : surface.getExportedActivities()) {
    System.out.println("  - " + comp.getName());
}

// 导出的 Service
System.out.println("导出的 Service:");
for (ComponentInfo comp : surface.getExportedServices()) {
    System.out.println("  - " + comp.getName());
}

// 导出的 Receiver
System.out.println("导出的 Receiver:");
for (ComponentInfo comp : surface.getExportedReceivers()) {
    System.out.println("  - " + comp.getName());
}

// 导出的 Provider
System.out.println("导出的 Provider:");
for (ComponentInfo comp : surface.getExportedProviders()) {
    System.out.println("  - " + comp.getName());
}

// Intent Filter 详情
System.out.println("Intent Filter 信息:");
for (ApiSurfaceInfo.IntentFilterInfo filter : surface.getIntentFilters()) {
    System.out.println("  组件: " + filter.getComponent() + " (" + filter.getComponentType() + ")");
    System.out.println("    Actions: " + filter.getActions());
    System.out.println("    Categories: " + filter.getCategories());
    System.out.println("    Data Schemes: " + filter.getDataSchemes());
}
```

输出 JSON 示例：

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
  "exportedServices": [
    {
      "name": "com.example.app.ExportService",
      "type": "service",
      "exported": true,
      "intentFilters": [],
      "permissions": []
    }
  ],
  "exportedReceivers": [],
  "exportedProviders": [],
  "intentFilters": [
    {
      "component": "com.example.app.MainActivity",
      "componentType": "activity",
      "actions": ["android.intent.action.MAIN"],
      "categories": ["android.intent.category.LAUNCHER"],
      "dataSchemes": []
    }
  ],
  "totalExportedComponents": 2
}
```

---

## getAllComponents() -- 获取所有组件

按类型分组返回 APK 中的所有四大组件。返回 `LinkedHashMap`，保证顺序为 activities -> services -> receivers -> providers。

```java
ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);
Map<String, List<ComponentInfo>> components = analyzer.getAllComponents();

for (Map.Entry<String, List<ComponentInfo>> entry : components.entrySet()) {
    String type = entry.getKey();
    List<ComponentInfo> list = entry.getValue();
    System.out.println(type + " (" + list.size() + "):");
    for (ComponentInfo comp : list) {
        System.out.println("  - " + comp.getName()
            + " [exported=" + comp.isExported()
            + ", filters=" + comp.getIntentFilters().size()
            + ", permissions=" + comp.getPermissions().size() + "]");
    }
}
```

输出示例：

```
activities (3):
  - com.example.app.MainActivity [exported=true, filters=1, permissions=0]
  - com.example.app.SettingsActivity [exported=false, filters=0, permissions=0]
  - com.example.app.DeepLinkActivity [exported=true, filters=2, permissions=0]
services (1):
  - com.example.app.ExportService [exported=true, filters=0, permissions=0]
receivers (1):
  - com.example.app.BootReceiver [exported=true, filters=1, permissions=0]
providers (0):
```

> 注意：如果 Manifest 不存在，返回空 Map（非 null）。

---

## JSON 序列化

所有数据模型均支持通过 Gson 进行 JSON 序列化：

```java
import brut.androlib.output.JsonOutput;

ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);

// 序列化为 JSON 字符串
ApkSummary summary = analyzer.getSummary();
String json = JsonOutput.toJson(summary);
System.out.println(json);

// 直接输出到 OutputStream
JsonOutput.write(summary, System.out);
```

`JsonOutput` 使用 Gson 并配置了 `disableHtmlEscaping()` 和 `setPrettyPrinting()`，输出格式化的 JSON 且不对 HTML 特殊字符进行转义。

---

## 完整分析示例

以下示例展示如何使用 ApkAnalyzer 对一个 APK 进行全面分析：

```java
import brut.androlib.Config;
import brut.androlib.analyze.*;
import brut.androlib.output.JsonOutput;
import java.io.File;

public class FullAnalysis {
    public static void main(String[] args) throws Exception {
        File apkFile = new File("target.apk");
        Config config = new Config("2.9.3");

        ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);

        // 1. 元数据摘要
        ApkSummary summary = analyzer.getSummary();
        System.out.println("=== APK 摘要 ===");
        System.out.println(JsonOutput.toJson(summary));

        // 2. Manifest 信息
        ManifestInfo manifest = analyzer.getManifestInfo();
        System.out.println("=== Manifest ===");
        System.out.println(JsonOutput.toJson(manifest));

        // 3. 安全报告
        SecurityReport security = analyzer.getSecurityReport();
        System.out.println("=== 安全分析 ===");
        System.out.println("风险评分: " + security.getRiskScore() + "/100");
        for (String finding : security.getFindings()) {
            System.out.println("  " + finding);
        }

        // 4. 资源摘要
        ResourceSummary resources = analyzer.getResourceSummary();
        System.out.println("=== 资源统计 ===");
        System.out.println("总条目: " + resources.getTotalEntries());

        // 5. 攻击面分析
        ApiSurfaceInfo surface = analyzer.getApiSurface();
        System.out.println("=== 攻击面 ===");
        System.out.println("导出组件: " + surface.getTotalExportedComponents());
    }
}
```

---

## 异常处理

所有分析方法都可能抛出 `AndrolibException`，建议在实际使用中进行妥善处理：

```java
try {
    ApkAnalyzer analyzer = new ApkAnalyzer(apkFile, config);
    ApkSummary summary = analyzer.getSummary();
    // ... 分析逻辑
} catch (AndrolibException e) {
    System.err.println("APK 分析失败: " + e.getMessage());
    e.printStackTrace();
}
```

常见异常原因：

- APK 文件不存在或无法读取
- APK 文件格式损坏
- resources.arsc 解析失败
- AndroidManifest.xml 缺失或格式异常
