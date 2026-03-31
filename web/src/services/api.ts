import type {Reservation, ReserveRequest, SpotState} from '../types'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, options)
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

  // GET /api/parking/state
  getReservations(): Promise<Reservation[]> {
    return request(`/api/parking/state`)
  },

  // POST /api/parking/reservations
  reserveSpot(body: ReserveRequest): Promise<Reservation> {
    return request('/api/parking/reservations', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(body),
    })
  },

  // POST /api/parking/reservations/{id}/checkin?employeeId=
  checkIn(id: string, employeeId: string): Promise<void> {
    return request(`/api/parking/reservations/${id}/checkin?employeeId=${encodeURIComponent(employeeId)}`, {
      method: 'POST',
    })
  },

  // DELETE /api/parking/reservations/{id}?employeeId=
  cancelReservation(id: string, employeeId: string): Promise<void> {
    return request(`/api/parking/reservations/${id}?employeeId=${encodeURIComponent(employeeId)}`, {
      method: 'DELETE',
    })
  },
}
