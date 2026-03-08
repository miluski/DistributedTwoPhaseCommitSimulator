import react from '@vitejs/plugin-react';
import { dirname, resolve } from 'path';
import { fileURLToPath } from 'url';
import { defineConfig } from 'vitest/config';

const __dirname = dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@features': resolve(__dirname, './src/features'),
      '@common': resolve(__dirname, './src/common'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
      '/ws': {
        target: 'https://localhost:8443',
        ws: true,
        changeOrigin: true,
        secure: false,
      },
    },
  },
  test: {
    root: __dirname,
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      exclude: [
        'src/main.tsx',
        'src/types/**',
        'src/common/types/**',
        'src/features/**/types/**',
        'src/**/index.ts',
        'src/common/index.ts',
        'tailwind.config.js',
        'postcss.config.js',
        'vite.config.ts',
      ],
      thresholds: {
        lines: 90,
        branches: 90,
        functions: 90,
        statements: 90,
      },
    },
  },
});
