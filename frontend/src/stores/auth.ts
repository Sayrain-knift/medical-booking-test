import { defineStore } from 'pinia'
import type { User } from '../types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    token: localStorage.getItem('token') || null,
    isLoading: false,
    error: null as string | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user
  },

  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUser(user: User) {
      this.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    clearError() {
      this.error = null
    },

    initAuth() {
      const userJson = localStorage.getItem('user')
      if (userJson) {
        this.user = JSON.parse(userJson)
      }
    }
  }
})
