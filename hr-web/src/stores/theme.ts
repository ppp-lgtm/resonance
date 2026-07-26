import { defineStore } from 'pinia'

interface ThemeState {
  isDark: boolean
}

export const useThemeStore = defineStore('theme', {
  state: (): ThemeState => ({
    isDark: localStorage.getItem('theme') === 'dark',
  }),
  actions: {
    toggle() {
      this.isDark = !this.isDark
      this.applyTheme()
    },
    set(dark: boolean) {
      this.isDark = dark
      this.applyTheme()
    },
    applyTheme() {
      if (this.isDark) {
        document.documentElement.classList.add('dark')
        localStorage.setItem('theme', 'dark')
      } else {
        document.documentElement.classList.remove('dark')
        localStorage.setItem('theme', 'light')
      }
    },
    init() {
      this.applyTheme()
    },
  },
})
