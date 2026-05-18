# Config 配置参考

`Config` 类是 AI-Apktool 的核心配置对象，用于控制 APK 解码（decode）和构建（build）的全部行为。

**包名**: `brut.androlib.Config`

## 构造函数

```java
public Config(String version)
```

创建一个新的 Config 实例。`version` 参数用于标识 Apktool 版本号，该版本号会写入解码后的 `apktool.yml` 中。

```java
Config config = new Config("3.0.3");
```

## 枚举类型

### DecodeSources

控制源代码（dex 文件）的解码策略。

| 值 | 说明 |
|------|------|
| `FULL` | 解码所有 dex 文件（包括 classes.dex、classes2.dex 等） |
| `ONLY_MAIN_CLASSES` | 仅解码 classes.dex（默认值） |
| `NONE` | 不解码源代码，保留原始 dex 文件 |

### DecodeResources

控制资源文件的解码策略。

| 值 | 说明 |
|------|------|
| `FULL` | 完整解码资源（包括 resources.arsc 和 res 目录）（默认值） |
| `ONLY_MANIFEST` | 仅解码 AndroidManifest.xml，保留原始 resources.arsc |
| `NONE` | 不解码资源文件，保留原始二进制形式 |

### DecodeResolve

控制资源冲突的解析策略。

| 值 | 说明 |
|------|------|
| `DEFAULT` | 默认解析策略（默认值） |
| `GREEDY` | 贪婪解析，尽可能解析所有引用 |
| `LAZY` | 懒加载解析，仅在需要时解析 |

### DecodeAssets

控制 assets 目录的处理策略。

| 值 | 说明 |
|------|------|
| `FULL` | 正常拷贝 assets 目录（默认值） |
| `NONE` | 不拷贝 assets 目录 |

## 通用选项

### jobs -- 并发线程数

```java
public int getJobs()
public void setJobs(int jobs)
```

控制解码和构建过程中使用的并发线程数。默认值取 `min(可用处理器核心数, 8)`。多线程主要用于 dex 文件的并行 baksmali/smali 处理。

```java
Config config = new Config("3.0.3");
config.setJobs(4);  // 使用 4 个线程
```

### frameworkDirectory -- 框架目录

```java
public String getFrameworkDirectory()
public void setFrameworkDirectory(String frameworkDirectory)
```

指定框架资源文件（framework-res.apk）的存储目录。默认为 `null`，表示使用操作系统默认路径（参见 [Framework 管理参考](framework.md)）。

```java
config.setFrameworkDirectory("/path/to/my/frameworks");
```

### frameworkTag -- 框架标签

```java
public String getFrameworkTag()
public void setFrameworkTag(String frameworkTag)
```

为框架资源文件添加标签，用于区分不同厂商或版本的框架。标签会附加到框架文件名中，例如 `1-tag.apk`。

```java
config.setFrameworkTag("samsung");
```

### libraryFiles -- 共享库文件

```java
public Map<String, String[]> getLibraryFiles()
```

获取共享库文件映射。该映射为 `Map<String, String[]>`，键为库的包名，值为该库包含的资源文件列表。

```java
Map<String, String[]> libs = config.getLibraryFiles();
libs.put("com.example.library", new String[]{"res/values/strings.xml"});
```

### forced -- 强制覆盖

```java
public boolean isForced()
public void setForced(boolean forced)
```

当设为 `true` 时，如果输出目录已存在则直接覆盖（删除后重建）；否则会在输出目录已存在时抛出 `OutDirExistsException`。默认值为 `false`。

```java
config.setForced(true);  // 强制覆盖已存在的输出目录
```

### verbose -- 详细日志

```java
public boolean isVerbose()
public void setVerbose(boolean verbose)
```

启用详细日志输出。默认值为 `false`。

```java
config.setVerbose(true);  // 启用详细日志
```

## 解码选项

### decodeSources -- 源代码解码模式

```java
public boolean isDecodeSourcesFull()
public boolean isDecodeSourcesNone()
public void setDecodeSources(DecodeSources decodeSources)
```

