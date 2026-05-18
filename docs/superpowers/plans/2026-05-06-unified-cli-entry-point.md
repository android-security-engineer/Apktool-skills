# Unified CLI Entry Point Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`
> Steps use checkbox (`- [ ]`) syntax.

**Goal:** 在项目根目录创建统一的 `apktool` CLI 入口脚本，让用户可以直接 `apktool info app.apk` 而不需要 `java -jar apktool.jar info app.apk`。同时更新所有 Skill 文档和 CLAUDE.md 使用新格式。

**Architecture:** 用户输入 `apktool <command>` → 根目录 wrapper 脚本自动查找构建产物 JAR（优先 `build/libs/apktool_*.jar`，然后 `brut.apktool/apktool-cli/build/libs/apktool-cli-all.jar`，最后 `apktool-cli.jar`）→ `exec java -jar $jarpath "$@"` → Main.java 处理命令。复用现有 scripts/osx/linux/windows wrapper 的逻辑，但简化为开发环境友好版本。

**Tech Stack:** Bash (shell wrapper), Java 17, Gradle 8.x

**Risks:**
- Task 1 wrapper 脚本需要兼容 macOS 和 Linux → 缓解：使用 POSIX sh 兼容语法，与现有 scripts/osx 和 scripts/linux 保持一致
- Task 3 修改 11 个 skill 文件，量大但都是简单文本替换 → 缓解：逐文件使用 replace_all

---

### Task 1: 创建根目录 apktool wrapper 脚本 — 统一 CLI 入口

**Depends on:** None
**Files:**
- Create: `apktool`（项目根目录）

- [ ] **Step 1: 创建 apktool wrapper 脚本 — 自动查找构建产物 JAR 并执行**

```bash
#!/bin/bash
# AI-Apktool unified CLI entry point
# Automatically finds the built JAR and executes it.
# Usage: apktool <command> [options] <args>

# Resolve script directory (follow symlinks)
prog="$0"
while [ -h "${prog}" ]; do
    newProg=$(readlink "${prog}")
    if expr "x${newProg}" : 'x/' >/dev/null; then
        prog="${newProg}"
    else
        progdir=$(dirname "${prog}")
        prog="${progdir}/${newProg}"
    fi
done
progdir=$(cd "$(dirname "${prog}")" && pwd)

# Find the JAR file — try multiple locations in order of preference
find_jar() {
    # 1. ProGuard-optimized release jar (apktool_*.jar in build/libs/)
    local release_jar
    release_jar=$(ls "$progdir/build/libs/apktool_"*.jar 2>/dev/null | sort -V | tail -n 1)
    if [ -n "$release_jar" ] && [ -r "$release_jar" ]; then
        echo "$release_jar"
        return 0
    fi

    # 2. Shadow jar (apktool-cli-all.jar)
    local shadow_jar="$progdir/brut.apktool/apktool-cli/build/libs/apktool-cli-all.jar"
    if [ -r "$shadow_jar" ]; then
        echo "$shadow_jar"
        return 0
    fi

    # 3. Plain cli jar (for development)
    local cli_jar="$progdir/brut.apktool/apktool-cli/build/libs/apktool-cli.jar"
    if [ -r "$cli_jar" ]; then
        echo "$cli_jar"
        return 0
    fi

    # 4. Root build/libs fallback
    local root_jar="$progdir/build/libs/apktool-cli.jar"
    if [ -r "$root_jar" ]; then
        echo "$root_jar"
        return 0
    fi

    echo "ERROR: Cannot find apktool JAR. Run './gradlew build shadowJar' first." >&2
    return 1
}

jarpath=$(find_jar) || exit 1

javaOpts="-Xmx1024M -Dfile.encoding=utf-8"
javaOpts="$javaOpts -Djdk.util.zip.disableZip64ExtraFieldValidation=true"
javaOpts="$javaOpts -Djdk.nio.zipfs.allowDotZipEntry=true"

# Extract -J options for Java
while expr "x$1" : 'x-J' >/dev/null; do
    opt=$(expr "$1" : '-J\(.*\)')
    javaOpts="${javaOpts} -${opt}"
    shift
done

exec java $javaOpts -Djava.awt.headless=true -jar "$jarpath" "$@"
```

- [ ] **Step 2: 设置脚本可执行权限**
Run: `chmod +x /Users/cc11001100/github/android-reverse-hub/AI-Apktool/apktool`
Expected:
  - Exit code: 0

- [ ] **Step 3: 验证脚本可执行**
Run: `test -x /Users/cc11001100/github/android-reverse-hub/AI-Apktool/apktool && echo "OK"`
Expected:
  - Exit code: 0
  - Output contains: "OK"

- [ ] **Step 4: 提交**
Run: `git add apktool && git commit -m "feat(cli): add unified apktool wrapper script at project root"`

---

### Task 2: 更新 CLAUDE.md — 使用 apktool 命令替代 java -jar 格式

**Depends on:** Task 1
**Files:**
- Modify: `CLAUDE.md:8-28`（Quick Start 区块）

