export type SpotType = 'ELECTRIC' | 'THERMAL'

export type SpotStatus = 'FREE' | 'RESERVED' | 'OCCUPIED' | 'UNAVAILABLE'

export type ReservationStatus = 'RESERVED' | 'OCCUPIED' | 'CANCELLED'

export interface ParkingSpot {
  label: string
  type: SpotType
}

export interface SpotState {
  spot: ParkingSpot
  status: SpotStatus
}

export interface Reservation {
  id: string
  parkingSpotLabel: string
  employeeId: string
  date: string
  status: ReservationStatus
}

export interface ReserveRequest {
  parkingSpotLabel: string
  employeeId: string
  date: string
}