控制 dex 文件的解码行为。

```java
// 解码所有 dex 文件为 smali
config.setDecodeSources(Config.DecodeSources.FULL);

// 仅解码 classes.dex（默认行为）
config.setDecodeSources(Config.DecodeSources.ONLY_MAIN_CLASSES);

// 不解码，保留原始 dex 文件
config.setDecodeSources(Config.DecodeSources.NONE);
```

### baksmaliDebugMode -- Baksmali 调试模式

```java
public boolean isBaksmaliDebugMode()
public void setBaksmaliDebugMode(boolean baksmaliDebugMode)
```

控制 baksmali 是否输出调试信息（行号、局部变量等）。默认值为 `true`。设为 `false` 可获得更紧凑的 smali 代码。

```java
config.setBaksmaliDebugMode(false);  // 不输出调试信息
```

### decodeResources -- 资源解码模式

```java
public boolean isDecodeResourcesFull()
public boolean isDecodeResourcesNone()
public void setDecodeResources(DecodeResources decodeResources)
```

控制资源文件的解码行为。

```java
// 完整解码资源（默认行为）
config.setDecodeResources(Config.DecodeResources.FULL);

// 仅解码 AndroidManifest.xml
config.setDecodeResources(Config.DecodeResources.ONLY_MANIFEST);

// 不解码资源（保留原始 resources.arsc 和 AndroidManifest.xml）
config.setDecodeResources(Config.DecodeResources.NONE);
```

### decodeResolve -- 资源解析策略

```java
public boolean isDecodeResolveLazy()
public boolean isDecodeResolveGreedy()
public void setDecodeResolve(DecodeResolve decodeResolve)
```

控制资源引用的解析策略。当 APK 中存在资源 ID 冲突时（通常出现在使用了多个共享库的情况下），选择不同的解析模式会影响解码结果。

```java
// 贪婪解析（适用于使用了多个共享库的 APK）
config.setDecodeResolve(Config.DecodeResolve.GREEDY);

// 懒加载解析
config.setDecodeResolve(Config.DecodeResolve.LAZY);
```

### keepBrokenResources -- 保留损坏资源

```java
public boolean isKeepBrokenResources()
public void setKeepBrokenResources(boolean keepBrokenResources)
```

当资源解码过程中遇到错误时，保留已解码的部分资源而非中断。适用于解码使用了非标准资源混淆的 APK。默认值为 `false`。

```java
config.setKeepBrokenResources(true);  // 保留损坏的资源
```

### ignoreRawValues -- 忽略原始值

```java
public boolean isIgnoreRawValues()
public void setIgnoreRawValues(boolean ignoreRawValues)
```

解码时忽略 `values` 目录中的原始 XML 值。默认值为 `false`。

```java
config.setIgnoreRawValues(true);
```

### analysisMode -- 分析模式

```java
public boolean isAnalysisMode()
public void setAnalysisMode(boolean analysisMode)
```

启用分析模式，在解码过程中收集额外的分析信息。默认值为 `false`。

```java
config.setAnalysisMode(true);  // 启用分析模式
```

### decodeAssets -- Assets 解码模式

```java
public boolean isDecodeAssetsFull()
public boolean isDecodeAssetsNone()
public void setDecodeAssets(DecodeAssets decodeAssets)
```

控制 assets 目录的处理方式。

```java
// 正常拷贝 assets 目录（默认行为）
config.setDecodeAssets(Config.DecodeAssets.FULL);

// 跳过 assets 目录
config.setDecodeAssets(Config.DecodeAssets.NONE);
```

## 构建选项

### noApk -- 不生成 APK

```java
public boolean isNoApk()
public void setNoApk(boolean noApk)
```

仅编译源代码和资源，不生成最终的 APK 文件。适用于只需要 smali -> dex 的编译场景。默认值为 `false`。

```java
config.setNoApk(true);  // 不打包 APK
```

### noCrunch -- 禁用资源压缩

