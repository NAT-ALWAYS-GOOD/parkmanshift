<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { isAuthenticated, logout, userProfile, roleLabel } = useAuth()

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
      <div v-if="userProfile" class="user-summary">
        <span class="user-fullname">{{ userProfile.fullName || userProfile.username }}</span>
        <span class="user-badge" :class="userProfile.role?.toLowerCase()">{{ roleLabel }}</span>
      </div>
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
  height: 64px;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

@media (min-width: 640px) {
  .navbar {
    padding: 0 32px;
  }
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-h);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-summary {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-right: 1rem;
  border-right: 1px solid var(--border);
}

.user-fullname {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-h);
}

.user-badge {
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  padding: 0.2rem 0.6rem;
  border-radius: 2rem;
  letter-spacing: 0.05em;
}

.user-badge.employee { background: rgba(59, 130, 246, 0.15); color: #60a5fa; }
.user-badge.manager { background: rgba(168, 85, 247, 0.15); color: #c084fc; }
.user-badge.secretary { background: rgba(234, 179, 8, 0.15); color: #fbbf24; }

.btn-logout {
  padding: 0.6rem 1.2rem;
  border: 1.5px solid var(--border);
  border-radius: 0.75rem;
  background: transparent;
  color: var(--text);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.2);
}
</style>