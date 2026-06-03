# apktool-ai-cli Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** Create a new Gradle module `apktool-ai-cli` that serves as the Java-level unified entry point for Skills — transforming Skills from pure Markdown orchestration (Claude Code reads SKILL.md → runs CLI commands) into programmable Java workflows that can be invoked via `apktool-ai-cli skill <name> <apk>`.

**Architecture:** User runs `apktool-ai-cli skill <name> <apk>` → Main.java dispatches to SkillRegistry → finds registered Skill implementation → Skill creates SkillContext (holding ApkAnalyzer, ApkSearcher, Config) → executes ordered SkillSteps → aggregates SkillResult → outputs JSON. This mirrors the existing CLI pattern but adds a Skill abstraction layer on top of atomic commands, enabling multi-step orchestration, shared analyzer instances, and conditional branching in Java.

**Tech Stack:** Java 8 (source compat), Gradle Kotlin DSL, Gson 2.11.0, Commons CLI 1.11.0, apktool-lib (project dependency)

**Scope:** Medium
**Risk:** Medium

**Risks:**
- Modifying shared settings.gradle.kts and build.gradle.kts → mitigation: only additive changes, no existing logic modified
- No existing test coverage in the project → mitigation: Task 5 includes build verification
- Boundary between apktool-cli (atomic commands) and apktool-ai-cli (skill orchestration) must stay clear → mitigation: apktool-ai-cli depends on apktool-lib directly, never delegates to apktool-cli

**Autonomy Level:** Full

---

### Task 1: Create apktool-ai-cli build configuration

**Depends on:** None
**Files:**
- Modify: `settings.gradle.kts:6` (add include)
- Modify: `build.gradle.kts:54` (add to mavenProjects)
- Create: `brut.apktool/apktool-ai-cli/build.gradle.kts`
- Create: `brut.apktool/apktool-ai-cli/proguard-rules.pro`
- Create: `brut.apktool/apktool-ai-cli/src/main/resources/apktool.properties`

- [ ] **Step 1: Add apktool-ai-cli to settings.gradle.kts**
文件: `settings.gradle.kts:6`（include 列表）

```kotlin
include(
    "brut.j.common", "brut.j.util", "brut.j.dir", "brut.j.xml", "brut.j.yaml",
    "brut.apktool:apktool-lib", "brut.apktool:apktool-cli", "brut.apktool:apktool-serve",
    "brut.apktool:apktool-ai-cli"
)
```

- [ ] **Step 2: Add apktool-ai-cli to root mavenProjects**
文件: `build.gradle.kts:54`（mavenProjects 数组）

```kotlin
val mavenProjects = arrayOf(
    "brut.j.common", "brut.j.util", "brut.j.dir", "brut.j.xml", "brut.j.yaml",
    "apktool-lib", "apktool-cli", "apktool-ai-cli"
)
```

- [ ] **Step 3: Create build.gradle.kts for apktool-ai-cli**

```kotlin
val gitRevision: String by rootProject.extra
val apktoolVersion: String by rootProject.extra
val r8: Configuration by configurations.creating

plugins {
    application
}

dependencies {
    implementation(project(":brut.apktool:apktool-lib"))
    implementation(libs.commons.cli)
    r8(libs.r8)
}

application {
    mainClass.set("brut.apktool.ai.cli.Main")
    tasks.run.get().workingDir = file(System.getProperty("user.dir"))
}

tasks {
    processResources {
        from("src/main/resources") {
            include("apktool.properties")
            expand("version" to apktoolVersion, "gitrev" to gitRevision)
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        includeEmptyDirs = false
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.register<Delete>("cleanOutputDirectory") {
    delete(fileTree("build/libs") {
        exclude("apktool-ai-cli-sources.jar")
        exclude("apktool-ai-cli-javadoc.jar")
        exclude("apktool-ai-cli-all.jar")
    })
}

val shadowJar = tasks.register("shadowJar", Jar::class) {
    dependsOn("build")
    dependsOn("cleanOutputDirectory")

    group = "build"
    description = "Creates a single executable JAR with all dependencies"
    manifest.attributes["Main-Class"] = "brut.apktool.ai.cli.Main"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)

    from(dependencies)
    with(tasks.jar.get())
}

tasks.register<JavaExec>("proguard") {
    dependsOn("shadowJar")

    onlyIf {
        JavaVersion.current().isJava11Compatible
    }

    val proguardRules = file("proguard-rules.pro")
    val originalJar = shadowJar.map { it.outputs.files.singleFile }

    inputs.files(originalJar, proguardRules)
    outputs.file("build/libs/apktool_ai_$apktoolVersion.jar")

    classpath(r8)
    mainClass.set("com.android.tools.r8.R8")

    args(
        "--release",
        "--classfile",
        "--no-minification",
        "--map-diagnostics:UnusedProguardKeepRuleDiagnostic", "info", "none",
        "--lib", javaLauncher.get().metadata.installationPath.toString(),
        "--output", outputs.files.singleFile.toString(),
        "--pg-conf", proguardRules.toString(),
        originalJar.get().toString()
    )
}
```

