import { ref, computed } from 'vue'

const TOKEN_KEY = 'parkmanshift_token'

const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value)

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

  return { isAuthenticated, login, logout }
}

export function getToken(): string | null {
  return token.value
}