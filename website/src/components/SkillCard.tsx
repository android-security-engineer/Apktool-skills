import { Card, Tag, Typography } from 'antd'
import { type Skill, SKILL_CATEGORY_COLOR } from '../data/skills'
import { useI18n } from '../i18n'
import { MONO } from '../theme'

const { Paragraph, Text } = Typography

export default function SkillCard({ skill }: { skill: Skill }) {
  const { lang, t } = useI18n()
  const loc = skill[lang]

  return (
    <Card
      style={{ height: '100%' }}
      styles={{ body: { display: 'flex', flexDirection: 'column', height: '100%' } }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <Text strong style={{ fontSize: 16 }}>
          {loc.title}
        </Text>
        <Tag color={SKILL_CATEGORY_COLOR[skill.category]}>{skill.category}</Tag>
      </div>
      <code
        style={{
          fontFamily: MONO,
          fontSize: 12.5,
          color: '#1F6FEB',
          background: '#eef3fe',
          padding: '2px 8px',
          borderRadius: 6,
          alignSelf: 'flex-start',
          marginBottom: 10,
        }}
      >
        /{skill.slug}
      </code>
      <Paragraph style={{ marginBottom: 10, color: '#444' }}>{loc.desc}</Paragraph>
      <div style={{ marginTop: 'auto' }}>
        <Text type="secondary" style={{ fontSize: 12.5 }}>
          {t('skills.when')}：
        </Text>
        <Paragraph type="secondary" style={{ fontSize: 12.5, marginBottom: 0 }}>
          {loc.whenToUse}
        </Paragraph>
      </div>
    </Card>
  )
}
