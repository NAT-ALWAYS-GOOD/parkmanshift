<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../services/api'
import UserCreateModal from '../components/UserCreateModal.vue'
import type { User } from '../types'

const users = ref<User[]>([])
const loading = ref(false)
const searchQuery = ref('')
const showCreateModal = ref(false)

async function fetchUsers() {
  loading.value = true
  try {
    const results = await api.searchUsers(searchQuery.value)
    users.value = results
  } catch (e: any) {
    console.error('Failed to fetch users', e)
  } finally {
    loading.value = false
  }
}

function handleCreateSuccess() {
  showCreateModal.value = false
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-management">
    <div class="header-actions">
      <h1>Gestion des Utilisateurs</h1>
      <button @click="showCreateModal = true" class="btn btn--primary">
        + Nouvel Utilisateur
      </button>
    </div>

    <div class="search-bar glass-card">
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="Rechercher un utilisateur par son username..."
        @input="fetchUsers"
      />
    </div>

    <div v-if="loading && users.length === 0" class="loading">
      Chargement des utilisateurs...
    </div>

    <div v-else class="user-list">
      <div v-for="user in users" :key="user.id" class="user-card glass-card">
        <div class="user-info">
          <div class="user-main">
            <h3>{{ user.fullName || user.username }}</h3>
            <span class="username">@{{ user.username }}</span>
          </div>
          <div class="user-details">
            <span class="badge" :class="user.role?.toLowerCase()">{{ user.role }}</span>
            <span v-if="user.checkInCode" class="check-in-code">Code Check-in: <b>{{ user.checkInCode }}</b></span>
          </div>
        </div>
      </div>
    </div>

    <UserCreateModal 
      :show="showCreateModal" 
      @close="showCreateModal = false"
      @success="handleCreateSuccess"
    />
  </div>
</template>

<style scoped>
.user-management {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  padding: 0.5rem;
  border-radius: 1rem;
}

.search-bar input {
  width: 100%;
  background: transparent;
  border: none;
  padding: 0.8rem 1rem;
  color: white;
  font-size: 1.1rem;
}

.search-bar input:focus {
  outline: none;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.user-card {
  padding: 1.5rem;
  border-radius: 1.25rem;
  transition: transform 0.2s;
  background: rgba(23, 23, 23, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.user-card:hover {
  transform: translateY(-4px);
  background: rgba(30, 30, 30, 0.95);
  border-color: rgba(255, 255, 255, 0.2);
}

.user-main h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #fff;
}

.username {
  color: #94a3b8;
  font-size: 0.9rem;
}

.user-details {
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 2rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
}

.badge.employee { background: rgba(59, 130, 246, 0.25); color: #60a5fa; }
.badge.manager { background: rgba(168, 85, 247, 0.25); color: #c084fc; }
.badge.secretary { background: rgba(234, 179, 8, 0.25); color: #fbbf24; }

.check-in-code {
  font-size: 0.85rem;
  color: #94a3b8;
}

.loading {
  text-align: center;
  padding: 4rem;
  color: var(--text-light);
}
</style>
