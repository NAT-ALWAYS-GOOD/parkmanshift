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

@keyframes pulse {
  0%, 100% { opacity: 0.4; }
  50%       { opacity: 0.8; }
}
</style>
