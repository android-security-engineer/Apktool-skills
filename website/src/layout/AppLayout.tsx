import { Layout, Menu, Segmented, Space, Typography, Grid, Drawer, Button } from 'antd'
import { GithubOutlined, MenuOutlined } from '@ant-design/icons'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useI18n } from '../i18n'
import { BRAND, MONO } from '../theme'

const { Header, Content, Footer } = Layout
const { useBreakpoint } = Grid

const GITHUB_URL = 'https://github.com/android-security-engineer/Apktool-skills'

const NAV: { key: string; path: string; labelKey: string }[] = [
  { key: 'home', path: '/', labelKey: 'nav.home' },
  { key: 'start', path: '/start', labelKey: 'nav.start' },
  { key: 'commands', path: '/commands', labelKey: 'nav.commands' },
  { key: 'skills', path: '/skills', labelKey: 'nav.skills' },
  { key: 'integration', path: '/integration', labelKey: 'nav.integration' },
  { key: 'api', path: '/api', labelKey: 'nav.api' },
]

function activeKey(pathname: string): string {
  if (pathname === '/' || pathname === '') return 'home'
  const seg = pathname.split('/')[1]
  return NAV.find((n) => n.path === `/${seg}`)?.key ?? 'home'
}

export default function AppLayout() {
  const { lang, setLang, t } = useI18n()
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const screens = useBreakpoint()
  const [drawer, setDrawer] = useState(false)
  const isMobile = !screens.md

  const menuItems = NAV.map((n) => ({
    key: n.key,
    label: <Link to={n.path}>{t(n.labelKey)}</Link>,
  }))

  const langToggle = (
    <Segmented
      size="small"
      value={lang}
      onChange={(v) => setLang(v as 'en' | 'zh')}
      options={[
        { label: 'EN', value: 'en' },
        { label: '中文', value: 'zh' },
      ]}
    />
  )

  const logo = (
    <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <span
        style={{
          width: 30,
          height: 30,
          borderRadius: 8,
          background: BRAND,
          color: '#fff',
          display: 'grid',
          placeItems: 'center',
          fontFamily: MONO,
          fontWeight: 700,
        }}
      >
        A
      </span>
      <Typography.Text strong style={{ fontSize: 17, whiteSpace: 'nowrap' }}>
        AI-Apktool
      </Typography.Text>
    </Link>
  )

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 10,
          display: 'flex',
          alignItems: 'center',
          gap: 24,
          padding: '0 20px',
          borderBottom: '1px solid #eaecef',
          boxShadow: '0 1px 3px rgba(0,0,0,0.03)',
        }}
      >
        {logo}
        {!isMobile && (
          <Menu
            mode="horizontal"
            selectedKeys={[activeKey(pathname)]}
            items={menuItems}
            style={{ flex: 1, borderBottom: 'none', minWidth: 0 }}
          />
        )}
        <div style={{ flex: isMobile ? 1 : 0 }} />
        <Space size={12}>
          {!isMobile && langToggle}
          <a href={GITHUB_URL} target="_blank" rel="noreferrer" aria-label="GitHub">
            <GithubOutlined style={{ fontSize: 20, color: '#24292f' }} />
          </a>
          {isMobile && (
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={() => setDrawer(true)}
              aria-label="Menu"
            />
          )}
        </Space>
      </Header>

      <Drawer
        open={drawer}
        onClose={() => setDrawer(false)}
        title="AI-Apktool"
        width={260}
      >
        <Menu
          mode="vertical"
          selectedKeys={[activeKey(pathname)]}
          items={NAV.map((n) => ({
            key: n.key,
            label: t(n.labelKey),
            onClick: () => {
              navigate(n.path)
              setDrawer(false)
            },
          }))}
          style={{ borderInlineEnd: 'none' }}
        />
        <div style={{ marginTop: 20 }}>{langToggle}</div>
      </Drawer>

      <Content style={{ padding: isMobile ? '24px 16px' : '40px 24px' }}>
        <div style={{ maxWidth: 1080, margin: '0 auto' }}>
          <Outlet />
        </div>
      </Content>

      <Footer style={{ textAlign: 'center', background: '#fff', borderTop: '1px solid #eaecef' }}>
        <Typography.Paragraph style={{ marginBottom: 4, color: '#5b6573' }}>
          {t('footer.tagline')}
        </Typography.Paragraph>
        <Typography.Text type="secondary" style={{ fontSize: 13 }}>
          <a href={GITHUB_URL} target="_blank" rel="noreferrer">
            GitHub
          </a>
          {' · '}
          {t('footer.license')}
        </Typography.Text>
      </Footer>
    </Layout>
  )
}
