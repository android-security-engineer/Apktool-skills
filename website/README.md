# AI-Apktool Documentation Website

The documentation site for [AI-Apktool](https://github.com/android-security-engineer/Apktool-skills)
— built with **Vite + React + TypeScript + Ant Design 5**, bilingual (EN / 中文),
and deployed to GitHub Pages.

Live: https://android-security-engineer.github.io/Apktool-skills/

## Pages

- **Home** — hero, capability stats, the feature tree, and the "Built on Apktool" comparison.
- **Get Started** — build the CLI, install the Claude Code plugin, first commands.
- **Commands** — all 51 commands in a searchable, category-filterable table with
  expandable usage / examples / output schema. Data-driven from the CLI catalog.
- **Skills** — the 11 Claude Code skills as cards.
- **AI Integration** — the six ways an agent can plug in.
- **HTTP API** — REST endpoints (GET analysis endpoints are derived from the catalog).

## Develop

```bash
cd website
npm install
npm run dev        # http://localhost:5173/Apktool-skills/
```

## Build

```bash
npm run build      # type-check + Vite build → dist/
npm run preview    # preview the production build
```

The base path defaults to `/Apktool-skills/` (the GitHub Pages project path).
Override it for other hosts:

```bash
BASE_PATH=/ npm run build
```

## Regenerate the command catalog

The command table is powered by `src/data/commands.json`, generated from the live CLI:

```bash
# from repo root: build the CLI first
./gradlew build shadowJar

# then regenerate the JSON
cd website
npm run gen:commands
```

This runs `apktool help --format=json` and writes the result to
`src/data/commands.json` (committed, so the site builds without the jar).

## Feature tree images

`public/feature-tree.png` and `public/feature-tree.zh.png` are copied from
`docs/assets/`. Regenerate them with the repo's
[`scripts/feature_tree.py`](../scripts/feature_tree.py), then copy into `public/`.
