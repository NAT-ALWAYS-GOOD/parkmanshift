<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import MonthCalendar from '../components/MonthCalendar.vue'
import SpotGrid from '../components/SpotGrid.vue'
import DrawerPanel from '../components/DrawerPanel.vue'
import DashboardPanel from '../components/DashboardPanel.vue'
import UserManagementView from './UserManagementView.vue'
import ChangePasswordModal from '../components/ChangePasswordModal.vue'
import { useToast } from '../composables/useToast'
import { api } from '../services/api'
import { useAuth } from '../composables/useAuth'
import type { Reservation, SpotState } from '../types'

const { show: showToast } = useToast()

const { isAuthenticated, hasRole, roleLabel, userProfile, fetchProfile } = useAuth()
const isSecretary = computed(() => hasRole('SECRETARY'))
const isManager = computed(() => hasRole('MANAGER'))

// ── View toggle ──────────────────────────────────────────────

// ── View toggle ──────────────────────────────────────────────
type ViewMode = 'calendar' | 'list' | 'dashboard' | 'account' | 'user-management'
const viewMode = ref<ViewMode>('calendar')

const showPasswordModal = ref(false)

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

watch(isAuthenticated, () => {
  loadBookings()
  fetchProfile()
})
onMounted(() => {
  loadBookings()
  fetchProfile()
})

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
const isEditing = ref(false)

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
  isEditing.value = false
  selectedSpotLabel.value = null
  targetUsername.value = ''
  bookingForOther.value = false
  submitError.value = ''
  actionError.value = ''
}

function closeDrawer() {
  drawerOpen.value = false
  selectedDate.value = null
  isEditing.value = false
}

// ── Spot availability ─────────────────────────────────────────
const spots = ref<SpotState[]>([])
const loadingSpots = ref(false)
const selectedSpotLabel = ref<string | null>(null)
const bookingForOther = ref(false)
const targetUsername = ref('')
const userResults = ref<any[]>([])
const searchingUsers = ref(false)
let searchTimeout: any = null

watch(targetUsername, (val) => {
  if (searchTimeout) clearTimeout(searchTimeout)
  if (!val || val.length < 2) {
    userResults.value = []
    return
  }
  searchTimeout = setTimeout(async () => {
    searchingUsers.value = true
    try {
      userResults.value = await api.searchUsers(val)
    } finally {
      searchingUsers.value = false
    }
  }, 300)
})

function selectUser(user: any) {
  targetUsername.value = user.username
  // We'll use a temporary ref to show the full name in the input if we want, 
  // but for now let's just make sure the results show the full name.
  // Actually, let's keep targetUsername as the source of truth for the input to avoid overcomplicating.
  userResults.value = []
}

