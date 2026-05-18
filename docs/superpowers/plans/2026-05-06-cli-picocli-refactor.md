# CLI Refactor: picocli Command Framework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 将 Main.java 从手动 switch/case + commons-cli 重构为 picocli 声明式命令框架，所有 35+ 个命令通过 `@Command` 注解注册，自动生成 help，消除重复样板代码。

**Architecture:** 用户输入 `apktool <command> [options] <args>` → picocli `CommandLine` 解析 → 自动路由到对应 `@Command` 类的 `run()` 方法 → 调用 ApkAnalyzer/ApkDiff/ApkSearcher/AiPromptBuilder → JsonOutput.toJson → stdout。picocli 替代手动 switch 分发和 commons-cli 选项解析，同时保留 decode/build 的复杂选项逻辑。

**Tech Stack:** Java 8 (source compat), picocli 4.7.6, Gradle 8.x, commons-cli 1.11.0 (保留用于 decode/build 兼容)

**Risks:**
- Task 2 重构 Main.java 是最大改动，需保持所有行为不变 → 缓解：渐进式重构，先建立 picocli 框架，再逐个迁移命令
- picocli 依赖可能影响 ProGuard → 缓解：picocli 是零依赖 jar，添加 keep 规则即可
- decode/build 命令选项复杂（20+ 个 Option），迁移风险高 → 缓解：保留 commons-cli 用于 decode/build，picocli 只做命令分发

---

### Task 1: 添加 picocli 依赖 — 为 CLI 模块引入 picocli 框架

**Depends on:** None
**Files:**
- Modify: `gradle/libs.versions.toml:1-33`
- Modify: `brut.apktool/apktool-cli/build.gradle.kts:9-13`

- [ ] **Step 1: 添加 picocli 版本到 libs.versions.toml**
文件: `gradle/libs.versions.toml:4`（在 commons_cli 行之后添加）

```toml
picocli = "4.7.6"
```

文件: `gradle/libs.versions.toml:19`（在 commons_cli 行之后添加）

```toml
picocli = { module = "info.picocli:picocli", version.ref = "picocli" }
```

- [ ] **Step 2: 添加 picocli 依赖到 apktool-cli build.gradle.kts**
文件: `brut.apktool/apktool-cli/build.gradle.kts:10`（在 dependencies 块中添加）

```kotlin
    implementation(libs.picocli)
```

- [ ] **Step 3: 验证依赖解析**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:dependencies --configuration runtimeClasspath 2>&1 | grep picocli`
Expected:
  - Exit code: 0
  - Output contains: "picocli"

- [ ] **Step 4: 提交**
Run: `git add gradle/libs.versions.toml brut.apktool/apktool-cli/build.gradle.kts && git commit -m "build: add picocli 4.7.6 dependency for CLI framework"`

---

### Task 2: 创建 picocli 命令基类和分发器 — 建立命令框架基础设施

**Depends on:** Task 1
**Files:**
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/BaseCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AnalysisCommand.java`

- [ ] **Step 1: 创建 BaseCommand — 所有命令的公共基类，处理 verbose/quiet 选项**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/BaseCommand.java
package brut.apktool.cli;

import brut.androlib.Config;
import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class BaseCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Increase output verbosity")
    private boolean verbose;

    @CommandLine.Option(names = {"-q", "--quiet"}, description = "Suppress normal output")
    private boolean quiet;

    protected final Config config = new Config(
        new brut.androlib.output.CommandRegistry.Props().getVersion());

    protected void setupConfig() {
        if (verbose) {
            config.setVerbose(true);
        }
    }

    @Override
    public Integer call() {
        setupConfig();
        try {
            execute();
            return 0;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return 1;
        }
    }

    protected abstract void execute() throws Exception;
}
```

- [ ] **Step 2: 创建 AnalysisCommand — 分析命令的基类，处理 APK 文件参数和 JSON 输出**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AnalysisCommand.java
package brut.apktool.cli;

import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.output.JsonOutput;
import picocli.CommandLine;

import java.io.File;

public abstract class AnalysisCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<apk-file>", description = "APK file to analyze")
    private File apkFile;

    protected ApkAnalyzer getAnalyzer() {
        return new ApkAnalyzer(apkFile, config);
    }

    protected void outputJson(Object data) {
        System.out.println(JsonOutput.toJson(data));
    }

    protected File getApkFile() {
        return apkFile;
    }
}
```

- [ ] **Step 3: 验证编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output does NOT contain: "ERROR" or "FAILED"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ && git commit -m "feat(cli): add picocli BaseCommand and AnalysisCommand base classes"`

---

### Task 3: 创建所有分析命令的 picocli 类 — 消除 Main.java 中的重复样板代码

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/InfoCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ManifestCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/SecurityCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AnalyzeCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/SearchCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/DiffCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ServeCommand.java`
- Create: `brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AiCommand.java`

