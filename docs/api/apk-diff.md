# ApkDiff 与 DiffResult API 参考

> 包路径: `brut.androlib.analyze.ApkDiff` / `brut.androlib.analyze.DiffResult`

ApkDiff 提供 APK 文件对比功能，可分析两个 APK 之间的权限、组件、版本号等差异。所有方法均为静态方法。

---

## ApkDiff

### diff

```java
public static DiffResult diff(File apk1, File apk2, Config config) throws AndrolibException
```

对比两个 APK 文件，分析它们之间的差异。内部使用 `ApkAnalyzer` 分别解析两个 APK 的 ManifestInfo，然后通过集合（Set）对比来找出新增和删除的项。

**对比内容包括:**
- 权限（Permissions）的新增与删除
- Activity 组件的新增与删除
- Service 组件的新增与删除
- 版本号（versionCode）变更
- 版本名称（versionName）变更
- 目标 SDK（targetSdkVersion）变更

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `apk1` | `File` | 旧版 APK 文件 |
| `apk2` | `File` | 新版 APK 文件 |
| `config` | `Config` | 配置对象 |

**返回:** `DiffResult`，包含所有差异数据。

> **注意:** 对比采用基于 Set 的比较算法，所有结果列表按字母顺序排序。

---

### getStructure

```java
public static StructureInfo getStructure(File apkFile, Config config) throws AndrolibException
```

获取 APK 文件的代码结构概览。当前实现返回 DEX 文件数量（从 ApkSummary 获取）。

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `File` | 目标 APK 文件 |
| `config` | `Config` | 配置对象 |

**返回:** `StructureInfo`，包含代码结构信息。

---

## DiffResult 数据模型

> 包路径: `brut.androlib.analyze.DiffResult`

```java
public class DiffResult {
    private List<String> addedPermissions;      // 新增的权限列表
    private List<String> removedPermissions;    // 删除的权限列表
    private List<String> addedActivities;       // 新增的 Activity 列表
    private List<String> removedActivities;     // 删除的 Activity 列表
    private List<String> addedServices;         // 新增的 Service 列表
    private List<String> removedServices;       // 删除的 Service 列表
    private List<String> addedDexFiles;         // 新增的 DEX 文件列表
    private List<String> removedDexFiles;       // 删除的 DEX 文件列表
    private List<String> addedNativeLibs;       // 新增的 Native 库列表
    private List<String> removedNativeLibs;     // 删除的 Native 库列表
    private String versionCodeChange;           // 版本号变更，格式: "旧值 -> 新值"，null 表示无变更
    private String versionNameChange;           // 版本名称变更，格式: "旧值 -> 新值"，null 表示无变更
    private String targetSdkChange;             // 目标 SDK 变更，格式: "旧值 -> 新值"，null 表示无变更
}
```

**字段说明:**

| 字段 | 类型 | 说明 |
|------|------|------|
| `addedPermissions` | `List<String>` | apk2 中新增而 apk1 中不存在的权限 |
| `removedPermissions` | `List<String>` | apk1 中存在但 apk2 中已删除的权限 |
| `addedActivities` | `List<String>` | apk2 中新增的 Activity |
| `removedActivities` | `List<String>` | apk2 中已删除的 Activity |
| `addedServices` | `List<String>` | apk2 中新增的 Service |
| `removedServices` | `List<String>` | apk2 中已删除的 Service |
| `addedDexFiles` | `List<String>` | 新增的 DEX 文件 |
| `removedDexFiles` | `List<String>` | 删除的 DEX 文件 |
| `addedNativeLibs` | `List<String>` | 新增的 Native 库 |
| `removedNativeLibs` | `List<String>` | 删除的 Native 库 |
| `versionCodeChange` | `String` | 版本号变化，格式 `"1 -> 2"`，无变化时为 `null` |
| `versionNameChange` | `String` | 版本名变化，格式 `"1.0 -> 2.0"`，无变化时为 `null` |
| `targetSdkChange` | `String` | 目标 SDK 变化，格式 `"30 -> 33"`，无变化时为 `null` |

> **排序规则:** 所有新增/删除列表均按字母顺序排序（`Collections.sort`）。

---

## JSON 输出示例

### 典型 DiffResult JSON

