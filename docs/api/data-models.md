# 数据模型完整参考

本文档涵盖 AI-Apktool 中所有数据模型类的完整参考，包括字段定义、Getter/Setter 方法、JSON 序列化示例。

所有模型通过 Gson 序列化，可使用 `JsonOutput.toJson()` 输出 JSON：

```java
import brut.androlib.output.JsonOutput;

String json = JsonOutput.toJson(anyModelObject);
```

---

## 目录

- [ApkSummary](#apksummary) -- APK 元数据摘要
- [ManifestInfo](#manifestinfo) -- Manifest 详细信息
- [ComponentInfo](#componentinfo) -- 组件信息
- [SecurityReport](#securityreport) -- 安全分析报告
- [ResourceSummary](#resourcesummary) -- 资源表摘要
- [ApiSurfaceInfo](#apisurfaceinfo) -- 攻击面分析
- [StructureInfo](#structureinfo) -- 代码结构信息
- [DiffResult](#diffresult) -- APK 差异比较结果
- [SearchResult](#searchresult) -- 搜索结果
- [AiContext](#aicontext) -- AI 上下文信息

---

## ApkSummary

> 包路径: `brut.androlib.analyze.ApkSummary`

APK 文件的元数据摘要，包含文件信息、包名版本、DEX 计数、资源/原生库检测结果以及四大组件数量统计。

### 字段定义

| 字段 | 类型 | Getter | Setter | 说明 |
|------|------|--------|--------|------|
| `fileName` | `String` | `getFileName()` | `setFileName(String)` | APK 文件名 |
| `fileSize` | `long` | `getFileSize()` | `setFileSize(long)` | 文件大小（字节） |
| `packageName` | `String` | `getPackageName()` | `setPackageName(String)` | 应用包名 |
| `versionName` | `String` | `getVersionName()` | `setVersionName(String)` | 版本名（如 "2.1.0"） |
| `versionCode` | `int` | `getVersionCode()` | `setVersionCode(int)` | 版本号（整数） |
| `minSdkVersion` | `String` | `getMinSdkVersion()` | `setMinSdkVersion(String)` | 最低支持 SDK 版本 |
| `targetSdkVersion` | `String` | `getTargetSdkVersion()` | `setTargetSdkVersion(String)` | 目标 SDK 版本 |
| `dexCount` | `int` | `getDexCount()` | `setDexCount(int)` | DEX 文件数量 |
| `hasResources` | `boolean` | `isHasResources()` | `setHasResources(boolean)` | 是否包含 resources.arsc |
| `hasAssets` | `boolean` | `isHasAssets()` | `setHasAssets(boolean)` | 是否包含 assets 目录 |
| `hasNativeLibs` | `boolean` | `isHasNativeLibs()` | `setHasNativeLibs(boolean)` | 是否包含原生库 |
| `architectures` | `List<String>` | `getArchitectures()` | `setArchitectures(List<String>)` | 支持的 CPU 架构列表 |
| `permissionCount` | `int` | `getPermissionCount()` | `setPermissionCount(int)` | 声明的权限总数 |
| `activityCount` | `int` | `getActivityCount()` | `setActivityCount(int)` | Activity 数量 |
| `serviceCount` | `int` | `getServiceCount()` | `setServiceCount(int)` | Service 数量 |
| `receiverCount` | `int` | `getReceiverCount()` | `setReceiverCount(int)` | BroadcastReceiver 数量 |
| `providerCount` | `int` | `getProviderCount()` | `setProviderCount(int)` | ContentProvider 数量 |

### JSON 输出示例

```json
{
  "fileName": "app-release.apk",
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

---

## ManifestInfo

> 包路径: `brut.androlib.analyze.ManifestInfo`

完整解析 AndroidManifest.xml 后的结构化信息，包含包名、版本、权限列表、四大组件详情和安全标记。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `packageName` | `String` | `getPackageName()` | `setPackageName(String)` | `null` | 应用包名 |
| `versionName` | `String` | `getVersionName()` | `setVersionName(String)` | `null` | 版本名 |
| `versionCode` | `int` | `getVersionCode()` | `setVersionCode(int)` | `0` | 版本号 |
| `minSdkVersion` | `String` | `getMinSdkVersion()` | `setMinSdkVersion(String)` | `null` | 最低 SDK |
| `targetSdkVersion` | `String` | `getTargetSdkVersion()` | `setTargetSdkVersion(String)` | `null` | 目标 SDK |
| `maxSdkVersion` | `String` | `getMaxSdkVersion()` | `setMaxSdkVersion(String)` | `null` | 最大 SDK |
| `permissions` | `List<String>` | `getPermissions()` | `setPermissions(List<String>)` | `new ArrayList<>()` | 权限列表 |
| `activities` | `List<ComponentInfo>` | `getActivities()` | `setActivities(List<ComponentInfo>)` | `new ArrayList<>()` | Activity 列表 |
| `services` | `List<ComponentInfo>` | `getServices()` | `setServices(List<ComponentInfo>)` | `new ArrayList<>()` | Service 列表 |
| `receivers` | `List<ComponentInfo>` | `getReceivers()` | `setReceivers(List<ComponentInfo>)` | `new ArrayList<>()` | BroadcastReceiver 列表 |
| `providers` | `List<ComponentInfo>` | `getProviders()` | `setProviders(List<ComponentInfo>)` | `new ArrayList<>()` | ContentProvider 列表 |
| `usesLibraries` | `List<String>` | `getUsesLibraries()` | `setUsesLibraries(List<String>)` | `new ArrayList<>()` | 依赖库列表 |
| `debuggable` | `boolean` | `isDebuggable()` | `setDebuggable(boolean)` | `false` | 是否可调试 |
| `allowBackup` | `boolean` | `isAllowBackup()` | `setAllowBackup(boolean)` | `false` | 是否允许备份 |
| `networkSecurityConfig` | `String` | `getNetworkSecurityConfig()` | `setNetworkSecurityConfig(String)` | `null` | 网络安全配置 |

> 注意：`permissions`、`activities`、`services`、`receivers`、`providers`、`usesLibraries` 字段在声明时即初始化为空 `ArrayList`，因此 Getter 永远不会返回 null，可以直接调用 `.add()` 等方法。

### JSON 输出示例

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
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION"
  ],
  "activities": [
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
  "receivers": [
    {
      "name": "com.example.app.BootReceiver",
      "type": "receiver",
      "exported": true,
      "intentFilters": ["android.intent.action.BOOT_COMPLETED"],
      "permissions": []
    }
  ],
  "providers": [],
  "usesLibraries": [],
  "debuggable": false,
  "allowBackup": true,
  "networkSecurityConfig": null
}
```

---

## ComponentInfo

> 包路径: `brut.androlib.analyze.ComponentInfo`

Android 四大组件（Activity、Service、BroadcastReceiver、ContentProvider）的通用描述模型。

### 构造函数

```java
// 无参构造
public ComponentInfo()

// 指定名称和类型的构造
public ComponentInfo(String name, String type)
```

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `name` | `String` | `getName()` | `setName(String)` | `null` | 组件完整类名 |
| `type` | `String` | `getType()` | `setType(String)` | `null` | 组件类型：`"activity"` / `"service"` / `"receiver"` / `"provider"` |
| `exported` | `boolean` | `isExported()` | `setExported(boolean)` | `false` | 是否被导出 |
| `intentFilters` | `List<String>` | `getIntentFilters()` | `setIntentFilters(List<String>)` | `new ArrayList<>()` | Intent Filter action 列表 |
| `permissions` | `List<String>` | `getPermissions()` | `setPermissions(List<String>)` | `new ArrayList<>()` | 组件所需的权限保护列表 |

### 使用示例

```java
// 通过构造函数创建
ComponentInfo comp = new ComponentInfo("com.example.app.MainActivity", "activity");
comp.setExported(true);
comp.getIntentFilters().add("android.intent.action.MAIN");
comp.getPermissions().add("android.permission.CAMERA");

// 通过 setter 创建
ComponentInfo comp2 = new ComponentInfo();
comp2.setName("com.example.app.ExportService");
comp2.setType("service");
comp2.setExported(true);
```

### JSON 输出示例

```json
{
  "name": "com.example.app.MainActivity",
  "type": "activity",
  "exported": true,
  "intentFilters": [
    "android.intent.action.MAIN",
    "android.intent.action.VIEW"
  ],
  "permissions": []
}
```

### type 字段取值

| 值 | 对应组件 |
|----|---------|
| `"activity"` | Activity |
| `"service"` | Service |
| `"receiver"` | BroadcastReceiver |
| `"provider"` | ContentProvider |

---

## SecurityReport

> 包路径: `brut.androlib.analyze.SecurityReport`

安全分析报告，包含危险权限识别、高风险组件检测、风险评分和安全发现列表。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `dangerousPermissions` | `List<String>` | `getDangerousPermissions()` | `setDangerousPermissions(List<String>)` | `new ArrayList<>()` | 危险权限列表 |
| `highRiskComponents` | `List<String>` | `getHighRiskComponents()` | `setHighRiskComponents(List<String>)` | `new ArrayList<>()` | 高风险组件列表（格式: `"type: name"`） |
| `debuggable` | `boolean` | `isDebuggable()` | `setDebuggable(boolean)` | `false` | 应用是否可调试 |
| `allowBackup` | `boolean` | `isAllowBackup()` | `setAllowBackup(boolean)` | `false` | 应用是否允许备份 |
| `usesCleartextTraffic` | `boolean` | `isUsesCleartextTraffic()` | `setUsesCleartextTraffic(boolean)` | `false` | 是否使用明文流量 |
| `findings` | `List<String>` | `getFindings()` | `setFindings(List<String>)` | `new ArrayList<>()` | 安全发现描述列表 |
| `riskScore` | `int` | `getRiskScore()` | `setRiskScore(int)` | `0` | 风险评分（0-100） |

### 风险评分计算规则

| 风险因素 | 分值贡献 |
|----------|---------|
| `debuggable = true` | +30 |
| `allowBackup = true` | +10 |
| 每个危险权限 | +2（上限 +20） |
| 每个无权限保护的导出组件 | +5（上限 +30） |
| **最大值** | **100** |

### highRiskComponents 条目格式

每条记录的格式为 `"组件类型: 组件完整类名"`，例如：

- `"activity: com.example.app.ExportActivity"`
- `"service: com.example.app.RemoteService"`
- `"receiver: com.example.app.GlobalReceiver"`
- `"provider: com.example.app.DataProvider"`

### findings 条目格式

每条发现以风险级别前缀开头：

| 前缀 | 典型内容 |
|------|---------|
| `HIGH:` | 可调试标记、无权限保护的导出组件 |
| `MEDIUM:` | 允许备份、危险权限数量 |

典型 findings 示例：
- `"HIGH: Application is debuggable - android:debuggable=true"`
- `"MEDIUM: Application allows backup - android:allowBackup=true"`
- `"MEDIUM: Application requests 5 dangerous permissions"`
- `"HIGH: 3 exported components without permission protection"`

### JSON 输出示例

```json
{
  "dangerousPermissions": [
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.RECORD_AUDIO"
  ],
  "highRiskComponents": [
    "activity: com.example.app.DeepLinkActivity",
    "service: com.example.app.ExportService"
  ],
  "debuggable": true,
  "allowBackup": true,
  "usesCleartextTraffic": false,
  "findings": [
    "HIGH: Application is debuggable - android:debuggable=true",
    "MEDIUM: Application allows backup - android:allowBackup=true",
    "MEDIUM: Application requests 3 dangerous permissions",
    "HIGH: 2 exported components without permission protection"
  ],
  "riskScore": 66
}
```

---

## ResourceSummary

> 包路径: `brut.androlib.analyze.ResourceSummary`

resources.arsc 资源表的摘要统计信息，包含资源包标识、各类型资源计数和支持的语言列表。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `packageName` | `String` | `getPackageName()` | `setPackageName(String)` | `null` | 资源包名 |
| `packageId` | `int` | `getPackageId()` | `setPackageId(int)` | `0` | 资源包 ID（通常为 0x7f = 127） |
| `typeCounts` | `Map<String, Integer>` | `getTypeCounts()` | `setTypeCounts(Map<String, Integer>)` | `new LinkedHashMap<>()` | 各资源类型的条目数量 |
| `locales` | `List<String>` | `getLocales()` | `setLocales(List<String>)` | `new ArrayList<>()` | 支持的语言/地区列表 |
| `totalEntries` | `int` | `getTotalEntries()` | `setTotalEntries(int)` | `0` | 资源总条目数 |

> `typeCounts` 使用 `LinkedHashMap` 保持插入顺序。键为资源类型名（如 `"string"`、`"drawable"`、`"layout"`），值为该类型的资源条目数量。

### 常见资源类型

| 类型名 | 说明 |
|--------|------|
| `anim` | 补间动画 |
| `animator` | 属性动画 |
| `color` | 颜色状态列表 |
| `dimen` | 尺寸值 |
| `drawable` | 图片/矢量图 |
| `id` | ID 资源 |
| `layout` | 布局文件 |
| `mipmap` | 应用图标 |
| `string` | 字符串 |
| `style` | 样式 |
| `array` | 数组资源 |
| `bool` | 布尔值 |
| `integer` | 整数值 |
| `xml` | 原始 XML |
| `raw` | 原始文件 |
| `menu` | 菜单 |
| `plurals` | 复数形式字符串 |
| `attr` | 属性声明 |
| `declare-styleable` | 自定义样式属性 |

### JSON 输出示例

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
    "style": 24,
    "attr": 18,
    "declare-styleable": 5
  },
  "locales": ["", "zh-CN", "en-US", "ja-JP"],
  "totalEntries": 380
}
```

---

## ApiSurfaceInfo

> 包路径: `brut.androlib.analyze.ApiSurfaceInfo`

攻击面分析结果，包含所有导出组件及其 Intent Filter 信息，用于评估应用对外暴露的攻击面。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `exportedActivities` | `List<ComponentInfo>` | `getExportedActivities()` | `setExportedActivities(List<ComponentInfo>)` | `new ArrayList<>()` | 导出的 Activity 列表 |
| `exportedServices` | `List<ComponentInfo>` | `getExportedServices()` | `setExportedServices(List<ComponentInfo>)` | `new ArrayList<>()` | 导出的 Service 列表 |
| `exportedReceivers` | `List<ComponentInfo>` | `getExportedReceivers()` | `setExportedReceivers(List<ComponentInfo>)` | `new ArrayList<>()` | 导出的 BroadcastReceiver 列表 |
| `exportedProviders` | `List<ComponentInfo>` | `getExportedProviders()` | `setExportedProviders(List<ComponentInfo>)` | `new ArrayList<>()` | 导出的 ContentProvider 列表 |
| `intentFilters` | `List<IntentFilterInfo>` | `getIntentFilters()` | `setIntentFilters(List<IntentFilterInfo>)` | `new ArrayList<>()` | Intent Filter 详情列表 |
| `totalExportedComponents` | `int` | `getTotalExportedComponents()` | `setTotalExportedComponents(int)` | `0` | 导出组件总数 |

### 内部类 IntentFilterInfo

> 包路径: `brut.androlib.analyze.ApiSurfaceInfo.IntentFilterInfo`

描述单个组件的 Intent Filter 详细信息。

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `component` | `String` | `getComponent()` | `setComponent(String)` | `null` | 组件完整类名 |
| `componentType` | `String` | `getComponentType()` | `setComponentType(String)` | `null` | 组件类型（activity/service/receiver） |
| `actions` | `List<String>` | `getActions()` | `setActions(List<String>)` | `new ArrayList<>()` | Intent action 列表 |
| `categories` | `List<String>` | `getCategories()` | `setCategories(List<String>)` | `new ArrayList<>()` | Intent category 列表 |
| `dataSchemes` | `List<String>` | `getDataSchemes()` | `setDataSchemes(List<String>)` | `new ArrayList<>()` | Data scheme 列表 |

> 注意：Intent Filter 信息仅从 Activity、Service 和 Receiver 中收集，不包含 Provider。

### JSON 输出示例

```json
{
  "exportedActivities": [
    {
      "name": "com.example.app.MainActivity",
      "type": "activity",
      "exported": true,
      "intentFilters": ["android.intent.action.MAIN"],
      "permissions": []
    },
    {
      "name": "com.example.app.DeepLinkActivity",
      "type": "activity",
      "exported": true,
      "intentFilters": ["android.intent.action.VIEW"],
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
    },
    {
      "component": "com.example.app.DeepLinkActivity",
      "componentType": "activity",
      "actions": ["android.intent.action.VIEW"],
      "categories": ["android.intent.category.DEFAULT", "android.intent.category.BROWSABLE"],
      "dataSchemes": ["https", "myapp"]
    }
  ],
  "totalExportedComponents": 3
}
```

---

## StructureInfo

> 包路径: `brut.androlib.analyze.StructureInfo`

APK 中 DEX 代码的结构统计信息，包含类、方法、字段计数以及包分布统计。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `totalClasses` | `int` | `getTotalClasses()` | `setTotalClasses(int)` | `0` | 类总数 |
| `totalMethods` | `int` | `getTotalMethods()` | `setTotalMethods(int)` | `0` | 方法总数 |
| `totalFields` | `int` | `getTotalFields()` | `setTotalFields(int)` | `0` | 字段总数 |
| `packageCounts` | `Map<String, Integer>` | `getPackageCounts()` | `setPackageCounts(Map<String, Integer>)` | `new LinkedHashMap<>()` | 各包的类数量统计 |
| `topClasses` | `List<String>` | `getTopClasses()` | `setTopClasses(List<String>)` | `new ArrayList<>()` | 按方法数排列的顶级类 |
| `dexCount` | `int` | `getDexCount()` | `setDexCount(int)` | `0` | DEX 文件数量 |
| `dexClassCounts` | `Map<String, Integer>` | `getDexClassCounts()` | `setDexClassCounts(Map<String, Integer>)` | `new LinkedHashMap<>()` | 各 DEX 文件的类数量 |

> `packageCounts` 和 `dexClassCounts` 使用 `LinkedHashMap` 保持插入顺序。

### JSON 输出示例

```json
{
  "totalClasses": 1250,
  "totalMethods": 8500,
  "totalFields": 4200,
  "packageCounts": {
    "com.example.app": 120,
    "com.example.app.ui": 85,
    "com.example.app.network": 42,
    "com.example.app.data": 38,
    "com.squareup.okhttp3": 156,
    "com.google.gson": 98
  },
  "topClasses": [
    "com.example.app.ui.MainActivity",
    "com.example.app.network.ApiClient",
    "com.example.app.data.DatabaseHelper"
  ],
  "dexCount": 2,
  "dexClassCounts": {
    "classes.dex": 980,
    "classes2.dex": 270
  }
}
```

---

## DiffResult

> 包路径: `brut.androlib.analyze.DiffResult`

两个 APK 文件的差异比较结果，记录权限、组件、DEX 文件和原生库的增减变化以及版本变更。

通常通过 `ApkDiff.diff()` 静态方法获取：

```java
import brut.androlib.analyze.ApkDiff;

DiffResult diff = ApkDiff.diff(new File("old.apk"), new File("new.apk"), config);
```

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `addedPermissions` | `List<String>` | `getAddedPermissions()` | `setAddedPermissions(List<String>)` | `new ArrayList<>()` | 新增的权限 |
| `removedPermissions` | `List<String>` | `getRemovedPermissions()` | `setRemovedPermissions(List<String>)` | `new ArrayList<>()` | 移除的权限 |
| `addedActivities` | `List<String>` | `getAddedActivities()` | `setAddedActivities(List<String>)` | `new ArrayList<>()` | 新增的 Activity |
| `removedActivities` | `List<String>` | `getRemovedActivities()` | `setRemovedActivities(List<String>)` | `new ArrayList<>()` | 移除的 Activity |
| `addedServices` | `List<String>` | `getAddedServices()` | `setAddedServices(List<String>)` | `new ArrayList<>()` | 新增的 Service |
| `removedServices` | `List<String>` | `getRemovedServices()` | `setRemovedServices(List<String>)` | `new ArrayList<>()` | 移除的 Service |
| `addedDexFiles` | `List<String>` | `getAddedDexFiles()` | `setAddedDexFiles(List<String>)` | `new ArrayList<>()` | 新增的 DEX 文件 |
| `removedDexFiles` | `List<String>` | `getRemovedDexFiles()` | `setRemovedDexFiles(List<String>)` | `new ArrayList<>()` | 移除的 DEX 文件 |
| `addedNativeLibs` | `List<String>` | `getAddedNativeLibs()` | `setAddedNativeLibs(List<String>)` | `new ArrayList<>()` | 新增的原生库 |
| `removedNativeLibs` | `List<String>` | `getRemovedNativeLibs()` | `setRemovedNativeLibs(List<String>)` | `new ArrayList<>()` | 移除的原生库 |
| `versionCodeChange` | `String` | `getVersionCodeChange()` | `setVersionCodeChange(String)` | `null` | 版本号变更（格式: `"old -> new"`） |
| `versionNameChange` | `String` | `getVersionNameChange()` | `setVersionNameChange(String)` | `null` | 版本名变更（格式: `"old -> new"`） |
| `targetSdkChange` | `String` | `getTargetSdkChange()` | `setTargetSdkChange(String)` | `null` | 目标 SDK 变更（格式: `"old -> new"`） |

### 变更字段格式

`versionCodeChange`、`versionNameChange`、`targetSdkChange` 三个字段仅在值发生变化时有值，格式为 `"旧值 -> 新值"`。若未变化则为 `null`。

```
// 有变化
"versionCodeChange": "200 -> 210"
"versionNameChange": "2.0.0 -> 2.1.0"
"targetSdkChange": "33 -> 34"

// 无变化
"versionCodeChange": null
```

### JSON 输出示例

```json
{
  "addedPermissions": [
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.BLUETOOTH_SCAN"
  ],
  "removedPermissions": [
    "android.permission.READ_EXTERNAL_STORAGE"
  ],
  "addedActivities": [
    "com.example.app.NewFeatureActivity",
    "com.example.app.OnboardingActivity"
  ],
  "removedActivities": [],
  "addedServices": [
    "com.example.app.BluetoothService"
  ],
  "removedServices": [],
  "addedDexFiles": [
    "classes3.dex"
  ],
  "removedDexFiles": [],
  "addedNativeLibs": [
    "lib/arm64-v8a/libnew.so"
  ],
  "removedNativeLibs": [],
  "versionCodeChange": "200 -> 210",
  "versionNameChange": "2.0.0 -> 2.1.0",
  "targetSdkChange": "33 -> 34"
}
```

---

## SearchResult

> 包路径: `brut.androlib.search.SearchResult`

在 APK 内搜索资源、字符串或组件后的结果。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `query` | `String` | `getQuery()` | `setQuery(String)` | `null` | 搜索查询关键字 |
| `type` | `String` | `getType()` | `setType(String)` | `null` | 搜索类型（如 `"string"`、`"resource"`、`"component"`） |
| `totalMatches` | `int` | `getTotalMatches()` | `setTotalMatches(int)` | `0` | 匹配总数 |
| `matches` | `List<SearchMatch>` | `getMatches()` | `setMatches(List<SearchMatch>)` | `new ArrayList<>()` | 匹配项列表 |

### 内部类 SearchMatch

> 包路径: `brut.androlib.search.SearchResult.SearchMatch`

单个搜索匹配项。

#### 构造函数

```java
// 无参构造
public SearchMatch()

// 指定名称、值和来源的构造
public SearchMatch(String name, String value, String source)
```

#### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `name` | `String` | `getName()` | `setName(String)` | `null` | 资源名称或标识 |
| `value` | `String` | `getValue()` | `setValue(String)` | `null` | 匹配到的值 |
| `source` | `String` | `getSource()` | `setSource(String)` | `null` | 来源位置（如资源文件路径） |

### JSON 输出示例

```json
{
  "query": "login",
  "type": "string",
  "totalMatches": 3,
  "matches": [
    {
      "name": "login_button_text",
      "value": "Login",
      "source": "res/values/strings.xml"
    },
    {
      "name": "login_error_title",
      "value": "Login Failed",
      "source": "res/values/strings.xml"
    },
    {
      "name": "login_success_message",
      "value": "Login successful!",
      "source": "res/values/strings.xml"
    }
  ]
}
```

---

## AiContext

> 包路径: `brut.androlib.ai.AiContext`

为 AI 大语言模型构建的上下文信息，包含 APK 的关键分析结果，用于生成 AI 可处理的提示信息。

### 字段定义

| 字段 | 类型 | Getter | Setter | 默认值 | 说明 |
|------|------|--------|--------|--------|------|
| `apkFileName` | `String` | `getApkFileName()` | `setApkFileName(String)` | `null` | APK 文件名 |
| `packageName` | `String` | `getPackageName()` | `setPackageName(String)` | `null` | 应用包名 |
| `manifestXml` | `String` | `getManifestXml()` | `setManifestXml(String)` | `null` | 解码后的 Manifest XML 内容 |
| `permissions` | `List<String>` | `getPermissions()` | `setPermissions(List<String>)` | `new ArrayList<>()` | 权限列表 |
| `components` | `List<String>` | `getComponents()` | `setComponents(List<String>)` | `new ArrayList<>()` | 组件列表 |
| `stringResources` | `List<String>` | `getStringResources()` | `setStringResources(List<String>)` | `new ArrayList<>()` | 字符串资源列表 |
| `securityReport` | `String` | `getSecurityReport()` | `setSecurityReport(String)` | `null` | 安全分析报告文本 |
| `estimatedTokenCount` | `int` | `getEstimatedTokenCount()` | `setEstimatedTokenCount(int)` | `0` | 预估的 Token 数量 |

> `estimatedTokenCount` 用于在发送给 AI 模型前估算上下文大小，帮助判断是否需要裁剪内容以适应模型的上下文窗口限制。

### JSON 输出示例

```json
{
  "apkFileName": "app-release.apk",
  "packageName": "com.example.app",
  "manifestXml": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    package=\"com.example.app\"\n    android:versionCode=\"210\"\n    android:versionName=\"2.1.0\">\n  ...\n</manifest>",
  "permissions": [
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.CAMERA"
  ],
  "components": [
    "activity: com.example.app.MainActivity",
    "activity: com.example.app.SettingsActivity",
    "service: com.example.app.BackgroundService",
    "receiver: com.example.app.BootReceiver"
  ],
  "stringResources": [
    "app_name=My App",
    "login_button_text=Login",
    "settings_title=Settings"
  ],
  "securityReport": "Risk Score: 30/100\nFindings:\n- MEDIUM: Application allows backup\n- MEDIUM: Application requests 1 dangerous permission",
  "estimatedTokenCount": 2850
}
```

---

## JSON 序列化说明

### JsonOutput 工具类

> 包路径: `brut.androlib.output.JsonOutput`

所有数据模型均可通过 `JsonOutput` 工具类序列化为 JSON：

```java
import brut.androlib.output.JsonOutput;

// 序列化为 JSON 字符串
String json = JsonOutput.toJson(anyModel);

// 直接写入 OutputStream
JsonOutput.write(anyModel, outputStream);
```

### 序列化配置

`JsonOutput` 内部使用 Gson 并配置了以下选项：

| 配置 | 说明 |
|------|------|
| `disableHtmlEscaping()` | 不对 `<`、`>`、`&` 等 HTML 特殊字符进行转义 |
| `setPrettyPrinting()` | 启用格式化输出，每行一个字段 |

### boolean 字段的序列化规则

Gson 序列化 boolean 字段时遵循 Java Bean 规范。对于以 `is` 开头的 boolean 字段（如 `debuggable`、`allowBackup`），Gson 序列化的 JSON 键名为字段名本身（即 `"debuggable"` 而非 `"isDebuggable"`）。具体规则：

| 字段声明 | Getter 方法 | JSON 键名 |
|----------|-------------|-----------|
| `boolean hasResources` | `isHasResources()` | `"hasResources"` |
| `boolean debuggable` | `isDebuggable()` | `"debuggable"` |
| `boolean exported` | `isExported()` | `"exported"` |

### List 字段的序列化

所有 List 字段在声明时即初始化为 `new ArrayList<>()`，因此：

- 空 List 序列化为 `[]`（而非 `null`）
- 调用 Getter 后可以直接使用 `.add()` 方法

### Map 字段的序列化

`typeCounts`、`packageCounts`、`dexClassCounts` 等使用 `LinkedHashMap`，序列化后的 JSON 对象键值对保持插入顺序。
