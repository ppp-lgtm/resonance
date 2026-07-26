/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        light: {
          bg: '#FFFFFF',
          card: '#F8FAFC',
          text: '#0F172A',
          accent: '#7C3AED',
        },
        dark: {
          bg: '#0B1121',
          card: '#1E293B',
          text: '#F1F5F9',
          accent: '#A78BFA',
        },
      },
      fontFamily: {
        heading: ['Inter', 'Satoshi', 'system-ui', 'sans-serif'],
        body: ['system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
}
