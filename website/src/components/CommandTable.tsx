import { useMemo, useState } from 'react'
import { Input, Segmented, Table, Tag, Typography, Space } from 'antd'
import { SearchOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import {
  COMMANDS,
  CATEGORY_ORDER,
  CATEGORY_COLOR,
  CATEGORY_LABEL,
  countByCategory,
  type Command,
  type CommandCategory,
} from '../data/commands'
import { useI18n } from '../i18n'
import { MONO } from '../theme'
import CodeBlock from './CodeBlock'

const { Text, Paragraph } = Typography

export default function CommandTable() {
  const { lang, t } = useI18n()
  const [query, setQuery] = useState('')
  const [cat, setCat] = useState<CommandCategory | 'all'>('all')
  const counts = useMemo(countByCategory, [])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    return COMMANDS.filter((c) => {
      if (cat !== 'all' && c.category !== cat) return false
      if (!q) return true
      return (
        c.name.toLowerCase().includes(q) ||
        (c.shortName ?? '').toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q) ||
        c.outputFormat.toLowerCase().includes(q)
      )
    })
  }, [query, cat])

  const segOptions = [
    { label: `${t('cmd.all')} (${COMMANDS.length})`, value: 'all' },
    ...CATEGORY_ORDER.map((c) => ({
      label: `${CATEGORY_LABEL[c][lang]} (${counts[c]})`,
      value: c,
    })),
  ]

  const columns: ColumnsType<Command> = [
    {
      title: t('cmd.col.name'),
      dataIndex: 'name',
      key: 'name',
      width: 220,
      render: (name: string, row) => (
        <Space direction="vertical" size={2}>
          <Text strong style={{ fontFamily: MONO, color: '#1F6FEB' }}>
            {name}
          </Text>
          {row.shortName && (
            <Text type="secondary" style={{ fontSize: 12, fontFamily: MONO }}>
              {t('cmd.alias')}: {row.shortName}
            </Text>
          )}
        </Space>
      ),
      sorter: (a, b) => a.name.localeCompare(b.name),
    },
    {
      title: t('cmd.col.category'),
      dataIndex: 'category',
      key: 'category',
      width: 130,
      render: (c: CommandCategory) => (
        <Tag color={CATEGORY_COLOR[c]}>{CATEGORY_LABEL[c][lang]}</Tag>
      ),
    },
    {
      title: t('cmd.col.desc'),
      dataIndex: 'description',
      key: 'description',
      render: (d: string) => <span style={{ color: '#444' }}>{d}</span>,
    },
  ]

  return (
    <div>
      <Space
        style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}
        wrap
      >
        <Input
          allowClear
          prefix={<SearchOutlined />}
          placeholder={t('cmd.search')}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{ width: 280 }}
        />
        <Text type="secondary">
          {filtered.length} {t('cmd.count')}
        </Text>
      </Space>
      <div style={{ overflowX: 'auto', marginBottom: 16 }}>
        <Segmented
          options={segOptions}
          value={cat}
          onChange={(v) => setCat(v as CommandCategory | 'all')}
        />
      </div>
      <Table<Command>
        rowKey="name"
        columns={columns}
        dataSource={filtered}
        pagination={false}
        size="middle"
        scroll={{ x: 720 }}
        expandable={{
          expandedRowRender: (row) => (
            <div style={{ padding: '4px 8px 8px' }}>
              <Text strong>{t('cmd.usage')}</Text>
              <CodeBlock lang="bash">{row.usage}</CodeBlock>
              {row.examples.length > 0 && (
                <>
                  <Text strong>{t('cmd.examples')}</Text>
                  <CodeBlock lang="bash">{row.examples.join('\n')}</CodeBlock>
                </>
              )}
              <Text strong>{t('cmd.output')}</Text>
              <Paragraph
                style={{
                  fontFamily: MONO,
                  fontSize: 12.5,
                  background: '#f6f8fa',
                  padding: '10px 12px',
                  borderRadius: 6,
                  marginTop: 8,
                  whiteSpace: 'pre-wrap',
                }}
              >
                {row.outputFormat}
              </Paragraph>
            </div>
          ),
        }}
      />
    </div>
  )
}
