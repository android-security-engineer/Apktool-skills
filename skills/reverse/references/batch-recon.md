# One-Pass Recon Script

The reconnaissance step (Step 1) and the security review (Step 4) hit the same APK with many commands. Batch them with `apktool run` so they share a **single parse** (much faster) with **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/recon.json`](../scripts/recon.json):

```json
{
  "apk": "app.apk",
  "commands": [
    "info",
    "security",
    "api-surface",
    "signing",
    "structure",
    "dex-info",
    { "command": "search", "params": { "type": "strings", "pattern": "password|secret|api.?key|Bearer|token|credential" } },
    { "command": "search", "params": { "type": "strings", "pattern": "https?://" } },
    { "command": "search", "params": { "type": "classes", "pattern": "Cipher|AES|RSA|DES|SecretKey|Login|Auth|Session|OAuth" } },
    { "command": "search", "params": { "type": "methods", "pattern": "exec|loadLibrary|DexClassLoader|encrypt|decrypt" } }
  ]
}
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/reverse/scripts/recon.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/reverse/scripts/recon.json | apktool pipe
```

The output is one JSON object keyed by command, so the whole recon pass comes from a single parse. Use it to scope the target, then `decode` and dig into specific classes (`class-info`, `inheritance`) for the slower, surgical work.