- [ ] **Step 1: 替换 CLAUDE.md 中的 java -jar 格式为 apktool 格式**
文件: `CLAUDE.md`

将所有 `java -jar apktool.jar` 替换为 `apktool`：

```markdown
## Quick Start

```bash
# Full analysis of an APK
apktool analyze app.apk

# Quick info
apktool info app.apk

# Security audit
apktool security app.apk

# Compare two versions
apktool diff app_v1.apk app_v2.apk

# Search for patterns
apktool search app.apk "password" -t strings

# Generate AI prompts
apktool ai app.apk -a security-review

# JSON help catalog
apktool help --format json
```
```

同时更新 Output Format 区块：

```markdown
## Output Format

All analysis commands output JSON to stdout. Use `jq` or similar tools to parse:

```bash
apktool info app.apk | jq '.packageName'
apktool security app.apk | jq '.riskScore'
apktool api-surface app.apk | jq '.exportedActivities[].name'
apktool ai app.apk -a context | jq '.'
```
```

同时更新 Build 区块：

```markdown
## Build

```bash
./gradlew build shadowJar
# Then use: apktool <command>
```
```

- [ ] **Step 2: 验证 CLAUDE.md 更新**
Run: `grep -c 'java -jar apktool.jar' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/CLAUDE.md`
Expected:
  - Output contains: "0"（不再有 java -jar 格式）

- [ ] **Step 3: 提交**
Run: `git add CLAUDE.md && git commit -m "docs: update CLAUDE.md to use apktool command instead of java -jar"`

---

### Task 3: 更新所有 11 个 Skill 文件 — 使用 apktool 命令格式

**Depends on:** Task 2
**Files:**
- Modify: `skills/quick-analysis/SKILL.md`
- Modify: `skills/security-audit/SKILL.md`
- Modify: `skills/compare/SKILL.md`
- Modify: `skills/reverse/SKILL.md`
- Modify: `skills/reference/SKILL.md`
- Modify: `skills/decode-build/SKILL.md`
- Modify: `skills/dex-deep-dive/SKILL.md`
- Modify: `skills/network-analysis/SKILL.md`
- Modify: `skills/malware-hunt/SKILL.md`
- Modify: `skills/resource-explorer/SKILL.md`
- Modify: `skills/signing-verify/SKILL.md`

- [ ] **Step 1: 批量替换所有 Skill 文件中的 java -jar apktool.jar 为 apktool**
Run: `cd /Users/cc11001100/github/android-reverse-hub/AI-Apktool && for f in skills/*/SKILL.md; do sed -i '' 's/java -jar apktool.jar/apktool/g' "$f"; done && echo "Replaced in all skill files"`
Expected:
  - Exit code: 0
  - Output contains: "Replaced in all skill files"

- [ ] **Step 2: 验证所有 Skill 文件不再包含 java -jar 格式**
Run: `grep -rl 'java -jar apktool.jar' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/ 2>/dev/null | wc -l`
Expected:
  - Output contains: "0"

- [ ] **Step 3: 提交**
Run: `git add skills/*/SKILL.md && git commit -m "docs(skills): update all 11 skills to use apktool command format"`

---

### Task 4: 更新 AI_COMMANDS.md 和 reference Skill — 使用 apktool 格式

**Depends on:** Task 3
**Files:**
- Modify: `AI_COMMANDS.md`
- Modify: `skills/reference/SKILL.md`（补充 Universal Patterns 区块）

- [ ] **Step 1: 替换 AI_COMMANDS.md 中的 java -jar 格式**
文件: `AI_COMMANDS.md`

将所有 `apktool` 前缀的命令格式统一（文件中已经是 `apktool <command>` 格式，无需替换 java -jar）。

验证文件格式一致性：
Run: `grep -c 'java -jar' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/AI_COMMANDS.md`
Expected:
  - Output contains: "0"

- [ ] **Step 2: 更新 reference Skill 的 Universal Patterns 区块 — 添加 apktool wrapper 说明**
文件: `skills/reference/SKILL.md:13-21`

```markdown
## Universal Patterns

All analysis commands output JSON to stdout:
```bash
apktool <command> <apk-file>
```

Using the unified CLI wrapper (project root `apktool` script):
```bash
apktool info app.apk
apktool security app.apk
apktool analyze app.apk
```

Or directly with the JAR:
```bash
java -jar apktool.jar <command> <apk-file>
```

JSON Help Catalog:
```bash
apktool help --format json
```
```

- [ ] **Step 3: 验证 reference Skill 更新**
Run: `grep -c 'Unified CLI wrapper' /Users/cc11001100/github/android-reverse-hub/AI-Apktool/skills/reference/SKILL.md`
Expected:
  - Output contains: "1"

- [ ] **Step 4: 提交**
Run: `git add AI_COMMANDS.md skills/reference/SKILL.md && git commit -m "docs: update AI_COMMANDS.md and reference skill with unified CLI wrapper info"`