---
name: reference
description: Exact CLI syntax, options, output JSON fields, and HTTP endpoints for every apktool command. Use this whenever you need the precise flags, parameters, or output schema of a specific command instead of a guided workflow, or are unsure how to invoke a command or parse its result.
---

# AI-Apktool Command Reference

The lookup hub for AI-Apktool. This page lists every command and the universal conventions; the detailed tables (output fields, flags, endpoints) live in `references/` and should be **read on demand** — open the file named below only when you need that level of detail.

## Conventions

- **Invocation**: `apktool <command> <apk-file> [options]`. The unified `apktool` wrapper invokes the shaded CLI jar.
- **Output**: every *analysis* command prints a single JSON object to **stdout**; diagnostics go to stderr. Pipe into `jq` to extract fields.
- **Regex**: all `pattern` arguments are **Java regular expressions**, case-sensitive by default. Prefix with `(?i)` for case-insensitive, e.g. `(?i)password`.
- **Exit codes**: `0` on success, non-zero on failure (bad args, missing file, parse error); the message goes to stderr.
- **Targets**: most commands take an `.apk`. Exceptions: `apk-info` reads a *decoded directory*; `diff` takes two APKs; `run` takes a script file; `pipe` reads stdin.
- **Machine catalog**: `apktool help --format=json` emits the full catalog (names, aliases, usage, examples) — the best source for tooling.

## Command index

Pick a command here, then open the matching reference file for its exact fields/flags.

- **Core ops** — `decode`/`d`, `build`/`b`, `install-framework`/`if`, `clean-frameworks`/`cf`, `list-frameworks`/`lf`, `publicize-resources`/`pr`
- **Metadata & components** — `info`, `manifest`, `manifest-xml`, `permissions`, `permission-detail`, `activities`, `services`, `receivers`, `providers`, `components`, `api-surface`, `sdk-info`, `version`, `apk-version`, `apk-info`
- **Resources & files** — `resources`, `resource-packages`, `lib-frame-packages`, `uses-libs`, `locales`, `native-libs`, `file-list`, `file-hash`, `asset-list`
- **Security** — `security`, `signing`, `manifest-flags`
- **DEX & code** — `dex-list`, `dex-info`, `dex-strings`, `class-list`, `class-info`, `method-search`, `field-search`, `inheritance`, `structure`
- **Strings / search / diff / combined** — `strings`, `search`, `diff`, `analyze`
- **Scripting / AI / service** — `run`, `pipe`, `ai`, `serve`
- **Meta** — `help`/`h`, `version`/`v`

## Deeper reference (read on demand)

| When you need… | Read |
|----------------|------|
| Output fields of an analysis command, positional-arg commands, `search`/`diff`/`structure` usage | [`references/commands.md`](references/commands.md) |
| `decode`/`build`/framework flags and core-command syntax | [`references/cli-flags.md`](references/cli-flags.md) |
| Batch scripting (`run`/`pipe` JSON format), `ai` actions, `serve` | [`references/scripting.md`](references/scripting.md) |
| HTTP API endpoints and `curl` examples | [`references/http-api.md`](references/http-api.md) |

## Help & version

```bash
apktool help                 # human-readable help
apktool help --format=json   # machine-readable command catalog
apktool version              # tool version (alias: -v)
```
