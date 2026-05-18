# ApkBuilder 构建参考

`ApkBuilder` 负责将解码后的目录结构重新构建为 APK 文件，包括 smali 编译、资源打包、Manifest 处理等。

**包名**: `brut.androlib.ApkBuilder`

## 构造函数

```java
public ApkBuilder(File apkDir, Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkDir` | `java.io.File` | 解码后的 APK 目录（包含 smali、res、AndroidManifest.xml 等） |
| `config` | `Config` | 构建配置对象 |

```java
Config config = new Config("3.0.3");
ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
```

## 主要方法

### build -- 执行构建

```java
public void build(File outApk) throws AndrolibException
```

将解码后的目录重新打包为 APK 文件。构建过程包括：

1. 从 `apktool.yml` 加载 APK 元数据
2. 编译 smali 代码为 dex 文件（或拷贝原始 dex）
3. 通过 AAPT 打包资源和 Manifest
4. 拷贝原始签名文件（如启用 `copyOriginal`）
5. 将所有内容打包为最终的 APK 文件

**多线程**: 当 `Config.getJobs() > 1` 时，smali -> dex 的编译过程会使用线程池并行执行。

| 参数 | 类型 | 说明 |
|------|------|------|
| `outApk` | `java.io.File` | 输出 APK 文件路径。可为 `null`，此时使用默认路径 `<apkDir>/dist/<original_name>` |

```java
Config config = new Config("3.0.3");
ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-rebuilt.apk"));
```

## 构建选项详解

### forced -- 强制重建

通过 `Config.setForced(true)` 启用。构建时会忽略文件时间戳比较，始终重新编译所有内容。

```java
config.setForced(true);  // 忽略缓存，强制完整重建
```

默认情况下（`forced = false`），构建过程会比较源文件和输出文件的时间戳，跳过未发生变化的部分以加快构建速度。

### debuggable -- 可调试标记

```java
config.setDebuggable(true);
```

构建时将 AndroidManifest.xml 中的 `android:debuggable` 属性设为 `true`。适用于需要 attach 调试器的场景。构建完成后 Manifest 会被恢复为原始状态。

### netSecConf -- 网络安全配置

```java
config.setNetSecConf(true);
```

构建时注入一个宽松的网络安全配置文件（`res/xml/network_security_config.xml`），并在 Manifest 中引用它。该配置允许明文 HTTP 流量，适用于使用抓包工具（如 mitmproxy、Charles）分析网络请求的场景。

注意：如果目标 SDK 版本低于 24（Android 7.0），网络安全配置可能被系统忽略，此时会输出警告日志。

### copyOriginal -- 拷贝原始签名

```java
config.setCopyOriginal(true);
```

构建时将 `original/META-INF/` 目录下的原始签名文件拷贝到新的 APK 中。注意：由于 APK 内容已修改，原始签名将失效，仅用于参考。

### noApk -- 不生成 APK 文件

```java
config.setNoApk(true);
```

仅执行 smali 编译和资源打包步骤，不生成最终的 APK 文件。适用于只需要中间产物的场景。此时 `outApk` 参数将被忽略。

### noCrunch -- 禁用资源压缩

```java
config.setNoCrunch(true);
```

构建时跳过 AAPT 的资源压缩（crunch）步骤。PNG 等资源文件将原样打包，不会被 AAPT 优化。可加快构建速度，但会增加 APK 体积。

### aaptBinary -- 自定义 AAPT

```java
config.setAaptBinary("/usr/local/bin/aapt2");
```

指定自定义的 AAPT 二进制文件路径。默认使用内置的 AAPT。

## 构建流程详解

### 源代码编译

`ApkBuilder` 会自动识别解码后的目录结构并执行对应的编译：

- `smali/` 目录 -> 编译为 `classes.dex`
- `smali_classes2/` 目录 -> 编译为 `classes2.dex`
- `smali_classes3/` 目录 -> 编译为 `classes3.dex`
- 以此类推...

如果目录中存在原始的 `.dex` 文件（未进行 smali 解码的情况），则会直接拷贝。

### 资源打包

资源打包策略根据输入目录的内容自动选择：

| 输入内容 | 打包策略 |
|----------|----------|
| 明文 AndroidManifest.xml + res/ 目录 | 通过 AAPT 完整打包 |
| 明文 AndroidManifest.xml（无 res/） | 仅打包 Manifest |
| 二进制 AndroidManifest.xml | 直接拷贝 |
| 二进制 resources.arsc | 直接拷贝 |

### 最终打包

所有编译产物会被打包到最终的 APK 文件中，包括：

1. AAPT 输出（Manifest、resources.arsc、res/）
2. dex 文件（编译产物或原始拷贝）
3. assets 目录（如存在）
4. lib 目录（原生库，如存在）
5. unknown 目录（未知文件，如存在）
6. 原始签名文件（如启用 `copyOriginal`）

压缩配置会参考 `apktool.yml` 中的 `doNotCompress` 设置。

## 异常说明

| 异常类 | 触发条件 |
|--------|----------|
| `AndrolibException` | 构建过程中的各类错误（smali 编译失败、AAPT 错误、IO 异常等） |

## 完整使用示例

### 基本构建

```java
import brut.androlib.Config;
import brut.androlib.ApkBuilder;
import java.io.File;

Config config = new Config("3.0.3");
ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-rebuilt.apk"));
```

### 使用默认输出路径

```java
Config config = new Config("3.0.3");
ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
// 输出到 app-decoded/dist/<original_name>.apk
builder.build(null);
```

### 构建可调试的 APK（用于逆向分析）

```java
Config config = new Config("3.0.3");
config.setDebuggable(true);
config.setNetSecConf(true);
config.setForced(true);

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-debug.apk"));
```

### 仅编译 smali 不打包 APK

```java
Config config = new Config("3.0.3");
config.setNoApk(true);

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(null);  // 不会生成 APK 文件
// 中间产物在 app-decoded/build/apk/ 目录下
```

### 快速构建（禁用资源压缩）

```java
Config config = new Config("3.0.3");
config.setNoCrunch(true);

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-fast.apk"));
```

### 使用自定义 AAPT

```java
Config config = new Config("3.0.3");
config.setAaptBinary("/usr/local/bin/aapt2");

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);
builder.build(new File("app-custom-aapt.apk"));
```

### 完整的解码 -> 修改 -> 重建流程

```java
import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkBuilder;
import java.io.File;

// 1. 解码 APK
Config decodeConfig = new Config("3.0.3");
decodeConfig.setDecodeSources(Config.DecodeSources.FULL);
decodeConfig.setForced(true);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), decodeConfig);
decoder.decode(new File("app-decoded"));

// 2. 在此修改 app-decoded 目录中的内容（修改 smali 代码、资源等）
// ...

// 3. 重新构建 APK
Config buildConfig = new Config("3.0.3");
buildConfig.setForced(true);

ApkBuilder builder = new ApkBuilder(new File("app-decoded"), buildConfig);
builder.build(new File("app-modified.apk"));
```

### 异常处理

```java
import brut.androlib.exceptions.AndrolibException;

Config config = new Config("3.0.3");
ApkBuilder builder = new ApkBuilder(new File("app-decoded"), config);

try {
    builder.build(new File("app-rebuilt.apk"));
} catch (AndrolibException e) {
    System.err.println("构建失败: " + e.getMessage());
    e.printStackTrace();
}
```
