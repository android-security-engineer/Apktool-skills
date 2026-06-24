# One-Pass DEX Survey Script

The DEX overview (Step 1) and the method/field searches (Step 3) hit the same APK with many commands. Batch them with `apktool run` for a **single shared parse** and **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/dex.json`](../scripts/dex.json):

```json
{
  "apk": "app.apk",
  "commands": [
    "dex-list",
    "dex-info",
    "structure",
    "class-list",
    { "command": "method-search", "params": { "pattern": "Cipher|SecretKey|MessageDigest|AES|RSA" } },
    { "command": "method-search", "params": { "pattern": "onCreate|onStart|onResume|onPause|onStop|onDestroy" } },
    { "command": "field-search", "params": { "pattern": "password|secret|key|token|credential" } }
  ]
}
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/dex-deep-dive/scripts/dex.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/dex-deep-dive/scripts/dex.json | apktool pipe
```

The output is one JSON object keyed by command. This script covers the breadth-first survey; the per-class commands that need a class name (`class-info`, `inheritance`) are inherently targeted — run those individually once `class-list` and the searches point you at the classes worth opening.
