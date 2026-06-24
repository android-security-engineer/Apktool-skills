import { Button, Col, Row, Space, Table, Tag, Typography } from 'antd'
import {
  ArrowRightOutlined,
  GithubOutlined,
  AppstoreOutlined,
} from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { useI18n } from '../i18n'
import { BRAND } from '../theme'
import StatCard from '../components/StatCard'
import { COMMANDS, CATEGORY_ORDER } from '../data/commands'
import { skills } from '../data/skills'

const { Title, Paragraph, Text } = Typography

const GITHUB_URL = 'https://github.com/android-security-engineer/Apktool-skills'

function asset(name: string) {
  return `${import.meta.env.BASE_URL}${name}`
}

export default function HomePage() {
  const { lang, t } = useI18n()

  const comparisonRows = [
    {
      key: 'user',
      cap: { en: 'Primary user', zh: '主要使用者' },
      up: { en: 'Human at a terminal', zh: '终端前的人类' },
      fork: { en: 'AI agent / automation', zh: 'AI 智能体 / 自动化' },
    },
    {
      key: 'decode',
      cap: { en: 'Decode / build / frameworks', zh: '解码 / 构建 / 框架' },
      up: { en: '✅', zh: '✅' },
      fork: { en: '✅ inherited', zh: '✅ 继承' },
    },
    {
      key: 'output',
      cap: { en: 'Output format', zh: '输出格式' },
      up: { en: 'Human-readable logs', zh: '人类可读日志' },
      fork: { en: 'Structured JSON everywhere', zh: '处处结构化 JSON' },
    },
    {
      key: 'analysis',
      cap: { en: 'Static analysis commands', zh: '静态分析命令' },
      up: { en: '—', zh: '—' },
      fork: { en: '38', zh: '38 个' },
    },
    {
      key: 'batch',
      cap: { en: 'Batch engine (run / pipe)', zh: '批处理引擎 (run / pipe)' },
      up: { en: '—', zh: '—' },
      fork: { en: '✅', zh: '✅' },
    },
    {
      key: 'http',
      cap: { en: 'HTTP REST API', zh: 'HTTP REST API' },
      up: { en: '—', zh: '—' },
      fork: { en: '✅ serve', zh: '✅ serve' },
    },
    {
      key: 'skills',
      cap: { en: 'Claude Code skills', zh: 'Claude Code Skills' },
      up: { en: '—', zh: '—' },
      fork: { en: '✅ 11', zh: '✅ 11 个' },
    },
  ]

  return (
    <div>
      {/* Hero */}
      <section
        style={{
          background: 'linear-gradient(135deg, #0d1117 0%, #161b3d 100%)',
          borderRadius: 16,
          padding: '48px 36px',
          color: '#fff',
          marginBottom: 32,
        }}
      >
        <Tag color={BRAND} style={{ marginBottom: 16 }}>
          v3.0.3 · Apktool fork
        </Tag>
        <Title style={{ color: '#fff', marginTop: 0, fontSize: 40, marginBottom: 8 }}>
          AI-Apktool
        </Title>
        <Title level={3} style={{ color: '#c9d4e3', marginTop: 0, fontWeight: 500 }}>
          {t('home.tagline')}
        </Title>
        <Paragraph style={{ color: '#aab6c8', maxWidth: 760, fontSize: 16 }}>
          {t('home.subtitle')}
        </Paragraph>
        <Space wrap size={12} style={{ marginTop: 8 }}>
          <Link to="/start">
            <Button type="primary" size="large" icon={<ArrowRightOutlined />}>
              {t('home.cta.start')}
            </Button>
          </Link>
          <Link to="/commands">
            <Button size="large" ghost icon={<AppstoreOutlined />}>
              {t('home.cta.commands')}
            </Button>
          </Link>
          <Button
            size="large"
            ghost
            icon={<GithubOutlined />}
            href={GITHUB_URL}
            target="_blank"
          >
            {t('home.cta.github')}
          </Button>
        </Space>
      </section>

      {/* Stats */}
      <Title level={2}>{t('home.stats.title')}</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 40 }}>
        <Col xs={12} md={6}>
          <StatCard value={COMMANDS.length} label={t('home.stat.commands')} />
        </Col>
        <Col xs={12} md={6}>
          <StatCard value={skills.length} label={t('home.stat.skills')} />
        </Col>
        <Col xs={12} md={6}>
          <StatCard value={CATEGORY_ORDER.length} label={t('home.stat.categories')} />
        </Col>
        <Col xs={12} md={6}>
          <StatCard value="100%" label={t('home.stat.json')} />
        </Col>
      </Row>

      {/* Feature tree */}
      <Title level={2}>{t('home.tree.title')}</Title>
      <Paragraph type="secondary">{t('home.tree.sub')}</Paragraph>
      <div style={{ textAlign: 'center', marginBottom: 40 }}>
        <img
          src={asset(lang === 'zh' ? 'feature-tree.zh.png' : 'feature-tree.png')}
          alt="AI-Apktool feature tree"
          style={{
            maxWidth: '100%',
            borderRadius: 12,
            border: '1px solid #e3e8ef',
            boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
          }}
        />
      </div>

      {/* Built on Apktool */}
      <Title level={2}>{t('home.built.title')}</Title>
      <Paragraph type="secondary" style={{ maxWidth: 820 }}>
        {t('home.built.body')}
      </Paragraph>
      <Table
        size="middle"
        pagination={false}
        style={{ marginBottom: 24 }}
        scroll={{ x: 560 }}
        rowKey="key"
        columns={[
          {
            title: '',
            dataIndex: 'cap',
            render: (v: { en: string; zh: string }) => <Text strong>{v[lang]}</Text>,
          },
          {
            title: t('home.built.upstream'),
            dataIndex: 'up',
            render: (v: { en: string; zh: string }) => (
              <Text type="secondary">{v[lang]}</Text>
            ),
          },
          {
            title: t('home.built.fork'),
            dataIndex: 'fork',
            render: (v: { en: string; zh: string }) => (
              <Text style={{ color: BRAND }}>{v[lang]}</Text>
            ),
          },
        ]}
        dataSource={comparisonRows}
      />
    </div>
  )
}
