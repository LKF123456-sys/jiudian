import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}')
  }),
  getters: {
    role: (state) => state.user?.role || '',
    isAdmin: (state) => state.user?.role === 'admin',
    isManager: (state) => state.user?.role === 'admin' || state.user?.role === 'manager'
  },
  actions: {
    setAuth(token, user) {
      this.token = token
      this.user = user || {}
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user || {}))
    },
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    clearAuth() {
      this.token = ''
      this.user = {}
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
