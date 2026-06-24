# One-Pass Audit Script

Steps 1-5 of the audit hit the same APK repeatedly. Batch them with `apktool run` so they share a **single parse** (much faster) with **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

```bash
cat > audit.json <<'JSON'
{
  "apk": "app.apk",
  "commands": [
    "security",
    "api-surface",
    "permission-detail",
    "manifest-flags",
    "signing",
    { "command": "search", "params": { "type": "strings",  "pattern": "password|secret|api.?key|token|credential" } },
    { "command": "search", "params": { "type": "strings",  "pattern": "https?://" } },
    { "command": "method-search", "params": { "pattern": "DexClassLoader|PathClassLoader|TrustManager|HostnameVerifier" } }
  ]
}
JSON
apktool run audit.json
```

The output is one JSON object keyed by command, so you can drive the entire audit from a single parse and pipe the result into `jq` for reporting.
