// Typed accessor over the generated CLI catalog (commands.json),
// produced by `npm run gen:commands` from `apktool help --format=json`.
import catalog from './commands.json'

export type CommandCategory =
  | 'core'
  | 'analysis'
  | 'search'
  | 'scripting'
  | 'ai'
  | 'service'
  | 'general'

export interface CommandParam {
  name: string
  description?: string
  required?: boolean
}

export interface Command {
  name: string
  shortName?: string
  description: string
  usage: string
  outputFormat: string
  examples: string[]
  params: CommandParam[]
  category: CommandCategory
}

export interface Catalog {
  tool: string
  version: string
  description: string
  commands: Command[]
}

export const CATALOG = catalog as Catalog
export const COMMANDS = CATALOG.commands

export const CATEGORY_ORDER: CommandCategory[] = [
  'core',
  'analysis',
  'search',
  'scripting',
  'ai',
  'service',
  'general',
]

export const CATEGORY_COLOR: Record<CommandCategory, string> = {
  core: 'blue',
  analysis: 'green',
  search: 'gold',
  scripting: 'purple',
  ai: 'magenta',
  service: 'cyan',
  general: 'default',
}

export const CATEGORY_LABEL: Record<CommandCategory, { en: string; zh: string }> = {
  core: { en: 'Core', zh: '核心' },
  analysis: { en: 'Analysis', zh: '分析' },
  search: { en: 'Search', zh: '搜索' },
  scripting: { en: 'Scripting', zh: '脚本' },
  ai: { en: 'AI', zh: 'AI' },
  service: { en: 'Service', zh: '服务' },
  general: { en: 'General', zh: '通用' },
}

export function countByCategory(): Record<CommandCategory, number> {
  const out = {} as Record<CommandCategory, number>
  for (const c of CATEGORY_ORDER) out[c] = 0
  for (const cmd of COMMANDS) out[cmd.category] = (out[cmd.category] ?? 0) + 1
  return out
}
