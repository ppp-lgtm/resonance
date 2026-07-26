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
          bg: '#F4F1EC',
          card: '#FBF9F6',
          surface: '#EDE9E2',
          text: '#1C1B1A',
          accent: '#B23A2E',
          accent2: '#2F4538',
          muted: '#6B6560',
          border: '#E2DED7',
          rule: '#D4CFC6',
        },
        dark: {
          bg: '#141312',
          card: '#1E1C1A',
          surface: '#252320',
          text: '#E7E1D8',
          accent: '#D4A843',
          accent2: '#7AA68C',
          muted: '#8A8278',
          border: '#2B2825',
          rule: '#34312C',
        },
      },
      fontFamily: {
        heading: ['"DM Serif Display"', '"Source Serif Pro"', 'Georgia', 'serif'],
        display: ['"Lora"', 'Georgia', 'serif'],
        body: ['"DM Sans"', '"Helvetica Neue"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', '"IBM Plex Mono"', 'monospace'],
      },
      fontSize: {
        'display': ['5.5rem', { lineHeight: '0.95', letterSpacing: '-0.04em' }],
        'display-sm': ['3.5rem', { lineHeight: '1', letterSpacing: '-0.03em' }],
        'heading-xl': ['3rem', { lineHeight: '1.05', letterSpacing: '-0.02em' }],
        'heading-lg': ['2.25rem', { lineHeight: '1.15', letterSpacing: '-0.015em' }],
        'heading-md': ['1.6rem', { lineHeight: '1.25' }],
        'eyebrow': ['0.7rem', { lineHeight: '1', letterSpacing: '0.32em' }],
      },
      letterSpacing: {
        'widest-xl': '0.35em',
      },
      backgroundImage: {
        'paper-grain': "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='1.1' numOctaves='3' stitchTiles='stitch'/%3E%3CfeColorMatrix type='matrix' values='0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.028 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E\")",
        'paper-grain-dark': "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='1.1' numOctaves='3' stitchTiles='stitch'/%3E%3CfeColorMatrix type='matrix' values='0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.035 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E\")",
      },
      animation: {
        'fade-in-up': 'fadeInUp 0.9s cubic-bezier(0.2, 0.7, 0.2, 1) forwards',
        'fade-in': 'fadeIn 0.8s cubic-bezier(0.2, 0.7, 0.2, 1) forwards',
        'slide-in-right': 'slideInRight 0.8s cubic-bezier(0.2, 0.7, 0.2, 1) forwards',
        'slide-in-left': 'slideInLeft 0.8s cubic-bezier(0.2, 0.7, 0.2, 1) forwards',
        'underline-grow': 'underlineGrow 0.5s cubic-bezier(0.2, 0.7, 0.2, 1) forwards',
        'blink-cursor': 'blinkCursor 1s steps(1) infinite',
        'tick-scroll': 'tickScroll 40s linear infinite',
      },
      keyframes: {
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(28px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideInRight: {
          '0%': { opacity: '0', transform: 'translateX(40px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        slideInLeft: {
          '0%': { opacity: '0', transform: 'translateX(-40px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        underlineGrow: {
          '0%': { width: '0%' },
          '100%': { width: '100%' },
        },
        blinkCursor: {
          '0%, 50%': { opacity: '1' },
          '51%, 100%': { opacity: '0' },
        },
        tickScroll: {
          '0%': { transform: 'translateX(0)' },
          '100%': { transform: 'translateX(-50%)' },
        },
      },
      borderRadius: {
        'none': '0',
      },
      spacing: {
        '128': '32rem',
      },
      boxShadow: {
        'paper': '0 1px 0 rgba(0,0,0,0.04), 0 20px 40px -24px rgba(0,0,0,0.12)',
        'paper-sm': '0 1px 0 rgba(0,0,0,0.04), 0 12px 24px -18px rgba(0,0,0,0.15)',
      },
    },
  },
  plugins: [],
}
