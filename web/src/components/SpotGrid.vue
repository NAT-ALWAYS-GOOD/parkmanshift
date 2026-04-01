<script setup lang="ts">
import { ref, computed } from 'vue'
import type { SpotState } from '../types'

const props = defineProps<{
  spots: SpotState[]
  selectedSpotLabel: string | null
  loading?: boolean
  isSecretary?: boolean
}>()

const emit = defineEmits<{
  (e: 'select', label: string): void
  (e: 'cancel', reservationId: string): void
}>()

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F']
const COLS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

function isEvRow(row: string) {
  return row === 'A' || row === 'F'
}

function toLabel(row: string, col: number) {
  return `${row}${String(col).padStart(2, '0')}`
}

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
  if (props.isSecretary && spot.status === 'OCCUPIED') return 'spot spot--occupied'
  if (props.isSecretary && spot.status === 'RESERVED') return 'spot spot--reserved'
  return 'spot spot--taken'
}

// Secretary popover state
const popoverSpot = ref<SpotState | null>(null)
const popoverLabel = ref('')
const popoverPos = ref({ top: 0, left: 0 })

function handleClick(row: string, col: number, event: MouseEvent) {
  const spot = getSpot(row, col)
  const label = toLabel(row, col)

  // Secretary can click taken spots to see details
  if (props.isSecretary && spot && spot.status !== 'FREE' && spot.reservedBy) {
    const rect = (event.target as HTMLElement).getBoundingClientRect()
    popoverPos.value = { top: rect.bottom + 6, left: rect.left + rect.width / 2 }
    popoverSpot.value = spot
    popoverLabel.value = label
    return
  }

  popoverSpot.value = null
  if (!spot || spot.status !== 'FREE') return
  emit('select', label)
}

function closePopover() {
  popoverSpot.value = null
}

const STATUS_LABELS: Record<string, string> = {
  RESERVED: '📋 Reserved',
  OCCUPIED: '🚗 Occupied',
}
</script>

<template>
  <div class="grid-wrap" @click.self="closePopover">
    <div class="legend">
      <span class="legend-item"><span class="dot dot--available"></span>Available</span>
      <span class="legend-item"><span class="dot dot--taken"></span>Taken</span>
      <span class="legend-item"><span class="dot dot--selected"></span>Selected</span>
      <span class="legend-item"><span class="dot dot--ev"></span>EV only (A, F)</span>
      <template v-if="isSecretary">
        <span class="legend-item"><span class="dot dot--reserved-legend"></span>Reserved</span>
        <span class="legend-item"><span class="dot dot--occupied-legend"></span>Occupied</span>
      </template>
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
          @click="handleClick(row, col, $event)"
        >
          {{ toLabel(row, col) }}
          <span v-if="isElectric(row, getSpot(row, col))" class="spot-ev-icon">⚡</span>
        </button>
      </div>
    </div>

    <!-- Secretary spot detail popover -->
    <Teleport to="body">
      <div v-if="popoverSpot" class="popover-overlay" @click="closePopover"></div>
      <div
        v-if="popoverSpot"
        class="spot-popover"
        :style="{ top: popoverPos.top + 'px', left: popoverPos.left + 'px' }"
      >
        <div class="popover-arrow"></div>
        <button class="popover-close" @click="closePopover">✕</button>
        <div class="popover-label">{{ popoverLabel }}</div>
        <div class="popover-user">👤 {{ popoverSpot.reservedBy }}</div>
        <div class="popover-status" :class="popoverSpot.reservationStatus === 'OCCUPIED' ? 'status--occupied' : 'status--reserved'">
          {{ STATUS_LABELS[popoverSpot.reservationStatus ?? ''] ?? popoverSpot.reservationStatus }}
        </div>
        
        <div v-if="isSecretary && popoverSpot.reservationId" class="popover-actions">
          <button class="btn-cancel-admin" @click="emit('cancel', popoverSpot.reservationId!); closePopover()">
            ❌ Cancel Booking
          </button>
        </div>
      </div>
    </Teleport>
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
.dot--reserved-legend { background: #f97316; }
.dot--occupied-legend { background: #dc2626; }

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
  border-radius: 6px;
  font-family: var(--mono);
  font-size: 9px;
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

.spot--reserved {
  background: #fff7ed;
  border-color: #fdba74;
  color: #c2410c;
  cursor: pointer;
}
.spot--reserved:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.25);
}

.spot--occupied {
  background: #fef2f2;
  border-color: #f87171;
  color: #991b1b;
  cursor: pointer;
}
.spot--occupied:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.25);
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
  .spot--reserved {
    background: #431407;
    border-color: #9a3412;
    color: #fdba74;
  }
  .spot--occupied {
    background: #450a0a;
    border-color: #991b1b;
    color: #fca5a5;
  }
}
</style>

<style>
/* Popover styles (unscoped so Teleport works) */
.popover-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9998;
  background: transparent;
}

.spot-popover {
  position: fixed;
  transform: translateX(-50%);
  z-index: 9999;
  background: var(--bg, #fff);
  border: 1px solid var(--border, #e5e7eb);
  border-radius: 10px;
  padding: 12px 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  min-width: 160px;
  text-align: center;
  animation: popIn 0.15s ease-out;
}

@keyframes popIn {
  from { opacity: 0; transform: translateX(-50%) translateY(-4px) scale(0.96); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0) scale(1); }
}

.popover-arrow {
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%) rotate(45deg);
  width: 12px;
  height: 12px;
  background: var(--bg, #fff);
  border-left: 1px solid var(--border, #e5e7eb);
  border-top: 1px solid var(--border, #e5e7eb);
}

.popover-close {
  position: absolute;
  top: 4px;
  right: 8px;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: var(--text, #6b7280);
  line-height: 1;
}

.popover-label {
  font-weight: 700;
  font-size: 15px;
  color: var(--text-h, #111827);
  margin-bottom: 6px;
  font-family: var(--mono, monospace);
}

.popover-user {
  font-size: 13px;
  color: var(--text, #6b7280);
  margin-bottom: 4px;
}

.popover-status {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
}

.status--reserved {
  background: #fff7ed;
  color: #c2410c;
}

.status--occupied {
  background: #fef2f2;
  color: #991b1b;
}

@media (prefers-color-scheme: dark) {
  .spot-popover {
    background: var(--bg, #1f2937);
    border-color: var(--border, #374151);
  }
  .popover-arrow {
    background: var(--bg, #1f2937);
    border-color: var(--border, #374151);
  }
  .status--reserved { background: #431407; color: #fdba74; }
  .status--occupied { background: #450a0a; color: #fca5a5; }
}
</style>
