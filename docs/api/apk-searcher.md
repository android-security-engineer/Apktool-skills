# ApkSearcher API 参考

> 包路径: `brut.androlib.search.ApkSearcher`

ApkSearcher 提供 APK 文件内容搜索功能，支持通过正则表达式在字符串资源、DEX 类名和方法名中进行搜索。所有搜索均为大小写不敏感。

---

## 构造函数

```java
public ApkSearcher(File apkFile, Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkFile` | `File` | 目标 APK 文件 |
| `config` | `Config` | 配置对象，可通过 `new Config("search")` 创建 |

---

## 方法

### searchStrings

```java
public SearchResult searchStrings(String pattern) throws AndrolibException
```

在 APK 的字符串资源中搜索匹配指定正则表达式的条目。搜索过程会加载资源表（ResTable），遍历主包（Main Package）中的所有 ResEntry，对类型为 `ResString` 的值进行正则匹配。

- 搜索类型标记: `"strings"`
- 匹配模式: 大小写不敏感 (`Pattern.CASE_INSENSITIVE`)
- 搜索范围: ResTable 中主包的所有 ResString 值
- 每个 match 的 `name` 格式为 `typeName/specName`，`source` 固定为 `"resources"`

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `pattern` | `String` | Java 正则表达式 |

**返回:** `SearchResult`，包含所有匹配的字符串资源。

---

### searchClasses

```java
public SearchResult searchClasses(String pattern) throws AndrolibException
```

在 APK 中所有 DEX 文件的类名中搜索匹配指定正则表达式的条目。使用 smali 的 `ZipDexContainer` 和 `DexBackedDexFile` 遍历所有 DEX 文件中的类定义。

- 搜索类型标记: `"classes"`
- 匹配模式: 大小写不敏感
- 搜索范围: APK 中所有 DEX 文件（包括 multidex）
- 每个 match 的 `name` 为人类可读的类名（如 `com.example.MyClass`），`value` 为 Dalvik 类型描述符（如 `Lcom/example/MyClass;`），`source` 为 DEX 文件名（如 `classes.dex`）

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `pattern` | `String` | Java 正则表达式 |

**返回:** `SearchResult`，包含所有匹配的类。

---

### searchMethods

```java
public SearchResult searchMethods(String pattern) throws AndrolibException
```

在 APK 中所有 DEX 文件的方法名中搜索匹配指定正则表达式的条目。遍历所有 DEX 文件中所有类的所有方法。

- 搜索类型标记: `"methods"`
- 匹配模式: 大小写不敏感
- 搜索范围: APK 中所有 DEX 文件的所有类的所有方法
- 每个 match 的 `name` 格式为 `全限定类名.方法名`（如 `com.example.MainActivity.onCreate`），`value` 为方法名，`source` 为 DEX 文件名

**参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| `pattern` | `String` | Java 正则表达式 |

**返回:** `SearchResult`，包含所有匹配的方法。

---

## SearchResult 数据模型

> 包路径: `brut.androlib.search.SearchResult`

```java
public class SearchResult {
    private String query;           // 原始搜索正则表达式
    private String type;            // 搜索类型: "strings" / "classes" / "methods"
    private int totalMatches;       // 匹配总数
    private List<SearchMatch> matches;  // 匹配结果列表
}
```

### SearchMatch 数据模型

```java
public static class SearchMatch {
    private String name;    // 匹配项的可读名称
    private String value;   // 匹配的具体值
    private String source;  // 来源（DEX 文件名或 "resources"）
}
```

### JSON 输出示例

```json
{
  "query": "Activity",
  "type": "classes",
  "totalMatches": 3,
  "matches": [
    {
      "name": "com.example.app.MainActivity",
      "value": "Lcom/example/app/MainActivity;",
      "source": "classes.dex"
    },
    {
      "name": "android.app.Activity",
      "value": "Landroid/app/Activity;",
      "source": "classes.dex"
    },
    {
      "name": "com.example.app.SettingsActivity",
      "value": "Lcom/example/app/SettingsActivity;",
      "source": "classes.dex"
    }
  ]
}
```

---

## 代码示例

### 基本搜索：搜索字符串资源

```java
import brut.androlib.Config;
import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import brut.androlib.output.JsonOutput;

import java.io.File;

public class SearchStringsExample {
    public static void main(String[] args) throws Exception {
        Config config = new Config("search");
        ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

        // 搜索所有包含 URL 的字符串
        SearchResult result = searcher.searchStrings("https?://.*");

        System.out.println("搜索类型: " + result.getType());
        System.out.println("匹配数量: " + result.getTotalMatches());

        for (SearchResult.SearchMatch match : result.getMatches()) {
            System.out.println("  名称: " + match.getName());
            System.out.println("  值: " + match.getValue());
            System.out.println("  来源: " + match.getSource());
        }

        // 输出 JSON 格式
        System.out.println(JsonOutput.toJson(result));
    }
}
```

### 搜索类名

```java
Config config = new Config("search");
ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

// 搜索所有包含 "Activity" 的类名
SearchResult result = searcher.searchClasses("Activity");

// 搜索特定包下的类
SearchResult result2 = searcher.searchClasses("com\\.example\\.app\\..*");

// 搜索所有类（使用 .* 通配符）
SearchResult allClasses = searcher.searchClasses(".*");

// 输出 JSON
System.out.println(JsonOutput.toJson(result));
```

### 搜索方法名

```java
Config config = new Config("search");
ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

// 搜索所有 onCreate 方法
SearchResult onCreate = searcher.searchMethods("onCreate");

// 搜索所有以 "get" 开头的方法
SearchResult getters = searcher.searchMethods("^get.*");

// 搜索与加密相关的方法
SearchResult crypto = searcher.searchMethods("encrypt|decrypt|cipher|hash");

System.out.println(JsonOutput.toJson(onCreate));
```

### 搜索字符串资源中的敏感信息

```java
Config config = new Config("search");
ApkSearcher searcher = new ApkSearcher(new File("app.apk"), config);

// 搜索 API 密钥模式
SearchResult apiKeys = searcher.searchStrings("(api[_-]?key|secret[_-]?key|token).{0,50}");

// 搜索服务器地址
SearchResult servers = searcher.searchStrings("https?://[\\w./-]+");

// 搜索邮箱地址
SearchResult emails = searcher.searchStrings("[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}");
```

---

## 性能建议

### 大型 APK 文件

- 对于包含多个 DEX 文件的大型 APK（如 `classes.dex`、`classes2.dex`、`classes3.dex` 等），`searchClasses` 和 `searchMethods` 需要遍历所有 DEX 文件，可能消耗较多内存和时间
- 建议在处理大型 APK 时使用更精确的正则表达式以减少匹配数量
- `searchStrings` 需要加载完整的资源表（ResTable），对于资源量大的 APK 可能较慢

### 正则表达式优化

- 避免使用过于宽泛的模式（如 `.*`）进行搜索，这会匹配所有条目
- 尽量使用具体的前缀或限定词（如 `com\.example\..*Activity$` 而非 `.*Activity.*`）
- 利用锚点（`^` 和 `$`）缩小匹配范围，例如 `^get.*` 只匹配以 "get" 开头的方法名
- 搜索方法名时，每个方法都会被执行正则匹配，类越多耗时越长

### 资源释放

- 每个搜索方法执行完毕后会自动关闭 APK 文件句柄（在 `finally` 块中调用 `mApkFile.close()`）
- 如果连续执行多次搜索，每次搜索都会创建新的 searcher 实例，确保不会出现文件句柄泄漏
