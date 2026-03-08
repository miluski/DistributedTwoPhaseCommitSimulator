/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        online: '#22c55e',
        crashed: '#ef4444',
        degraded: '#f59e0b',
        uncertain: '#8b5cf6',
      },
    },
  },
  plugins: [],
};
