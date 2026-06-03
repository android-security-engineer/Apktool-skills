# Infrastructure: Install JDK-17 on Android (Pixel 7 / aarch64 / Termux)

**Current State:** No JDK installed. Termux environment exists at `/data/data/com.termux/files/usr/` with 753+ binaries. Network currently unreachable.
**Target State:** OpenJDK-17 installed in Termux, `JAVA_HOME` and `PATH` configured, Gradle 8.14.4 available for building AI-Apktool.
**Change List:**
1. Download `openjdk-17_17.0.19_aarch64.deb` from Termux repo
2. Download `libandroid-spawn_0.3_aarch64.deb` (JDK dependency)
3. Extract both into `/data/data/com.termux/files/usr/`
4. Download Gradle 8.14.4 distribution
5. Configure `JAVA_HOME`, `PATH`, `LD_LIBRARY_PATH` in shell profile
6. Verify `java -version` and `./gradlew build`

**Rollback Plan:**
```bash
rm -rf /data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
rm -f /data/data/com.termux/files/usr/lib/libandroid-spawn.so
rm -rf /data/local/tmp/gradle-8.14.4
# Remove JAVA_HOME lines from shell profile
```

**Scope:** Small
**Risk:** Low
**Risks:**
- Network unreachable → cannot download packages (current blocker)
- Termux deb extraction requires `ar` + `xz` + `tar` (all verified present)
- Gradle launch script has quoting issues on Android shell → need to invoke java directly or fix DEFAULT_JVM_OPTS

---

### Task 1: Install OpenJDK-17 and libandroid-spawn

**Depends on:** None
**Files:**
- Download: `openjdk-17_17.0.19_aarch64.deb` (95MB)
- Download: `libandroid-spawn_0.3_aarch64.deb` (15KB)
- Extract to: `/data/data/com.termux/files/usr/`

- [ ] **Step 1: Download JDK deb package from Termux mirror**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
CURL=/data/data/com.termux/files/usr/bin/curl

$CURL -sL -o /data/local/tmp/openjdk-17.deb \
  "https://mirrors.nju.edu.cn/termux/apt/termux-main/pool/main/o/openjdk-17/openjdk-17_17.0.19_aarch64.deb"
```

Expected:
  - Exit code: 0
  - File size ~95MB

- [ ] **Step 2: Download libandroid-spawn deb package**

```bash
$CURL -sL -o /data/local/tmp/libandroid-spawn.deb \
  "https://mirrors.nju.edu.cn/termux/apt/termux-main/pool/main/liba/libandroid-spawn/libandroid-spawn_0.3_aarch64.deb"
```

Expected:
  - Exit code: 0
  - File size ~15KB

- [ ] **Step 3: Extract and install both packages**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
AR=/data/data/com.termux/files/usr/bin/ar
XZ=/data/data/com.termux/files/usr/bin/xz
TAR=/data/data/com.termux/files/usr/bin/tar
PREFIX=/data/data/com.termux/files/usr

# Install libandroid-spawn
cd /data/local/tmp && $AR x libandroid-spawn.deb
$XZ -d data.tar.xz
$TAR -xf data.tar -C / 2>/dev/null  # ownership errors are expected on Android
rm -f data.tar control.tar.xz debian-binary

# Install OpenJDK-17
cd /data/local/tmp && $AR x openjdk-17.deb
$XZ -d data.tar.xz
$TAR -xf data.tar -C / 2>/dev/null
rm -f data.tar control.tar.xz debian-binary
```

Expected:
  - `/data/data/com.termux/files/usr/lib/libandroid-spawn.so` exists
  - `/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk/bin/java` exists

- [ ] **Step 4: Verify Java installation**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
$JAVA_HOME/bin/java -version
```

Expected:
  - Output contains: "openjdk version \"17.0.19"
  - Exit code: 0

- [ ] **Step 5: Configure shell environment**

```bash
# Add to shell profile for persistence
cat >> /data/data/com.termux/files/usr/etc/bash.bashrc << 'EOF'

# JDK-17 Environment
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export PATH=$JAVA_HOME/bin:$PATH
EOF
```

Expected:
  - File appended without error

- [ ] **Step 6: Quality gate — verify all JDK tools work**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

java -version 2>&1 && javac -version 2>&1 && jar --version 2>&1
```

Expected:
  - All three commands succeed
  - java reports 17.0.19
  - javac reports 17.0.19

- [ ] **Step 7: Commit**
Run: `echo "JDK-17 installed successfully — no code changes to commit"`

