<script setup lang="ts">
import { ref } from 'vue'
import { useEmployee } from '../composables/useEmployee'

const { employeeId, setEmployeeId } = useEmployee()
const editing = ref(false)
const draft = ref('')

function startEdit() {
  draft.value = employeeId.value
  editing.value = true
}

function saveEdit() {
  if (draft.value.trim()) {
    setEmployeeId(draft.value.trim())
    editing.value = false
  }
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span>🚗</span>
      <span class="brand-name">ParkManShift</span>
    </div>
    <div class="navbar-employee">
      <template v-if="!editing">
        <span class="employee-label">{{ employeeId || 'No ID set' }}</span>
        <button class="btn-ghost" @click="startEdit">✏️</button>
      </template>
      <template v-else>
        <input
          v-model="draft"
          class="employee-input"
          placeholder="Employee ID"
          @keyup.enter="saveEdit"
          @keyup.escape="editing = false"
          autofocus
        />
        <button class="btn-ghost" @click="saveEdit">✓</button>
      </template>
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

.navbar-employee {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.employee-label {
  color: var(--text);
  font-family: var(--mono);
  font-size: 13px;
}

.btn-ghost {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.btn-ghost:hover {
  background: var(--accent-bg);
}

.employee-input {
  padding: 4px 8px;
  border: 1px solid var(--accent-border);
  border-radius: 6px;
  background: var(--bg);
  color: var(--text-h);
  font-family: var(--mono);
  font-size: 13px;
  width: 120px;
  outline: none;
}

.employee-input:focus {
  border-color: var(--accent);
}
</style>