```java
public boolean isNoCrunch()
public void setNoCrunch(boolean noCrunch)
```

构建时不对 PNG 等资源进行压缩预处理。可以加快构建速度，但可能导致部分优化被跳过。默认值为 `false`。

```java
config.setNoCrunch(true);  // 禁用资源压缩
```

### copyOriginal -- 拷贝原始签名文件

```java
public boolean isCopyOriginal()
public void setCopyOriginal(boolean copyOriginal)
```

构建时将 `original/` 目录下的原始签名文件（META-INF）拷贝到新的 APK 中。默认值为 `false`。

```java
config.setCopyOriginal(true);  // 保留原始签名
```

### debuggable -- 可调试标记

```java
public boolean isDebuggable()
public void setDebuggable(boolean debuggable)
```

在构建时将 AndroidManifest.xml 中的 `android:debuggable` 属性设为 `true`。适用于需要调试的场景。默认值为 `false`。

```java
config.setDebuggable(true);  // 标记为可调试
```

### netSecConf -- 网络安全配置

```java
public boolean isNetSecConf()
public void setNetSecConf(boolean netSecConf)
```

构建时注入一个宽松的网络安全配置（network security config），允许明文流量。适用于抓包分析等逆向场景。默认值为 `false`。

```java
config.setNetSecConf(true);  // 注入宽松的网络安全配置
```

### aaptBinary -- 自定义 AAPT 二进制

```java
public String getAaptBinary()
public void setAaptBinary(String aaptBinary)
```

指定自定义的 AAPT（Android Asset Packaging Tool）二进制文件路径。默认为 `null`，表示使用内置的 AAPT。

```java
config.setAaptBinary("/usr/local/bin/aapt2");
```

## 默认值汇总

| 属性 | 默认值 |
|------|--------|
| `version` | 由构造函数参数指定 |
| `jobs` | `min(可用处理器核心数, 8)` |
| `frameworkDirectory` | `null`（使用系统默认路径） |
| `frameworkTag` | `null` |
| `libraryFiles` | 空 `LinkedHashMap` |
| `forced` | `false` |
| `verbose` | `false` |
| `decodeSources` | `DecodeSources.ONLY_MAIN_CLASSES` |
| `baksmaliDebugMode` | `true` |
| `decodeResources` | `DecodeResources.FULL` |
| `decodeResolve` | `DecodeResolve.DEFAULT` |
| `keepBrokenResources` | `false` |
| `ignoreRawValues` | `false` |
| `analysisMode` | `false` |
| `decodeAssets` | `DecodeAssets.FULL` |
| `noApk` | `false` |
| `noCrunch` | `false` |
| `copyOriginal` | `false` |
| `debuggable` | `false` |
| `netSecConf` | `false` |
| `aaptBinary` | `null` |

## 完整使用示例

### 场景一：默认解码

```java
Config config = new Config("3.0.3");
ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### 场景二：完整解码所有内容

```java
Config config = new Config("3.0.3");
config.setDecodeSources(Config.DecodeSources.FULL);
config.setDecodeResources(Config.DecodeResources.FULL);
config.setDecodeAssets(Config.DecodeAssets.FULL);
config.setForced(true);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### 场景三：仅解码 Manifest

```java
Config config = new Config("3.0.3");
config.setDecodeSources(Config.DecodeSources.NONE);
config.setDecodeResources(Config.DecodeResources.ONLY_MANIFEST);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### 场景四：构建可调试的 APK

```java
Config config = new Config("3.0.3");
config.setDebuggable(true);
config.setNetSecConf(true);
config.setForced(true);

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-debug.apk"));
```

### 场景五：处理资源混淆的 APK

```java
Config config = new Config("3.0.3");
config.setKeepBrokenResources(true);
config.setDecodeResolve(Config.DecodeResolve.GREEDY);
config.setForced(true);

ApkDecoder decoder = new ApkDecoder(new File("obfuscated.apk"), config);
decoder.decode(new File("obfuscated-decoded"));
```
