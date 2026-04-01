<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../services/api'
import type { DashboardStats } from '../types'

const stats = ref<DashboardStats | null>(null)
const loading = ref(true)
const error = ref('')

const filterMonth = ref('')
const filterEmployee = ref('')

async function loadStats() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await api.getDashboardStats(filterMonth.value || undefined, filterEmployee.value || undefined)
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2 class="title">Manager Dashboard</h2>
      
      <div class="filters">
        <div class="filter-group">
          <label>Month</label>
          <input type="month" v-model="filterMonth" class="form-input" />
        </div>
        <div class="filter-group">
          <label>Employee ID</label>
          <input type="text" v-model="filterEmployee" placeholder="e.g. employee1" class="form-input" />
        </div>
        <button class="btn btn--primary" @click="loadStats">Filter</button>
      </div>
    </div>
    
    <div v-if="loading" class="loading">Loading dashboard stats...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>
    
    <div v-else-if="stats" class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ Number(stats.occupancyRate || 0).toFixed(1) }}<span class="unit">%</span></div>
        <div class="stat-label">Occupancy Rate</div>
        <div class="stat-desc">Percentage of checked-in vs total reservations</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-value">{{ Number(stats.noShowProportion || 0).toFixed(1) }}<span class="unit">%</span></div>
        <div class="stat-label">No-Show Proportion</div>
        <div class="stat-desc">Cancelled reservations out of total</div>
      </div>

      <div class="stat-card">
        <div class="stat-value">{{ Number(stats.electricSpotProportion || 0).toFixed(1) }}<span class="unit">%</span></div>
        <div class="stat-label">EV Spot Usage</div>
        <div class="stat-desc">Percentage of electric spots reserved</div>
      </div>

      <div class="stat-card">
        <div class="stat-value">{{ stats.totalReservations }}</div>
        <div class="stat-label">Total Reservations</div>
        <div class="stat-desc">Global sum of all reservations</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  animation: fade 0.3s ease;
}

@keyframes fade {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

.dashboard-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

@media (min-width: 768px) {
  .dashboard-header {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.title {
  margin: 0;
  font-size: 24px;
  color: var(--text-h);
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  background: var(--code-bg);
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid var(--border);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
  text-transform: uppercase;
}

.form-input {
  padding: 7px 12px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 14px;
  outline: none;
  height: 34px;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: var(--accent);
}

.btn {
  padding: 0 16px;
  height: 34px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.12s;
}

.btn--primary {
  background: var(--accent);
  color: #fff;
}

.btn--primary:not(:disabled):hover {
  box-shadow: 0 2px 10px rgba(170, 59, 255, 0.35);
}

.loading {
  color: var(--text);
  padding: 16px 0;
}

.alert {
  padding: 12px 16px;
  border-radius: 8px;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.stat-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
}

.unit {
  font-size: 20px;
  font-weight: 500;
  margin-left: 2px;
  opacity: 0.8;
}

.stat-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-h);
}

.stat-desc {
  font-size: 13px;
  color: var(--text);
  line-height: 1.4;
}
</style>
