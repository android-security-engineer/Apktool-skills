import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// GitHub Pages serves this project site under /Apktool-skills/.
// Allow override via BASE_PATH env (e.g. '/' for a custom domain or local preview).
const base = process.env.BASE_PATH ?? '/Apktool-skills/'

export default defineConfig({
  base,
  plugins: [react()],
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1200,
  },
})
