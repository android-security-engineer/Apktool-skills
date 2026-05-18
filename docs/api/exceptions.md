# Exception 类参考

本文档详细说明 AI-Apktool 中所有异常类的层次结构、构造方法、使用场景以及最佳实践。

---

## 异常层次结构

```
java.lang.Exception
  └── brut.common.BrutException                    (brut.* 异常基类)
        └── brut.androlib.exceptions.AndrolibException   (APK 操作异常基类)
              ├── FrameworkNotFoundException
              ├── InFileNotFoundException
              ├── OutDirExistsException
              ├── RawXmlEncounteredException
              ├── UndefinedResObjectException
              └── NinePatchNotFoundException
```

---

## BrutException

**包**: `brut.common.BrutException`
**源文件**: `brut.j.common/src/main/java/brut/common/BrutException.java`

所有 `brut.*` 模块异常的基类，继承自 `java.lang.Exception`。该类作为整个工具链中 checked exception 的根，为 `brut.j.dir`、`brut.j.util`、`brut.apktool` 等子模块提供统一的异常抽象。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `BrutException()` | 创建无详细信息的异常实例 |
| `BrutException(String message)` | 创建带有错误描述信息的异常实例 |
| `BrutException(Throwable cause)` | 创建包装底层异常的实例 |
| `BrutException(String message, Throwable cause)` | 创建带有错误描述和底层异常的实例 |

### 源码参考

```java
package brut.common;

public class BrutException extends Exception {

    public BrutException() {
        super();
    }

    public BrutException(String message) {
        super(message);
    }

    public BrutException(Throwable cause) {
        super(cause);
    }

    public BrutException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## AndrolibException

**包**: `brut.androlib.exceptions.AndrolibException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/AndrolibException.java`

APK 操作的通用基类异常。所有 APK 解码 (decode)、构建 (build)、分析 (analyze) 过程中产生的异常均直接或间接继承此类。`ApkDecoder`、`ApkBuilder`、`ApkAnalyzer` 等核心类的方法签名中大量使用此异常。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `AndrolibException()` | 创建无详细信息的异常实例 |
| `AndrolibException(String message)` | 创建带有错误描述信息的异常实例 |
| `AndrolibException(Throwable cause)` | 创建包装底层异常的实例，常用于包装 `IOException`、`DirectoryException` 等 |
| `AndrolibException(String message, Throwable cause)` | 创建带有错误描述和底层异常的实例 |

### 使用场景

- `ApkDecoder.decode()` 方法在解码过程中遇到错误时抛出
- `ApkBuilder.build()` 方法在构建过程中遇到错误时抛出
- `ApkAnalyzer` 的所有分析方法在解析失败时抛出
- `ApkInfo.load()` / `ApkInfo.save()` 在 YAML 读写失败时抛出
- `ResDecoder`、`ResTable` 等资源处理类在资源解析失败时抛出

### 源码参考

```java
package brut.androlib.exceptions;

import brut.common.BrutException;

public class AndrolibException extends BrutException {

    public AndrolibException() {
        super();
    }

    public AndrolibException(String message) {
        super(message);
    }

    public AndrolibException(Throwable cause) {
        super(cause);
    }