watch(selectedDate, async (date) => {
  spots.value = []
  selectedSpotLabel.value = null
  if (!date) return
  
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

async function confirmBooking() {
  if (!selectedSpotLabel.value || !selectedDate.value) return
  if (bookingForOther.value && !targetUsername.value.trim()) return
  submitting.value = true
  submitError.value = ''
  try {
    const target = bookingForOther.value ? targetUsername.value.trim() : undefined
    await api.reserveSpot({
      parkingSpotLabel: selectedSpotLabel.value,
      date: selectedDate.value,
      ...(target && { targetUsername: target }),
    })
    const label = target ? `${selectedSpotLabel.value} booked for ${target}` : `${selectedSpotLabel.value} booked for ${selectedDate.value}`
    showToast(label)
    closeDrawer()
    await loadBookings()
  } catch (e: any) {
    submitError.value = e.message
  } finally {
    submitting.value = false
  }
}

async function startEditing() {
  if (!selectedBooking.value) return
  isEditing.value = true
  selectedSpotLabel.value = selectedBooking.value.parkingSpotLabel
  loadingSpots.value = true
  try {
    spots.value = await api.getParkingState(selectedBooking.value.date)
  } catch { /* ignore */ } finally {
    loadingSpots.value = false
  }
}

async function confirmUpdate() {
  if (!selectedBooking.value || !selectedSpotLabel.value || !selectedDate.value) return
  submitting.value = true
  submitError.value = ''
  try {
    await api.updateReservation(selectedBooking.value.id, {
      parkingSpotLabel: selectedSpotLabel.value,
      date: selectedDate.value
    })
    showToast(`Reservation updated to ${selectedSpotLabel.value}`)
    closeDrawer()
    await loadBookings()
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

async function confirmCancelBooking(reservationId: string) {
  if (!confirm('Are you sure you want to cancel this reservation?')) return
  try {
    await api.cancelReservation(reservationId)
    showToast('Reservation cancelled')
    // Refresh spots for current date
    if (selectedDate.value) {
      spots.value = await api.getParkingState(selectedDate.value)
    }
    await loadBookings()
  } catch (e: any) {
    showToast(e.message, 'error')
  }
}

// ── List view ─────────────────────────────────────────────────
const listTab = ref<'upcoming' | 'history'>('upcoming')

const upcoming = computed(() =>
  [...bookings.value]
    .filter(b => b.date >= today && (b.status === 'RESERVED' || b.status === 'OCCUPIED'))
    .sort((a, b) => a.date.localeCompare(b.date))
)

const history = computed(() =>
  [...bookings.value]
    .filter(b => b.date < today || b.status === 'CANCELLED')
    .sort((a, b) => b.date.localeCompare(a.date))
)

const bookingLimit = computed(() => isManager.value ? 30 : 5)
const bookingLimitReached = computed(() =>
  !bookingForOther.value && upcoming.value.length >= bookingLimit.value
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
            Calendar
          </button>
          <button
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'list' }]"
            @click="viewMode = 'list'"
          >
            List
          </button>
          <button
            v-if="isManager || isSecretary"
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'dashboard' }]"
            @click="viewMode = 'dashboard'"
          >
            Dashboard
          </button>
          <button
            v-if="isSecretary"
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'user-management' }]"
            @click="viewMode = 'user-management'"
          >
            Users
          </button>
          <button
            :class="['toggle-btn', { 'toggle-btn--active': viewMode === 'account' }]"
            @click="viewMode = 'account'"
          >
            Account
          </button>
        </div>
      </div>

      <!-- ═══ USER MANAGEMENT VIEW ═══ -->
      <div v-if="viewMode === 'user-management'" class="user-mgmt-section">
        <UserManagementView />
      </div>

      <!-- ═══ DASHBOARD VIEW ═══ -->
      <DashboardPanel v-if="viewMode === 'dashboard'" />

      <!-- ═══ MY ACCOUNT VIEW ═══ -->
      <div v-if="viewMode === 'account'" class="account-section">
        <div class="account-card glass-card">
          <div class="account-hero">
            <div class="avatar-large">{{ userProfile?.username?.charAt(0).toUpperCase() }}</div>
            <div class="account-main-info">
              <h2>{{ userProfile?.fullName || userProfile?.username }}</h2>
              <div class="account-badges">
                <span class="badge" :class="userProfile?.role?.toLowerCase()">{{ roleLabel }}</span>
                <span class="username-tag">@{{ userProfile?.username }}</span>
              </div>
            </div>
          </div>
          
          <div class="account-content">
            <div class="info-grid">
              <div class="info-card">
                <span class="info-label">Code de Check-in</span>
                <div class="code-display">
                  <span v-for="digit in (userProfile?.checkInCode || '----').split('')" :key="digit" class="digit-box">
                    {{ digit }}
                  </span>
                </div>
                <p class="info-hint">Utilisez ce code à 4 chiffres pour confirmer votre présence lors du scan du QR code sur votre place.</p>
              </div>

              <div class="info-card actions-card">
                <span class="info-label">Sécurité</span>
                <p class="info-hint">Protégez votre compte en changeant régulièrement votre mot de passe.</p>
                <button @click="showPasswordModal = true" class="btn btn--outline password-btn">
                  Modifier le mot de passe
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <ChangePasswordModal 
        :show="showPasswordModal" 
        @close="showPasswordModal = false"
        @success="showPasswordModal = false; showToast('Mot de passe mis à jour !')"
      />

      <!-- ═══ CALENDAR VIEW ═══ -->
      <div v-if="viewMode === 'calendar'" class="cal-section">
        <div v-if="loadingBookings" class="cal-skeleton">
          <div class="skeleton-header skeleton"></div>
          <div class="skeleton-grid">
            <div v-for="i in 25" :key="i" class="skeleton-cell skeleton"></div>
          </div>
        </div>
        <div v-else class="cal-nav">
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
      <div v-else-if="viewMode === 'list'" class="list-section">
        <div v-if="bookingError" class="alert alert--error">{{ bookingError }}</div>
        <template v-if="loadingBookings">
          <div class="tabs">
            <div class="tab skeleton" style="width: 100px; height: 20px; margin: 7px 14px;"></div>
            <div class="tab skeleton" style="width: 100px; height: 20px; margin: 7px 14px;"></div>
          </div>
          <div class="booking-list">
            <div v-for="i in 3" :key="i" class="booking-card skeleton" style="height: 60px;"></div>
          </div>
        </template>
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
    <div class="drawer-content">
      <!-- 1. Reservation Details (if any) -->
      <template v-if="selectedBooking && !isEditing">
        <div class="drawer-section">
          <h3 class="section-title">Your Booking</h3>
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
              class="btn btn--secondary"
              @click="startEditing"
            >Modify</button>
            <button
              v-if="selectedBooking.status === 'RESERVED'"
              class="btn btn--cancel"
              @click="cancelBooking(selectedBooking)"
            >Cancel booking</button>
          </div>
        </div>
      </template>

      <!-- 2. Warning if limit reached -->
      <div v-if="!selectedBooking && bookingLimitReached && !isEditing && isFutureOrToday" class="alert alert--warning" style="margin-bottom: 16px;">
        You've reached the {{ bookingLimit }}-booking limit. Cancel an existing reservation to book a new one.
      </div>

      <!-- 3. Booking flow or consultation -->
      <div class="drawer-section">
        <h3 class="section-title">{{ isFutureOrToday ? (isEditing ? 'New Spot Selection' : 'Parking State & Booking') : 'Parking State (Past)' }}</h3>
        
        <div v-if="isSecretary && !isEditing && !selectedBooking && isFutureOrToday" class="for-other-row" style="margin-bottom: 12px;">
          <label class="for-other-label">
            <input type="checkbox" v-model="bookingForOther" />
            Book for someone else
          </label>
          <div v-if="bookingForOther" class="user-search-wrap">
            <input
              v-model="targetUsername"
              class="for-other-input"
              placeholder="Search by full name..."
              autocomplete="off"
            />
            <div v-if="userResults.length" class="user-dropdown">
              <div
                v-for="u in userResults"
                :key="u.id"
                class="user-item"
                @click="selectUser(u)"
              >
                <div class="user-info">
                  <span class="user-fullname">{{ u.fullName || u.username }}</span>
                  <span class="user-username">@{{ u.username }}</span>
                </div>
              </div>
            </div>
            <div v-else-if="searchingUsers" class="user-dropdown">
              <div class="user-item-info">Searching...</div>
            </div>
          </div>
        </div>

        <p class="drawer-hint">
          {{ isFutureOrToday ? (isEditing ? 'Change your spot:' : 'Select a spot to book.') : 'View occupancy on this date.' }}
        </p>

        <SpotGrid
          :spots="spots"
          :selected-spot-label="selectedSpotLabel"
          :loading="loadingSpots"
          :is-secretary="isSecretary"
          @select="isFutureOrToday && !selectedBooking ? selectedSpotLabel = $event : isEditing ? selectedSpotLabel = $event : null"
          @cancel="confirmCancelBooking($event)"
        />

        <div v-if="submitError" class="alert alert--error">{{ submitError }}</div>

        <!-- 4. Confirm actions -->
        <div v-if="selectedSpotLabel && isFutureOrToday" class="confirm-row">
          <span class="confirm-label">Spot <strong>{{ selectedSpotLabel }}</strong> selected</span>
          <div class="confirm-actions">
            <button
                v-if="isEditing"
                class="btn btn--outline"
                @click="isEditing = false"
            >Cancel</button>
            <button
              class="btn btn--primary"
              :disabled="submitting || (!isEditing && (bookingLimitReached || (bookingForOther && !targetUsername.trim())))"
              @click="isEditing ? confirmUpdate() : confirmBooking()"
            >
              {{ submitting ? (isEditing ? 'Updating…' : 'Booking…') : (isEditing ? 'Save Changes' : 'Confirm') }}
            </button>
          </div>
        </div>
      </div>
    </div>
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
  width: 100%;
  margin-bottom: 2rem;
}