- [ ] **Step 4: Create proguard-rules.pro**

```proguard
-keep class brut.apktool.ai.cli.Main { *; }
-keep class brut.apktool.ai.cli.Skill { *; }
-keep class brut.apktool.ai.cli.SkillResult { *; }
-keep class brut.apktool.ai.cli.SkillContext { *; }
-keep class brut.apktool.ai.cli.SkillStep { *; }
-keep class brut.apktool.ai.cli.SkillRegistry { *; }
-keep class brut.apktool.ai.cli.skill.** { *; }
-keepattributes *Annotation*
-dontwarn javax.annotation.**
```

- [ ] **Step 5: Create apktool.properties**

```properties
version=@version@
gitrev=@gitrev@
```

- [ ] **Step 6: 验证 build 配置**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:build --dry-run 2>&1 || true`
Expected:
  - Output contains "apktool-ai-cli"
  - No "Unresolved reference" errors in build script evaluation

- [ ] **Step 7: 提交**
Run: `git add settings.gradle.kts build.gradle.kts brut.apktool/apktool-ai-cli/ && git commit -m "feat(build): add apktool-ai-cli module skeleton"`

---

### Task 2: Create core Skill interfaces and data models

**Depends on:** Task 1
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/Skill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillStep.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillResult.java`

- [ ] **Step 1: 创建 Skill 接口 — 定义 Skill 的统一契约**

```java
package brut.apktool.ai.cli;

import java.io.File;
import java.util.List;

public interface Skill {

    String name();

    String description();

    List<SkillStep> steps();

    SkillResult execute(File apkFile, SkillContext context);
}
```

- [ ] **Step 2: 创建 SkillStep 数据模型 — 表示 Skill 中的一个执行步骤**

```java
package brut.apktool.ai.cli;

import java.util.Map;

public class SkillStep {

    private final String name;
    private final String description;
    private final String command;
    private final Map<String, String> params;
    private StepStatus status;
    private Object result;

    public enum StepStatus {
        PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    }

    public SkillStep(String name, String description, String command, Map<String, String> params) {
        this.name = name;
        this.description = description;
        this.command = command;
        this.params = params;
        this.status = StepStatus.PENDING;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCommand() { return command; }
    public Map<String, String> getParams() { return params; }
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
}
```

- [ ] **Step 3: 创建 SkillResult 数据模型 — 表示 Skill 执行的聚合结果**

```java
package brut.apktool.ai.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillResult {

    private final String skillName;
    private final Map<String, Object> data;
    private final List<SkillStep> steps;
    private boolean success;

    public SkillResult(String skillName) {
        this.skillName = skillName;
        this.data = new LinkedHashMap<>();
        this.steps = new ArrayList<>();
        this.success = true;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void addStep(SkillStep step) {
        steps.add(step);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSkillName() { return skillName; }
    public Map<String, Object> getData() { return data; }
    public List<SkillStep> getSteps() { return steps; }
    public boolean isSuccess() { return success; }
}
```

- [ ] **Step 4: 验证编译**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains "BUILD SUCCESSFUL"

- [ ] **Step 5: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/ && git commit -m "feat(ai-cli): add Skill, SkillStep, SkillResult core interfaces"`

---

### Task 3: Create SkillRegistry and SkillContext

**Depends on:** Task 2
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillContext.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/SkillRegistry.java`

