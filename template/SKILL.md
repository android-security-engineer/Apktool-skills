---
name: skill-name
description: One sentence on WHAT this skill does, then a pushy trigger list. Use this whenever <list concrete user phrases, contexts, and keywords>, even if the user doesn't explicitly say "<obvious keyword>".
---

<!--
  HOW TO USE THIS TEMPLATE
  ------------------------
  1. Copy this folder to skills/<your-skill-name>/ (the dir name MUST equal the
     frontmatter `name`: lowercase letters, numbers, hyphens only).
  2. Frontmatter rules (see CONTRIBUTING.md for the full checklist):
       - ONLY `name` and `description`. Do NOT add `autoInvoke` or other fields
         (`compatibility` is allowed but rarely needed). Total frontmatter < 1024 chars.
       - `description` must state BOTH what the skill does AND when to trigger,
         and be slightly "pushy" to combat undertriggering — per the official
         Anthropic skill-creator spec. Keep it under 500 chars, keyword-rich.
  3. Keep this SKILL.md body under 500 lines. Push exact syntax / long tables /
     reference material into references/*.md and point to them (progressive
     disclosure) — see the `reference` skill for the pattern.
  4. Delete every comment block before committing.
-->

# Skill Title

One- or two-sentence overview of what this workflow accomplishes and its core idea.

## When to Use

- <symptom or task that should trigger this skill>
- <another concrete situation>
- <when NOT to use it, if non-obvious>

## Prerequisites

The unified `apktool` CLI must be on your PATH. Build it from source with `./gradlew build shadowJar` (the `./apktool` wrapper invokes the resulting jar). All analysis commands print JSON to stdout; run `apktool help --format=json` for the full machine-readable command catalog.

## Workflow

### Step 1: <Action>

```bash
apktool <command> <apk-file>
```

Review: `<key output fields the reader should look at>`

### Step 2: <Action>

```bash
apktool <command> <apk-file> -p '<pattern>'
```

## Common Patterns

```bash
# <what this recipe accomplishes>
apktool <command> app.apk
apktool <command> app.apk
```

---

> **Exact syntax & fields:** for any command's full options, output fields, or HTTP endpoint, use the **`reference`** skill — it keeps the full tables in on-demand `references/` files.
