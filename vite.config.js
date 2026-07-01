import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Relative base ('./') makes the build work when deployed to
// https://<user>.github.io/<repo>/ (a sub-path) without extra config.
export default defineConfig({
  plugins: [react()],
  base: './',
})
