# ApkInfo 与 Meta 模型参考

本文档详细说明 AI-Apktool 中 APK 元数据模型及其子模型的完整 API 参考。这些模型类用于序列化和反序列化 `apktool.yml` 配置文件，记录 APK 解码过程中的关键元信息。

---

## ApkInfo

**包**: `brut.androlib.meta.ApkInfo`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/meta/ApkInfo.java`
**实现接口**: `brut.yaml.YamlSerializable`

`ApkInfo` 是 APK 元数据的核心模型，对应 `apktool.yml` 文件的完整结构。在解码 (decode) APK 时生成，在构建 (build) APK 时读取。它记录了 APK 的版本信息、SDK 要求、framework 依赖、资源配置等关键元数据。

### 常量

| 常量 | 类型 | 值/说明 |
|------|------|---------|
| `RAW_DIRS` | `String[]` | `{"assets", "lib"}` -- 原始目录名列表，这些目录的内容不解码直接复制 |
| `CLASSES_FILES_PATTERN` | `Pattern` | 匹配 `classes.dex`、`classes2.dex`、`classes10.dex` 等 DEX 文件名 |
| `ORIGINAL_FILES_PATTERN` | `Pattern` | 匹配原始签名文件：`AndroidManifest.xml`、`META-INF/*.{RSA,SF,MF}`、`stamp-cert-sha256` |
| `STANDARD_FILES_PATTERN` | `Pattern` | 匹配所有标准 APK 文件：`resources.arsc`、assets/lib 目录内容、DEX 文件、原始文件 |

### 构造方法

```java
public ApkInfo()
```

创建一个空的 `ApkInfo` 实例，所有子模型初始化为空对象：

- `mUsesFramework` = 新的空 `UsesFramework`
- `mUsesLibrary` = 新的空 `ArrayList`
- `mSdkInfo` = 新的空 `SdkInfo`
- `mVersionInfo` = 新的空 `VersionInfo`
- `mResourcesInfo` = 新的空 `ResourcesInfo`
- `mFeatureFlags` = 新的空 `LinkedHashMap`
- `mDoNotCompress` = 新的空 `ArrayList`

### 静态方法

#### `load(File apkDir)`

```java
public static ApkInfo load(File apkDir) throws AndrolibException
```

从指定目录中读取 `apktool.yml` 文件并反序列化为 `ApkInfo` 对象。

**参数**:
- `apkDir` -- 包含 `apktool.yml` 文件的目录

**异常**:
- `AndrolibException` -- 当文件读取失败或 YAML 解析出错时抛出

**使用示例**:

```java
ApkInfo info = ApkInfo.load(new File("/path/to/decoded/apk"));
System.out.println("Package: " + info.getApkFileName());
System.out.println("Version: " + info.getVersion());
```

#### `load(InputStream in)`

```java
static ApkInfo load(InputStream in)
```

从输入流中读取 YAML 数据并反序列化。主要用于测试场景。此方法不会关闭输入流，调用者负责关闭。

**参数**:
- `in` -- 包含 YAML 数据的输入流

### 实例方法

#### 序列化

| 方法签名 | 说明 |
|---------|------|
| `void save(File apkDir)` | 将当前 `ApkInfo` 序列化为 YAML 并写入 `apkDir/apktool.yml` |
| `void readItem(YamlReader reader)` | (YamlSerializable) 从 YAML 读取器中读取单个键值对 |
| `void write(YamlWriter writer)` | (YamlSerializable) 将所有非空字段写入 YAML 写入器 |

#### 属性访问

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `getVersion()` | `String` | 获取 Apktool 版本号（生成此文件的 Apktool 版本） |
| `setVersion(String)` | `void` | 设置 Apktool 版本号 |
| `getApkFileName()` | `String` | 获取原始 APK 文件名 |
| `setApkFileName(String)` | `void` | 设置原始 APK 文件名（含安全校验，拒绝 `.`、`..` 和包含路径分隔符的值） |
| `getUsesFramework()` | `UsesFramework` | 获取 framework 依赖信息 |
| `getUsesLibrary()` | `List<String>` | 获取使用的共享库列表 |
| `getSdkInfo()` | `SdkInfo` | 获取 SDK 版本信息 |
| `getVersionInfo()` | `VersionInfo` | 获取应用版本信息 |
| `getResourcesInfo()` | `ResourcesInfo` | 获取资源配置信息 |
| `getFeatureFlags()` | `Map<String, Boolean>` | 获取功能标志映射 |
| `getDoNotCompress()` | `List<String>` | 获取不压缩文件列表 |
| `getApkFile()` | `ExtFile` | 获取关联的 APK 文件对象（仅在从文件加载时设置） |
| `setApkFile(ExtFile)` | `void` | 设置 APK 文件对象（同时更新 `apkFileName`） |

#### 检查方法

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `hasSources()` | `boolean` | 检查 APK 中是否包含 `classes.dex`（即是否有 Dalvik 源代码） |
| `hasManifest()` | `boolean` | 检查 APK 中是否包含 `AndroidManifest.xml` |
| `hasResources()` | `boolean` | 检查 APK 中是否包含 `resources.arsc`（即是否有编译资源） |

**注意**: 三个检查方法要求 `setApkFile()` 已被调用，否则返回 `false`。它们会抛出 `AndrolibException`。

### 安全特性

`setApkFileName()` 方法内置了安全校验，防止路径遍历攻击：

```java
// 以下值会被拒绝并抛出 SecurityException:
setApkFileName(".");
setApkFileName("..");
setApkFileName("path/to/file.apk");
setApkFileName("path\\to\\file.apk");
```

---

## SdkInfo

**包**: `brut.androlib.meta.SdkInfo`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/meta/SdkInfo.java`
**实现接口**: `brut.yaml.YamlSerializable`

记录 APK 的 SDK 版本要求信息，对应 `apktool.yml` 中的 `sdkInfo` 节。

### 字段

| 字段 | 类型 | YAML 键 | 说明 |
|------|------|---------|------|
| `minSdkVersion` | `String` | `minSdkVersion` | 最低支持的 SDK 版本 |
| `targetSdkVersion` | `String` | `targetSdkVersion` | 目标 SDK 版本 |
| `maxSdkVersion` | `String` | `maxSdkVersion` | 最高支持的 SDK 版本 |

### 实例方法

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `getMinSdkVersion()` | `String` | 获取最低 SDK 版本 |
| `setMinSdkVersion(String)` | `void` | 设置最低 SDK 版本 |
| `getTargetSdkVersion()` | `String` | 获取目标 SDK 版本 |
| `getTargetSdkVersionBounded()` | `String` | 获取受约束的目标 SDK 版本（不超出 min/max 范围） |
| `setTargetSdkVersion(String)` | `void` | 设置目标 SDK 版本 |
| `getMaxSdkVersion()` | `String` | 获取最高 SDK 版本 |
| `setMaxSdkVersion(String)` | `void` | 设置最高 SDK 版本 |
| `clear()` | `void` | 重置所有字段为 null |
| `isEmpty()` | `boolean` | 当所有字段均为 null 时返回 true |

### 静态方法

#### `parseSdkInt(String version)`

```java
public static int parseSdkInt(String sdkVersion)
```

将 SDK 版本字符串解析为整数。支持以下格式：

- **数字字符串**: `"33"` -> `33`
- **字母代号**: `"M"` -> `23`, `"N"` -> `24`, `"O"` -> `26`, `"P"` -> `28`, `"Q"` -> `29`, `"R"` -> `30`, `"S"` -> `31`, `"SV2"` -> `32`
- **甜点代号名称**: `"TIRAMISU"` / `"T"` -> `33`, `"UPSIDEDOWNCAKE"` / `"UPSIDE_DOWN_CAKE"` -> `34`, `"VANILLAICECREAM"` / `"VANILLA_ICE_CREAM"` -> `35`, `"BAKLAVA"` -> `36`
- **开发版本**: `"SDK_CUR_DEVELOPMENT"` -> `10000`

**异常**: 如果字符串不匹配任何已知代号且无法解析为整数，抛出 `NumberFormatException`。

**使用示例**:

```java
int sdkInt = SdkInfo.parseSdkInt("TIRAMISU");  // 返回 33
int sdkInt = SdkInfo.parseSdkInt("31");         // 返回 31
int sdkInt = SdkInfo.parseSdkInt("S");          // 返回 31
```

---

## VersionInfo

**包**: `brut.androlib.meta.VersionInfo`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/meta/VersionInfo.java`
**实现接口**: `brut.yaml.YamlSerializable`

记录应用的版本信息，对应 `apktool.yml` 中的 `versionInfo` 节。

### 字段

| 字段 | 类型 | YAML 键 | 说明 |
|------|------|---------|------|
| `versionCode` | `Integer` | `versionCode` | 版本号（整数，未设置时 `getVersionCode()` 返回 -1） |
| `versionName` | `String` | `versionName` | 版本名称字符串 |

### 实例方法

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `getVersionCode()` | `int` | 获取版本号，未设置时返回 -1 |
| `setVersionCode(int)` | `void` | 设置版本号 |
| `getVersionName()` | `String` | 获取版本名称 |
| `setVersionName(String)` | `void` | 设置版本名称 |
| `clear()` | `void` | 重置所有字段 |
| `isEmpty()` | `boolean` | 当所有字段均为 null 时返回 true |

---

## ResourcesInfo

**包**: `brut.androlib.meta.ResourcesInfo`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/meta/ResourcesInfo.java`
**实现接口**: `brut.yaml.YamlSerializable`

记录资源表 (resources.arsc) 的配置信息，对应 `apktool.yml` 中的 `resourcesInfo` 节。

### 字段

| 字段 | 类型 | YAML 键 | 默认值 | 说明 |
|------|------|---------|--------|------|
| `packageId` | `Integer` | `packageId` | -1 | 资源包 ID |
| `packageName` | `String` | `packageName` | null | 资源包名称 |
| `sparseEntries` | `Boolean` | `sparseEntries` | false | 是否使用稀疏资源条目 |
| `compactEntries` | `Boolean` | `compactEntries` | false | 是否使用紧凑资源条目 |
| `keepRawValues` | `Boolean` | `keepRawValues` | false | 是否保留原始 XML 属性值 |

### 实例方法

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `getPackageId()` | `int` | 获取资源包 ID，未设置时返回 -1 |
| `setPackageId(int)` | `void` | 设置资源包 ID |
| `getPackageName()` | `String` | 获取资源包名称 |
| `setPackageName(String)` | `void` | 设置资源包名称 |
| `isSparseEntries()` | `boolean` | 是否稀疏条目，未设置时返回 false |
| `setSparseEntries(boolean)` | `void` | 设置稀疏条目标志 |
| `isCompactEntries()` | `boolean` | 是否紧凑条目，未设置时返回 false |
| `setCompactEntries(boolean)` | `void` | 设置紧凑条目标志 |
| `isKeepRawValues()` | `boolean` | 是否保留原始值，未设置时返回 false |
| `setKeepRawValues(boolean)` | `void` | 设置保留原始值标志 |
| `clear()` | `void` | 重置所有字段 |
| `isEmpty()` | `boolean` | 当所有字段均为 null 时返回 true |

---

## UsesFramework

**包**: `brut.androlib.meta.UsesFramework`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/meta/UsesFramework.java`
**实现接口**: `brut.yaml.YamlSerializable`

记录 APK 依赖的 framework 资源信息，对应 `apktool.yml` 中的 `usesFramework` 节。

### 字段

| 字段 | 类型 | YAML 键 | 说明 |
|------|------|---------|------|
| `ids` | `List<Integer>` | `ids` | 依赖的 framework package ID 列表 |
| `tag` | `String` | `tag` | framework 标签，用于区分不同的 framework 集合 |

### 实例方法

| 方法签名 | 返回类型 | 说明 |
|---------|---------|------|
| `getIds()` | `List<Integer>` | 获取 framework ID 列表 |
| `getTag()` | `String` | 获取 framework 标签 |
| `setTag(String)` | `void` | 设置 framework 标签 |
| `clear()` | `void` | 清空 ID 列表并重置 tag |
| `isEmpty()` | `boolean` | 当 ID 列表为空且 tag 为 null 时返回 true |

---

## YAML 输出示例

以下是一个完整的 `apktool.yml` 文件示例，展示了所有可能的字段：

```yaml
version: 2.9.3
apkFileName: com.example.app-1.0.0.apk
usesFramework:
  ids:
    - 1
    - 2
  tag: samsung
usesLibrary:
  - com.google.android.maps
  - com.android.future.usb.accessory
sdkInfo:
  minSdkVersion: "24"
  targetSdkVersion: "34"
  maxSdkVersion: "35"
versionInfo:
  versionCode: 100
  versionName: "1.0.0"
resourcesInfo:
  packageId: 127
  packageName: com.example.app
  sparseEntries: false
  compactEntries: true
  keepRawValues: false
featureFlags:
  split: false
  isolatedSplit: false
doNotCompress:
  - assets/data.bin
  - lib/arm64-v8a/libnative.so
  - resources.arsc
```

### 最小化 YAML 示例

当大多数字段为空时，生成的 `apktool.yml` 只包含必要字段：

```yaml
version: 2.9.3
apkFileName: simple-app.apk
sdkInfo:
  minSdkVersion: "21"
  targetSdkVersion: "33"
versionInfo:
  versionCode: 1
  versionName: "1.0"
```

### YAML 字段与 Java 模型映射

| YAML 节 | Java 类 | 说明 |
|----------|---------|------|
| `version` | `ApkInfo.getVersion()` | Apktool 版本号 |
| `apkFileName` | `ApkInfo.getApkFileName()` | 原始 APK 文件名 |
| `usesFramework` | `ApkInfo.getUsesFramework()` | Framework 依赖 |
| `usesFramework.ids` | `UsesFramework.getIds()` | Framework ID 列表 |
| `usesFramework.tag` | `UsesFramework.getTag()` | Framework 标签 |
| `usesLibrary` | `ApkInfo.getUsesLibrary()` | 共享库列表 |
| `sdkInfo` | `ApkInfo.getSdkInfo()` | SDK 版本信息 |
| `sdkInfo.minSdkVersion` | `SdkInfo.getMinSdkVersion()` | 最低 SDK |
| `sdkInfo.targetSdkVersion` | `SdkInfo.getTargetSdkVersion()` | 目标 SDK |
| `sdkInfo.maxSdkVersion` | `SdkInfo.getMaxSdkVersion()` | 最高 SDK |
| `versionInfo` | `ApkInfo.getVersionInfo()` | 应用版本信息 |
| `versionInfo.versionCode` | `VersionInfo.getVersionCode()` | 版本号 |
| `versionInfo.versionName` | `VersionInfo.getVersionName()` | 版本名称 |
| `resourcesInfo` | `ApkInfo.getResourcesInfo()` | 资源配置 |
| `resourcesInfo.packageId` | `ResourcesInfo.getPackageId()` | 资源包 ID |
| `resourcesInfo.packageName` | `ResourcesInfo.getPackageName()` | 资源包名称 |
| `resourcesInfo.sparseEntries` | `ResourcesInfo.isSparseEntries()` | 稀疏条目标志 |
| `resourcesInfo.compactEntries` | `ResourcesInfo.isCompactEntries()` | 紧凑条目标志 |
| `resourcesInfo.keepRawValues` | `ResourcesInfo.isKeepRawValues()` | 保留原始值标志 |
| `featureFlags` | `ApkInfo.getFeatureFlags()` | 功能标志映射 |
| `doNotCompress` | `ApkInfo.getDoNotCompress()` | 不压缩文件列表 |

---

## 完整使用示例

### 读取 APK 元信息

```java
import brut.androlib.meta.ApkInfo;
import brut.androlib.meta.SdkInfo;
import brut.androlib.meta.VersionInfo;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;

public class ReadApkInfo {
    public static void main(String[] args) throws AndrolibException {
        // 从解码后的目录加载 apktool.yml
        ApkInfo info = ApkInfo.load(new File("decoded_app/"));

        // 基本信息
        System.out.println("Apktool version: " + info.getVersion());
        System.out.println("APK file: " + info.getApkFileName());

        // SDK 信息
        SdkInfo sdk = info.getSdkInfo();
        System.out.println("Min SDK: " + sdk.getMinSdkVersion());
        System.out.println("Target SDK: " + sdk.getTargetSdkVersion());
        System.out.println("Target SDK (bounded): " + sdk.getTargetSdkVersionBounded());

        // 版本信息
        VersionInfo version = info.getVersionInfo();
        System.out.println("Version code: " + version.getVersionCode());
        System.out.println("Version name: " + version.getVersionName());

        // Framework 依赖
        if (!info.getUsesFramework().isEmpty()) {
            System.out.println("Framework tag: " + info.getUsesFramework().getTag());
            System.out.println("Framework IDs: " + info.getUsesFramework().getIds());
        }

        // 检查 APK 内容
        System.out.println("Has sources: " + info.hasSources());
        System.out.println("Has manifest: " + info.hasManifest());
        System.out.println("Has resources: " + info.hasResources());
    }
}
```

### 创建并保存 ApkInfo

```java
import brut.androlib.meta.ApkInfo;
import brut.androlib.meta.SdkInfo;
import brut.androlib.meta.VersionInfo;
import brut.androlib.meta.ResourcesInfo;
import brut.directory.ExtFile;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;

public class CreateApkInfo {
    public static void main(String[] args) throws AndrolibException {
        ApkInfo info = new ApkInfo();

        // 设置基本信息
        info.setVersion("2.9.3");
        info.setApkFileName("my-app.apk");

        // 设置 SDK 信息
        SdkInfo sdk = info.getSdkInfo();
        sdk.setMinSdkVersion("24");
        sdk.setTargetSdkVersion("34");

        // 设置版本信息
        VersionInfo version = info.getVersionInfo();
        version.setVersionCode(42);
        version.setVersionName("2.1.0");

        // 设置资源配置
        ResourcesInfo resInfo = info.getResourcesInfo();
        resInfo.setPackageName("com.example.myapp");
        resInfo.setCompactEntries(true);

        // 设置不压缩文件
        info.getDoNotCompress().add("assets/data.bin");
        info.getDoNotCompress().add("resources.arsc");

        // 关联 APK 文件
        info.setApkFile(new ExtFile(new File("my-app.apk")));

        // 保存到目录
        info.save(new File("decoded_output/"));
    }
}
```

### SDK 版本解析

```java
import brut.androlib.meta.SdkInfo;

public class SdkParseExample {
    public static void main(String[] args) {
        // 数字版本
        System.out.println(SdkInfo.parseSdkInt("33"));         // 33

        // 字母代号
        System.out.println(SdkInfo.parseSdkInt("T"));          // 33
        System.out.println(SdkInfo.parseSdkInt("S"));          // 31
        System.out.println(SdkInfo.parseSdkInt("SV2"));        // 32

        // 甜点名称
        System.out.println(SdkInfo.parseSdkInt("TIRAMISU"));   // 33
        System.out.println(SdkInfo.parseSdkInt("UPSIDE_DOWN_CAKE")); // 34
        System.out.println(SdkInfo.parseSdkInt("VANILLA_ICE_CREAM")); // 35
        System.out.println(SdkInfo.parseSdkInt("BAKLAVA"));    // 36

        // 开发版本
        System.out.println(SdkInfo.parseSdkInt("SDK_CUR_DEVELOPMENT")); // 10000
    }
}
```