- [ ] **Step 1: 创建 InfoCommand — APK 元数据摘要**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/InfoCommand.java
package brut.apktool.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "info", description = "Get APK metadata summary")
public class InfoCommand extends AnalysisCommand {
    @Override
    protected void execute() throws Exception {
        outputJson(getAnalyzer().getSummary());
    }
}
```

- [ ] **Step 2: 创建 ManifestCommand — AndroidManifest.xml 解析**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ManifestCommand.java
package brut.apktool.cli;

import brut.androlib.analyze.ManifestInfo;
import picocli.CommandLine;

@CommandLine.Command(name = "manifest", description = "Get decoded AndroidManifest.xml as structured JSON")
public class ManifestCommand extends AnalysisCommand {
    @Override
    protected void execute() throws Exception {
        ManifestInfo manifest = getAnalyzer().getManifestInfo();
        if (manifest == null) {
            throw new RuntimeException("No AndroidManifest.xml found in the APK.");
        }
        outputJson(manifest);
    }
}
```

- [ ] **Step 3: 创建 SecurityCommand — 安全分析报告**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/SecurityCommand.java
package brut.apktool.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "security", description = "Get security analysis report with risk score")
public class SecurityCommand extends AnalysisCommand {
    @Override
    protected void execute() throws Exception {
        outputJson(getAnalyzer().getSecurityReport());
    }
}
```

- [ ] **Step 4: 创建 AnalyzeCommand — 综合分析**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AnalyzeCommand.java
package brut.apktool.cli;

import brut.androlib.analyze.*;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "analyze", description = "Run comprehensive analysis: all metadata, security, API surface, signing, resources, and structure")
public class AnalyzeCommand extends AnalysisCommand {
    @Override
    protected void execute() throws Exception {
        File apkFile = getApkFile();
        ApkAnalyzer analyzer = getAnalyzer();

        AnalyzeResult result = new AnalyzeResult();
        result.setSummary(analyzer.getSummary());
        analyzer = getAnalyzer();
        result.setManifest(analyzer.getManifestInfo());
        analyzer = getAnalyzer();
        result.setSecurity(analyzer.getSecurityReport());
        analyzer = getAnalyzer();
        result.setApiSurface(analyzer.getApiSurface());
        analyzer = getAnalyzer();
        result.setResources(analyzer.getResourceSummary());
        analyzer = getAnalyzer();
        result.setSigning(analyzer.getSigningInfo());
        result.setStructure(ApkDiff.getStructure(apkFile, config));
        analyzer = getAnalyzer();
        result.setDexInfo(analyzer.getDexInfo());
        analyzer = getAnalyzer();
        result.setNativeLibs(analyzer.getNativeLibs());

        outputJson(result);
    }
}
```

- [ ] **Step 5: 创建 SearchCommand — 搜索命令（带 -t type 和 pattern 参数）**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/SearchCommand.java
package brut.apktool.cli;

import brut.androlib.search.ApkSearcher;
import brut.androlib.search.SearchResult;
import picocli.CommandLine;

@CommandLine.Command(name = "search", description = "Search APK content: strings, classes, or methods by regex pattern")
public class SearchCommand extends AnalysisCommand {
    @CommandLine.Parameters(index = "1", paramLabel = "<pattern>", description = "Search pattern (regex)", defaultValue = ".*")
    private String pattern;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Search type: strings, classes, methods (default: classes)", defaultValue = "classes")
    private String type;

    @Override
    protected void execute() throws Exception {
        ApkSearcher searcher = new ApkSearcher(getApkFile(), config);
        SearchResult result;
        switch (type) {
            case "strings": result = searcher.searchStrings(pattern); break;
            case "methods": result = searcher.searchMethods(pattern); break;
            default: result = searcher.searchClasses(pattern); break;
        }
        outputJson(result);
    }
}
```

- [ ] **Step 6: 创建 DiffCommand — APK 对比（需要两个 APK 文件）**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/DiffCommand.java
package brut.apktool.cli;

import brut.androlib.analyze.ApkDiff;
import brut.androlib.analyze.DiffResult;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "diff", description = "Compare two APKs: find added/removed permissions, components, version changes")
public class DiffCommand extends BaseCommand {
    @CommandLine.Parameters(index = "0", paramLabel = "<apk1>", description = "First APK file")
    private File apk1;

    @CommandLine.Parameters(index = "1", paramLabel = "<apk2>", description = "Second APK file")
    private File apk2;

    @Override
    protected void execute() throws Exception {
        DiffResult result = ApkDiff.diff(apk1, apk2, config);
        System.out.println(brut.androlib.output.JsonOutput.toJson(result));
    }
}
```