- [ ] **Step 1: 创建 SkillContext — 持有 Skill 执行期间的共享状态和工具实例**

```java
package brut.apktool.ai.cli;

import brut.androlib.Config;
import brut.androlib.analyze.ApkAnalyzer;
import brut.androlib.search.ApkSearcher;
import brut.androlib.ai.AiPromptBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SkillContext {

    private final File apkFile;
    private final Config config;
    private ApkAnalyzer analyzer;
    private ApkSearcher searcher;
    private AiPromptBuilder promptBuilder;
    private final Map<String, Object> sharedData;

    public SkillContext(File apkFile, Config config) {
        this.apkFile = apkFile;
        this.config = config;
        this.sharedData = new HashMap<>();
    }

    public ApkAnalyzer getAnalyzer() {
        if (analyzer == null) {
            analyzer = new ApkAnalyzer(apkFile, config);
        }
        return analyzer;
    }

    public ApkSearcher getSearcher() {
        if (searcher == null) {
            searcher = new ApkSearcher(apkFile, config);
        }
        return searcher;
    }

    public AiPromptBuilder getPromptBuilder() {
        if (promptBuilder == null) {
            promptBuilder = new AiPromptBuilder(apkFile, config);
        }
        return promptBuilder;
    }

    public File getApkFile() { return apkFile; }
    public Config getConfig() { return config; }

    public void putShared(String key, Object value) {
        sharedData.put(key, value);
    }

    public Object getShared(String key) {
        return sharedData.get(key);
    }
}
```

- [ ] **Step 2: 创建 SkillRegistry — 发现、注册和查找 Skill 实现**

```java
package brut.apktool.ai.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillRegistry {

    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public void register(Skill skill) {
        skills.put(skill.name(), skill);
    }

    public Skill get(String name) {
        return skills.get(name);
    }

    public List<Skill> all() {
        return Collections.unmodifiableList(new ArrayList<>(skills.values()));
    }

    public List<String> names() {
        return new ArrayList<>(skills.keySet());
    }

    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

    public static SkillRegistry createDefault() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new brut.apktool.ai.cli.skill.QuickAnalysisSkill());
        registry.register(new brut.apktool.ai.cli.skill.SecurityAuditSkill());
        return registry;
    }
}
```

- [ ] **Step 3: 验证编译**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/ && git commit -m "feat(ai-cli): add SkillRegistry and SkillContext with lazy initialization"`

---

### Task 4: Implement QuickAnalysisSkill and SecurityAuditSkill

**Depends on:** Task 3
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/QuickAnalysisSkill.java`
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/skill/SecurityAuditSkill.java`

- [ ] **Step 1: 创建 QuickAnalysisSkill — 快速评估 Skill**

```java
package brut.apktool.ai.cli.skill;

