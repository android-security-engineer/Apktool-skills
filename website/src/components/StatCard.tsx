import { Card } from 'antd'
import type { ReactNode } from 'react'
import { BRAND } from '../theme'

interface Props {
  value: ReactNode
  label: string
  icon?: ReactNode
}

export default function StatCard({ value, label, icon }: Props) {
  return (
    <Card variant="borderless" style={{ textAlign: 'center', height: '100%' }}>
      {icon && (
        <div style={{ fontSize: 28, color: BRAND, marginBottom: 4 }}>{icon}</div>
      )}
      <div style={{ fontSize: 34, fontWeight: 700, color: BRAND, lineHeight: 1.1 }}>
        {value}
      </div>
      <div style={{ color: '#5b6573', marginTop: 6, fontSize: 14 }}>{label}</div>
    </Card>
  )
}
