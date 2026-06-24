# One-Pass Resource Audit Script

The resource overview, file exploration, and string steps all hit the same APK with many commands. Batch them with `apktool run` for a **single shared parse** and **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/resources.json`](../scripts/resources.json):

```json
{
  "apk": "app.apk",
  "commands": [
    "resources",
    "resource-packages",
    "lib-frame-packages",
    "locales",
    "file-list",
    "file-hash",
    "asset-list",
    "native-libs",
    "uses-libs",
    { "command": "strings", "params": { "pattern": "google-analytics|firebase|mixpanel|flurry|amplitude" } }
  ]
}
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/resource-explorer/scripts/resources.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/resource-explorer/scripts/resources.json | apktool pipe
```

The output is one JSON object keyed by command — a full resource/locale/asset/native-lib inventory from a single parse. The `apk-info` command is omitted here because it reads a *decoded* apktool directory rather than the APK; run it separately against your decode output.