---

### Task 2: Install Gradle and verify build

**Depends on:** Task 1
**Files:**
- Download: Gradle 8.14.4 distribution (~137MB)
- Extract to: `/data/local/tmp/gradle-8.14.4/`

- [ ] **Step 1: Download Gradle distribution**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
CURL=/data/data/com.termux/files/usr/bin/curl

$CURL -sL -o /data/local/tmp/gradle.zip \
  "https://services.gradle.org/distributions/gradle-8.14.4-bin.zip"
```

Expected:
  - Exit code: 0
  - File size ~137MB

- [ ] **Step 2: Extract Gradle**

```bash
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
/data/data/com.termux/files/usr/bin/unzip -o /data/local/tmp/gradle.zip -d /data/local/tmp/
```

Expected:
  - `/data/local/tmp/gradle-8.14.4/bin/gradle` exists

- [ ] **Step 3: Fix Gradle launch script for Android**

The Gradle shell script has `DEFAULT_JVM_OPTS` with nested quotes that break on Android's shell. Fix by simplifying the JVM opts.

```bash
sed -i 's/^DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS="-Xmx512m -Xms64m"/' \
  /data/local/tmp/gradle-8.14.4/bin/gradle
```

Expected:
  - The DEFAULT_JVM_OPTS line is simplified without nested quotes

- [ ] **Step 4: Run Gradle build to verify compilation**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export PATH=$JAVA_HOME/bin:$PATH

cd /data/local/tmp/workspace/github/AI-Apktool
/data/local/tmp/gradle-8.14.4/bin/gradle :brut.apktool:apktool-ai-cli:compileJava --no-daemon
```

Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 5: Quality gate — full project build**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export PATH=$JAVA_HOME/bin:$PATH

cd /data/local/tmp/workspace/github/AI-Apktool
/data/local/tmp/gradle-8.14.4/bin/gradle build --no-daemon
```

Expected:
  - Exit code: 0
  - Output contains: "BUILD SUCCESSFUL"

- [ ] **Step 6: Commit**
Run: `echo "Gradle 8.14.4 installed and build verified — no code changes to commit"`

---

## Quick-Run Script

Save this as `/data/local/tmp/install-jdk.sh` and run when network is available:

```bash
#!/bin/bash
set -e
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
CURL=/data/data/com.termux/files/usr/bin/curl
AR=/data/data/com.termux/files/usr/bin/ar
XZ=/data/data/com.termux/files/usr/bin/xz
TAR=/data/data/com.termux/files/usr/bin/tar
PREFIX=/data/data/com.termux/files/usr
WORKDIR=/data/local/tmp

echo "[1/5] Downloading libandroid-spawn..."
$CURL -sL -o $WORKDIR/libandroid-spawn.deb \
  "https://mirrors.nju.edu.cn/termux/apt/termux-main/pool/main/liba/libandroid-spawn/libandroid-spawn_0.3_aarch64.deb"

echo "[2/5] Downloading OpenJDK-17..."
$CURL -sL -o $WORKDIR/openjdk-17.deb \
  "https://mirrors.nju.edu.cn/termux/apt/termux-main/pool/main/o/openjdk-17/openjdk-17_17.0.19_aarch64.deb"

echo "[3/5] Installing packages..."
cd $WORKDIR
$AR x libandroid-spawn.deb && $XZ -d data.tar.xz && $TAR -xf data.tar -C / 2>/dev/null; rm -f data.tar control.tar.xz debian-binary
$AR x openjdk-17.deb && $XZ -d data.tar.xz && $TAR -xf data.tar -C / 2>/dev/null; rm -f data.tar control.tar.xz debian-binary

echo "[4/5] Downloading Gradle 8.14.4..."
$CURL -sL -o $WORKDIR/gradle.zip "https://services.gradle.org/distributions/gradle-8.14.4-bin.zip"
/data/data/com.termux/files/usr/bin/unzip -o $WORKDIR/gradle.zip -d $WORKDIR/
sed -i 's/^DEFAULT_JVM_OPTS=.*/DEFAULT_JVM_OPTS="-Xmx512m -Xms64m"/' $WORKDIR/gradle-8.14.4/bin/gradle

echo "[5/5] Configuring environment..."
cat >> $PREFIX/etc/bash.bashrc << 'ENVEOF'
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib
export PATH=$JAVA_HOME/bin:$PATH
ENVEOF

echo "Done! Run: source /data/data/com.termux/files/usr/etc/bash.bashrc"
echo "Then: java -version"
```
