# ApkDecoder 解码参考

`ApkDecoder` 负责将 APK 文件解码为可读的目录结构，包括 smali 代码、XML 资源、AndroidManifest.xml 等。

**包名**: `brut.androlib.ApkDecoder`

## 构造函数

```java
public ApkDecoder(File apkFile, Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `java.io.File` | 待解码的 APK 文件 |
| `config` | `Config` | 解码配置对象 |

```java
Config config = new Config("3.0.3");
ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
```

## 主要方法

### decode -- 执行解码

```java
public void decode(File outDir) throws AndrolibException
```

将 APK 文件解码到指定的输出目录。解码过程包括：

1. 源代码解码（dex -> smali）
2. 资源文件解码（resources.arsc -> res/）
3. AndroidManifest.xml 解码（二进制 XML -> 可读 XML）
4. 原始文件拷贝（META-INF 签名文件 -> original/）
5. 原始目录拷贝（assets、lib 等）
6. 未知文件拷贝（非标准文件 -> unknown/）
7. 写入 apktool.yml 元数据

**多线程**: 当 `Config.getJobs() > 1` 时，dex 文件的 baksmali 处理会使用线程池并行执行。

| 参数 | 类型 | 说明 |
|------|------|------|
| `outDir` | `java.io.File` | 解码输出目录 |

**前置检查**:
- 如果输出目录已存在且 `forced` 未启用，抛出 `OutDirExistsException`
- 如果 APK 文件不存在或不可读，抛出 `InFileNotFoundException`

```java
Config config = new Config("3.0.3");
ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### getApkInfo -- 获取 APK 信息

```java
public ApkInfo getApkInfo()
```

返回解码过程中解析到的 `ApkInfo` 对象，包含 APK 的元数据信息（包名、版本号、SDK 信息、压缩配置等）。必须在调用 `decode()` 之后调用，否则返回 `null`。

```java
decoder.decode(new File("app-decoded"));
ApkInfo info = decoder.getApkInfo();
System.out.println("包名: " + info.getPackageInfo().getPackageName());
```

## 解码选项详解

### 源代码解码模式

通过 `Config.setDecodeSources()` 控制：

| 模式 | 行为 |
|------|------|
| `FULL` | 解码所有 dex 文件（classes.dex、classes2.dex 等）为 smali 目录 |
| `ONLY_MAIN_CLASSES` | 仅解码 classes.dex 为 `smali/` 目录（默认） |
| `NONE` | 保留原始 dex 文件不进行 baksmali |

```java
// 解码全部 dex 文件
config.setDecodeSources(Config.DecodeSources.FULL);
// 生成: smali/, smali_classes2/, smali_classes3/ ...

// 不解码源代码
config.setDecodeSources(Config.DecodeSources.NONE);
// 保留: classes.dex, classes2.dex ...
```

### 资源解码模式

通过 `Config.setDecodeResources()` 控制：

| 模式 | 行为 |
|------|------|
| `FULL` | 完整解码 resources.arsc 和 res 目录，解码 AndroidManifest.xml（默认） |
| `ONLY_MANIFEST` | 仅解码 AndroidManifest.xml，保留原始 resources.arsc |
| `NONE` | 保留原始二进制的 resources.arsc 和 AndroidManifest.xml |

```java
// 完整解码所有资源
config.setDecodeResources(Config.DecodeResources.FULL);

// 仅解码 Manifest（快速查看包名、权限等）
config.setDecodeResources(Config.DecodeResources.ONLY_MANIFEST);

// 不解码任何资源
config.setDecodeResources(Config.DecodeResources.NONE);
```

### Assets 处理模式

通过 `Config.setDecodeAssets()` 控制：

| 模式 | 行为 |
|------|------|
| `FULL` | 拷贝 assets 目录到输出目录（默认） |
| `NONE` | 跳过 assets 目录 |

```java
// 跳过 assets（减小输出体积）
config.setDecodeAssets(Config.DecodeAssets.NONE);
```

### 覆盖已有目录

通过 `Config.setForced()` 控制。默认情况下，如果输出目录已存在，解码会失败。设置 `forced = true` 后会先删除已有目录再解码。

