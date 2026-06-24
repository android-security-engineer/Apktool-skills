import { Typography } from 'antd'
import { useI18n } from '../i18n'
import CommandTable from '../components/CommandTable'

const { Title, Paragraph } = Typography

export default function CommandsPage() {
  const { t } = useI18n()
  return (
    <div>
      <Title level={1}>{t('cmd.title')}</Title>
      <Paragraph type="secondary" style={{ fontSize: 16, maxWidth: 840 }}>
        {t('cmd.intro')}
      </Paragraph>
      <CommandTable />
    </div>
  )
}
