import { ref, computed } from 'vue'
import { api } from '../services/api'
import type { User } from '../types'

const TOKEN_KEY = 'parkmanshift_token'
const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
const userProfile = ref<User | null>(null)

function decodeToken(t: string | null) {
  if (!t) return null
  try {
    const base64Url = t.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64))
  } catch (e) {
    return null
  }
}

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value)
  const claims = computed(() => decodeToken(token.value))
  const roles = computed<string[]>(() => claims.value?.roles ?? [])

  function hasRole(role: string): boolean {
    const r = role.startsWith('ROLE_') ? role : `ROLE_${role}`
    return roles.value.includes(r)
  }

  const roleLabel = computed(() => {
    if (hasRole('SECRETARY')) return 'Secrétaire'
    if (hasRole('MANAGER')) return 'Manager'
    return 'Employé'
  })

  async function fetchProfile() {
    if (!token.value) return
    try {
      userProfile.value = await api.getMe()
    } catch (e) {
      console.error('Failed to fetch profile', e)
    }
  }

  async function login(username: string, password: string): Promise<void> {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    if (!res.ok) {
      const text = await res.text().catch(() => res.statusText)
      throw new Error(text || `HTTP ${res.status}`)
    }
    const data = await res.json()
    token.value = data.token
    localStorage.setItem(TOKEN_KEY, data.token)
    await fetchProfile()
  }

  function logout() {
    token.value = null
    userProfile.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { isAuthenticated, roles, hasRole, roleLabel, userProfile, login, logout, fetchProfile }
}

export function getToken(): string | null {
  return token.value
}