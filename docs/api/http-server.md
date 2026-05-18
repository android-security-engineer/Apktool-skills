# HTTP API Server 参考

> 包路径: `brut.apktool.serve`

HTTP Server 模块提供基于 Javalin 的 RESTful HTTP API 服务，支持 AI Agent 和其他外部工具通过 HTTP 协议访问 APK 分析功能。

---

## ApktoolServer

> 包路径: `brut.apktool.serve.ApktoolServer`

### 构造函数

```java
public ApktoolServer(int port)
```

创建并启动一个 Javalin HTTP 服务器。构造时会自动创建 `ApiHandler` 并注册所有路由。

| 参数 | 类型 | 说明 |
|------|------|------|
| `port` | `int` | 服务器监听端口 |

**内部行为:**
1. 创建 `Config` 对象（version 为 `"ai-apktool-serve"`）
2. 创建 `ApiHandler` 实例
3. 初始化 Javalin 应用
4. 注册所有 REST API 路由
5. 启动服务器并监听指定端口

---

### stop

```java
public void stop()
```

停止 HTTP 服务器，释放所有资源。

---

### main

```java
public static void main(String[] args)
```

服务器启动入口。默认端口为 8080，可通过命令行参数指定其他端口。

**用法:**

```bash
# 使用默认端口 8080
java -cp apktool.jar brut.apktool.serve.ApktoolServer

# 指定端口
java -cp apktool.jar brut.apktool.serve.ApktoolServer 9090
```

---

## ApiHandler

> 包路径: `brut.apktool.serve.ApiHandler`

### 构造函数

```java
public ApiHandler(Config config)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `config` | `Config` | 配置对象，由 ApktoolServer 创建并传入 |

---

### handleInfo

```java
public String handleInfo(String apkPath) throws Exception
```

获取 APK 文件摘要信息。返回 ApkSummary 的 JSON 字符串。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |

**返回:** JSON 字符串，包含文件名、包名、版本号、组件数量等信息。

---

### handleManifest

```java
public String handleManifest(String apkPath) throws Exception
```

获取 APK 的 AndroidManifest.xml 结构化信息。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |

**返回:** JSON 字符串，包含 ManifestInfo 对象。如果未找到 Manifest，返回 `{"error": "No AndroidManifest.xml found"}`。

---

### handlePermissions

```java
public String handlePermissions(String apkPath) throws Exception
```

获取 APK 声明的所有权限列表。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |

**返回:** JSON 字符串数组，包含所有权限字符串。如果未找到 Manifest，返回错误信息。

---

### handleSecurity

```java
public String handleSecurity(String apkPath) throws Exception
```

获取 APK 安全分析报告。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |

**返回:** JSON 字符串，包含 SecurityReport 对象（危险权限、高风险组件、风险评分等）。

---

### handleSearch

```java
public String handleSearch(String apkPath, String type, String pattern) throws Exception
```

搜索 APK 内容。根据 type 参数选择搜索类型。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |
| `type` | `String` | 搜索类型: `"strings"` / `"classes"` / `"methods"` |
| `pattern` | `String` | 正则表达式 |

**返回:** JSON 字符串，包含 SearchResult 对象。

> 当 `type` 不是 `"strings"` 或 `"methods"` 时，默认执行 `"classes"` 搜索。

---

### handleDiff

```java
public String handleDiff(String apkPath1, String apkPath2) throws Exception
```

对比两个 APK 文件。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath1` | `String` | 旧版 APK 文件路径 |
| `apkPath2` | `String` | 新版 APK 文件路径 |

**返回:** JSON 字符串，包含 DiffResult 对象。

---

### handleResources

```java
public String handleResources(String apkPath) throws Exception
```

获取 APK 资源概览。

| 参数 | 类型 | 说明 |
|------|------|------|
| `apkPath` | `String` | APK 文件的绝对路径 |

**返回:** JSON 字符串，包含 ResourceSummary 对象。

---

## REST API 端点

所有端点均为 **GET** 请求，响应格式为 **application/json**。

### GET /api/v1/health

健康检查端点。

**请求参数:** 无

**响应:**

```json
{"status": "ok"}
```

**curl 示例:**

```bash
curl http://localhost:8080/api/v1/health
```

---

### GET /api/v1/info

获取 APK 摘要信息。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/info?apk=/path/to/app.apk"
```

**响应示例:**

```json
{
  "fileName": "app.apk",
  "fileSize": 5242880,
  "packageName": "com.example.myapp",
  "versionName": "2.0.0",
  "versionCode": 20,
  "minSdkVersion": "21",
  "targetSdkVersion": "33",
  "dexCount": 2,
  "hasResources": true,
  "hasAssets": true,
  "hasNativeLibs": true,
  "architectures": ["armeabi-v7a", "arm64-v8a"],
  "permissionCount": 5,
  "activityCount": 3,
  "serviceCount": 1,
  "receiverCount": 2,
  "providerCount": 1
}
```

---

### GET /api/v1/manifest

获取 AndroidManifest.xml 信息。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/manifest?apk=/path/to/app.apk"
```

**响应示例:**

