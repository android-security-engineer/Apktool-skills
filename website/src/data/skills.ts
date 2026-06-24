// The 11 Claude Code skills shipped by AI-Apktool.
// Descriptions are sourced from each skills/<name>/SKILL.md and the README table.

export interface Skill {
  name: string
  /** kebab slug used for the /skill invocation */
  slug: string
  category: SkillCategory
  en: { title: string; desc: string; whenToUse: string }
  zh: { title: string; desc: string; whenToUse: string }
  /** example natural-language invocation */
  invoke: string
}

export type SkillCategory =
  | 'triage'
  | 'security'
  | 'code'
  | 'network'
  | 'resources'
  | 'workflow'

export const SKILL_CATEGORY_COLOR: Record<SkillCategory, string> = {
  triage: 'blue',
  security: 'red',
  code: 'purple',
  network: 'cyan',
  resources: 'gold',
  workflow: 'green',
}

export const skills: Skill[] = [
  {
    name: 'quick-analysis',
    slug: 'quick-analysis',
    category: 'triage',
    en: {
      title: 'Quick Analysis',
      desc: 'Fast APK assessment in seconds — package, version, SDK, permissions, components and a risk snapshot.',
      whenToUse: 'First encounter with an APK; you want a 5-second triage before going deeper.',
    },
    zh: {
      title: '快速分析',
      desc: '数秒内完成 APK 快速评估——包名、版本、SDK、权限、组件与风险概览。',
      whenToUse: '首次接触一个 APK，深入分析前先做 5 秒分诊。',
    },
    invoke: '/quick-analysis analyze this APK: /path/to/app.apk',
  },
  {
    name: 'security-audit',
    slug: 'security-audit',
    category: 'security',
    en: {
      title: 'Security Audit',
      desc: 'Comprehensive security audit: dangerous permissions, exported component risk, manifest flags, 0-100 risk score.',
      whenToUse: 'Vulnerability assessment, OWASP MASVS-style review, or compliance checks.',
    },
    zh: {
      title: '安全审计',
      desc: '全面安全审计：危险权限、导出组件风险、Manifest 标志，0-100 风险评分。',
      whenToUse: '漏洞评估、OWASP MASVS 风格审查或合规检查。',
    },
    invoke: '/security-audit run a security audit on app.apk',
  },
  {
    name: 'compare',
    slug: 'compare',
    category: 'workflow',
    en: {
      title: 'Compare',
      desc: 'Diff two APK versions: permission, component, signing and version changes side by side.',
      whenToUse: 'Checking what changed between two builds or releases of an app.',
    },
    zh: {
      title: '版本对比',
      desc: '对比两个 APK 版本：权限、组件、签名与版本变更并排呈现。',
      whenToUse: '检查同一 App 两个构建/发布版本之间的差异。',
    },
    invoke: '/compare compare app_v1.apk and app_v2.apk',
  },
  {
    name: 'reverse',
    slug: 'reverse',
    category: 'workflow',
    en: {
      title: 'Reverse Engineering',
      desc: 'Full reverse-engineering workflow: decode, explore code, modify, rebuild — recon script included.',
      whenToUse: 'Deep analysis, modification, or malware investigation that needs the whole toolchain.',
    },
    zh: {
      title: '逆向工程',
      desc: '完整逆向工程工作流：解码、探索代码、修改、重新打包——附带侦察脚本。',
      whenToUse: '需要全套工具链的深度分析、修改或恶意软件调查。',
    },
    invoke: '/reverse reverse engineer app.apk',
  },
  {
    name: 'reference',
    slug: 'reference',
    category: 'workflow',
    en: {
      title: 'Reference',
      desc: 'CLI command reference hub — exact syntax, options, output fields and HTTP endpoints on demand.',
      whenToUse: 'Looking up the precise syntax or output schema of any command.',
    },
    zh: {
      title: '命令参考',
      desc: 'CLI 命令参考中枢——按需提供精确语法、选项、输出字段与 HTTP 端点。',
      whenToUse: '查询任意命令的精确语法或输出 schema。',
    },
    invoke: '/reference show the usage of the search command',
  },
  {
    name: 'decode-build',
    slug: 'decode-build',
    category: 'workflow',
    en: {
      title: 'Decode & Build',
      desc: 'Decode an APK to smali/resources, rebuild it, and manage framework files.',
      whenToUse: 'Decoding, repacking, or installing/cleaning frameworks.',
    },
    zh: {
      title: '解码与构建',
      desc: '将 APK 解码为 smali/资源、重新打包，并管理框架文件。',
      whenToUse: '解码、重打包，或安装/清理框架。',
    },
    invoke: '/decode-build decode app.apk then rebuild it',
  },
  {
    name: 'dex-deep-dive',
    slug: 'dex-deep-dive',
    category: 'code',
    en: {
      title: 'DEX Deep Dive',
      desc: 'Class/method/field exploration and inheritance tracing across DEX files.',
      whenToUse: 'Tracing a class hierarchy, finding methods/fields, or auditing DEX internals.',
    },
    zh: {
      title: 'DEX 深度分析',
      desc: '跨 DEX 文件的类/方法/字段探索与继承链追踪。',
      whenToUse: '追踪类继承关系、查找方法/字段或审计 DEX 内部。',
    },
    invoke: '/dex-deep-dive trace the class hierarchy in app.apk',
  },
  {
    name: 'network-analysis',
    slug: 'network-analysis',
    category: 'network',
    en: {
      title: 'Network Analysis',
      desc: 'Find endpoints, URLs and cleartext-traffic exposure; review network security config.',
      whenToUse: 'Hunting for hardcoded endpoints, cleartext HTTP, or insecure network config.',
    },
    zh: {
      title: '网络分析',
      desc: '查找端点、URL 与明文流量暴露；审查网络安全配置。',
      whenToUse: '排查硬编码端点、明文 HTTP 或不安全的网络配置。',
    },
    invoke: '/network-analysis find network endpoints in app.apk',
  },
  {
    name: 'malware-hunt',
    slug: 'malware-hunt',
    category: 'security',
    en: {
      title: 'Malware Hunt',
      desc: 'Hunt malware indicators: suspicious permissions, APIs, obfuscation and known patterns.',
      whenToUse: 'Investigating a suspicious APK for malicious behavior.',
    },
    zh: {
      title: '恶意软件狩猎',
      desc: '狩猎恶意指标：可疑权限、API、混淆与已知模式。',
      whenToUse: '调查可疑 APK 是否存在恶意行为。',
    },
    invoke: '/malware-hunt is app.apk malicious?',
  },
  {
    name: 'resource-explorer',
    slug: 'resource-explorer',
    category: 'resources',
    en: {
      title: 'Resource Explorer',
      desc: 'Explore resources, locales, assets and file structure of an APK.',
      whenToUse: 'Inspecting resource tables, supported locales, assets, or the file layout.',
    },
    zh: {
      title: '资源探索',
      desc: '探索 APK 的资源、语言区域、assets 与文件结构。',
      whenToUse: '检查资源表、支持的语言区域、assets 或文件布局。',
    },
    invoke: '/resource-explorer list the resources and locales in app.apk',
  },
  {
    name: 'signing-verify',
    slug: 'signing-verify',
    category: 'security',
    en: {
      title: 'Signing & Verify',
      desc: 'Verify signing certificates: subject/issuer, fingerprints, v1/v2/v3/v4 schemes, signer comparison.',
      whenToUse: 'Checking who signed an app, whether the signature is trusted, or comparing certs.',
    },
    zh: {
      title: '签名验证',
      desc: '验证签名证书：主题/颁发者、指纹、v1/v2/v3/v4 方案、签名者对比。',
      whenToUse: '确认谁签名了 App、签名是否可信，或对比证书。',
    },
    invoke: '/signing-verify who signed app.apk?',
  },
]
