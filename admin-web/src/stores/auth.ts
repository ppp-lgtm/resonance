import { defineStore } from 'pinia'

interface AuthState {
  token: string | null
  username: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('admin_token') || null,
    username: localStorage.getItem('admin_username') || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    login(token: string, username: string) {
      this.token = token
      this.username = username
      localStorage.setItem('admin_token', token)
      localStorage.setItem('admin_username', username)
    },
    logout() {
      this.token = null
      this.username = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
    },
  },
})
