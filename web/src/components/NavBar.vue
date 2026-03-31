<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { isAuthenticated, logout } = useAuth()

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span>🚗</span>
      <span class="brand-name">ParkManShift</span>
    </div>
    <div v-if="isAuthenticated" class="navbar-right">
      <button class="btn-logout" @click="handleLogout">Sign out</button>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 48px;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
}

@media (min-width: 640px) {
  .navbar {
    padding: 0 24px;
    height: 52px;
  }
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-h);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-logout {
  padding: 5px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: none;
  color: var(--text);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.12s;
}

.btn-logout:hover {
  background: var(--accent-bg);
  color: var(--text-h);
}
</style>