```java
config.setForced(true);  // 允许覆盖已存在的输出目录
```

### 资源冲突处理

当 APK 使用了多个共享库导致资源 ID 冲突时，可通过 `Config.setDecodeResolve()` 选择不同的解析策略：

```java
// 贪婪解析，尽可能解析所有冲突引用
config.setDecodeResolve(Config.DecodeResolve.GREEDY);

// 懒加载解析
config.setDecodeResolve(Config.DecodeResolve.LAZY);
```

### 保留损坏资源

```java
config.setKeepBrokenResources(true);
```

某些 APK 使用了非标准的资源混淆或打包方式，正常解码可能导致部分资源文件缺失。启用此选项后，即使部分资源解码失败，也会尽可能保留已解码的内容。

## 解码输出结构

```
app-decoded/
├── AndroidManifest.xml      # 解码后的 Manifest（可读 XML）
├── apktool.yml               # Apktool 元数据
├── assets/                   # assets 目录（如未跳过）
├── lib/                      # 原生库（按架构分目录）
│   ├── arm64-v8a/
│   ├── armeabi-v7a/
│   └── x86/
├── original/                 # 原始签名文件
│   └── META-INF/
├── res/                      # 解码后的资源文件
│   ├── drawable/
│   ├── layout/
│   ├── values/
│   └── ...
├── smali/                    # classes.dex 反编译的 smali 代码
├── smali_classes2/           # classes2.dex 反编译的 smali 代码（如有）
├── unknown/                  # 非标准文件
└── resources.arsc            # 原始资源表（仅在 decodeResources != FULL 时保留）
```

## 异常说明

| 异常类 | 触发条件 |
|--------|----------|
| `InFileNotFoundException` | 输入 APK 文件不存在或不可读 |
| `OutDirExistsException` | 输出目录已存在且未设置 `forced` |
| `FrameworkNotFoundException` | 缺少对应的框架资源文件 |
| `AndrolibException` | 解码过程中的其他错误（IO 异常、格式错误等） |

所有异常均继承自 `brut.androlib.exceptions.AndrolibException`。

## 完整使用示例

### 基本解码

```java
import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import java.io.File;

Config config = new Config("3.0.3");
ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-decoded"));
```

### 完整解码所有 dex 文件

```java
Config config = new Config("3.0.3");
config.setDecodeSources(Config.DecodeSources.FULL);
config.setForced(true);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-full-decoded"));
```

### 仅解码 Manifest（快速预览）

```java
Config config = new Config("3.0.3");
config.setDecodeSources(Config.DecodeSources.NONE);
config.setDecodeResources(Config.DecodeResources.ONLY_MANIFEST);
config.setDecodeAssets(Config.DecodeAssets.NONE);

ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);
decoder.decode(new File("app-manifest-only"));
```

### 处理混淆 APK

```java
Config config = new Config("3.0.3");
config.setKeepBrokenResources(true);
config.setDecodeResolve(Config.DecodeResolve.GREEDY);
config.setForced(true);

ApkDecoder decoder = new ApkDecoder(new File("obfuscated.apk"), config);
decoder.decode(new File("obfuscated-decoded"));
```

### 异常处理

```java
import brut.androlib.Config;
import brut.androlib.ApkDecoder;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.exceptions.InFileNotFoundException;
import brut.androlib.exceptions.OutDirExistsException;
import brut.androlib.exceptions.FrameworkNotFoundException;
import java.io.File;

Config config = new Config("3.0.3");
ApkDecoder decoder = new ApkDecoder(new File("app.apk"), config);

try {
    decoder.decode(new File("app-decoded"));
} catch (InFileNotFoundException e) {
    System.err.println("APK 文件不存在: " + e.getMessage());
} catch (OutDirExistsException e) {
    System.err.println("输出目录已存在: " + e.getMessage());
} catch (FrameworkNotFoundException e) {
    System.err.println("缺少框架资源: " + e.getMessage());
} catch (AndrolibException e) {
    System.err.println("解码失败: " + e.getMessage());
}
```