import brut.androlib.analyze.ApkAnalyzer;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickAnalysisSkill implements Skill {

    @Override
    public String name() {
        return "quick-analysis";
    }

    @Override
    public String description() {
        return "Quick APK assessment: overview, security, attack surface, and risk interpretation";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("overview", "Get APK metadata overview", "info", Collections.emptyMap()));
        steps.add(new SkillStep("security", "Get security report with risk score", "security", Collections.emptyMap()));
        steps.add(new SkillStep("api-surface", "Get exported components attack surface", "api-surface", Collections.emptyMap()));
        steps.add(new SkillStep("interpret", "Interpret risk score and flag issues", "interpret", Collections.emptyMap()));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        try {
            ApkAnalyzer analyzer = context.getAnalyzer();

            SkillStep step1 = steps().get(0);
            step1.setStatus(SkillStep.StepStatus.RUNNING);
            Object info = analyzer.getInfo();
            step1.setResult(info);
            step1.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("info", info);
            result.addStep(step1);

            SkillStep step2 = steps().get(1);
            step2.setStatus(SkillStep.StepStatus.RUNNING);
            Object securityReport = analyzer.getSecurityReport();
            step2.setResult(securityReport);
            step2.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("security", securityReport);
            result.addStep(step2);

            SkillStep step3 = steps().get(2);
            step3.setStatus(SkillStep.StepStatus.RUNNING);
            Object apiSurface = analyzer.getApiSurface();
            step3.setResult(apiSurface);
            step3.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("apiSurface", apiSurface);
            result.addStep(step3);

            SkillStep step4 = steps().get(3);
            step4.setStatus(SkillStep.StepStatus.RUNNING);
            Object riskInterpretation = analyzer.getSecurityReport();
            step4.setResult(riskInterpretation);
            step4.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("riskInterpretation", riskInterpretation);
            result.addStep(step4);

        } catch (Exception e) {
            result.setSuccess(false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
```

- [ ] **Step 2: 创建 SecurityAuditSkill — 安全审计 Skill**

```java
package brut.apktool.ai.cli.skill;

import brut.androlib.analyze.ApkAnalyzer;
import brut.apktool.ai.cli.Skill;
import brut.apktool.ai.cli.SkillContext;
import brut.apktool.ai.cli.SkillResult;
import brut.apktool.ai.cli.SkillStep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecurityAuditSkill implements Skill {

    @Override
    public String name() {
        return "security-audit";
    }

    @Override
    public String description() {
        return "Comprehensive security audit: OWASP-mapped findings, risk score, and remediation";
    }

    @Override
    public List<SkillStep> steps() {
        List<SkillStep> steps = new ArrayList<>();
        steps.add(new SkillStep("security-report", "Automated security report", "security", Collections.emptyMap()));
        steps.add(new SkillStep("api-surface", "Attack surface analysis", "api-surface", Collections.emptyMap()));
        steps.add(new SkillStep("permissions", "Permission deep dive", "permissions", Collections.emptyMap()));
        steps.add(new SkillStep("manifest-flags", "Manifest security flags", "manifest-flags", Collections.emptyMap()));
        steps.add(new SkillStep("signing", "Signing certificate review", "signing", Collections.emptyMap()));
        steps.add(new SkillStep("sensitive-data", "Search for hardcoded secrets", "search", Collections.singletonMap("type", "strings")));
        return steps;
    }

    @Override
    public SkillResult execute(File apkFile, SkillContext context) {
        SkillResult result = new SkillResult(name());
        try {
            ApkAnalyzer analyzer = context.getAnalyzer();

            SkillStep step1 = steps().get(0);
            step1.setStatus(SkillStep.StepStatus.RUNNING);
            Object securityReport = analyzer.getSecurityReport();
            step1.setResult(securityReport);
            step1.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("security", securityReport);
            result.addStep(step1);

            SkillStep step2 = steps().get(1);
            step2.setStatus(SkillStep.StepStatus.RUNNING);
            Object apiSurface = analyzer.getApiSurface();
            step2.setResult(apiSurface);
            step2.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("apiSurface", apiSurface);
            result.addStep(step2);

            SkillStep step3 = steps().get(2);
            step3.setStatus(SkillStep.StepStatus.RUNNING);
            Object manifestInfo = analyzer.getManifestInfo();
            step3.setResult(manifestInfo);
            step3.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("manifest", manifestInfo);
            result.addStep(step3);

            SkillStep step4 = steps().get(3);
            step4.setStatus(SkillStep.StepStatus.RUNNING);
            Object manifestFlags = analyzer.getManifestFlags();
            step4.setResult(manifestFlags);
            step4.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("manifestFlags", manifestFlags);
            result.addStep(step4);

            SkillStep step5 = steps().get(4);
            step5.setStatus(SkillStep.StepStatus.RUNNING);
            Object signing = analyzer.getSigningInfo();
            step5.setResult(signing);
            step5.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("signing", signing);
            result.addStep(step5);

            SkillStep step6 = steps().get(5);
            step6.setStatus(SkillStep.StepStatus.RUNNING);
            Object searchResults = context.getSearcher().searchStrings("password|secret|token|api.?key");
            step6.setResult(searchResults);
            step6.setStatus(SkillStep.StepStatus.COMPLETED);
            result.put("sensitiveData", searchResults);
            result.addStep(step6);

        } catch (Exception e) {
            result.setSuccess(false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
```

- [ ] **Step 3: 验证编译**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:compileJava 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains "BUILD SUCCESSFUL"

- [ ] **Step 4: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/ && git commit -m "feat(ai-cli): add QuickAnalysisSkill and SecurityAuditSkill implementations"`

---

### Task 5: Create Main entry point and verify build

**Depends on:** Task 3
**Files:**
- Create: `brut.apktool/apktool-ai-cli/src/main/java/brut/apktool/ai/cli/Main.java`

- [ ] **Step 1: 创建 Main.java — apktool-ai-cli 的入口点**

```java
package brut.apktool.ai.cli;

import brut.androlib.Config;
import brut.androlib.output.JsonOutput;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class Main {

    private static final Properties props = new Properties();
    private static final Config config;

    static {
        try {
            props.load(Main.class.getResourceAsStream("/apktool.properties"));
        } catch (Exception ignored) {}
        config = new Config(props.getProperty("version", "unknown"));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        String command = args[0];

        switch (command) {
            case "skill":
                cmdSkill(args);
                break;
            case "list":
            case "ls":
                cmdListSkills();
                break;
            case "help":
            case "h":
                printUsage();
                break;
            case "version":
            case "v":
                System.out.println("apktool-ai-cli " + props.getProperty("version", "unknown"));
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    private static void cmdSkill(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: apktool-ai-cli skill <skill-name> <apk-file>");
            System.exit(1);
        }

        String skillName = args[1];
        String apkPath = args[2];

        SkillRegistry registry = SkillRegistry.createDefault();

        if (!registry.hasSkill(skillName)) {
            System.err.println("Unknown skill: " + skillName);
            System.err.println("Available skills: " + String.join(", ", registry.names()));
            System.exit(1);
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            System.err.println("APK file not found: " + apkPath);
            System.exit(1);
        }

        SkillContext context = new SkillContext(apkFile, config);
        Skill skill = registry.get(skillName);

        System.err.println("Running skill: " + skill.name() + " - " + skill.description());
        SkillResult result = skill.execute(apkFile, context);

        System.out.println(JsonOutput.toJson(result));
    }

    private static void cmdListSkills() {
        SkillRegistry registry = SkillRegistry.createDefault();
        List<Skill> skills = registry.all();

        System.out.println("{");
        System.out.println("  \"totalSkills\": " + skills.size() + ",");
        System.out.println("  \"skills\": [");
        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            System.out.println("    {\"name\": \"" + s.name() + "\", " +
                "\"description\": \"" + s.description() + "\", " +
                "\"steps\": " + s.steps().size() + "}" +
                (i < skills.size() - 1 ? "," : ""));
        }
        System.out.println("  ]");
        System.out.println("}");
    }

    private static void printUsage() {
        System.out.println("apktool-ai-cli - AI-native Android reverse engineering skill executor");
        System.out.println();
        System.out.println("Usage: apktool-ai-cli <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  skill <name> <apk>  Execute a named skill on an APK file");
        System.out.println("  list                List all available skills");
        System.out.println("  help                Show this help message");
        System.out.println("  version             Show version");
    }
}
```

- [ ] **Step 2: 验证完整构建**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:build 2>&1 | tail -10`
Expected:
  - Exit code: 0
  - Output contains "BUILD SUCCESSFUL"

- [ ] **Step 3: 验证 shadowJar 产出**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-ai-cli:shadowJar 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - JAR file exists at `brut.apktool/apktool-ai-cli/build/libs/`

- [ ] **Step 4: 验证 list 命令**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && java -jar brut.apktool/apktool-ai-cli/build/libs/apktool-ai-cli-*.jar list 2>&1 || true`
Expected:
  - Output contains "quick-analysis" and "security-audit"
  - Output contains "totalSkills: 2"

- [ ] **Step 5: 验证原有模块未受影响**
Run: `cd /data/local/tmp/workspace/github/AI-Apktool && ./gradlew :brut.apktool:apktool-cli:build 2>&1 | tail -5`
Expected:
  - Exit code: 0
  - Output contains "BUILD SUCCESSFUL"

- [ ] **Step 6: 提交**
Run: `git add brut.apktool/apktool-ai-cli/src/ && git commit -m "feat(ai-cli): add Main entry point with skill/list/help/version commands"`