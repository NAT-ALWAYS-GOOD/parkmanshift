<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { login } = useAuth()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  if (!username.value || !password.value) return
  loading.value = true
  error.value = ''
  try {
    await login(username.value, password.value)
    router.push('/')
  } catch (e: any) {
    error.value = e.message ?? 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">ParkManShift</h1>
      <p class="login-subtitle">Sign in to manage your parking reservations</p>

      <form class="login-form" @submit.prevent="submit">
        <div class="field">
          <label class="label" for="username">Username</label>
          <input
            id="username"
            v-model="username"
            class="input"
            type="text"
            placeholder="your.username"
            autocomplete="username"
            autofocus
          />
        </div>

        <div class="field">
          <label class="label" for="password">Password</label>
          <input
            id="password"
            v-model="password"
            class="input"
            type="password"
            placeholder="••••••••"
            autocomplete="current-password"
          />
        </div>

        <div v-if="error" class="alert">{{ error }}</div>

        <button
          class="submit-btn"
          type="submit"
          :disabled="loading || !username || !password"
        >
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 40px 36px;
  width: 100%;
  max-width: 380px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-h);
}

.login-subtitle {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--text);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-h);
}

.input {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 15px;
  outline: none;
  transition: border-color 0.15s;
  width: 100%;
  box-sizing: border-box;
}

.input:focus {
  border-color: var(--accent);
}

.alert {
  padding: 10px 14px;
  border-radius: 7px;
  font-size: 14px;
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fca5a5;
}

@media (prefers-color-scheme: dark) {
  .alert {
    background: #450a0a;
    color: #fca5a5;
    border-color: #7f1d1d;
  }
}

.submit-btn {
  padding: 11px;
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s, box-shadow 0.15s;
  margin-top: 4px;
}

.submit-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.submit-btn:not(:disabled):hover {
  box-shadow: 0 2px 12px rgba(170, 59, 255, 0.4);
}
</style>