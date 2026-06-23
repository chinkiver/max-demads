import { defineStore } from 'pinia'
import { ref } from 'vue'

const USER_INFO_KEY = 'userInfo'

const getStoredUserInfo = () => {
  try {
    const stored = localStorage.getItem(USER_INFO_KEY)
    return stored ? JSON.parse(stored) : null
  } catch (e) {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(getStoredUserInfo())

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
    } else {
      localStorage.removeItem(USER_INFO_KEY)
    }
  }

  const clearToken = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem(USER_INFO_KEY)
  }

  const isLoggedIn = () => !!token.value

  return { token, userInfo, setToken, setUserInfo, clearToken, isLoggedIn }
})
