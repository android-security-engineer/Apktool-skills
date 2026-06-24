import { Button, message, Tooltip } from 'antd'
import { CopyOutlined, CheckOutlined } from '@ant-design/icons'
import { useState } from 'react'
import { MONO } from '../theme'
import { useI18n } from '../i18n'

interface Props {
  /** code text; multiple lines allowed */
  children: string
  /** optional language tag shown top-left */
  lang?: string
}

export default function CodeBlock({ children, lang }: Props) {
  const { t } = useI18n()
  const [copied, setCopied] = useState(false)
  const [msg, ctx] = message.useMessage()

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(children)
      setCopied(true)
      msg.success(t('copy.done'))
      setTimeout(() => setCopied(false), 1500)
    } catch {
      msg.error('Copy failed')
    }
  }

  return (
    <div
      style={{
        position: 'relative',
        background: '#0d1117',
        borderRadius: 8,
        border: '1px solid #1f2630',
        margin: '12px 0',
      }}
    >
      {ctx}
      {lang && (
        <span
          style={{
            position: 'absolute',
            top: 8,
            left: 14,
            fontSize: 12,
            color: '#7d8590',
            fontFamily: MONO,
            userSelect: 'none',
          }}
        >
          {lang}
        </span>
      )}
      <Tooltip title="Copy">
        <Button
          type="text"
          size="small"
          aria-label="Copy code"
          icon={
            copied ? (
              <CheckOutlined style={{ color: '#3fb950' }} />
            ) : (
              <CopyOutlined style={{ color: '#7d8590' }} />
            )
          }
          onClick={copy}
          style={{ position: 'absolute', top: 6, right: 6 }}
        />
      </Tooltip>
      <pre
        style={{
          margin: 0,
          padding: lang ? '30px 16px 16px' : '16px',
          overflowX: 'auto',
          fontFamily: MONO,
          fontSize: 13.5,
          lineHeight: 1.6,
          color: '#e6edf3',
        }}
      >
        <code>{children}</code>
      </pre>
    </div>
  )
}