.toggle-group {
  display: flex;
  background: var(--code-bg);
  border-radius: 1rem;
  padding: 0.5rem;
  gap: 0.5rem;
  width: 100%;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-btn {
  flex: 1;
  padding: 0.85rem 1.5rem;
  border: none;
  border-radius: 0.75rem;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  background: transparent;
  color: var(--text);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.toggle-btn--active {
  background: var(--bg);
  color: var(--accent);
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.toggle-btn:not(.toggle-btn--active):hover {
  background: rgba(255, 255, 255, 0.05);
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
.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
/* ── Account Section ── */
.account-section {
  max-width: 800px;
  margin: 2rem auto;
}

.account-card {
  padding: 0;
  overflow: hidden;
  border-radius: 2rem;
}

.account-hero {
  background: linear-gradient(135deg, var(--accent) 0%, #6d28d9 100%);
  padding: 3rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  color: white;
}

.avatar-large {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.2);
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-radius: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  font-weight: 800;
  backdrop-filter: blur(10px);
}

.account-main-info h2 {
  font-size: 2.5rem;
  margin: 0;
  color: white;
  letter-spacing: -0.02em;
}

.account-badges {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
  align-items: center;
}

.username-tag {
  font-family: var(--mono);
  opacity: 0.8;
  font-size: 0.9rem;
}

.account-content {
  padding: 3rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.info-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 2rem;
  border-radius: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-label {
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text);
  letter-spacing: 0.1em;
}

.code-display {
  display: flex;
  gap: 0.75rem;
  margin: 0.5rem 0;
}

.digit-box {
  width: 48px;
  height: 60px;
  background: var(--code-bg);
  border: 1px solid var(--border);
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 800;
  font-family: var(--mono);
  color: var(--accent);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.info-hint {
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--text);
  opacity: 0.7;
}

.actions-card {
  justify-content: space-between;
}

.password-btn {
  width: 100%;
  margin-top: 1rem;
}

.drawer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text);
  margin: 0;
  opacity: 0.8;
}

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

.for-other-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 14px;
  background: var(--accent-bg);
  border: 1px solid var(--accent-border);
  border-radius: 8px;
}

.for-other-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-h);
  cursor: pointer;
  user-select: none;
}

