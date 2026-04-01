import type { Reservation, ReserveRequest, SpotState, DashboardStats } from '../types'
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
    const text = await res.text().catch(() => res.statusText)
    throw new Error(text || `HTTP ${res.status}`)
  }
  if (res.status === 204) return undefined as T
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
}