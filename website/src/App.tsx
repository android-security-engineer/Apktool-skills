import { ConfigProvider } from 'antd'
import enUS from 'antd/locale/en_US'
import zhCN from 'antd/locale/zh_CN'
import { HashRouter, Routes, Route } from 'react-router-dom'
import { LangProvider, useI18n } from './i18n'
import { theme } from './theme'
import AppLayout from './layout/AppLayout'
import HomePage from './pages/HomePage'
import GettingStartedPage from './pages/GettingStartedPage'
import CommandsPage from './pages/CommandsPage'
import SkillsPage from './pages/SkillsPage'
import IntegrationPage from './pages/IntegrationPage'
import ApiPage from './pages/ApiPage'

function Shell() {
  const { lang } = useI18n()
  return (
    <ConfigProvider theme={theme} locale={lang === 'zh' ? zhCN : enUS}>
      <HashRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/start" element={<GettingStartedPage />} />
            <Route path="/commands" element={<CommandsPage />} />
            <Route path="/skills" element={<SkillsPage />} />
            <Route path="/integration" element={<IntegrationPage />} />
            <Route path="/api" element={<ApiPage />} />
            <Route path="*" element={<HomePage />} />
          </Route>
        </Routes>
      </HashRouter>
    </ConfigProvider>
  )
}

export default function App() {
  return (
    <LangProvider>
      <Shell />
    </LangProvider>
  )
}
