import { Table, Tag, Typography } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { useI18n } from '../i18n'
import { getEndpoints, postEndpoints, type Endpoint } from '../data/endpoints'
import CodeBlock from '../components/CodeBlock'
import { MONO } from '../theme'

const { Title, Paragraph, Text } = Typography

export default function ApiPage() {
  const { t } = useI18n()
  const gets = getEndpoints()

  const columns: ColumnsType<Endpoint> = [
    {
      title: t('api.col.method'),
      dataIndex: 'method',
      width: 90,
      render: (m: string) => (
        <Tag color={m === 'GET' ? 'green' : 'volcano'}>{m}</Tag>
      ),
    },
    {
      title: t('api.col.path'),
      dataIndex: 'path',
      width: 240,
      render: (p: string) => (
        <Text style={{ fontFamily: MONO, fontSize: 13 }}>{p}</Text>
      ),
    },
    {
      title: t('api.col.params'),
      dataIndex: 'extraParams',
      width: 240,
      render: (p?: string) => (
        <Text type="secondary" style={{ fontFamily: MONO, fontSize: 12.5 }}>
          {p ? `?${p}` : '?apk=<path>'}
        </Text>
      ),
    },
    {
      title: t('api.col.desc'),
      dataIndex: 'description',
      render: (d: string) => <span style={{ color: '#444' }}>{d}</span>,
    },
  ]

  return (
    <div>
      <Title level={1}>{t('api.title')}</Title>
      <Paragraph type="secondary" style={{ fontSize: 16, maxWidth: 840 }}>
        {t('api.intro')}
      </Paragraph>

      <Title level={4}>{t('api.example')}</Title>
      <CodeBlock lang="bash">{`apktool serve -p 8080
curl 'http://localhost:8080/api/v1/info?apk=/path/to/app.apk'
curl 'http://localhost:8080/api/v1/security?apk=/path/to/app.apk' | jq '.riskScore'`}</CodeBlock>

      <Title level={3} style={{ marginTop: 28 }}>
        {t('api.get')}{' '}
        <Tag color="green">{gets.length}</Tag>
      </Title>
      <Table<Endpoint>
        rowKey="path"
        size="small"
        columns={columns}
        dataSource={gets}
        pagination={false}
        scroll={{ x: 760 }}
      />

      <Title level={3} style={{ marginTop: 32 }}>
        {t('api.post')} <Tag color="volcano">{postEndpoints.length}</Tag>
      </Title>
      <Table<Endpoint>
        rowKey="path"
        size="small"
        columns={columns}
        dataSource={postEndpoints}
        pagination={false}
        scroll={{ x: 760 }}
      />
    </div>
  )
}