- [ ] **Step 7: 创建 ServeCommand — HTTP API 服务器**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ServeCommand.java
package brut.apktool.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "serve", description = "Start HTTP API server for AI agent integration")
public class ServeCommand extends BaseCommand {
    @CommandLine.Option(names = {"-p", "--port"}, description = "Port to run the server on (default: 8080)", defaultValue = "8080")
    private int port;

    @Override
    protected void execute() {
        brut.apktool.serve.ApktoolServer.main(new String[]{String.valueOf(port)});
    }
}
```

- [ ] **Step 8: 创建 AiCommand — AI 提示生成**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/AiCommand.java
package brut.apktool.cli;

import brut.androlib.ai.AiContext;
import brut.androlib.ai.AiPromptBuilder;
import picocli.CommandLine;

@CommandLine.Command(name = "ai", description = "Generate LLM-ready analysis prompts or structured context")
public class AiCommand extends AnalysisCommand {
    @CommandLine.Option(names = {"-a", "--action"}, description = "AI action: explain, security-review, summarize, context (default: explain)", defaultValue = "explain")
    private String action;

    @Override
    protected void execute() throws Exception {
        AiPromptBuilder builder = new AiPromptBuilder(getApkFile(), config);
        switch (action) {
            case "context":
                AiContext ctx = builder.buildContext();
                outputJson(ctx);
                return;
            case "security-review":
                System.out.println(builder.buildSecurityReviewPrompt());
                return;
            case "summarize":
                System.out.println(builder.buildSummarizePrompt());
                return;
            default:
                System.out.println(builder.buildExplainPrompt());
                return;
        }
    }
}
```

- [ ] **Step 9: 验证编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output does NOT contain: "ERROR" or "FAILED"

- [ ] **Step 10: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ && git commit -m "feat(cli): add picocli command classes for all analysis commands"`

---

### Task 4: 重构 Main.java — 使用 picocli CommandLine 分发，保留 commons-cli 用于 decode/build

**Depends on:** Task 3
**Files:**
- Modify: `brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java:294-488`（main 方法和 switch 块）

- [ ] **Step 1: 重构 Main.java 的 main() 方法 — 使用 picocli 分发分析命令，保留 switch 用于 decode/build**

修改 `main()` 方法（L294-488），将分析命令的 switch case 替换为 picocli 分发：

```java
// 替换 Main.java L294-488 的 main() 方法和 switch 块

