<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../services/api'

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits(['close', 'success'])

const form = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const error = ref('')

async function submit() {
  if (form.value.newPassword !== form.value.confirmPassword) {
    error.value = 'Les mots de passe ne correspondent pas'
    return
  }
  
  loading.value = true
  error.value = ''
  try {
    await api.changePassword({
      currentPassword: form.value.currentPassword,
      newPassword: form.value.newPassword
    })
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
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  error.value = ''
}

function close() {
  emit('close')
  reset()
}
</script>

<template>
  <div v-if="show" class="modal-backdrop">
    <div class="modal-content glass-card">
      <h2>Changer le mot de passe</h2>
      
      <form @submit.prevent="submit" class="form">
        <div class="form-group">
          <label for="currentPassword">Mot de passe actuel</label>
          <input id="currentPassword" v-model="form.currentPassword" type="password" required placeholder="••••••••" />
        </div>

        <div class="form-group">
          <label for="newPassword">Nouveau mot de passe</label>
          <input id="newPassword" v-model="form.newPassword" type="password" required placeholder="••••••••" />
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirmer le nouveau mot de passe</label>
          <input id="confirmPassword" v-model="form.confirmPassword" type="password" required placeholder="••••••••" />
        </div>

        <div v-if="error" class="error-banner">
          {{ error }}
        </div>

        <div class="modal-actions">
          <button type="button" @click="close" class="btn btn--outline" :disabled="loading">Annuler</button>
          <button type="submit" class="btn btn--primary" :disabled="loading">
            {{ loading ? 'Mise à jour...' : 'Changer le mot de passe' }}
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
  max-width: 450px;
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

.form-group input {
  padding: 0.8rem 1rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: white;
  font-size: 1rem;
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
