import { ref, computed } from 'vue'

const TOKEN_KEY = 'parkmanshift_token'

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))

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
  }

  function logout() {
    token.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { isAuthenticated, roles, hasRole, login, logout }
}

export function getToken(): string | null {
  return token.value
}