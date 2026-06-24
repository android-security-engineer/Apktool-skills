#!/usr/bin/env node
/**
 * Regenerate src/data/commands.json from the live CLI catalog.
 *
 *   node scripts/gen-commands.mjs
 *
 * Runs `./apktool help --format=json` at the repo root and writes the result
 * to src/data/commands.json so the site builds without the jar present.
 * Requires the shadowJar to be built (./gradlew build shadowJar).
 */
import { execFileSync } from 'node:child_process'
import { writeFileSync, mkdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))
const repoRoot = resolve(here, '..', '..')
const apktool = resolve(repoRoot, 'apktool')
const outFile = resolve(here, '..', 'src', 'data', 'commands.json')

try {
  const raw = execFileSync(apktool, ['help', '--format=json'], {
    cwd: repoRoot,
    encoding: 'utf8',
    maxBuffer: 16 * 1024 * 1024,
  })
  // The wrapper may emit log lines before the JSON; slice from the first '{'.
  const start = raw.indexOf('{')
  if (start < 0) throw new Error('no JSON found in `apktool help` output')
  const catalog = JSON.parse(raw.slice(start))
  const count = catalog.commands?.length ?? 0
  if (count === 0) throw new Error('catalog has 0 commands')
  mkdirSync(dirname(outFile), { recursive: true })
  writeFileSync(outFile, JSON.stringify(catalog, null, 2) + '\n')
  console.log(`wrote ${outFile} — ${count} commands (v${catalog.version})`)
} catch (err) {
  console.error('Failed to regenerate commands.json:', err.message)
  console.error('Build the CLI first:  ./gradlew build shadowJar')
  process.exit(1)
}
