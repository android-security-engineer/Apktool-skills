import { Col, Row, Typography } from 'antd'
import { useI18n } from '../i18n'
import { skills } from '../data/skills'
import SkillCard from '../components/SkillCard'

const { Title, Paragraph } = Typography

export default function SkillsPage() {
  const { t } = useI18n()
  return (
    <div>
      <Title level={1}>{t('skills.title')}</Title>
      <Paragraph type="secondary" style={{ fontSize: 16, maxWidth: 840 }}>
        {t('skills.intro')}
      </Paragraph>
      <Row gutter={[16, 16]}>
        {skills.map((s) => (
          <Col xs={24} sm={12} lg={8} key={s.name}>
            <SkillCard skill={s} />
          </Col>
        ))}
      </Row>
    </div>
  )
}