.for-other-input {
  padding: 7px 12px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--bg);
  color: var(--text-h);
  font-size: 14px;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}

.for-other-input:focus {
  border-color: var(--accent);
}

.user-search-wrap {
  position: relative;
  width: 100%;
}

.user-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  z-index: 100;
  margin-top: 4px;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: var(--shadow);
}

.user-item {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-h);
  transition: background 0.12s;
}

.user-item:hover {
  background: var(--accent-bg);
  color: var(--accent);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-fullname {
  font-weight: 600;
  font-size: 14px;
}

.user-username {
  font-size: 11px;
  opacity: 0.6;
  font-family: var(--mono);
}

.user-item-info {
  padding: 8px 12px;
  font-size: 13px;
  color: var(--text);
  font-style: italic;
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
.alert--warning { background: #fef9c3; color: #854d0e; border: 1px solid #fde047; }

@media (prefers-color-scheme: dark) {
  .alert--error   { background: #450a0a; color: #fca5a5; border-color: #7f1d1d; }
  .alert--success { background: #14532d; color: #86efac; border-color: #166534; }
  .alert--warning { background: #422006; color: #fde047; border-color: #854d0e; }
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


/* ── Skeletons ── */
.cal-skeleton {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.skeleton-header {
  height: 32px;
  width: 200px;
  align-self: center;
}
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 3px;
}
.skeleton-cell {
  height: 64px;
}

@media (max-width: 640px) {
  .skeleton-cell { height: 44px; }
}
</style>