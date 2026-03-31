<script setup lang="ts">
import { computed } from 'vue'
import type { SpotState } from '../types'

const props = defineProps<{
  spots: SpotState[]
  selectedSpotLabel: string | null
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'select', label: string): void
}>()

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F']
const COLS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

// EV rows are A and F by parking lot convention (fallback when no API data yet)
function isEvRow(row: string) {
  return row === 'A' || row === 'F'
}

function toLabel(row: string, col: number) {
  return `${row}${String(col).padStart(2, '0')}`
}

// Build lookup: label → SpotState
const spotMap = computed(() => {
  const m: Record<string, SpotState> = {}
  for (const s of props.spots) m[s.spot.label] = s
  return m
})

function getSpot(row: string, col: number): SpotState | undefined {
  return spotMap.value[toLabel(row, col)]
}

function isElectric(row: string, spot?: SpotState): boolean {
  return spot ? spot.spot.type === 'ELECTRIC' : isEvRow(row)
}

function spotClass(row: string, col: number): string {
  const label = toLabel(row, col)
  const spot = getSpot(row, col)

  if (label === props.selectedSpotLabel) return 'spot spot--selected'
  if (props.loading) return 'spot spot--loading'
  if (!spot) return 'spot spot--unknown'
  if (spot.status === 'FREE') return 'spot spot--available'
  return 'spot spot--taken'
}

function handleClick(row: string, col: number) {
  const spot = getSpot(row, col)
  if (!spot || spot.status !== 'FREE') return
  emit('select', toLabel(row, col))
}
</script>

<template>
  <div class="grid-wrap">
    <div class="legend">
      <span class="legend-item"><span class="dot dot--available"></span>Available</span>
      <span class="legend-item"><span class="dot dot--taken"></span>Taken</span>
      <span class="legend-item"><span class="dot dot--selected"></span>Selected</span>
      <span class="legend-item"><span class="dot dot--ev"></span>EV only (A, F)</span>
    </div>
    <div class="grid">
      <div v-for="row in ROWS" :key="row" class="grid-row">
        <div class="row-label" :class="{ 'row-label--ev': isEvRow(row) }">
          {{ row }}
        </div>
        <button
          v-for="col in COLS"
          :key="col"
          :class="spotClass(row, col)"
          :title="`${toLabel(row, col)}${isElectric(row, getSpot(row, col)) ? ' (EV)' : ''}`"
          @click="handleClick(row, col)"
        >
          {{ String(col).padStart(2, '0') }}
          <span v-if="isElectric(row, getSpot(row, col))" class="spot-ev-icon">⚡</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid-wrap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--text);
  flex-wrap: wrap;
}

@media (min-width: 480px) {
  .legend { gap: 14px; font-size: 12px; }
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 11px;
  height: 11px;
  border-radius: 3px;
  flex-shrink: 0;
}
.dot--available { background: #22c55e; }
.dot--taken     { background: #ef4444; }
.dot--selected  { background: var(--accent); }
.dot--ev        { background: #f59e0b; }

.grid {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

@media (min-width: 480px) {
  .grid { gap: 3px; }
}

.grid-row:nth-child(odd) {
  margin-bottom: 12px;
}

@media (min-width: 480px) {
  .grid-row:nth-child(odd) { margin-bottom: 18px; }
}

@media (min-width: 768px) {
  .grid-row:nth-child(odd) { margin-bottom: 24px; }
}

.grid-row {
  display: flex;
  align-items: center;
  gap: 2px;
}

@media (min-width: 480px) {
  .grid-row { gap: 3px; }
}

.row-label {
  width: 24px;
  font-weight: 600;
  font-size: 11px;
  color: var(--text-h);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 1px;
}

@media (min-width: 480px) {
  .row-label { width: 28px; font-size: 12px; }
}

@media (min-width: 768px) {
  .row-label { width: 34px; font-size: 13px; }
}

.row-label--ev { color: #f59e0b; }

.spot-ev-icon {
  display: block;
  font-size: 8px;
  line-height: 1;
}

@media (min-width: 480px) {
  .spot-ev-icon { font-size: 9px; }
}

.spot {
  width: 26px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid transparent;
  font-size: 9px;
  font-family: var(--mono);
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.1s;
  appearance: none;
  -webkit-appearance: none;
  background: var(--code-bg);
  color: var(--text-h);
  padding: 0;
  line-height: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
}

@media (min-width: 480px) {
  .spot { width: 30px; height: 32px; font-size: 10px; border-radius: 4px; }
}

@media (min-width: 768px) {
  .spot { width: 36px; height: 38px; font-size: 11px; border-radius: 5px; }
}

.spot--available {
  background: #dcfce7;
  border-color: #86efac;
  color: #15803d;
}
.spot--available:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.spot--taken {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #b91c1c;
  cursor: not-allowed;
}

.spot--disabled {
  background: var(--code-bg);
  border-color: var(--border);
  color: var(--text);
  opacity: 0.35;
  cursor: not-allowed;
}

.spot--selected {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(170, 59, 255, 0.4);
}

.spot--loading {
  background: var(--code-bg);
  border-color: var(--border);
  color: transparent;
  cursor: default;
  animation: pulse 1.2s ease-in-out infinite;
}

.spot--unknown {
  background: var(--code-bg);
  border-color: var(--border);
  color: var(--text);
  opacity: 0.4;
  cursor: not-allowed;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; }
  50%       { opacity: 0.8; }
}

@media (prefers-color-scheme: dark) {
  .spot--available {
    background: #14532d;
    border-color: #166534;
    color: #86efac;
  }
  .spot--taken {
    background: #450a0a;
    border-color: #7f1d1d;
    color: #fca5a5;
  }
}
</style>
