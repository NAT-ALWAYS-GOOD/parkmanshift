<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import MonthCalendar from '../components/MonthCalendar.vue'
import SpotGrid from '../components/SpotGrid.vue'
import DrawerPanel from '../components/DrawerPanel.vue'
import { api } from '../services/api'
import { useAuth } from '../composables/useAuth'
import type { Reservation, SpotState } from '../types'

const { isAuthenticated } = useAuth()

// ── View toggle ──────────────────────────────────────────────
type ViewMode = 'calendar' | 'list'
const viewMode = ref<ViewMode>('calendar')

// ── Reservations ──────────────────────────────────────────────
const bookings = ref<Reservation[]>([])
const loadingBookings = ref(false)
const bookingError = ref('')

async function loadBookings() {
  if (!isAuthenticated.value) return
  loadingBookings.value = true
  bookingError.value = ''
  try {
    bookings.value = await api.getReservations()
  } catch (e: any) {
    bookingError.value = e.message
  } finally {
    loadingBookings.value = false
  }
}

watch(isAuthenticated, loadBookings)
onMounted(loadBookings)

// ── Calendar navigation ───────────────────────────────────────
const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth())

function prevMonth() {
  if (calMonth.value === 0) { calYear.value--; calMonth.value = 11 }
  else calMonth.value--
}

function nextMonth() {
  if (calMonth.value === 11) { calYear.value++; calMonth.value = 0 }
  else calMonth.value++
}

// ── Day selection ─────────────────────────────────────────────
const today = new Date().toISOString().slice(0, 10)
const selectedDate = ref<string | null>(null)
const drawerOpen = ref(false)

const bookingByDate = computed(() => {
  const map: Record<string, Reservation> = {}
  for (const b of bookings.value) {
    if (b.status !== 'CANCELLED') map[b.date] = b
  }
  return map
})

const selectedBooking = computed(() =>
  selectedDate.value ? bookingByDate.value[selectedDate.value] ?? null : null
)

const isFutureOrToday = computed(() =>
  selectedDate.value != null && selectedDate.value >= today
)

function openDay(date: string) {
  selectedDate.value = date
  drawerOpen.value = true
  selectedSpotLabel.value = null
  submitError.value = ''
  submitSuccess.value = ''
  actionError.value = ''
}

function closeDrawer() {
  drawerOpen.value = false
  selectedDate.value = null
}

// ── Spot availability ─────────────────────────────────────────
const spots = ref<SpotState[]>([])
const loadingSpots = ref(false)
const selectedSpotLabel = ref<string | null>(null)

watch(selectedDate, async (date) => {
  spots.value = []
  selectedSpotLabel.value = null
  if (!date || date < today || selectedBooking.value) return
  loadingSpots.value = true
  try {
    spots.value = await api.getParkingState(date)
  } catch { /* ignore */ } finally {
    loadingSpots.value = false
  }
})

// ── Create booking ────────────────────────────────────────────
const submitting = ref(false)
const submitError = ref('')
const submitSuccess = ref('')

async function confirmBooking() {
  if (!selectedSpotLabel.value || !selectedDate.value) return
  submitting.value = true
  submitError.value = ''
  submitSuccess.value = ''
  try {
    await api.reserveSpot({
      parkingSpotLabel: selectedSpotLabel.value,
      date: selectedDate.value,
    })
    submitSuccess.value = `${selectedSpotLabel.value} booked for ${selectedDate.value}`
    selectedSpotLabel.value = null
    await loadBookings()
    spots.value = await api.getParkingState(selectedDate.value)
  } catch (e: any) {
    submitError.value = e.message
  } finally {
    submitting.value = false
  }
}

// ── Check in / Cancel ─────────────────────────────────────────
const actionError = ref('')

async function checkIn(reservation: Reservation) {
  actionError.value = ''
  try {
    await api.checkIn(reservation.id)
    const idx = bookings.value.findIndex(b => b.id === reservation.id)
    if (idx !== -1) bookings.value[idx] = { ...bookings.value[idx], status: 'OCCUPIED' }
  } catch (e: any) {
    actionError.value = e.message
  }
}

async function cancelBooking(reservation: Reservation) {
  if (!confirm(`Cancel booking for ${reservation.parkingSpotLabel} on ${reservation.date}?`)) return
  actionError.value = ''
  try {
    await api.cancelReservation(reservation.id)
    const idx = bookings.value.findIndex(b => b.id === reservation.id)
    if (idx !== -1) bookings.value[idx] = { ...bookings.value[idx], status: 'CANCELLED' }
    closeDrawer()
    await loadBookings()
  } catch (e: any) {
    actionError.value = e.message
  }
}

// ── List view ─────────────────────────────────────────────────
const listTab = ref<'upcoming' | 'history'>('upcoming')

const upcoming = computed(() =>
  bookings.value.filter(
    b => b.date >= today && (b.status === 'RESERVED' || b.status === 'OCCUPIED')
  )
)

const history = computed(() =>
  bookings.value.filter(
    b => b.date < today || b.status === 'CANCELLED'
  )
)

