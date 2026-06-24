# Batch Scripting, AI & Service

`run`/`pipe` execute many analysis commands against a **single shared parse** of one APK — far faster than invoking the CLI N times, with **error isolation** (one command failing does not stop the others).

## `run` / `pipe`

```bash
apktool run <script.json>    # read the JSON script from a file
apktool pipe < script.json   # read the JSON script from stdin
```

Script format — commands may be bare strings or `{command, params}` objects:

```json
{
  "apk": "app.apk",
  "commands": [
    "info",
    "security",
    "signing",
    { "command": "search", "params": { "type": "strings", "pattern": "password|secret|key" } },
    { "command": "method-search", "params": { "pattern": "Cipher|DexClassLoader" } },
    "analyze"
  ]
}
```

Output is a single JSON object: `{ apk, totalCommands, results: { <command>: <result-or-error> } }`. When the same command appears more than once, its result key is suffixed with an index. The `apk` field is required (it must point at an existing file) for both `run` and `pipe`. To retarget a saved script without editing it, rewrite the field on the fly: `jq '.apk="other.apk"' script.json | apktool pipe`.

## `ai`

```bash
apktool ai <apk-file> -a <action>     # -a / --action, default: explain
```

| Action | Output |
|--------|--------|
| `explain` | Natural-language prompt describing the app for an LLM |
| `security-review` | Prompt geared toward a security assessment |
| `summarize` | Concise summary prompt |
| `context` | Structured `AiContext` JSON (facts, not a prompt) to feed an agent |

## `serve`

```bash
apktool serve [-p <port>]    # -p / --port, default 8080
```

Starts the HTTP API. See [http-api.md](http-api.md) for endpoints.
