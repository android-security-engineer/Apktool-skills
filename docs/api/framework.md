# Framework 管理参考

`Framework` 类负责管理 Android 框架资源文件（framework-res.apk），这些文件在解码和构建过程中用于解析系统资源 ID 和属性。

**包名**: `brut.androlib.res.Framework`

## 构造函数

```java
public Framework(Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `config` | `Config` | 配置对象，其中的 `frameworkDirectory` 和 `frameworkTag` 会影响框架文件的存储路径 |

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);
```

## 默认框架目录

当未通过 `Config.setFrameworkDirectory()` 指定自定义路径时，框架文件存储在操作系统的默认位置：

| 操作系统 | 默认路径 |
|----------|----------|
| macOS | `~/Library/apktool/framework/` |
| Windows | `%LOCALAPPDATA%\apktool\framework\` |
| Linux | `${XDG_DATA_HOME}/apktool/framework/`（如未设置 `XDG_DATA_HOME` 则为 `~/.local/share/apktool/framework/`） |

## 主要方法

### install -- 安装框架

```java
public void install(File apkFile) throws AndrolibException
```

从一个 APK 文件中提取并安装框架资源。该方法会：

1. 读取 APK 中的 `resources.arsc`
2. 解析资源表并获取 Package ID
3. 将所有资源条目标记为 PUBLIC（`0x40000000` 标志位）
4. 以 `<pkgId>[-tag].apk` 的格式保存到框架目录中

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `java.io.File` | 包含框架资源的 APK 文件（通常为厂商的 framework-res.apk） |

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);
framework.install(new File("framework-res.apk"));
```

安装带标签的框架（用于区分不同厂商）：

```java
Config config = new Config("3.0.3");
config.setFrameworkTag("samsung");

Framework framework = new Framework(config);
framework.install(new File("samsung-framework-res.apk"));
// 保存为: ~/Library/apktool/framework/1-samsung.apk
```

### cleanDirectory -- 清理框架目录

```java
public void cleanDirectory() throws AndrolibException
```

删除框架目录中的所有框架文件。此操作不可恢复。

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);
framework.cleanDirectory();
```

### listDirectory -- 列出框架文件

```java
public List<File> listDirectory() throws AndrolibException
```

列出框架目录中所有已安装的框架文件。返回的列表只包含合法命名的文件（数字 ID + `.apk` 后缀）。

- 当 `Config.isForced() = true` 时，忽略 `frameworkTag`，列出所有框架文件
- 当 `Config.isForced() = false` 时，只列出与当前 `frameworkTag` 匹配的文件

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);

List<File> files = framework.listDirectory();
for (File file : files) {
    System.out.println("框架文件: " + file.getName());
}
```

### publicizeResources -- 公开化资源

```java
public void publicizeResources(File arscFile) throws AndrolibException
```

将指定 `resources.arsc` 文件中的所有资源条目标记为 PUBLIC。这在处理某些需要公开化资源 ID 的场景中很有用。该方法会直接修改传入的文件。

| 参数 | 类型 | 说明 |
|------|------|------|
| `arscFile` | `java.io.File` | 需要处理的 `resources.arsc` 文件 |

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);
framework.publicizeResources(new File("resources.arsc"));
```

### getDirectory -- 获取框架目录

```java
public File getDirectory() throws AndrolibException
```

返回框架文件的存储目录。如果目录不存在会自动创建。此方法会进行以下检查：

- 如果指定了自定义路径（通过 `Config.setFrameworkDirectory()`），使用自定义路径
- 如果未指定，使用操作系统默认路径
- 如果路径存在但不是目录，抛出 `AndrolibException`
- 如果路径不存在，自动创建（包含父目录）

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);
File dir = framework.getDirectory();
System.out.println("框架目录: " + dir.getAbsolutePath());
```

### getApkFile -- 获取框架 APK 文件

```java
public File getApkFile(int id) throws AndrolibException
public File getApkFile(int id, String tag) throws AndrolibException
```

根据 Package ID 查找并返回对应的框架 APK 文件。查找逻辑如下：

1. 先查找带标签的框架文件（`<id>-<tag>.apk`）
2. 如果未找到，回退到无标签的框架文件（`<id>.apk`）
3. 如果是默认框架（`id == 1`）且文件不存在，自动提取内置的 `android-framework.jar`

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `int` | 框架的 Package ID（通常为 `1`） |
| `tag` | `String` | 可选，框架标签。单参数版本使用 `Config.getFrameworkTag()` |

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);

// 获取默认框架（id=1）
File defaultFramework = framework.getApkFile(1);

// 获取带标签的框架
File taggedFramework = framework.getApkFile(1, "samsung");
```

## 异常说明

| 异常类 | 触发条件 |
|--------|----------|
| `AndrolibException` | 框架目录创建失败、路径非目录、APK 中缺少 resources.arsc、IO 错误等 |
| `FrameworkNotFoundException` | 指定 ID 的框架文件不存在且非默认框架（id != 1） |

## 完整使用示例

### 安装厂商框架

在解码使用了厂商定制资源的 APK 前，需要先安装对应厂商的框架资源：

```java
import brut.androlib.Config;
import brut.androlib.res.Framework;
import java.io.File;

// 1. 配置框架标签
Config config = new Config("3.0.3");
config.setFrameworkTag("samsung");

// 2. 安装三星框架
Framework framework = new Framework(config);
framework.install(new File("samsung-framework-res.apk"));

// 3. 使用该框架解码 APK
config.setForced(true);
ApkDecoder decoder = new ApkDecoder(new File("samsung-app.apk"), config);
decoder.decode(new File("samsung-app-decoded"));
```

### 查看已安装的框架

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);

List<File> files = framework.listDirectory();
System.out.println("已安装的框架数量: " + files.size());
for (File file : files) {
    System.out.println(" - " + file.getName() + " (" + file.length() + " bytes)");
}
```

### 清理所有框架

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);

// 列出当前框架
List<File> before = framework.listDirectory();
System.out.println("清理前: " + before.size() + " 个框架文件");

// 清理
framework.cleanDirectory();

List<File> after = framework.listDirectory();
System.out.println("清理后: " + after.size() + " 个框架文件");
```

### 使用自定义框架目录

```java
Config config = new Config("3.0.3");
config.setFrameworkDirectory("/path/to/custom/frameworks");

Framework framework = new Framework(config);
framework.install(new File("framework-res.apk"));

// 框架文件将保存到 /path/to/custom/frameworks/1.apk
```

### 获取框架信息

```java
Config config = new Config("3.0.3");
Framework framework = new Framework(config);

// 获取框架目录
File dir = framework.getDirectory();
System.out.println("框架目录: " + dir.getAbsolutePath());

// 获取默认框架文件（id=1 为 Android 标准框架）
File defaultFramework = framework.getApkFile(1);
System.out.println("默认框架: " + defaultFramework.getAbsolutePath());
System.out.println("文件大小: " + defaultFramework.length() + " bytes");
```