const STATUS_LABEL: Record<string, string> = {
  RESERVED: 'Reserved',
  OCCUPIED: 'Occupied',
  CANCELLED: 'Cancelled',
}
</script>

<template>
  <div class="page">
    <template v-if="isAuthenticated">
      <!-- Toggle -->
      <div class="toolbar">
        <div class="toggle-group">
          <button
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'calendar' }]"
            @click="viewMode = 'calendar'"
          >
            📅 Calendar
          </button>
          <button
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'list' }]"
            @click="viewMode = 'list'"
          >
            📋 List
          </button>
        </div>
      </div>

      <!-- ═══ CALENDAR VIEW ═══ -->
      <div v-if="viewMode === 'calendar'" class="cal-section">
        <div class="cal-nav">
          <button class="nav-btn" @click="prevMonth">‹</button>
          <MonthCalendar
            :year="calYear"
            :month="calMonth"
            :bookings="bookings"
            :selected-date="selectedDate"
            @select="openDay"
          />
          <button class="nav-btn" @click="nextMonth">›</button>
        </div>
      </div>

      <!-- ═══ LIST VIEW ═══ -->
      <div v-else class="list-section">
        <div v-if="bookingError" class="alert alert--error">{{ bookingError }}</div>
        <div v-if="loadingBookings" class="loading">Loading…</div>
        <template v-else>
          <div class="tabs">
            <button
              :class="['tab', { 'tab--active': listTab === 'upcoming' }]"
              @click="listTab = 'upcoming'"
            >
              Upcoming <span class="badge">{{ upcoming.length }}</span>
            </button>
            <button
              :class="['tab', { 'tab--active': listTab === 'history' }]"
              @click="listTab = 'history'"
            >
              History <span class="badge">{{ history.length }}</span>
            </button>
          </div>
          <div class="booking-list">
            <template v-if="listTab === 'upcoming'">
              <div v-if="!upcoming.length" class="empty">No upcoming bookings. Switch to calendar to book a spot.</div>
              <div v-for="b in upcoming" :key="b.id" class="booking-card">
                <div class="booking-main">
                  <span class="spot-label">{{ b.parkingSpotLabel }}</span>
                  <span class="booking-date">{{ b.date }}</span>
                </div>
                <div class="booking-right">
                  <span :class="['status-badge', `status-badge--${b.status.toLowerCase()}`]">
                    {{ STATUS_LABEL[b.status] }}
                  </span>
                  <div class="booking-actions">
                    <button
                      v-if="b.status === 'RESERVED' && b.date === today"
                      class="btn btn--checkin"
                      @click="checkIn(b)"
                    >Check in</button>
                    <button
                      v-if="b.status === 'RESERVED'"
                      class="btn btn--cancel"
                      @click="cancelBooking(b)"
                    >Cancel</button>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div v-if="!history.length" class="empty">No past bookings.</div>
              <div v-for="b in history" :key="b.id" class="booking-card booking-card--past">
                <div class="booking-main">
                  <span class="spot-label">{{ b.parkingSpotLabel }}</span>
                  <span class="booking-date">{{ b.date }}</span>
                </div>
                <div class="booking-right">
                  <span :class="['status-badge', `status-badge--${b.status.toLowerCase()}`]">
                    {{ STATUS_LABEL[b.status] }}
                  </span>
                </div>
              </div>
            </template>
          </div>
        </template>
      </div>
    </template>
  </div>

  <!-- ═══ DRAWER ═══ -->
  <DrawerPanel
    :open="drawerOpen"
    :title="selectedDate ?? ''"
    @close="closeDrawer"
  >
    <!-- Booked day -->
    <template v-if="selectedBooking">
      <div class="drawer-spot">{{ selectedBooking.parkingSpotLabel }}</div>
      <span :class="['status-badge', `status-badge--${selectedBooking.status.toLowerCase()}`]">
        {{ STATUS_LABEL[selectedBooking.status] }}
      </span>
      <div v-if="actionError" class="alert alert--error">{{ actionError }}</div>
      <div class="drawer-actions">
        <button
          v-if="selectedBooking.status === 'RESERVED' && selectedBooking.date === today"
          class="btn btn--checkin"
          @click="checkIn(selectedBooking)"
        >Check in</button>
        <button
          v-if="selectedBooking.status === 'RESERVED'"
          class="btn btn--cancel"
          @click="cancelBooking(selectedBooking)"
        >Cancel booking</button>
      </div>
    </template>

    <!-- Free future day -->
    <template v-else-if="isFutureOrToday">
      <p class="drawer-hint">Select a spot to book for this day.</p>
      <SpotGrid
        :spots="spots"
        :selected-spot-label="selectedSpotLabel"
        :loading="loadingSpots"
        @select="selectedSpotLabel = $event"
      />
      <div v-if="submitError" class="alert alert--error">{{ submitError }}</div>
      <div v-if="submitSuccess" class="alert alert--success">{{ submitSuccess }}</div>
      <div v-if="selectedSpotLabel" class="confirm-row">
        <span class="confirm-label">Spot <strong>{{ selectedSpotLabel }}</strong> selected</span>
        <button class="btn btn--primary" :disabled="submitting" @click="confirmBooking">
          {{ submitting ? 'Booking…' : 'Confirm' }}
        </button>
      </div>
    </template>

    <!-- Past day, no booking -->
    <template v-else>
      <p class="drawer-hint past-hint">No booking on this day.</p>
    </template>
  </DrawerPanel>