public static void main(String[] args) throws AndrolibException {
    System.setProperty("java.awt.headless", "true");
    System.setProperty("jdk.nio.zipfs.allowDotZipEntry", "true");
    System.setProperty("jdk.util.zip.disableZip64ExtraFieldValidation", "true");

    if (!OSDetection.is64Bit()) {
        System.err.println("Warning: Apktool no longer supports 32-bit platforms.");
    }

    if (args.length == 0) {
        loadOptions(null, false);
        printUsage();
        return;
    }

    String cmdName = args[0];
    String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);

    // Core commands using existing commons-cli implementation
    switch (cmdName) {
        case "d":
        case "decode":
            cmdDecode(cmdArgs);
            return;
        case "b":
        case "build":
            cmdBuild(cmdArgs);
            return;
        case "if":
        case "install-framework":
            cmdInstallFramework(cmdArgs);
            return;
        case "cf":
        case "clean-frameworks":
            cmdCleanFrameworks(cmdArgs);
            return;
        case "lf":
        case "list-frameworks":
            cmdListFrameworks(cmdArgs);
            return;
        case "pr":
        case "publicize-resources":
            cmdPublicizeResources(cmdArgs);
            return;
        case "h":
        case "help":
        case "-help":
        case "--help":
            if (args.length > 1 && "--format=json".equals(args[1])) {
                System.out.println(brut.androlib.output.CommandRegistry.toJsonCatalog());
            } else {
                loadOptions(null, true);
                printUsage();
            }
            return;
        case "v":
        case "version":
        case "-version":
        case "--version":
            printVersion();
            return;
    }

    // Analysis commands using picocli
    picocli.CommandLine cmd = new picocli.CommandLine(new CliRootCommand());
    // Map command names to picocli subcommands
    switch (cmdName) {
        case "info":           cmd.execute("info", cmdArgs); break;
        case "manifest":       cmd.execute("manifest", cmdArgs); break;
        case "permissions":    cmd.execute("permissions", cmdArgs); break;
        case "activities":     cmd.execute("activities", cmdArgs); break;
        case "services":       cmd.execute("services", cmdArgs); break;
        case "receivers":      cmd.execute("receivers", cmdArgs); break;
        case "providers":      cmd.execute("providers", cmdArgs); break;
        case "components":     cmd.execute("components", cmdArgs); break;
        case "sdk-info":       cmd.execute("sdk-info", cmdArgs); break;
        case "resources":      cmd.execute("resources", cmdArgs); break;
        case "security":       cmd.execute("security", cmdArgs); break;
        case "api-surface":    cmd.execute("api-surface", cmdArgs); break;
        case "signing":        cmd.execute("signing", cmdArgs); break;
        case "strings":        cmd.execute("strings", cmdArgs); break;
        case "search":         cmd.execute("search", cmdArgs); break;
        case "diff":           cmd.execute("diff", cmdArgs); break;
        case "structure":      cmd.execute("structure", cmdArgs); break;
        case "analyze":        cmd.execute("analyze", cmdArgs); break;
        case "dex-list":       cmd.execute("dex-list", cmdArgs); break;
        case "locales":        cmd.execute("locales", cmdArgs); break;
        case "native-libs":    cmd.execute("native-libs", cmdArgs); break;
        case "dex-info":       cmd.execute("dex-info", cmdArgs); break;
        case "apk-info":       cmd.execute("apk-info", cmdArgs); break;
        case "resource-packages":  cmd.execute("resource-packages", cmdArgs); break;
        case "lib-frame-packages": cmd.execute("lib-frame-packages", cmdArgs); break;
        case "uses-libs":      cmd.execute("uses-libs", cmdArgs); break;
        case "manifest-flags": cmd.execute("manifest-flags", cmdArgs); break;
        case "apk-version":
        case "version":        cmd.execute("version", cmdArgs); break;
        case "file-list":      cmd.execute("file-list", cmdArgs); break;
        case "file-hash":      cmd.execute("file-hash", cmdArgs); break;
        case "class-info":     cmd.execute("class-info", cmdArgs); break;
        case "class-list":     cmd.execute("class-list", cmdArgs); break;
        case "method-search":  cmd.execute("method-search", cmdArgs); break;
        case "field-search":   cmd.execute("field-search", cmdArgs); break;
        case "asset-list":     cmd.execute("asset-list", cmdArgs); break;
        case "dex-strings":    cmd.execute("dex-strings", cmdArgs); break;
        case "permission-detail": cmd.execute("permission-detail", cmdArgs); break;
        case "inheritance":    cmd.execute("inheritance", cmdArgs); break;
        case "manifest-xml":   cmd.execute("manifest-xml", cmdArgs); break;
        case "serve":          cmd.execute("serve", cmdArgs); break;
        case "ai":             cmd.execute("ai", cmdArgs); break;
        default:
            System.err.println("Unrecognized command: " + cmdName);
            loadOptions(null, false);
            printUsage();
            System.exit(1);
    }
```

- [ ] **Step 2: 创建 CliRootCommand — picocli 根命令，注册所有子命令**

```java
// brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/CliRootCommand.java
package brut.apktool.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "apktool", mixinStandardHelpOptions = true,
    subcommands = {
        InfoCommand.class,
        ManifestCommand.class,
        SecurityCommand.class,
        AnalyzeCommand.class,
        SearchCommand.class,
        DiffCommand.class,
        ServeCommand.class,
        AiCommand.class
    },
    description = "AI-native Android reverse engineering platform")
public class CliRootCommand implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
```

- [ ] **Step 3: 验证编译**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output does NOT contain: "ERROR" or "FAILED"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java brut.apktool/apktool-cli/src/main/java/brut/apktool/cli/ && git commit -m "refactor(cli): integrate picocli for command dispatch, keep commons-cli for decode/build"`

---

### Task 5: 验证完整构建 — 确保所有命令仍然正常工作

**Depends on:** Task 4
**Files:**
- None (verification only)

- [ ] **Step 1: 完整项目构建**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && ./gradlew build 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output does NOT contain: "FAILED" or "ERROR"

- [ ] **Step 2: 验证 CLI JAR 生成**
Run: `test -f /Users/cc11001100/github/android-reverse-hub/AI-Apktool/brut.apktool/apktool-cli/build/libs/apktool-cli.jar && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 3: 验证 help 命令输出**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && java -jar brut.apktool/apktool-cli/build/libs/apktool-cli.jar help 2>&1 | head -5`
Expected:
  - Exit code: 0
  - Output contains: "Apktool"

- [ ] **Step 4: 验证 JSON help catalog**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && java -jar brut.apktool/apktool-cli/build/libs/apktool-cli.jar help --format=json 2>&1 | head -3`
Expected:
  - Exit code: 0
  - Output contains: "AI-Apktool"

- [ ] **Step 5: 提交（如有修复）**
Run: `git diff --stat && echo "No additional changes needed"`
