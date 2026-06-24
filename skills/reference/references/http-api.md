# HTTP API Reference (`apktool serve`)

Start with `apktool serve [-p <port>]` (default 8080). Base path: `/api/v1`. Pass the APK with `?apk=<path>` unless noted. The CLI and HTTP API are thin wrappers over the same library methods, so JSON field names match across both surfaces (see [commands.md](commands.md)).

## Analysis (GET, `?apk=<path>`)

`health`, `info`, `manifest`, `manifest-xml`, `permissions`, `permission-detail`, `activities`, `services`, `receivers`, `providers`, `components`, `api-surface`, `sdk-info`, `version`, `resources`, `resource-packages`, `lib-frame-packages`, `uses-libs`, `locales`, `native-libs`, `security`, `signing`, `manifest-flags`, `dex-list`, `dex-info`, `dex-strings`, `class-list`, `structure`, `file-list`, `file-hash`, `asset-list`, `analyze`.

```bash
curl 'http://localhost:8080/api/v1/info?apk=/path/to/app.apk'
curl 'http://localhost:8080/api/v1/security?apk=/path/to/app.apk' | jq '.riskScore'
```

## Parameterized GET

```
GET /api/v1/search?apk=<path>&type=<strings|classes|methods>&pattern=<pattern>
GET /api/v1/strings?apk=<path>&pattern=<pattern>
GET /api/v1/class-info?apk=<path>&class=<name>
GET /api/v1/inheritance?apk=<path>&class=<name>
GET /api/v1/method-search?apk=<path>&pattern=<pattern>
GET /api/v1/field-search?apk=<path>&pattern=<pattern>
GET /api/v1/apk-info?dir=<path>
GET /api/v1/diff?apk1=<path>&apk2=<path>
GET /api/v1/ai?apk=<path>&action=<explain|security-review|summarize|context>
GET /api/v1/list-frameworks
```

## Operations (POST)

```
POST /api/v1/decode?apk=<path>&output=<dir>
POST /api/v1/build?dir=<path>&output=<apk>
POST /api/v1/install-framework?apk=<path>
POST /api/v1/clean-frameworks
POST /api/v1/publicize-resources?arsc=<path>
```

> Note: `run`/`pipe` are CLI-only — there is no batch-scripting HTTP endpoint.