</template>

<style scoped>
.page {
  padding: 16px;
  text-align: left;
}

@media (min-width: 640px) {
  .page {
    padding: 24px 32px;
  }
}

@media (min-width: 1024px) {
  .page {
    padding: 32px 48px;
    max-width: 1100px;
  }
}

.notice {
  padding: 16px;
  border-radius: 8px;
  background: var(--accent-bg);
  border: 1px solid var(--accent-border);
  color: var(--text-h);
  font-size: 15px;
}

/* ── Toggle ── */
.toolbar {
  justify-self: end;
  margin-bottom: 20px;
}

.toggle-group {
  display: flex;
  background: var(--code-bg);
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
  width: fit-content;
}

.toggle-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  background: none;
  color: var(--text);
  transition: background 0.15s, color 0.15s;
}

.toggle-btn--active {
  background: var(--bg);
  color: var(--text-h);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.toggle-btn:not(.toggle-btn--active):hover {
  background: var(--accent-bg);
  color: var(--text-h);
}

/* ── Calendar ── */
.cal-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cal-nav {
  display: grid;
  grid-template-columns: 32px 1fr 32px;
  gap: 6px;
  align-items: start;
}

@media (min-width: 640px) {
  .cal-nav { gap: 8px; }
}

.nav-btn {
  margin-top: 2px;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.12s;
}

.nav-btn:hover { background: var(--accent-bg); }

/* ── Drawer content ── */
.drawer-spot {
  font-size: 26px;
  font-weight: 700;
  font-family: var(--mono);
  color: var(--text-h);
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-hint {
  font-size: 14px;
  color: var(--text);
  margin: 0;
}

.past-hint { opacity: 0.6; }

.drawer-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.confirm-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: var(--accent-bg);
  border: 1px solid var(--accent-border);
  border-radius: 8px;
}

.confirm-label {
  flex: 1;
  font-size: 14px;
  color: var(--text-h);
}

/* ── Alerts ── */
.alert {
  padding: 10px 14px;
  border-radius: 7px;
  font-size: 14px;
}

.alert--error   { background: #fee2e2; color: #b91c1c; border: 1px solid #fca5a5; }
.alert--success { background: #dcfce7; color: #15803d; border: 1px solid #86efac; }

@media (prefers-color-scheme: dark) {
  .alert--error   { background: #450a0a; color: #fca5a5; border-color: #7f1d1d; }
  .alert--success { background: #14532d; color: #86efac; border-color: #166534; }
}

.loading {
  color: var(--text);
  font-size: 14px;
  padding: 8px 0;
}

/* ── List view ── */
.list-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tabs {
  display: flex;
  gap: 2px;
  border-bottom: 1px solid var(--border);
}

.tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: var(--text);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.12s;
}

.tab:hover { color: var(--text-h); }


.badge {
  background: var(--code-bg);
  color: var(--text);
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 11px;
  font-family: var(--mono);
}

.empty {
  padding: 32px 0;
  text-align: center;
  color: var(--text);
  font-size: 14px;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.booking-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: var(--bg);
  transition: box-shadow 0.12s;
}

.booking-card:hover { box-shadow: var(--shadow); }
.booking-card--past { opacity: 0.7; }

.booking-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.spot-label {
  font-size: 17px;
  font-weight: 600;
  font-family: var(--mono);
  color: var(--text-h);
  display: flex;
  align-items: center;
  gap: 5px;
}

.booking-date {
  font-size: 12px;
  color: var(--text);
  font-family: var(--mono);
}

.booking-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.booking-actions {
  display: flex;
  gap: 5px;
}

/* ── Status badges ── */
.status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 9px;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}


/* ── Buttons ── */
.btn {
  padding: 6px 13px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.12s;
}

.btn:disabled { opacity: 0.45; cursor: not-allowed; }

.btn--primary {
  background: var(--accent);
  color: #fff;
  padding: 7px 16px;
}

.btn--primary:not(:disabled):hover {
  box-shadow: 0 2px 10px rgba(170, 59, 255, 0.35);
}

.btn--checkin { background: #dcfce7; color: #15803d; }
.btn--checkin:hover { background: #bbf7d0; }
.btn--cancel  { background: #fee2e2; color: #b91c1c; }
.btn--cancel:hover  { background: #fecaca; }

@media (prefers-color-scheme: dark) {
  .btn--checkin { background: #14532d; color: #86efac; }
  .btn--cancel  { background: #450a0a; color: #fca5a5; }
}
</style>