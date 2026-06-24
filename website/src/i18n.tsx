import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from 'react'

export type Lang = 'en' | 'zh'

type Dict = Record<string, { en: string; zh: string }>

// UI chrome + page narrative copy. Technical data (command names, output
// schemas, skill slugs) intentionally stays English to match the jar output.
export const dict: Dict = {
  // nav
  'nav.home': { en: 'Home', zh: '首页' },
  'nav.start': { en: 'Get Started', zh: '快速开始' },
  'nav.commands': { en: 'Commands', zh: '命令参考' },
  'nav.skills': { en: 'Skills', zh: 'Skills' },
  'nav.integration': { en: 'AI Integration', zh: 'AI 接入' },
  'nav.api': { en: 'HTTP API', zh: 'HTTP API' },
  'nav.github': { en: 'GitHub', zh: 'GitHub' },
  'footer.tagline': {
    en: 'AI-native Android reverse engineering — built on Apktool.',
    zh: 'AI 原生 Android 逆向工程——基于 Apktool 二次开发。',
  },
  'footer.license': { en: 'Apache-2.0 licensed.', zh: '采用 Apache-2.0 许可。' },

  // home hero
  'home.tagline': {
    en: 'AI-native Android reverse engineering platform',
    zh: 'AI 原生 Android 逆向工程平台',
  },
  'home.subtitle': {
    en: 'Every analysis capability emits structured JSON, so Claude Code or any LLM agent can reason over an APK without scraping logs. 51 CLI commands, 11 Claude Code skills, an HTTP API, and batch scripting — one consistent core.',
    zh: '所有分析能力均输出结构化 JSON，让 Claude Code 或任意 LLM 智能体无需解析日志即可对 APK 进行推理。51 个 CLI 命令、11 个 Claude Code Skills、HTTP API 与批量脚本——共享同一内核。',
  },
  'home.cta.start': { en: 'Get Started', zh: '快速开始' },
  'home.cta.commands': { en: 'Browse Commands', zh: '浏览命令' },
  'home.cta.github': { en: 'View on GitHub', zh: '在 GitHub 查看' },
  'home.stats.title': { en: 'At a glance', zh: '能力一览' },
  'home.stat.commands': { en: 'CLI commands', zh: 'CLI 命令' },
  'home.stat.skills': { en: 'Claude Code skills', zh: 'Claude Code Skills' },
  'home.stat.categories': { en: 'command categories', zh: '命令分类' },
  'home.stat.json': { en: 'JSON output', zh: 'JSON 输出' },
  'home.tree.title': { en: 'Feature tree', zh: '功能树' },
  'home.tree.sub': {
    en: 'One picture beats a thousand words — the full capability tree at a glance.',
    zh: '一图抵千言——下面这棵树涵盖全部能力分类。',
  },
  'home.built.title': { en: 'Built on Apktool', zh: '基于 Apktool 二次开发' },
  'home.built.body': {
    en: 'A fork of Apktool by Connor Tumbleson / iBotPeaches, re-engineered into an AI-native platform. The battle-tested decode/build engine stays intact; everything that reasons about an APK and exposes it to an agent is what this fork adds.',
    zh: '本项目是 Connor Tumbleson / iBotPeaches 的 Apktool 的 fork，被重构为 AI 原生平台。久经考验的解码/构建引擎保持不变；所有对 APK 进行推理并暴露给智能体的能力，都是本 fork 新增的。',
  },
  'home.built.upstream': { en: 'Upstream Apktool', zh: '上游 Apktool' },
  'home.built.fork': { en: 'AI-Apktool (this fork)', zh: 'AI-Apktool（本 fork）' },

  // getting started
  'start.title': { en: 'Get Started', zh: '快速开始' },
  'start.intro': {
    en: 'Build the unified CLI, then drive it directly or install the Claude Code skills.',
    zh: '构建统一 CLI，然后直接使用，或安装 Claude Code Skills。',
  },
  'start.prereq': { en: 'Prerequisites', zh: '前置条件' },
  'start.prereq.claude': { en: 'Claude Code installed', zh: '已安装 Claude Code' },
  'start.prereq.jdk': { en: 'JDK 17+ (to build the CLI)', zh: 'JDK 17+（用于构建 CLI）' },
  'start.build': { en: 'Build the CLI', zh: '构建 CLI' },
  'start.plugin': { en: 'Install as a Claude Code plugin', zh: '作为 Claude Code 插件安装' },
  'start.manual': { en: 'Or install the skills manually', zh: '或手动安装 Skills' },
  'start.verify': { en: 'Verify', zh: '验证' },
  'start.first': { en: 'Your first commands', zh: '首批命令' },

  // commands page
  'cmd.title': { en: 'Command Reference', zh: '命令参考' },
  'cmd.intro': {
    en: 'All 51 commands, generated from the live `apktool help --format=json` catalog. Search, filter by category, and expand a row for usage, examples and output schema.',
    zh: '全部 51 个命令，由实时的 `apktool help --format=json` 目录生成。可搜索、按分类筛选，并展开行查看用法、示例与输出 schema。',
  },
  'cmd.search': { en: 'Search commands…', zh: '搜索命令…' },
  'cmd.col.name': { en: 'Command', zh: '命令' },
  'cmd.col.category': { en: 'Category', zh: '分类' },
  'cmd.col.desc': { en: 'Description', zh: '描述' },
  'cmd.all': { en: 'All', zh: '全部' },
  'cmd.usage': { en: 'Usage', zh: '用法' },
  'cmd.examples': { en: 'Examples', zh: '示例' },
  'cmd.output': { en: 'Output', zh: '输出' },
  'cmd.alias': { en: 'alias', zh: '别名' },
  'cmd.count': { en: 'commands', zh: '个命令' },

  // skills page
  'skills.title': { en: 'Claude Code Skills', zh: 'Claude Code Skills' },
  'skills.intro': {
    en: 'Eleven model-invoked skills covering the full APK workflow. Once installed, Claude Code discovers and invokes the right one automatically based on your task.',
    zh: '11 个由模型自动调用的 Skills，覆盖完整 APK 工作流。安装后，Claude Code 会根据任务自动发现并调用合适的那个。',
  },
  'skills.when': { en: 'When to use', zh: '适用场景' },
  'skills.invoke': { en: 'Manual invocation', zh: '手动调用' },

  // integration page
  'intg.title': { en: 'AI Agent Integration', zh: 'AI Agent 接入方式' },
  'intg.intro': {
    en: 'AI-Apktool offers an agent multiple ways to plug in, all sharing the same JSON-emitting core. Pick the surface that matches your runtime.',
    zh: 'AI-Apktool 为智能体提供多种接入方式，全部共享同一个输出 JSON 的内核。按你的运行环境选择合适的接入面。',
  },

  // api page
  'api.title': { en: 'HTTP API', zh: 'HTTP API' },
  'api.intro': {
    en: 'Start the server with `apktool serve` and reach every capability over REST. Analysis endpoints are GET; operations are POST.',
    zh: '用 `apktool serve` 启动服务，即可通过 REST 访问全部能力。分析端点为 GET，操作端点为 POST。',
  },
  'api.get': { en: 'GET — analysis endpoints', zh: 'GET —— 分析端点' },
  'api.post': { en: 'POST — operations', zh: 'POST —— 操作' },
  'api.col.method': { en: 'Method', zh: '方法' },
  'api.col.path': { en: 'Path', zh: '路径' },
  'api.col.params': { en: 'Query params', zh: '查询参数' },
  'api.col.desc': { en: 'Description', zh: '描述' },
  'api.example': { en: 'Example', zh: '示例' },

  // misc
  'copy.done': { en: 'Copied to clipboard', zh: '已复制到剪贴板' },
}

