import type { ThemeConfig } from 'antd'

export const BRAND = '#1F6FEB'

export const theme: ThemeConfig = {
  token: {
    colorPrimary: BRAND,
    colorLink: BRAND,
    borderRadius: 8,
    fontSize: 15,
    fontFamily:
      "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif",
  },
  components: {
    Layout: {
      headerBg: '#ffffff',
      headerHeight: 60,
      bodyBg: '#f5f7fa',
    },
    Menu: {
      horizontalItemSelectedColor: BRAND,
    },
    Table: {
      headerBg: '#f0f3f8',
    },
  },
}

// Monospace stack used by code blocks and inline command names.
export const MONO =
  "'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, 'Courier New', monospace"
