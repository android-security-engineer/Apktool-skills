# One-Pass Network Audit Script

The network workflow fires endpoint searches, client/SSL method searches, and manifest/security checks at one APK. Batch them with `apktool run` for a **single shared parse** and **per-command error isolation**. See the `reference` skill (`references/scripting.md`) for the full script format.

A ready-to-run script ships with this skill at [`scripts/network.json`](../scripts/network.json):

```json
{
  "apk": "app.apk",
  "commands": [
    "manifest-flags",
    "security",
    "api-surface",
    "components",
    { "command": "strings", "params": { "pattern": "https?://|api\\.|/v[0-9]/|graphql|websocket|/api/" } },
    { "command": "method-search", "params": { "pattern": "OkHttp|Retrofit|HttpURLConnection|Volley|HttpClient" } },
    { "command": "method-search", "params": { "pattern": "SSLSocket|TrustManager|HostnameVerifier|SSLContext" } },
    { "command": "method-search", "params": { "pattern": "@GET|@POST|@PUT|@DELETE" } }
  ]
}
```

Run it directly (the embedded `apk` field expects a file named `app.apk` in the working directory):

```bash
apktool run skills/network-analysis/scripts/network.json
```

Or aim it at any target without editing the file — rewrite the `apk` field on the fly with `jq` and feed the result to `pipe` (which reads the script, including its `apk` field, from stdin):

```bash
jq '.apk="target.apk"' skills/network-analysis/scripts/network.json | apktool pipe
```

The result is one JSON object keyed by command — cross-reference `manifest-flags.usesCleartextTraffic` with the discovered `http://` endpoints, and the `TrustManager`/`HostnameVerifier` hits with any custom SSL handling, all from a single parse.
