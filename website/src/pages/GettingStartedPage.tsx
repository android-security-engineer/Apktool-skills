import { Typography, Steps, List } from 'antd'
import { useI18n } from '../i18n'
import CodeBlock from '../components/CodeBlock'

const { Title, Paragraph } = Typography

export default function GettingStartedPage() {
  const { t } = useI18n()

  return (
    <div>
      <Title level={1}>{t('start.title')}</Title>
      <Paragraph type="secondary" style={{ fontSize: 16 }}>
        {t('start.intro')}
      </Paragraph>

      <Title level={3}>{t('start.prereq')}</Title>
      <List
        size="small"
        dataSource={[t('start.prereq.claude'), t('start.prereq.jdk')]}
        renderItem={(item) => <List.Item>• {item}</List.Item>}
        style={{ marginBottom: 24, maxWidth: 480 }}
      />

      <Steps
        direction="vertical"
        current={-1}
        items={[
          {
            title: t('start.build'),
            description: (
              <CodeBlock lang="bash">{`git clone https://github.com/android-security-engineer/Apktool-skills.git
cd Apktool-skills
./gradlew build shadowJar
# the unified wrapper is ./apktool — add it to your PATH if you like`}</CodeBlock>
            ),
          },
          {
            title: t('start.plugin'),
            description: (
              <CodeBlock lang="bash">{`claude config add marketplace ai-apktool https://github.com/android-security-engineer/Apktool-skills.git
claude plugin install ai-apktool@ai-apktool`}</CodeBlock>
            ),
          },
          {
            title: t('start.manual'),
            description: (
              <CodeBlock lang="bash">{`git clone https://github.com/android-security-engineer/Apktool-skills.git \\
  ~/.claude/skills/ai-apktool`}</CodeBlock>
            ),
          },
          {
            title: t('start.verify'),
            description: (
              <CodeBlock lang="bash">{`claude skill list
#   ai-apktool:quick-analysis
#   ai-apktool:security-audit
#   ai-apktool:compare
#   … 11 skills total`}</CodeBlock>
            ),
          },
          {
            title: t('start.first'),
            description: (
              <CodeBlock lang="bash">{`apktool analyze app.apk            # one-shot full analysis as JSON
apktool security app.apk | jq '.riskScore'
apktool diff app_v1.apk app_v2.apk
apktool help --format=json         # machine-readable catalog`}</CodeBlock>
            ),
          },
        ]}
      />
    </div>
  )
}
