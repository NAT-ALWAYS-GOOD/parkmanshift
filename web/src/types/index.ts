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
  reservedBy?: string
  reservationStatus?: ReservationStatus
  reservationId?: string
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
  date: string
  targetUsername?: string
}

export interface DashboardStats {
  occupancyRate: number
  noShowProportion: number
  electricSpotProportion: number
  totalReservations: number
}

export type UserRole = 'EMPLOYEE' | 'MANAGER' | 'SECRETARY'

export interface User {
  id: string
  username: string
  fullName?: string
  role?: UserRole
  checkInCode: string
}

export interface UserRegistrationRequest {
  username: string
  fullName: string
  password?: string
  role: UserRole
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface CheckInVerification {
  username: string
  fullName: string
  existingReservationSpotLabel?: string | null
  conflict: boolean
}
