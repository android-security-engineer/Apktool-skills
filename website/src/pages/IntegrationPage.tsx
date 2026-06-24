import { Card, Tag, Typography } from 'antd'
import { useI18n, type Lang } from '../i18n'
import CodeBlock from '../components/CodeBlock'

const { Title, Paragraph } = Typography

interface Method {
  tag: string
  title: { en: string; zh: string }
  body: { en: string; zh: string }
  code: string
}

const METHODS: Method[] = [
  {
    tag: '1',
    title: {
      en: 'Claude Code Skills (native, auto-invoked)',
      zh: 'Claude Code Skills（原生，自动调用）',
    },
    body: {
      en: 'The richest path. Install once and Claude Code discovers the 11 skills and invokes the right one automatically based on the task.',
      zh: '能力最完整的方式。一次安装后，Claude Code 自动发现 11 个 skill，并按任务自动调用合适的那个。',
    },
    code: `claude config add marketplace ai-apktool https://github.com/android-security-engineer/Apktool-skills.git
claude plugin install ai-apktool@ai-apktool
# then just ask: "analyze this APK" / "is app.apk safe?"`,
  },
  {
    tag: '2',
    title: {
      en: 'Unified CLI → JSON (any agent, via shell)',
      zh: '统一 CLI → JSON（任意智能体，经 shell）',
    },
    body: {
      en: 'Every analysis command prints JSON to stdout, so any agent that can run a shell command consumes the output directly — no log scraping.',
      zh: '每条分析命令都向 stdout 输出 JSON，任何能执行 shell 的智能体都能直接消费——无需解析日志。',
    },
    code: `apktool analyze app.apk        # one-shot full analysis as JSON
apktool security app.apk | jq '.riskScore'`,
  },
  {
    tag: '3',
    title: {
      en: 'Batch scripting — run / pipe (single parse)',
      zh: '批处理脚本 — run / pipe（一次解析）',
    },
    body: {
      en: 'Hand the agent a JSON script; it executes all commands against one shared parse with per-command error isolation — far cheaper than N separate invocations.',
      zh: '把一份 JSON 脚本交给智能体，它对同一次解析执行全部命令，并带逐命令错误隔离——远比 N 次独立调用省成本。',
    },
    code: `echo '{"apk":"app.apk","commands":["info","security","signing","api-surface"]}' | apktool pipe
# ready-made audit/hunt/recon scripts ship in skills/*/scripts/*.json`,
  },
  {
    tag: '4',
    title: {
      en: 'HTTP REST API — serve (remote agents)',
      zh: 'HTTP REST API — serve（远程智能体）',
    },
    body: {
      en: "For agents that aren't co-located with the binary, expose the whole capability set over REST.",
      zh: '对于与二进制不在同一机器上的智能体，可通过 REST 暴露全部能力集。',
    },
    code: `apktool serve -p 8080
curl 'http://localhost:8080/api/v1/security?apk=/path/to/app.apk' | jq '.riskScore'`,
  },
  {
    tag: '5',
    title: { en: 'Prompt & context generation — ai', zh: '提示词与上下文生成 — ai' },
    body: {
      en: 'Let the tool draft the prompt or hand back structured facts for your own model to reason over.',
      zh: '让工具替你起草提示词，或直接返回结构化事实，供你自己的模型推理。',
    },
    code: `apktool ai app.apk -a security-review   # an LLM-ready prompt
apktool ai app.apk -a context           # structured AiContext JSON (facts, not prose)`,
  },
  {
    tag: '6',
    title: {
      en: 'Capability discovery — help --format=json',
      zh: '能力发现 — help --format=json',
    },
    body: {
      en: 'Agents can introspect the entire command surface (names, params, output schema, categories) at runtime to plan tool calls dynamically.',
      zh: '智能体可在运行时自省整个命令面（名称、参数、输出 schema、分类），从而动态规划工具调用。',
    },
    code: `apktool help --format=json | jq '.commands | length'   # 51`,
  },
]

export default function IntegrationPage() {
  const { lang, t } = useI18n()
  const l = lang as Lang
  return (
    <div>
      <Title level={1}>{t('intg.title')}</Title>
      <Paragraph type="secondary" style={{ fontSize: 16, maxWidth: 840 }}>
        {t('intg.intro')}
      </Paragraph>
      {METHODS.map((m) => (
        <Card key={m.tag} style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
            <Tag color="#1F6FEB" style={{ margin: 0 }}>
              {m.tag}
            </Tag>
            <Title level={4} style={{ margin: 0 }}>
              {m.title[l]}
            </Title>
          </div>
          <Paragraph style={{ color: '#444', marginBottom: 4 }}>{m.body[l]}</Paragraph>
          <CodeBlock lang="bash">{m.code}</CodeBlock>
        </Card>
      ))}
    </div>
  )
}