    public AndrolibException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## FrameworkNotFoundException

**包**: `brut.androlib.exceptions.FrameworkNotFoundException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/FrameworkNotFoundException.java`

当解码 APK 时找不到指定 package ID 对应的 framework 资源文件时抛出此异常。通常出现在解码厂商定制 ROM 的 APK 或系统应用时，因为它们依赖特定的 framework-res.apk。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `FrameworkNotFoundException(int pkgId)` | 根据未找到的 framework package ID 创建异常，自动生成提示信息 |

### 异常消息格式

```
Could not find framework resources for package ID {pkgId}.
You must install proper framework files, see project website for more info.
```

### 典型触发场景

- 解码使用了厂商定制 framework 的系统 APK
- framework 文件被删除或路径配置错误
- 使用了错误的 framework tag

### 解决方式

使用 `install-framework` 命令安装对应的 framework 文件：

```bash
apktool if framework-res.apk
apktool if samsung_framework-res.apk -t samsung
```

然后重新执行 decode 命令。

### 源码参考

```java
package brut.androlib.exceptions;

public class FrameworkNotFoundException extends AndrolibException {

    public FrameworkNotFoundException(int pkgId) {
        super("Could not find framework resources for package ID " + pkgId + "." + System.lineSeparator()
            + "You must install proper framework files, see project website for more info.");
    }
}
```

---

## InFileNotFoundException

**包**: `brut.androlib.exceptions.InFileNotFoundException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/InFileNotFoundException.java`

当指定的输入 APK 文件不存在或不可读时抛出此异常。在 `ApkDecoder` 开始解码之前进行文件可用性检查时触发。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `InFileNotFoundException(String path)` | 根据文件路径创建异常，自动生成描述信息 |

### 异常消息格式

```
Input file ({path}) was not found or was not readable.
```

### 典型触发场景

- APK 文件路径拼写错误
- 文件权限不足导致无法读取
- 相对路径解析不正确

### 源码参考

```java
package brut.androlib.exceptions;

public class InFileNotFoundException extends AndrolibException {

    public InFileNotFoundException(String path) {
        super("Input file (" + path + ") was not found or was not readable.");
    }
}
```

---

## OutDirExistsException

**包**: `brut.androlib.exceptions.OutDirExistsException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/OutDirExistsException.java`

当输出目录已经存在且未设置 force 标志时抛出此异常。这是为了防止意外覆盖已解码的内容。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `OutDirExistsException(String path)` | 根据输出目录路径创建异常，自动生成提示信息 |

### 异常消息格式

```
Destination directory ({path}) already exists. Use -f option if you want to overwrite it.
```

### 典型触发场景

- 重复执行 decode 命令但未使用 `-f` 选项
- 输出目录被其他进程占用

### 解决方式

添加 `-f` / `--force` 选项强制覆盖：

```bash
apktool d app.apk -f
```

### 源码参考

```java
package brut.androlib.exceptions;

public class OutDirExistsException extends AndrolibException {

    public OutDirExistsException(String path) {
        super("Destination directory (" + path + ") already exists. Use -f option if you want to overwrite it.");
    }
}
```

---

## RawXmlEncounteredException

**包**: `brut.androlib.exceptions.RawXmlEncounteredException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/RawXmlEncounteredException.java`

当解码过程中遇到无法解析的原始 XML 数据时抛出此异常。通常出现在某些被混淆或故意损坏的 APK 中，其 XML 文件格式不符合 Android 二进制 XML 规范。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `RawXmlEncounteredException(Throwable cause)` | 包装底层解析异常，附带固定的 "Could not decode XML." 错误描述 |

### 异常消息格式

```
Could not decode XML.
```

同时通过 `getCause()` 可获取导致此异常的底层异常信息。

### 典型触发场景

- APK 中的 XML 文件被加壳或混淆
- 二进制 XML 格式损坏
- 非标准 Android XML 编码格式

### 源码参考

```java
package brut.androlib.exceptions;

public class RawXmlEncounteredException extends AndrolibException {

    public RawXmlEncounteredException(Throwable cause) {
        super("Could not decode XML.", cause);
    }
}
```

---

## UndefinedResObjectException

**包**: `brut.androlib.exceptions.UndefinedResObjectException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/UndefinedResObjectException.java`

当引用了未定义的资源对象时抛出此异常。例如引用了不存在的资源 ID 或资源名称。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `UndefinedResObjectException(String message)` | 创建带有具体描述信息的异常实例 |

### 典型触发场景

- 资源文件中引用了不存在的资源 ID
- framework 资源缺失导致引用无法解析
- 资源表 (resources.arsc) 损坏或不完整

### 源码参考

```java
package brut.androlib.exceptions;

public class UndefinedResObjectException extends AndrolibException {

    public UndefinedResObjectException(String message) {
        super(message);
    }
}
```

---

## NinePatchNotFoundException

**包**: `brut.androlib.exceptions.NinePatchNotFoundException`
**源文件**: `brut.apktool/apktool-lib/src/main/java/brut/androlib/exceptions/NinePatchNotFoundException.java`

当处理 9-patch (.9.png) 图片但找不到对应的 nine-patch chunk 数据时抛出此异常。通常出现在图片文件损坏或格式不正确的情况下。

### 构造方法

| 构造方法 | 说明 |
|---------|------|
| `NinePatchNotFoundException()` | 创建异常实例，附带固定的 "Could not find nine patch chunk." 错误描述 |

### 异常消息格式

```
Could not find nine patch chunk.
```

### 典型触发场景

- .9.png 文件缺少 nine-patch 边框数据
- PNG 文件的 nine-patch chunk 被移除或损坏
- 非 9-patch 图片被错误地标记为 9-patch

### 源码参考

```java
package brut.androlib.exceptions;

public class NinePatchNotFoundException extends AndrolibException {

    public NinePatchNotFoundException() {
        super("Could not find nine patch chunk.");
    }
}
```

---

## 异常处理最佳实践

### 1. 基本异常捕获模式

在调用 APK 操作 API 时，应按照从具体到一般的顺序捕获异常：

```java
import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.*;

import java.io.File;

public class DecodeExample {
    public static void main(String[] args) {
        Config config = new Config("2.9.3");
        File apkFile = new File("app.apk");
        File outDir = new File("app_out");

        try {
            new ApkDecoder(apkFile, config).decode(outDir);
            System.out.println("Decode succeeded.");
        } catch (InFileNotFoundException e) {
            // 输入文件不存在 -- 检查文件路径
            System.err.println("File not found: " + e.getMessage());
        } catch (OutDirExistsException e) {
            // 输出目录已存在 -- 使用 -f 或清理目录
            System.err.println("Output exists: " + e.getMessage());
        } catch (FrameworkNotFoundException e) {
            // 缺少 framework -- 需要先安装
            System.err.println("Missing framework: " + e.getMessage());
        } catch (AndrolibException e) {
            // 其他 APK 操作错误
            System.err.println("Decode error: " + e.getMessage());
        }
    }
}
```

### 2. 分析操作异常处理

`ApkAnalyzer` 的分析方法同样抛出 `AndrolibException`：

```java
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ApkSummary;
import brut.androlib.analyze.ManifestInfo;
import brut.androlib.analyze.SecurityReport;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;

public class AnalyzeExample {
    public static void main(String[] args) {
        Config config = new Config("2.9.3");

        try {
            ApkAnalyzer analyzer = new ApkAnalyzer(new File("app.apk"), config);

            // 获取 APK 概要信息
            ApkSummary summary = analyzer.getSummary();
            System.out.println("Package: " + summary.getPackageName());
            System.out.println("DEX count: " + summary.getDexCount());

            // 获取 Manifest 信息
            ManifestInfo manifest = analyzer.getManifestInfo();
            if (manifest == null) {
                System.err.println("No AndroidManifest.xml found in APK.");
                return;
            }
            System.out.println("Permissions: " + manifest.getPermissions().size());

            // 获取安全报告
            SecurityReport report = analyzer.getSecurityReport();
            System.out.println("Risk score: " + report.getRiskScore() + "/100");

        } catch (AndrolibException e) {
            System.err.println("Analysis failed: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        }
    }
}
```

### 3. 搜索操作异常处理

```java
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;

public class SearchExample {
    public static void main(String[] args) {
        Config config = new Config("2.9.3");

        try {
            ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

            // 搜索类名
            SearchResult classResult = searcher.searchClasses("Activity");
            System.out.println("Found " + classResult.getTotalMatches() + " matching classes");

            // 搜索字符串
            SearchResult stringResult = searcher.searchStrings("http://.*");
            System.out.println("Found " + stringResult.getTotalMatches() + " matching strings");

            // 搜索方法名
            SearchResult methodResult = searcher.searchMethods("onCreate");
            System.out.println("Found " + methodResult.getTotalMatches() + " matching methods");

        } catch (AndrolibException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
    }
}
```

### 4. 异常链追踪

当 `AndrolibException` 包装了底层异常时，应遍历异常链获取根因：

```java
try {
    // APK 操作...
} catch (AndrolibException e) {
    System.err.println("Error: " + e.getMessage());

    Throwable cause = e.getCause();
    while (cause != null) {
        System.err.println("  Caused by: " + cause.getClass().getSimpleName()
            + " - " + cause.getMessage());
        cause = cause.getCause();
    }
}
```

### 5. HTTP API 中的异常处理

在 HTTP 服务端场景中，应将异常转换为对客户端友好的 JSON 响应：

```java
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.output.JsonOutput;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiExceptionHandler {

    public static String safeAnalyze(String apkPath, Config config) {
        try {
            ApkAnalyzer analyzer = new ApkAnalyzer(new File(apkPath), config);
            return JsonOutput.toJson(analyzer.getSummary());
        } catch (AndrolibException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return JsonOutput.toJson(error);
        }
    }
}
```

### 6. 批量处理中的容错模式

在批量分析多个 APK 文件时，应逐个捕获异常以避免一个失败导致全部中断：

```java
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.analyze.ApkSummary;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BatchAnalyze {
    public static void main(String[] args) {
        Config config = new Config("2.9.3");
        File[] apkFiles = new File("apks/").listFiles((d, n) -> n.endsWith(".apk"));

        List<ApkSummary> results = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        if (apkFiles != null) {
            for (File apk : apkFiles) {
                try {
                    ApkAnalyzer analyzer = new ApkAnalyzer(apk, config);
                    results.add(analyzer.getSummary());
                } catch (AndrolibException e) {
                    failures.add(apk.getName() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Analyzed: " + results.size());
        System.out.println("Failed: " + failures.size());
        for (String failure : failures) {
            System.err.println("  - " + failure);
        }
    }
}
```

---

## 异常对照速查表

| 异常类 | 触发时机 | 严重程度 | 建议处理方式 |
|--------|---------|---------|-------------|
| `BrutException` | brut.* 模块通用错误 | 中 | 检查错误消息和 cause |
| `AndrolibException` | APK 操作通用错误 | 中 | 检查错误消息和 cause |
| `FrameworkNotFoundException` | 缺少 framework 资源 | 高 | 安装对应 framework 后重试 |
| `InFileNotFoundException` | 输入文件不可读 | 高 | 检查文件路径和权限 |
| `OutDirExistsException` | 输出目录已存在 | 低 | 使用 `-f` 强制覆盖或指定新路径 |
| `RawXmlEncounteredException` | XML 解码失败 | 高 | 使用 `--keep-broken-res` 尝试部分解码 |
| `UndefinedResObjectException` | 资源引用未定义 | 中 | 检查资源表完整性 |
| `NinePatchNotFoundException` | 9-patch 数据缺失 | 低 | 检查图片文件格式 |