```json
{
  "packageName": "com.example.myapp",
  "versionName": "2.0.0",
  "versionCode": 20,
  "minSdkVersion": "21",
  "targetSdkVersion": "33",
  "permissions": [
    "android.permission.INTERNET",
    "android.permission.CAMERA"
  ],
  "activities": [
    {
      "name": "com.example.myapp.MainActivity",
      "exported": true,
      "intentFilters": ["android.intent.action.MAIN"],
      "permissions": []
    }
  ],
  "services": [],
  "receivers": [],
  "providers": [],
  "debuggable": false,
  "allowBackup": true
}
```

---

### GET /api/v1/permissions

获取权限列表。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/permissions?apk=/path/to/app.apk"
```

**响应示例:**

```json
[
  "android.permission.INTERNET",
  "android.permission.CAMERA",
  "android.permission.ACCESS_FINE_LOCATION",
  "android.permission.RECORD_AUDIO",
  "android.permission.WRITE_EXTERNAL_STORAGE"
]
```

---

### GET /api/v1/security

获取安全分析报告。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/security?apk=/path/to/app.apk"
```

**响应示例:**

```json
{
  "debuggable": false,
  "allowBackup": true,
  "usesCleartextTraffic": false,
  "dangerousPermissions": [
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.RECORD_AUDIO"
  ],
  "highRiskComponents": [],
  "findings": [
    "AllowBackup is enabled - data may be accessible via adb backup"
  ],
  "riskScore": 35
}
```

---

### GET /api/v1/search

搜索 APK 内容。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |
| `type` | 否 | 搜索类型: `strings` / `classes` / `methods`（默认: `classes`） |
| `pattern` | 否 | 正则表达式（默认: `.*`） |

**curl 示例:**

```bash
# 搜索类名
curl "http://localhost:8080/api/v1/search?apk=/path/to/app.apk&type=classes&pattern=Activity"

# 搜索字符串资源
curl "http://localhost:8080/api/v1/search?apk=/path/to/app.apk&type=strings&pattern=https%3F%3A%2F%2F.*"

# 搜索方法名
curl "http://localhost:8080/api/v1/search?apk=/path/to/app.apk&type=methods&pattern=onCreate"
```

**响应示例:**

```json
{
  "query": "Activity",
  "type": "classes",
  "totalMatches": 2,
  "matches": [
    {
      "name": "com.example.myapp.MainActivity",
      "value": "Lcom/example/myapp/MainActivity;",
      "source": "classes.dex"
    },
    {
      "name": "com.example.myapp.SettingsActivity",
      "value": "Lcom/example/myapp/SettingsActivity;",
      "source": "classes.dex"
    }
  ]
}
```

---

### GET /api/v1/diff

对比两个 APK 文件。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk1` | 是 | 旧版 APK 文件路径 |
| `apk2` | 是 | 新版 APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/diff?apk1=/path/to/app_v1.apk&apk2=/path/to/app_v2.apk"
```

**响应示例:**

```json
{
  "addedPermissions": ["android.permission.CAMERA"],
  "removedPermissions": [],
  "addedActivities": ["com.example.myapp.CameraActivity"],
  "removedActivities": [],
  "addedServices": [],
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

---

### GET /api/v1/resources

获取资源概览。

**请求参数:**

| 参数 | 必填 | 说明 |
|------|------|------|
| `apk` | 是 | APK 文件路径 |

**curl 示例:**

```bash
curl "http://localhost:8080/api/v1/resources?apk=/path/to/app.apk"
```

**响应示例:**

```json
{
  "packageName": "com.example.myapp",
  "packageId": 127,
  "typeCounts": {
    "string": 150,
    "drawable": 45,
    "layout": 12,
    "color": 8,
    "dimen": 20
  },
  "locales": ["", "zh-CN", "en-US"],
  "totalEntries": 235
}
```

---

## 错误处理

所有端点在发生错误时返回 HTTP 500 状态码，响应体为 JSON 格式的错误信息:

```json
{"error": "错误描述信息"}
```

**常见错误:**

| 场景 | 错误信息 |
|------|----------|
| 缺少必填参数 | `Missing required parameter: apk` |
| APK 文件不存在 | 文件读取异常 |
| APK 格式无效 | 解析异常 |

---

## 代码示例

### 启动服务器

```java
import brut.apktool.serve.ApktoolServer;

public class ServerExample {
    public static void main(String[] args) {
        // 在 9090 端口启动服务器
        ApktoolServer server = new ApktoolServer(9090);

        // ... 执行操作 ...

        // 停止服务器
        server.stop();
    }
}
```

### 命令行启动

```bash
# 使用 CLI 命令启动
java -jar apktool.jar serve -p 8080

# 或直接指定 main 类
java -cp apktool.jar brut.apktool.serve.ApktoolServer 8080
```

### 与 AI Agent 集成的 Python 示例

```python
import requests

BASE_URL = "http://localhost:8080/api/v1"
APK_PATH = "/path/to/app.apk"

# 获取 APK 基本信息
info = requests.get(f"{BASE_URL}/info", params={"apk": APK_PATH}).json()
print(f"包名: {info['packageName']}")
print(f"版本: {info['versionName']}")

# 获取安全报告
security = requests.get(f"{BASE_URL}/security", params={"apk": APK_PATH}).json()
print(f"风险评分: {security['riskScore']}")

# 搜索敏感类
result = requests.get(f"{BASE_URL}/search", params={
    "apk": APK_PATH,
    "type": "classes",
    "pattern": "crypto|cipher|ssl|tls"
}).json()
print(f"找到 {result['totalMatches']} 个加密相关类")
```
