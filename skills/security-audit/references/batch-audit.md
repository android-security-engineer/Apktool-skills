# One-Pass Audit Script

Steps 1-5 of the audit hit the same APK repeatedly. Batch them with `apktool run` so they share a **single parse** (much faster) with **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/audit.json`](../scripts/audit.json):

```json
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
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/security-audit/scripts/audit.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/security-audit/scripts/audit.json | apktool pipe
```

The output is one JSON object keyed by command, so you can drive the entire audit from a single parse and pipe the result into `jq` for reporting.
