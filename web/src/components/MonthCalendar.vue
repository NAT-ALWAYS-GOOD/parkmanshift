<script setup lang="ts">
import { computed } from 'vue'
import type { Reservation } from '../types'

const props = defineProps<{
  year: number
  month: number        // 0-based
  bookings: Reservation[]
  selectedDate: string | null
}>()

const emit = defineEmits<{
  (e: 'select', date: string): void
}>()

const WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
]

const today = new Date().toISOString().slice(0, 10)

// Map date string → reservation
const bookingByDate = computed(() => {
  const map: Record<string, Reservation> = {}
  for (const b of props.bookings) {
    if (b.status !== 'CANCELLED') map[b.date] = b
  }
  return map
})

// Build 6×7 grid (null = padding)
const cells = computed<(number | null)[]>(() => {
  const firstDow = (new Date(props.year, props.month, 1).getDay() + 6) % 7 // Mon=0
  const daysInMonth = new Date(props.year, props.month + 1, 0).getDate()
  const result: (number | null)[] = Array(firstDow).fill(null)
  for (let d = 1; d <= daysInMonth; d++) result.push(d)
  while (result.length % 7 !== 0) result.push(null)
  return result
})

function dateStr(day: number) {
  return `${props.year}-${String(props.month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

function isPast(day: number) {
  return dateStr(day) < today
}

function cellClass(day: number | null) {
  if (!day) return 'cell cell--empty'
  const d = dateStr(day)
  const booking = bookingByDate.value[d]
  const classes = ['cell']
  if (d === today) classes.push('cell--today')
  if (d === props.selectedDate) classes.push('cell--selected')
  if (isPast(day)) classes.push('cell--past')
  if (booking) {
    classes.push(`cell--${booking.status.toLowerCase().replace('_', '-')}`)
  } else if (!isPast(day)) {
    classes.push('cell--free')
  }
  return classes.join(' ')
}

function handleClick(day: number | null) {
  if (!day) return
  emit('select', dateStr(day))
}

const title = computed(() => `${MONTHS[props.month]} ${props.year}`)
</script>

<template>
  <div class="calendar">
    <div class="cal-header">
      <span class="cal-title">{{ title }}</span>
    </div>
    <div class="cal-grid">
      <div v-for="wd in WEEKDAYS" :key="wd" class="cell cell--heading">{{ wd }}</div>
      <div
        v-for="(day, i) in cells"
        :key="i"
        :class="cellClass(day)"
        @click="handleClick(day)"
      >
        <template v-if="day">
          <span class="day-num">{{ day }}</span>
          <span v-if="bookingByDate[dateStr(day)]" class="day-spot">
            {{ bookingByDate[dateStr(day)].parkingSpotLabel }}
          </span>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.calendar {
  width: 100%;
}

.cal-header {
  height: 32px;
  display: flex;
  justify-content: center;
}

.cal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-h);
}

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

@media (min-width: 480px) {
  .cal-grid { gap: 3px; }
}

.cell {
  min-height: 44px;
  border-radius: 5px;
  padding: 4px 3px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: default;
  border: 1px solid transparent;
  transition: box-shadow 0.12s, border-color 0.12s;
  box-sizing: border-box;
}

@media (min-width: 480px) {
  .cell {
    min-height: 54px;
    padding: 5px 6px;
    border-radius: 6px;
  }
}

@media (min-width: 768px) {
  .cell {
    min-height: 64px;
    padding: 6px 8px;
    border-radius: 7px;
  }
}

.cell--heading {
  min-height: unset;
  padding: 3px 2px;
  font-size: 10px;
  font-weight: 500;
  color: var(--text);
  text-align: center;
  justify-content: center;
  align-items: center;
  background: none;
  border: none;
  cursor: default;
}

@media (min-width: 480px) {
  .cell--heading {
    padding: 4px 4px;
    font-size: 11px;
  }
}

@media (min-width: 768px) {
  .cell--heading {
    padding: 4px 8px;
    font-size: 12px;
  }
}

.cell--empty {
  background: none;
  border: none;
  cursor: default;
}

.cell--past {
  background: var(--code-bg);
  opacity: 0.5;
}

.cell--free {
  background: var(--bg);
  border-color: var(--border);
  cursor: pointer;
}

.cell--free:hover {
  border-color: var(--accent-border);
  box-shadow: 0 0 0 2px var(--accent-bg);
}

.cell--today {
  border-color: var(--accent) !important;
}

.cell--selected {
  box-shadow: 0 0 0 2px var(--accent) !important;
  border-color: var(--accent) !important;
}

.cell--reserved {
  background: var(--accent-bg);
  border-color: var(--accent-border);
  cursor: pointer;
}

.cell--reserved:hover {
  box-shadow: 0 0 0 2px var(--accent-border);
}

.cell--occupied {
  background: #dcfce7;
  border-color: #86efac;
  cursor: pointer;
}

.day-num {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-h);
  line-height: 1;
}

@media (min-width: 480px) {
  .day-num { font-size: 12px; }
}

@media (min-width: 768px) {
  .day-num { font-size: 13px; }
}

.day-spot {
  display: none;
}

@media (min-width: 380px) {
  .day-spot {
    display: block;
    font-size: 9px;
    font-family: var(--mono);
    font-weight: 500;
    color: var(--accent);
    background: var(--accent-bg);
    border-radius: 3px;
    padding: 1px 3px;
    width: fit-content;
    overflow: hidden;
    max-width: 100%;
    white-space: nowrap;
  }
}

@media (min-width: 480px) {
  .day-spot {
    font-size: 10px;
    padding: 1px 4px;
  }
}

@media (min-width: 768px) {
  .day-spot { font-size: 11px; }
}

.cell--checked-in .day-spot {
  color: #15803d;
  background: #dcfce7;
}

.cell--occupied .day-spot {
  color: #15803d;
  background: #dcfce7;
}

@media (prefers-color-scheme: dark) {
  .cell--occupied {
    background: #14532d;
    border-color: #166534;
  }
  .cell--occupied .day-spot {
    color: #86efac;
    background: #14532d;
  }
  .cell--no-show .day-spot {
    color: #fca5a5;
    background: #450a0a;
  }
}
</style>