# One-Pass Signing Audit Script

The signing audit pulls the certificate, manifest flags, security report, and file hashes from one APK. Batch them with `apktool run` for a **single shared parse** and **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/signing.json`](../scripts/signing.json):

```json
{
  "apk": "app.apk",
  "commands": [
    "signing",
    "manifest-flags",
    "security",
    "file-hash"
  ]
}
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/signing-verify/scripts/signing.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/signing-verify/scripts/signing.json | apktool pipe
```

The result is one JSON object keyed by command — read `signing.certificates[]` for subject/issuer/fingerprints and signing schemes, `manifest-flags.debuggable` for debug builds, and `file-hash` for the integrity baseline. Cross-version certificate comparison still uses `diff` against a second APK (a two-APK operation, outside a single-parse script).
