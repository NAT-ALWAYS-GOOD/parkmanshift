<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../services/api'
import type { CheckInVerification } from '../types'

const route = useRoute()
const router = useRouter()
const spotLabel = route.params.spotLabel as string

const code = ref('')
const step = ref<'input' | 'confirm' | 'success'>('input')
const verification = ref<CheckInVerification | null>(null)
const loading = ref(false)
const error = ref('')

const isChecking = ref(true)

onMounted(async () => {
  if (!spotLabel) {
    router.push('/')
    return
  }
  try {
    const status = await api.getSpotStatus(spotLabel)
    if (status === 'OCCUPIED') {
      error.value = `Spot ${spotLabel} is already occupied by someone else.`
    }
  } catch (e: any) {
    error.value = `Could not verify spot status. Please try again later.`
  } finally {
    isChecking.value = false
  }
})

const isBlockingError = ref(false)

async function handleVerify() {
  if (code.value.length !== 4) return
  loading.value = true
  error.value = ''
  isBlockingError.value = false
  try {
    verification.value = await api.verifyPublicCheckIn(spotLabel, code.value)
    step.value = 'confirm'
  } catch (e: any) {
    error.value = e.message
    // If the error is about someone else having the spot, make it blocking
    if (e.message.toLowerCase().includes('someone else') || e.message.toLowerCase().includes('occupied')) {
      isBlockingError.value = true
    }
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  loading.value = true
  error.value = ''
  try {
    await api.confirmPublicCheckIn(spotLabel, code.value)
    step.value = 'success'
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function reset() {
  code.value = ''
  step.value = 'input'
  verification.value = null
  error.value = ''
}
</script>

<template>
  <div class="checkin-container">
    <div class="glass-card">
      <div class="header">
        <div class="logo">P</div>
        <h1>Parkmanshift</h1>
        <p class="subtitle">Spot Check-in: <strong>{{ spotLabel }}</strong></p>
      </div>

      <div v-if="isChecking" class="step-content">
        <div class="loader-wrap">
          <div class="spinner"></div>
          <p>Verifying spot availability...</p>
        </div>
      </div>

      <div v-else-if="(error && step === 'input' && isBlockingError)" class="step-content">
        <div class="error-box">
          <div class="error-icon">!</div>
          <h3>Process halted</h3>
          <p>{{ error }}</p>
          <button class="btn-primary" @click="router.push('/')" style="margin-top: 16px;">
            Back to Dashboard
          </button>
        </div>
      </div>

      <!-- Step 1: Input Code -->
      <div v-else-if="step === 'input'" class="step-content">
        <p class="instruction">Enter your 4-digit check-in code</p>
        <div class="input-wrap">
          <input
            v-model="code"
            type="text"
            maxlength="4"
            placeholder="0000"
            class="code-input"
            @keyup.enter="handleVerify"
            :disabled="loading"
          />
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button
          class="btn-primary"
          :disabled="code.length !== 4 || loading"
          @click="handleVerify"
        >
          {{ loading ? 'Verifying...' : 'Continue' }}
        </button>
      </div>

      <!-- Step 2: Confirmation -->
      <div v-if="step === 'confirm' && verification" class="step-content">
        <div class="user-profile">
          <div class="avatar">{{ verification.fullName.charAt(0).toUpperCase() }}</div>
          <h2>Welcome, {{ verification.fullName }}!</h2>
        </div>

        <div v-if="verification.conflict" class="warning-box">
          <div class="warning-icon">!</div>
          <div class="warning-text">
            <strong>Reservation Conflict</strong>
            <p>Checking in here will cancel your existing reservation for spot <strong>{{ verification.existingReservationSpotLabel }}</strong> today.</p>
          </div>
        </div>

        <p class="confirm-text">Confirm check-in for spot <strong>{{ spotLabel }}</strong>?</p>

        <div v-if="error" class="error-msg">{{ error }}</div>
        
        <div class="actions">
          <button class="btn-secondary" @click="reset" :disabled="loading">Back</button>
          <button class="btn-primary" @click="handleConfirm" :disabled="loading">
            {{ loading ? 'Confirming...' : 'Confirm Check-in' }}
          </button>
        </div>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 'success'" class="step-content success-step">
        <div class="success-icon">✓</div>
        <h2>Success!</h2>
        <p>You are now checked in at spot <strong>{{ spotLabel }}</strong>.</p>
        <p class="enjoy">Enjoy your stay!</p>
        <button class="btn-primary" @click="router.push('/')">Go to Dashboard</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkin-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
  text-align: center;
  backdrop-filter: blur(10px);
}

@media (prefers-color-scheme: dark) {
  .glass-card {
    background: rgba(30, 41, 59, 0.95);
    color: white;
  }
}

.header {
  margin-bottom: 32px;
}

.logo {
  font-size: 40px;
  margin-bottom: 12px;
}

h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.02em;
}

.subtitle {
  margin: 8px 0 0;
  opacity: 0.7;
  font-size: 16px;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.instruction {
  font-weight: 600;
  font-size: 15px;
}

.input-wrap {
  display: flex;
  justify-content: center;
}

.code-input {
  width: 100%;
  max-width: 200px;
  height: 60px;
  font-size: 32px;
  text-align: center;
  letter-spacing: 0.5em;
  padding-left: 0.25em;
  border: 2px solid var(--border, #e2e8f0);
  border-radius: 16px;
  background: var(--bg, white);
  color: var(--text, #1e293b);
  font-family: var(--mono, monospace);
  font-weight: 700;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.code-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
  padding: 16px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s, background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-2px);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  border: 1px solid #e2e8f0;
  padding: 16px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  color: inherit;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 12px;
}

.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 64px;
  height: 64px;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  border-radius: 50%;
}

.warning-box {
  background: #fffbeb;
  border: 1px solid #fef3c7;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 12px;
  text-align: left;
}

@media (prefers-color-scheme: dark) {
  .warning-box {
    background: rgba(146, 64, 14, 0.1);
    border-color: rgba(146, 64, 14, 0.2);
  }
}

.warning-icon {
  font-size: 24px;
}

.warning-text strong {
  display: block;
  color: #92400e;
  margin-bottom: 4px;
}

.warning-text p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: #92400e;
}

@media (prefers-color-scheme: dark) {
  .warning-text strong, .warning-text p {
    color: #fcd34d;
  }
}

.success-step h2 {
  color: #10b981;
}

.success-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.enjoy {
  font-style: italic;
  opacity: 0.7;
}

.error-msg {
  color: #ef4444;
  font-size: 14px;
  font-weight: 600;
}

.loader-wrap {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
}

@keyframes rotate {
  to { transform: rotate(360deg); }
}

.error-box {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 16px;
  padding: 24px;
  color: #991b1b;
}

@media (prefers-color-scheme: dark) {
  .error-box {
    background: rgba(153, 27, 27, 0.1);
    border-color: rgba(153, 27, 27, 0.2);
    color: #fca5a5;
  }
}

.error-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.error-box h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.error-box p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  opacity: 0.8;
}
</style>
