import type { Reservation, ReserveRequest, SpotState, DashboardStats, User, CheckInVerification, UserRegistrationRequest, ChangePasswordRequest } from '../types'
import { getToken } from '../composables/useAuth'

function authHeaders(): HeadersInit {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...options,
    headers: {
      ...authHeaders(),
      ...options?.headers,
    },
  })
  if (!res.ok) {
    let message = res.statusText
    try {
      const body = await res.json()
      message = body.error || body.message || JSON.stringify(body)
    } catch {
      message = await res.text().catch(() => res.statusText)
    }
    throw new Error(message || `HTTP ${res.status}`)
  }
  if (res.status === 201 || res.status === 204) {
    return undefined as T
  }
  return res.json()
}

export const api = {
  // GET /api/parking/state?date=YYYY-MM-DD
  getParkingState(date?: string): Promise<SpotState[]> {
    const query = date ? `?date=${date}` : ''
    return request(`/api/parking/state${query}`)
  },

  // GET /api/parking/reservations/history
  getReservations(): Promise<Reservation[]> {
    return request(`/api/parking/reservations/history`)
  },

  // POST /api/parking/reservations
  reserveSpot(body: ReserveRequest): Promise<Reservation> {
    return request('/api/parking/reservations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  // POST /api/parking/reservations/{id}/checkin
  checkIn(id: string): Promise<void> {
    return request(`/api/parking/reservations/${id}/checkin`, {
      method: 'POST',
    })
  },

  // DELETE /api/parking/reservations/{id}
  cancelReservation(id: string): Promise<void> {
    return request(`/api/parking/reservations/${id}`, {
      method: 'DELETE',
    })
  },

  // GET /api/parking/dashboard
  getDashboardStats(yearMonth?: string, employeeId?: string): Promise<DashboardStats> {
    const params = new URLSearchParams()
    if (yearMonth) params.append('yearMonth', yearMonth)
    if (employeeId) params.append('employeeId', employeeId)
    const qs = params.toString() ? `?${params.toString()}` : ''
    return request(`/api/parking/dashboard${qs}`)
  },

  // PUT /api/parking/reservations/{id}
  updateReservation(id: string, body: ReserveRequest): Promise<Reservation> {
    return request(`/api/parking/reservations/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  // GET /api/users/search
  searchUsers(query: string): Promise<any[]> {
    return request(`/api/users/search?query=${encodeURIComponent(query)}`)
  },

  // GET /api/users/me
  getMe(): Promise<User> {
    return request('/api/users/me')
  },

  // POST /api/auth/register-employee
  registerEmployee(body: UserRegistrationRequest): Promise<void> {
    return request('/api/auth/register-employee', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  // POST /api/users/change-password
  changePassword(body: ChangePasswordRequest): Promise<void> {
    return request('/api/users/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  // GET /api/parking/public/check-in/verify
  verifyPublicCheckIn(spotLabel: string, code: string): Promise<CheckInVerification> {
    return request(`/api/parking/public/check-in/verify?spotLabel=${encodeURIComponent(spotLabel)}&code=${encodeURIComponent(code)}`)
  },

  getSpotStatus(label: string): Promise<string> {
    return request(`/api/parking/public/spots/${encodeURIComponent(label)}/status`)
  },

  // POST /api/parking/public/check-in/confirm
  confirmPublicCheckIn(spotLabel: string, code: string): Promise<void> {
    return request(`/api/parking/public/check-in/confirm?spotLabel=${encodeURIComponent(spotLabel)}&code=${encodeURIComponent(code)}`, {
      method: 'POST',
    })
  },
}