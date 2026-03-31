<script setup lang="ts">
import NavBar from './components/NavBar.vue'
import { RouterView } from 'vue-router'
import { useEmployee } from './composables/useEmployee'
import { ref, onMounted } from 'vue'

const { employeeId, setEmployeeId } = useEmployee()
const setupId = ref('')
const showSetup = ref(false)

onMounted(() => {
  if (!employeeId.value) showSetup.value = true
})

function submitSetup() {
  if (setupId.value.trim()) {
    setEmployeeId(setupId.value.trim())
    showSetup.value = false
  }
}
</script>

<template>
  <NavBar />

  <div v-if="showSetup" class="setup-overlay">
    <div class="setup-card">
      <h2>Welcome to ParkManShift 🚗</h2>
      <p>Enter your employee ID to get started.</p>
      <input
        v-model="setupId"
        class="setup-input"
        placeholder="e.g. EMP042"
        @keyup.enter="submitSetup"
        autofocus
      />
      <button class="setup-btn" :disabled="!setupId.trim()" @click="submitSetup">
        Continue
      </button>
    </div>
  </div>

  <RouterView />
</template>

<style scoped>
.setup-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.setup-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 36px;
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: var(--shadow);
  text-align: center;
}

.setup-card h2 {
  margin: 0;
}

.setup-card p {
  color: var(--text);
  font-size: 15px;
}

.setup-input {
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 16px;
  font-family: var(--mono);
  text-align: center;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}

.setup-input:focus {
  border-color: var(--accent);
}

.setup-btn {
  padding: 10px;
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.15s;
}

.setup-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.setup-btn:not(:disabled):hover {
  box-shadow: 0 2px 12px rgba(170, 59, 255, 0.4);
}
</style>