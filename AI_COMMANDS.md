# AI-Apktool Commands Reference

## Phase 1: Information Query Commands

### `apktool info <apk-file>`
Returns APK metadata summary as JSON (package name, version, file size, dex count, architectures, component counts).

### `apktool manifest <apk-file>`
Returns decoded AndroidManifest.xml content as JSON (all components, permissions, SDK info, debuggable flag).

### `apktool permissions <apk-file>`
Returns permission list as JSON array.

### `apktool activities <apk-file>`
Returns Activity list as JSON (name, exported status, intent filters).

### `apktool services <apk-file>`
Returns Service list as JSON.

### `apktool receivers <apk-file>`
Returns BroadcastReceiver list as JSON.

### `apktool providers <apk-file>`
Returns ContentProvider list as JSON.

### `apktool sdk-info <apk-file>`
Returns SDK version information as JSON (minSdkVersion, targetSdkVersion, maxSdkVersion).

### `apktool resources <apk-file>`
Returns resource table summary as JSON (package name, type counts, locales, total entries).

## Phase 2: Search Commands

### `apktool search <apk-file> [pattern] -t <type>`
Searches APK content. Types: `strings`, `classes`, `methods`. Default: `classes`.
Pattern is a Java regex (default: `.*` for match all).

Examples:
```bash
apktool search app.apk "Activity" -t classes
apktool search app.apk "http.*" -t strings
apktool search app.apk "onCreate" -t methods
```

## Phase 3: Analysis Commands

### `apktool security <apk-file>`
Returns security analysis report as JSON (dangerous permissions, high-risk exported components, risk score 0-100, findings list).

### `apktool diff <apk1> <apk2>`
Compares two APKs and returns differences as JSON (added/removed permissions, activities, services, version changes).

### `apktool structure <apk-file>`
Returns code structure overview as JSON (total classes, methods, fields, package distribution).

## Phase 4: HTTP API Server

### `apktool serve [-p <port>]`
Starts HTTP API server (default port 8080).

REST Endpoints:
- `GET /api/v1/info?apk=<path>` - APK summary
- `GET /api/v1/manifest?apk=<path>` - Manifest info
- `GET /api/v1/permissions?apk=<path>` - Permission list
- `GET /api/v1/security?apk=<path>` - Security report
- `GET /api/v1/search?apk=<path>&type=classes&pattern=.*` - Search
- `GET /api/v1/diff?apk1=<path>&apk2=<path>` - Diff two APKs
- `GET /api/v1/resources?apk=<path>` - Resource summary
- `GET /api/v1/health` - Health check

## Phase 5: AI Integration

### `apktool ai <apk-file> -a <action>`
Generates LLM-ready prompts for AI analysis. Actions:
- `explain` (default) - Generate a comprehensive analysis prompt
- `security-review` - Generate a focused security review prompt
- `summarize` - Generate a concise summary prompt

The output is a text prompt that can be fed directly to any LLM (Claude, GPT, etc.).

## Original Commands (preserved)

All original Apktool commands remain unchanged:
- `apktool d|decode` - Decode APK
- `apktool b|build` - Build APK
- `apktool if|install-framework` - Install framework
- `apktool cf|clean-frameworks` - Clean frameworks
- `apktool lf|list-frameworks` - List frameworks
- `apktool pr|publicize-resources` - Publicize resources