```json
{
  "addedPermissions": [
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO"
  ],
  "removedPermissions": [
    "android.permission.READ_PHONE_STATE"
  ],
  "addedActivities": [
    "com.example.app.CameraActivity",
    "com.example.app.VideoRecorderActivity"
  ],
  "removedActivities": [],
  "addedServices": [
    "com.example.app.MediaUploadService"
  ],
  "removedServices": [],
  "addedDexFiles": [],
  "removedDexFiles": [],
  "addedNativeLibs": [],
  "removedNativeLibs": [],
  "versionCodeChange": "10 -> 20",
  "versionNameChange": "1.0.0 -> 2.0.0",
  "targetSdkChange": "30 -> 33"
}
```

### 无差异的 DiffResult JSON

```json
{
  "addedPermissions": [],
  "removedPermissions": [],
  "addedActivities": [],
  "removedActivities": [],
  "addedServices": [],
  "removedServices": [],
  "addedDexFiles": [],
  "removedDexFiles": [],
  "addedNativeLibs": [],
  "removedNativeLibs": [],
  "versionCodeChange": null,
  "versionNameChange": null,
  "targetSdkChange": null
}
```

---

## 代码示例

### 基本对比两个 APK

```java
import brut.androlib.Config;
import brut.androlib.analyze.ApkDiff;
import brut.androlib.analyze.DiffResult;
import brut.androlib.output.JsonOutput;

import java.io.File;

public class DiffExample {
    public static void main(String[] args) throws Exception {
        Config config = new Config("diff");

        File apkV1 = new File("app-v1.apk");
        File apkV2 = new File("app-v2.apk");

        DiffResult result = ApkDiff.diff(apkV1, apkV2, config);

        // 查看新增权限
        System.out.println("新增权限: " + result.getAddedPermissions());
        // 查看删除权限
        System.out.println("删除权限: " + result.getRemovedPermissions());
        // 查看版本变化
        System.out.println("版本号变化: " + result.getVersionCodeChange());

        // 输出完整 JSON
        System.out.println(JsonOutput.toJson(result));
    }
}
```

### 检查特定变更

```java
Config config = new Config("diff");
DiffResult result = ApkDiff.diff(
    new File("old.apk"),
    new File("new.apk"),
    config
);

// 检查是否新增了危险权限
List<String> dangerous = Arrays.asList(
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.ACCESS_FINE_LOCATION"
);

for (String added : result.getAddedPermissions()) {
    if (dangerous.contains(added)) {
        System.out.println("警告: 新增危险权限 - " + added);
    }
}

// 检查版本回退
if (result.getVersionCodeChange() != null) {
    System.out.println("版本号已变更: " + result.getVersionCodeChange());
}

// 检查 SDK 升级
if (result.getTargetSdkChange() != null) {
    System.out.println("目标 SDK 已变更: " + result.getTargetSdkChange());
}
```

### 获取代码结构概览

```java
import brut.androlib.analyze.ApkDiff;
import brut.androlib.analyze.StructureInfo;

Config config = new Config("diff");
StructureInfo info = ApkDiff.getStructure(new File("app.apk"), config);

System.out.println("DEX 文件数量: " + info.getDexCount());

// 输出完整 JSON
System.out.println(JsonOutput.toJson(info));
```

### 对比结果分析工具方法

```java
/**
 * 检查 DiffResult 是否存在任何差异
 */
public static boolean hasChanges(DiffResult result) {
    return !result.getAddedPermissions().isEmpty()
        || !result.getRemovedPermissions().isEmpty()
        || !result.getAddedActivities().isEmpty()
        || !result.getRemovedActivities().isEmpty()
        || !result.getAddedServices().isEmpty()
        || !result.getRemovedServices().isEmpty()
        || result.getVersionCodeChange() != null
        || result.getVersionNameChange() != null
        || result.getTargetSdkChange() != null;
}
```

---

## 对比算法说明

ApkDiff 使用 **基于 Set 的集合比较** 算法:

1. 从两个 APK 各自提取 ManifestInfo，获取权限列表和组件列表
2. 将列表转换为 `HashSet`，去除重复项
3. 计算差集: `newSet - oldSet` 得到新增项，`oldSet - newSet` 得到删除项
4. 将结果列表通过 `Collections.sort()` 按字母顺序排序

版本号、版本名称和目标 SDK 的对比使用 `Objects.equals()` 进行比较，仅在值不同时才设置对应的 change 字段。变更值格式统一为 `"旧值 -> 新值"`。
