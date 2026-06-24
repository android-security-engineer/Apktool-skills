// HTTP API endpoint model. GET analysis endpoints are derived from the command
// catalog; the operational POST endpoints are listed explicitly.
import { COMMANDS } from './commands'

export interface Endpoint {
  method: 'GET' | 'POST'
  path: string
  description: string
  /** extra query params beyond ?apk=<path> */
  extraParams?: string
}

// Commands that are NOT simple `?apk=<path>` GET analysis endpoints.
const NON_GET_ANALYSIS = new Set([
  'decode',
  'build',
  'install-framework',
  'clean-frameworks',
  'list-frameworks',
  'publicize-resources',
  'run',
  'pipe',
  'serve',
  'help',
])

// Commands needing an extra query param beyond ?apk=.
const EXTRA_PARAMS: Record<string, string> = {
  'class-info': 'class=<name>',
  inheritance: 'class=<name>',
  search: 'type=<type>&pattern=<pattern>',
  'method-search': 'pattern=<pattern>',
  'field-search': 'pattern=<pattern>',
  strings: 'pattern=<pattern>',
  ai: 'action=<explain|security-review|summarize|context>',
}

export function getEndpoints(): Endpoint[] {
  const eps: Endpoint[] = COMMANDS.filter(
    (c) => !NON_GET_ANALYSIS.has(c.name) && c.category !== 'general',
  ).map((c) => ({
    method: 'GET' as const,
    path: `/api/v1/${c.name}`,
    description: c.description,
    extraParams: EXTRA_PARAMS[c.name],
  }))
  // A couple of derived/aliased endpoints surfaced by the server.
  eps.push({
    method: 'GET',
    path: '/api/v1/health',
    description: 'Health check (no apk parameter).',
  })
  eps.push({
    method: 'GET',
    path: '/api/v1/diff',
    description: 'Compare two APKs.',
    extraParams: 'apk1=<path>&apk2=<path>',
  })
  eps.push({
    method: 'GET',
    path: '/api/v1/list-frameworks',
    description: 'List installed framework files (no apk parameter).',
  })
  return eps.sort((a, b) => a.path.localeCompare(b.path))
}

export const postEndpoints: Endpoint[] = [
  {
    method: 'POST',
    path: '/api/v1/decode',
    description: 'Decode an APK to a directory.',
    extraParams: 'apk=<path>&output=<dir>',
  },
  {
    method: 'POST',
    path: '/api/v1/build',
    description: 'Build an APK from a decoded directory.',
    extraParams: 'dir=<path>&output=<apk>',
  },
  {
    method: 'POST',
    path: '/api/v1/install-framework',
    description: 'Install a framework APK.',
    extraParams: 'apk=<path>',
  },
  {
    method: 'POST',
    path: '/api/v1/clean-frameworks',
    description: 'Clean framework files.',
  },
  {
    method: 'POST',
    path: '/api/v1/publicize-resources',
    description: 'Make resources public in the ARSC.',
    extraParams: 'arsc=<path>',
  },
]
