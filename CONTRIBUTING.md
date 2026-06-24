# Contributing

This repository is a [Claude Code](https://docs.claude.com/en/docs/claude-code) **skills plugin + marketplace** built on top of an Apktool fork. It follows the official [Anthropic Agent Skills spec](https://github.com/anthropics/skills) and the [`skill-creator`](https://github.com/anthropics/skills/blob/main/skills/skill-creator/SKILL.md) authoring guidance.

## Repository layout

```
.
├── .claude-plugin/
│   ├── plugin.json        # plugin manifest (name, version, repo, keywords)
│   └── marketplace.json   # marketplace manifest so the repo is installable
├── skills/                # one directory per skill
│   └── <skill-name>/
│       ├── SKILL.md       # required: frontmatter + Markdown instructions
│       ├── references/    # optional: on-demand detail (progressive disclosure)
│       └── scripts/       # optional: runnable artifacts (e.g. `apktool run` JSON)
├── template/
│   └── SKILL.md           # copy this to start a new skill
├── CLAUDE.md              # project instructions + full command catalog
└── README.md / README.zh-CN.md
```

The `brut.apktool/` tree and Gradle files are the underlying Apktool engine the skills drive; you normally don't touch them when working on skills.

## Adding a new skill

1. **Copy the template:** `cp -r template skills/<skill-name>` (the directory name must equal the frontmatter `name` — lowercase letters, numbers, hyphens only).
2. **Write the frontmatter.** Only `name` and `description` (`compatibility` is allowed but rarely needed). Total frontmatter under 1024 chars.
3. **Write the `description` to trigger well** (this is the single most important field — it's how Claude decides to load the skill):
   - State **both what the skill does AND when to use it**.
   - Be slightly **"pushy"** to combat undertriggering, e.g. `… Use this whenever the user mentions X, Y, or Z, even if they don't explicitly ask for "X".`
   - Keep it under 500 chars and rich in trigger keywords/synonyms.
4. **Keep `SKILL.md` under 500 lines.** Move exact command syntax, long tables, and reference material into `references/*.md` and point to them from the body — see the `reference` skill for the established pattern. Add a table of contents to any reference file over ~300 lines.
5. **End with the reference pointer footer** so the skill defers exact syntax to the `reference` hub (see any existing skill).
6. **Register it in the docs:** add a row to the skill table in `README.md`, `README.zh-CN.md`, and `CLAUDE.md`.

## Conventions

- **No `autoInvoke`** (or other non-spec frontmatter fields) — it is inert; skills are model-invoked by `description` match.
- **Commands print JSON to stdout.** The machine-readable catalog is `apktool help --format=json` (note the `=`; the space form falls through to text help).
- **One excellent example** beats many mediocre ones — prefer real, runnable commands.
- Build before testing: `./gradlew build shadowJar`, then drive skills through the `./apktool` wrapper.

## Skill anatomy (from the official spec)

Skills use three-level progressive disclosure:

1. **Metadata** (`name` + `description`) — always in context.
2. **`SKILL.md` body** — loaded when the skill triggers (< 500 lines ideal).
3. **Bundled resources** (`references/`, optional `scripts/`, `assets/`) — read on demand.

Put searchable terms early and often so future Claude can find the skill.

**`scripts/` example:** `security-audit/scripts/audit.json` and `malware-hunt/scripts/hunt.json` are runnable [`apktool run`](https://github.com/android-security-engineer/Apktool-skills) batch scripts. They execute many analysis commands against a single shared parse of one APK, with per-command error isolation. Ship deterministic, repetitive command sequences as `scripts/` artifacts rather than as copy-paste heredocs in Markdown — the skill body links to them and shows how to run or `pipe` them at any target.
