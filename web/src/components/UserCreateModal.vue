<script setup lang="ts">
import { ref, watch } from 'vue'
import { api } from '../services/api'
import type { UserRegistrationRequest } from '../types'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['close', 'success'])

const form = ref<UserRegistrationRequest>({
  username: '',
  fullName: '',
  password: '',
  role: 'EMPLOYEE'
})

const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await api.registerEmployee(form.value)
    emit('success')
    reset()
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function reset() {
  form.value = {
    username: '',
    fullName: '',
    password: '',
    role: 'EMPLOYEE'
  }
  error.value = ''
}

function close() {
  emit('close')
  reset()
}

watch(() => form.value.fullName, (newVal) => {
  if (!newVal) {
    form.value.username = ''
    return
  }
  
  const words = newVal.trim().split(/\s+/)
  if (words.length === 0 || (words.length === 1 && words[0] === '')) {
    form.value.username = ''
    return
  }

  const firstWord = words[0]
  if (words.length === 1) {
    form.value.username = firstWord.charAt(0).toLowerCase() + '.'
    return
  }

  const lastWord = words[words.length - 1]
  const middleWords = words.slice(1, words.length - 1)
  
  let username = firstWord.charAt(0).toLowerCase() + '.'
  middleWords.forEach(w => {
    username += w.charAt(0).toLowerCase()
  })
  username += lastWord.toLowerCase()
  
  form.value.username = username
})

function onNameBlur() {
  const val = form.value.fullName.trim()
  if (!val) return
  
  const words = val.split(/\s+/)
  const firstWord = words[0]
  const otherWords = words.slice(1)
  
  // Format Prénom (First word) - handles hyphens
  const formattedFirst = firstWord.split('-')
    .map(p => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase())
    .join('-')

  // Format NOM (Other words) - handles hyphens and keeps uppercase
  const formattedOthers = otherWords.map(w => w.toUpperCase()).join(' ')
  
  form.value.fullName = formattedOthers ? `${formattedFirst} ${formattedOthers}` : formattedFirst
}
</script>

<template>
  <div v-if="show" class="modal-backdrop">
    <div class="modal-content glass-card">
      <h2>Créer un nouvel utilisateur</h2>
      
      <form @submit.prevent="submit" class="form">
        <div class="form-group">
          <label for="fullName">Nom complet</label>
          <input 
            id="fullName" 
            v-model="form.fullName" 
            type="text" 
            required 
            placeholder="ex: Jean Dupont"
            @blur="onNameBlur"
          />
        </div>

        <div class="form-group">
          <label for="username">Nom d'utilisateur (auto-généré)</label>
          <input 
            id="username" 
            v-model="form.username" 
            type="text" 
            readonly 
            class="readonly-input"
            placeholder="Généré automatiquement..." 
          />
        </div>

        <div class="form-group">
          <label for="password">Mot de passe</label>
          <input id="password" v-model="form.password" type="password" required placeholder="••••••••" />
        </div>

        <div class="form-group">
          <label for="role">Rôle</label>
          <select id="role" v-model="form.role">
            <option value="EMPLOYEE">Employé</option>
            <option value="MANAGER">Manager</option>
            <option value="SECRETARY">Secrétaire</option>
          </select>
        </div>

        <div v-if="error" class="error-banner">
          {{ error }}
        </div>

        <div class="modal-actions">
          <button type="button" @click="close" class="btn btn--outline" :disabled="loading">
          Annuler
        </button>
        <button type="submit" class="btn btn--primary" :disabled="loading">
          {{ loading ? 'Création...' : 'Créer l\'utilisateur' }}
        </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: #121212;
  padding: 2.5rem;
  border-radius: 1.5rem;
  width: 90%;
  max-width: 500px;
  position: relative;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: modalIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-light);
}

.form-group input, .form-group select {
  width: 100%;
  background: rgba(255, 255, 255, 0.05);
  border: 1.5px solid rgba(255, 255, 255, 0.1);
  border-radius: 0.75rem;
  padding: 0.8rem 1rem;
  color: white;
  font-size: 1rem;
  transition: all 0.2s;
}

.readonly-input {
  opacity: 0.7;
  cursor: not-allowed;
  background: rgba(255, 255, 255, 0.02) !important;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.error-banner {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  padding: 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.9rem;
}
</style>