interface LangCtx {
  lang: Lang
  setLang: (l: Lang) => void
  t: (key: keyof typeof dict | string) => string
}

const Ctx = createContext<LangCtx | null>(null)

const STORAGE_KEY = 'ai-apktool-lang'

function detectInitial(): Lang {
  try {
    const saved = localStorage.getItem(STORAGE_KEY) as Lang | null
    if (saved === 'en' || saved === 'zh') return saved
    if (typeof navigator !== 'undefined' && navigator.language?.startsWith('zh'))
      return 'zh'
  } catch {
    /* ignore */
  }
  return 'en'
}

export function LangProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(detectInitial)

  const setLang = useCallback((l: Lang) => {
    setLangState(l)
    try {
      localStorage.setItem(STORAGE_KEY, l)
    } catch {
      /* ignore */
    }
    document.documentElement.lang = l
  }, [])

  const t = useCallback(
    (key: string) => {
      const entry = dict[key]
      if (!entry) return key
      return entry[lang]
    },
    [lang],
  )

  return <Ctx.Provider value={{ lang, setLang, t }}>{children}</Ctx.Provider>
}

export function useI18n(): LangCtx {
  const ctx = useContext(Ctx)
  if (!ctx) throw new Error('useI18n must be used within LangProvider')
  return ctx
}
